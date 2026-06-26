<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addChapter, createComment, getChapterComments, getNovel, toggleCommentLike } from '../api'
import TopNav from '../components/TopNav.vue'
import ParagraphCommentPanel from '../components/ParagraphCommentPanel.vue'

const props = defineProps({ id: String })
const router = useRouter()
const novel = ref(null)
const error = ref('')
const publishing = ref(false)
const form = reactive({ title: '', content: '' })
const user = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))
const hasToken = computed(() => Boolean(localStorage.getItem('microworld-token')))
const comments = ref([])
const activeParagraphIndex = ref(null)
const submittingParagraph = ref(null)
const likingCommentIds = ref([])

const owner = computed(() => user.value?.userId === novel.value?.authorId)
const isMicro = computed(() => novel.value?.type === 'MICRO')
const typeLabel = computed(() => (isMicro.value ? '微小说' : '连载小说'))
const microParagraphs = computed(() =>
  (novel.value?.microContent || '').split(/\n\s*\n|\r?\n/).map((item) => item.trim()).filter(Boolean)
)
const activeParagraph = computed(() =>
  activeParagraphIndex.value == null ? '' : microParagraphs.value[activeParagraphIndex.value] || ''
)
const activeComments = computed(() =>
  activeParagraphIndex.value == null
    ? []
    : comments.value.filter((item) => item.paragraphIndex === activeParagraphIndex.value)
)

function commentsOf(index) {
  return comments.value.filter((item) => item.paragraphIndex === index)
}

async function load() {
  try {
    error.value = ''
    novel.value = await getNovel(props.id)
    comments.value = []
    activeParagraphIndex.value = null

    if (novel.value?.type === 'MICRO' && novel.value?.microChapterId) {
      comments.value = await getChapterComments(novel.value.microChapterId)
    }
  } catch (e) {
    error.value = e.message
  }
}

async function ensureMicroChapterId() {
  if (novel.value?.microChapterId) return novel.value.microChapterId

  const refreshed = await getNovel(props.id)
  novel.value = refreshed

  if (refreshed?.microChapterId) return refreshed.microChapterId

  throw new Error('微小说评论通道还没有就绪。请重启后端后再试一次。')
}

async function submitChapter() {
  publishing.value = true
  error.value = ''
  try {
    const chapter = await addChapter(props.id, form)
    router.push(`/chapters/${chapter.id}`)
  } catch (e) {
    error.value = e.message
  } finally {
    publishing.value = false
  }
}

async function handleSubmitMicroComment({ paragraphIndex, content, parentCommentId, reset }) {
  const text = content.trim()
  if (!text) return

  submittingParagraph.value = paragraphIndex
  try {
    error.value = ''
    const chapterId = await ensureMicroChapterId()
    const saved = await createComment(chapterId, {
      paragraphIndex,
      parentCommentId,
      content: text
    })
    comments.value = [...comments.value, saved]
    activeParagraphIndex.value = paragraphIndex
    reset?.()
  } catch (e) {
    error.value = e.message
  } finally {
    submittingParagraph.value = null
  }
}

async function handleToggleLike(commentId) {
  if (likingCommentIds.value.includes(commentId)) return

  likingCommentIds.value = [...likingCommentIds.value, commentId]
  try {
    error.value = ''
    const updated = await toggleCommentLike(commentId)
    comments.value = comments.value.map((item) => (item.id === commentId ? updated : item))
  } catch (e) {
    error.value = e.message
  } finally {
    likingCommentIds.value = likingCommentIds.value.filter((id) => id !== commentId)
  }
}

function toggleParagraph(index) {
  activeParagraphIndex.value = activeParagraphIndex.value === index ? null : index
}

function openChapter(id) {
  router.push(`/chapters/${id}`)
}

onMounted(load)
</script>

