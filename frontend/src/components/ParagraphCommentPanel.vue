<script setup>
import { computed, reactive, ref, watch } from 'vue'

const props = defineProps({
  activeParagraphIndex: Number,
  activeParagraph: { type: String, default: '' },
  comments: { type: Array, default: () => [] },
  authorName: { type: String, default: '' },
  error: { type: String, default: '' },
  user: { type: Object, default: null },
  hasToken: { type: Boolean, default: false },
  submittingParagraph: { type: Number, default: null },
  likingCommentIds: { type: Array, default: () => [] }
})

const emit = defineEmits(['submit-comment', 'toggle-like', 'delete-comment'])

const replyForms = reactive({})
const replyTargetId = ref(null)
const sortBy = ref('latest')

function formatRelativeTime(value) {
  if (!value) return ''

  const target = new Date(value).getTime()
  const now = Date.now()
  const diff = Math.max(0, now - target)

  const minute = 60 * 1000
  const hour = 60 * minute
  const day = 24 * hour

  if (diff < minute) return '刚刚'
  if (diff < hour) return `${Math.floor(diff / minute)} 分钟前`
  if (diff < day) return `${Math.floor(diff / hour)} 小时前`
  if (diff < 7 * day) return `${Math.floor(diff / day)} 天前`

  return new Date(value).toLocaleDateString('zh-CN')
}

function sortCommentItems(items) {
  return [...items].sort((a, b) => {
    if (sortBy.value === 'hot' && (b.likeCount || 0) !== (a.likeCount || 0)) {
      return (b.likeCount || 0) - (a.likeCount || 0)
    }
    return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
  })
}

function rootCommentIdOf(comment) {
  let current = comment
  while (current?.parentCommentId != null) {
    current = props.comments.find((item) => item.id === current.parentCommentId)
  }
  return current?.id ?? comment.id
}

const topLevelComments = computed(() =>
  sortCommentItems(
    props.comments
      .filter((item) => item.parentCommentId == null)
      .map((item) => ({
        ...item,
        replies: sortCommentItems(
          props.comments.filter((reply) => reply.parentCommentId != null && rootCommentIdOf(reply) === item.id)
        )
      }))
  )
)

const floorMap = computed(() => {
  const ordered = [...props.comments]
    .filter((item) => item.parentCommentId == null)
    .sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())

  return new Map(ordered.map((item, index) => [item.id, index + 1]))
})

const likingSet = computed(() => new Set(props.likingCommentIds))

function floorOf(commentId) {
  return floorMap.value.get(commentId) || 0
}

function isAuthorComment(username) {
  return Boolean(props.authorName) && username === props.authorName
}

function canDelete(comment) {
  return Boolean(props.user) && (props.user.userId === comment.userId || props.user.username === props.authorName)
}

function replyTargetNameOf(comment) {
  if (comment.parentUsername) return comment.parentUsername
  const parent = props.comments.find((item) => item.id === comment.parentCommentId)
  return parent?.username || ''
}

function openReply(comment) {
  replyTargetId.value = replyTargetId.value === comment.id ? null : comment.id
}

function submitMainComment() {
  emit('submit-comment', {
    paragraphIndex: props.activeParagraphIndex,
    content: replyForms.main || '',
    parentCommentId: null,
    reset: () => {
      replyForms.main = ''
    }
  })
}

function submitReply(comment) {
  emit('submit-comment', {
    paragraphIndex: props.activeParagraphIndex,
    content: replyForms[comment.id] || '',
    parentCommentId: comment.id,
    reset: () => {
      replyForms[comment.id] = ''
      replyTargetId.value = null
    }
  })
}

function toggleLike(commentId) {
  emit('toggle-like', commentId)
}

function removeComment(commentId) {
  emit('delete-comment', commentId)
}

watch(() => props.activeParagraphIndex, () => {
  replyTargetId.value = null
})
</script>

