<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getMyInteractions, markAllInteractionsRead, updateInteractionReadState } from '../api'
import TopNav from '../components/TopNav.vue'

const props = defineProps({ id: String })
const route = useRoute()

const error = ref('')
const interactions = ref({ receivedComments: [] })
const readFilter = ref('all')
const togglingReadId = ref(null)
const markingAll = ref(false)

const currentUser = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))
const isOwner = computed(() => String(currentUser.value?.userId || '') === String(props.id || ''))

const interactionFilter = computed(() => {
  const value = String(route.query.filter || 'all')
  return ['all', 'main', 'reply', 'popular'].includes(value) ? value : 'all'
})

const allComments = computed(() => interactions.value?.receivedComments || [])
const unreadCount = computed(() => allComments.value.filter((item) => !item.readByAuthor).length)
const mainCount = computed(() => allComments.value.filter((item) => !item.parentCommentId).length)
const replyCount = computed(() => allComments.value.filter((item) => !!item.parentCommentId).length)
const popularCount = computed(() => allComments.value.filter((item) => item.likeCount >= 3).length)

const filteredInteractions = computed(() => {
  return allComments.value.filter((item) => {
    const matchesType =
      interactionFilter.value === 'all' ||
      (interactionFilter.value === 'main' && !item.parentCommentId) ||
      (interactionFilter.value === 'reply' && !!item.parentCommentId) ||
      (interactionFilter.value === 'popular' && item.likeCount >= 3)

    const matchesRead =
      readFilter.value === 'all' ||
      (readFilter.value === 'unread' && !item.readByAuthor) ||
      (readFilter.value === 'read' && item.readByAuthor)

    return matchesType && matchesRead
  })
})

const filterLabel = computed(() => {
  const map = {
    all: '全部互动',
    main: '主评论',
    reply: '回复',
    popular: '被点赞较多'
  }
  return map[interactionFilter.value] || '全部互动'
})

async function loadInteractions() {
  if (!isOwner.value) {
    interactions.value = { receivedComments: [] }
    error.value = '这里只能查看你自己收到的评论与回复。'
    return
  }

  try {
    error.value = ''
    interactions.value = await getMyInteractions()
  } catch (e) {
    error.value = e.message
  }
}

async function toggleReadState(item, read) {
  if (togglingReadId.value) return
  togglingReadId.value = item.id

  try {
    error.value = ''
    interactions.value = await updateInteractionReadState(item.id, { read })
  } catch (e) {
    error.value = e.message
  } finally {
    togglingReadId.value = null
  }
}

async function markAllRead() {
  if (markingAll.value || !unreadCount.value) return
  markingAll.value = true

  try {
    error.value = ''
    interactions.value = await markAllInteractionsRead()
  } catch (e) {
    error.value = e.message
  } finally {
    markingAll.value = false
  }
}