<template>
  <main class="page-shell">
    <TopNav mode="simple" back-to="/published" back-label="返回书库" />

    <section v-if="novel" class="detail container detail-layout">
      <div class="floating-card detail-hero">
        <div class="detail-type-row">
          <RouterLink :to="`/users/${novel.authorId}`" class="eyebrow author-link">{{ novel.authorName }} 创作</RouterLink>
          <span class="count-pill">{{ typeLabel }}</span>
        </div>

        <h1>{{ novel.title }}</h1>
        <p class="lead">{{ novel.description || '作者暂时还没有补充简介。' }}</p>

        <div class="detail-tags">
          <span v-if="novel.category" class="tag-chip category-chip">{{ novel.category }}</span>
          <span v-for="tag in novel.tags" :key="tag" class="tag-chip">{{ tag }}</span>
        </div>

        <details v-if="novel.worldSetting || novel.outlineContent" class="meta cute-meta">
          <summary>世界设定 / 大纲</summary>
          <p v-if="novel.worldSetting">{{ novel.worldSetting }}</p>
          <p v-if="novel.outlineContent">{{ novel.outlineContent }}</p>
        </details>

        <section v-if="isMicro" class="micro-reader">
          <div class="section-heading cute-heading">
            <h2>正文</h2>
          </div>

          <div class="micro-reader-layout">
            <div class="content micro-content">
              <section
                v-for="(paragraph, index) in microParagraphs"
                :key="index"
                class="paragraph-block"
                :class="{ active: activeParagraphIndex === index }"
              >
                <p class="paragraph-line">
                  <span>{{ paragraph }}</span>
                  <button
                    class="comment-toggle-inline sparkle-button"
                    :class="{ expanded: activeParagraphIndex === index }"
                    @click="toggleParagraph(index)"
                  >
                    <i class="hover-particle star"></i>
                    <i class="hover-particle heart"></i>
                    <span class="comment-bubble-icon">
                      <span class="comment-bubble-number">{{ commentsOf(index).length }}</span>
                    </span>
                    <span class="comment-bubble-arrow">⌃</span>
                  </button>
                </p>
              </section>
            </div>

            <aside class="reader-comment-panel floating-card micro-comment-panel">
              <ParagraphCommentPanel
                :active-paragraph-index="activeParagraphIndex"
                :active-paragraph="activeParagraph"
                :comments="activeComments"
                :author-name="novel.authorName"
                :error="error"
                :user="user"
                :has-token="hasToken"
                :submitting-paragraph="submittingParagraph"
                :liking-comment-ids="likingCommentIds"
                @submit-comment="handleSubmitMicroComment"
                @toggle-like="handleToggleLike"
              />
            </aside>
          </div>
        </section>
      </div>

      <template v-if="!isMicro">
        <section class="chapter-list floating-card">
          <div class="section-heading cute-heading">
            <h2>主线章节</h2>
            <span class="count-pill">{{ novel.chapters.length }} 章</span>
          </div>

          <article
            v-for="chapter in novel.chapters"
            :key="chapter.id"
            class="chapter-row cute-row clickable-card"
            @click="openChapter(chapter.id)"
          >
            <span>第 {{ chapter.sequenceNo }} 章</span>
            <strong>{{ chapter.title }}</strong>
            <RouterLink :to="`/users/${chapter.authorId}`" class="author-link inline-author" @click.stop>
              {{ chapter.authorName }}
            </RouterLink>
          </article>
        </section>

        <form v-if="owner" class="chapter-form cute-panel" @submit.prevent="submitChapter">
          <p class="eyebrow">CONTINUE WRITING</p>
          <h2>续写主线</h2>

          <label>
            章节标题
            <input v-model.trim="form.title" required maxlength="200" />
          </label>

          <label>
            正文
            <textarea v-model="form.content" class="story-input" required maxlength="100000"></textarea>
          </label>

          <button class="primary publish cute-primary" :disabled="publishing">
            {{ publishing ? '正在发布…' : '发布下一章' }}
          </button>
        </form>
      </template>
    </section>

    <p v-else-if="error" class="empty-state cute-empty">{{ error }}</p>
  </main>
</template>