<template>
  <div class="reader-comment-panel-inner">
    <div class="reader-comment-panel-head">
      <p class="eyebrow">Paragraph Talk</p>
      <h2>段落评论</h2>
      <p v-if="activeParagraphIndex != null" class="reader-comment-subtitle">
        当前查看第 {{ activeParagraphIndex + 1 }} 段
      </p>
      <p v-else class="reader-comment-subtitle">
        点击正文末尾的聊天气泡，在这里展开对应段落的评论。
      </p>
    </div>

    <template v-if="activeParagraphIndex != null">
      <div class="comment-sort-bar">
        <span class="comment-sort-label">排序方式</span>
        <div class="comment-sort-actions">
          <button class="comment-sort-chip" :class="{ active: sortBy === 'latest' }" @click="sortBy = 'latest'">最新</button>
          <button class="comment-sort-chip" :class="{ active: sortBy === 'hot' }" @click="sortBy = 'hot'">最热</button>
        </div>
      </div>

      <blockquote class="reader-comment-quote">
        {{ activeParagraph }}
      </blockquote>

      <p v-if="error" class="comment-inline-error">{{ error }}</p>

      <div v-if="topLevelComments.length" class="reader-comment-list community-comment-list">
        <article v-for="comment in topLevelComments" :key="comment.id" class="comment-thread-card">
          <div class="comment-thread-main">
            <div class="comment-meta">
              <div class="comment-meta-main">
                <strong>{{ comment.username }}</strong>
                <span v-if="isAuthorComment(comment.username)" class="comment-author-badge">作者</span>
                <span class="comment-floor">#{{ floorOf(comment.id) }}</span>
              </div>
              <span>{{ formatRelativeTime(comment.createdAt) }}</span>
            </div>
            <p class="comment-body">{{ comment.content }}</p>

            <div class="comment-actions-row">
              <button
                class="comment-action-button"
                :class="{ liked: comment.likedByCurrentUser, loading: likingSet.has(comment.id) }"
                :disabled="!hasToken || likingSet.has(comment.id)"
                @click="toggleLike(comment.id)"
              >
                <span>👍</span>
                <strong>{{ comment.likeCount }}</strong>
              </button>

              <button class="comment-action-button ghost" :disabled="!hasToken" @click="openReply(comment)">
                <span>↩</span>
                <strong>回复</strong>
              </button>

              <button v-if="canDelete(comment)" class="comment-action-button ghost" @click="removeComment(comment.id)">
                <span>✕</span>
                <strong>删除</strong>
              </button>
            </div>
          </div>

          <div v-if="comment.replies.length" class="comment-reply-box">
            <article v-for="reply in comment.replies" :key="reply.id" class="comment-reply-item">
              <div class="comment-meta compact">
                <div class="comment-meta-main">
                  <strong>{{ reply.username }}</strong>
                  <span v-if="isAuthorComment(reply.username)" class="comment-author-badge">作者</span>
                </div>
                <span>{{ formatRelativeTime(reply.createdAt) }}</span>
              </div>
              <p class="comment-reply-content">
                <template v-if="replyTargetNameOf(reply)">
                  <span class="reply-target">回复 {{ replyTargetNameOf(reply) }}：</span>
                </template>
                {{ reply.content }}
              </p>

              <div class="comment-actions-row compact">
                <button
                  class="comment-action-button mini"
                  :class="{ liked: reply.likedByCurrentUser, loading: likingSet.has(reply.id) }"
                  :disabled="!hasToken || likingSet.has(reply.id)"
                  @click="toggleLike(reply.id)"
                >
                  <span>👍</span>
                  <strong>{{ reply.likeCount }}</strong>
                </button>

                <button class="comment-action-button ghost mini" :disabled="!hasToken" @click="openReply(reply)">
                  <span>↩</span>
                  <strong>回复</strong>
                </button>

                <button v-if="canDelete(reply)" class="comment-action-button ghost mini" @click="removeComment(reply.id)">
                  <span>✕</span>
                  <strong>删除</strong>
                </button>
              </div>

              <form
                v-if="replyTargetId === reply.id && user && hasToken"
                class="comment-form nested-reply-form"
                @submit.prevent="submitReply(reply)"
              >
                <textarea v-model="replyForms[reply.id]" maxlength="5000" :placeholder="`回复 ${reply.username}…`"></textarea>
                <button class="cute-primary comment-submit" :disabled="submittingParagraph === activeParagraphIndex">
                  {{ submittingParagraph === activeParagraphIndex ? '发布中…' : '发布回复' }}
                </button>
              </form>
            </article>
          </div>

          <form
            v-if="replyTargetId === comment.id && user && hasToken"
            class="comment-form nested-reply-form"
            @submit.prevent="submitReply(comment)"
          >
            <textarea v-model="replyForms[comment.id]" maxlength="5000" :placeholder="`回复 ${comment.username}…`"></textarea>
            <button class="cute-primary comment-submit" :disabled="submittingParagraph === activeParagraphIndex">
              {{ submittingParagraph === activeParagraphIndex ? '发布中…' : '发布回复' }}
            </button>
          </form>
        </article>
      </div>

      <div v-else class="comment-empty">这一段还没有评论，来留下第一条吧。</div>

      <form v-if="user && hasToken" class="comment-form reader-comment-form" @submit.prevent="submitMainComment">
        <textarea v-model="replyForms.main" maxlength="5000" placeholder="写下你的主评论……"></textarea>
        <button class="cute-primary comment-submit" :disabled="submittingParagraph === activeParagraphIndex">
          {{ submittingParagraph === activeParagraphIndex ? '发布中…' : '发布评论' }}
        </button>
      </form>

      <p v-else class="comment-login-tip">登录后即可参与段落评论、点赞与回复。</p>
    </template>

    <div v-else class="reader-comment-empty">
      <div class="reader-comment-empty-bubble">
        <span>💬</span>
      </div>
      <p>想对某一句、某一段说些什么？</p>
      <small>点一下正文段尾的小气泡，右侧就会切换到对应评论区。</small>
    </div>
  </div>
</template>