function formatRelativeTime(value) {
  if (!value) return ''

  const time = new Date(value).getTime()
  const diffSeconds = Math.max(0, Math.floor((Date.now() - time) / 1000))

  if (diffSeconds < 60) return '刚刚'

  const diffMinutes = Math.floor(diffSeconds / 60)
  if (diffMinutes < 60) return `${diffMinutes} 分钟前`

  const diffHours = Math.floor(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours} 小时前`

  const diffDays = Math.floor(diffHours / 24)
  if (diffDays < 30) return `${diffDays} 天前`

  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

watch(() => props.id, loadInteractions)
onMounted(loadInteractions)
</script>

<template>
  <main class="page-shell">
    <TopNav mode="simple" :back-to="`/users/${props.id}`" back-label="返回个人主页" />

    <section class="profile container inbox-page">
      <div class="cute-panel inbox-hero inbox-hero-upgraded">
        <div>
          <p class="eyebrow">AUTHOR INBOX</p>
          <h1>站内消息中心</h1>
          <p class="lead">这里集中查看你的作品收到的评论、回复和点赞反馈。</p>
        </div>

        <div class="inbox-hero-actions">
          <span class="count-pill">未读 {{ unreadCount }}</span>
          <span class="count-pill">当前分类：{{ filterLabel }}</span>
          <button class="primary cute-primary inbox-read-all" :disabled="markingAll || !unreadCount" @click="markAllRead">
            {{ markingAll ? '处理中…' : '一键全部已读' }}
          </button>
        </div>
      </div>

      <p v-if="error" class="form-message">{{ error }}</p>

      <div class="inbox-layout">
        <aside class="cute-panel inbox-sidebar">
          <div class="inbox-sidebar-title">
            <p class="eyebrow">MESSAGE FILTER</p>
            <h2>消息分类</h2>
          </div>

          <nav class="inbox-nav">
            <RouterLink class="inbox-nav-item" :class="{ active: interactionFilter === 'all' }" :to="`/users/${props.id}/inbox?filter=all`">
              <span>全部互动</span>
              <strong>{{ allComments.length }}</strong>
            </RouterLink>

            <RouterLink class="inbox-nav-item" :class="{ active: interactionFilter === 'main' }" :to="`/users/${props.id}/inbox?filter=main`">
              <span>主评论</span>
              <strong>{{ mainCount }}</strong>
            </RouterLink>

            <RouterLink class="inbox-nav-item" :class="{ active: interactionFilter === 'reply' }" :to="`/users/${props.id}/inbox?filter=reply`">
              <span>回复</span>
              <strong>{{ replyCount }}</strong>
            </RouterLink>

            <RouterLink class="inbox-nav-item" :class="{ active: interactionFilter === 'popular' }" :to="`/users/${props.id}/inbox?filter=popular`">
              <span>被点赞较多</span>
              <strong>{{ popularCount }}</strong>
            </RouterLink>
          </nav>

          <div class="inbox-sidebar-meta">
            <div class="inbox-meta-card">
              <span>未读消息</span>
              <strong>{{ unreadCount }}</strong>
            </div>
            <div class="inbox-meta-card">
              <span>已读消息</span>
              <strong>{{ allComments.length - unreadCount }}</strong>
            </div>
          </div>
        </aside>

        <section class="inbox-main">
          <div class="cute-panel inbox-toolbar">
            <div>
              <p class="eyebrow">MESSAGE LIST</p>
              <h2>消息列表</h2>
            </div>

            <div class="filter-bar inbox-read-filter">
              <button class="filter-chip" :class="{ active: readFilter === 'all' }" @click="readFilter = 'all'">全部状态</button>
              <button class="filter-chip" :class="{ active: readFilter === 'unread' }" @click="readFilter = 'unread'">未读</button>
              <button class="filter-chip" :class="{ active: readFilter === 'read' }" @click="readFilter = 'read'">已读</button>
            </div>
          </div>

          <div v-if="!filteredInteractions.length" class="empty-state cute-empty">当前筛选下还没有消息。</div>

          <div v-else class="profile-interaction-list">
            <article
              v-for="item in filteredInteractions"
              :key="item.id"
              class="cute-panel profile-interaction-card"
              :class="{ unread: !item.readByAuthor }"
            >
              <div class="profile-interaction-head">
                <div class="profile-interaction-head-main">
                  <strong>{{ item.username }}</strong>
                  <span v-if="item.parentUsername" class="profile-interaction-reply">回复 {{ item.parentUsername }}</span>
                  <span class="profile-read-badge" :class="{ unread: !item.readByAuthor }">
                    {{ item.readByAuthor ? '已读' : '未读' }}
                  </span>
                </div>
                <span>{{ formatRelativeTime(item.createdAt) }}</span>
              </div>

              <RouterLink :to="item.novelId ? `/novels/${item.novelId}` : '/published'" class="profile-interaction-target">
                《{{ item.novelTitle }}》 · 第 {{ item.paragraphIndex + 1 }} 段
              </RouterLink>

              <p class="profile-interaction-content">{{ item.content }}</p>

              <div class="profile-interaction-likes">
                <strong>点赞 {{ item.likeCount }}</strong>
                <span v-if="item.likedUsers?.length" class="profile-like-user-list">
                  ：
                  <RouterLink
                    v-for="user in item.likedUsers"
                    :key="`${item.id}-${user.userId}-${user.likedAt}`"
                    :to="`/users/${user.userId}`"
                    class="profile-like-user"
                  >
                    {{ user.username }}
                  </RouterLink>
                </span>
                <span v-else>：还没有人给这条评论点赞</span>
              </div>

              <div class="profile-interaction-actions">
                <button
                  class="filter-chip profile-read-toggle"
                  :disabled="togglingReadId === item.id"
                  @click="toggleReadState(item, !item.readByAuthor)"
                >
                  {{
                    togglingReadId === item.id
                      ? '处理中…'
                      : item.readByAuthor
                        ? '标记为未读'
                        : '标记为已读'
                  }}
                </button>
              </div>
            </article>
          </div>
        </section>
      </div>
    </section>
  </main>
</template>
