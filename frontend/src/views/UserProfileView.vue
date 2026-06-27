<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { useRouter } from 'vue-router'
import {
  getUserProfile,
  updateMyProfile
} from '../api'
import TopNav from '../components/TopNav.vue'
import NovelCard from '../components/NovelCard.vue'

const props = defineProps({ id: String })
const router = useRouter()

const profile = ref(null)
const error = ref('')
const saving = ref(false)
const bioDraft = ref('')
const currentUser = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))

const isOwner = computed(() => String(currentUser.value?.userId || '') === String(props.id || ''))
const joinedText = computed(() => (profile.value?.joinedAt ? new Date(profile.value.joinedAt).toLocaleDateString('zh-CN') : ''))
const activeText = computed(() => {
  if (!profile.value?.recentActiveAt) return '暂无动态'
  return formatRelativeTime(profile.value.recentActiveAt)
})

async function loadProfile() {
  try {
    error.value = ''
    profile.value = await getUserProfile(props.id)
    bioDraft.value = profile.value.bio || ''
  } catch (e) {
    error.value = e.message
  }
}

async function saveBio() {
  saving.value = true
  try {
    error.value = ''
    profile.value = await updateMyProfile({ bio: bioDraft.value })
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}

function openInbox(type = 'all') {
  if (!isOwner.value) return
  router.push(`/users/${props.id}/inbox?filter=${type}`)
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

watch(() => props.id, loadProfile)
onMounted(loadProfile)
</script>

<template>
  <main class="page-shell">
    <TopNav mode="simple" back-to="/" back-label="返回首页" />

    <section v-if="profile" class="profile container">
      <div class="profile-hero cute-panel profile-hero-upgraded">
        <div class="profile-avatar profile-avatar-large">{{ profile.username.slice(0, 1).toUpperCase() }}</div>

        <div class="profile-hero-copy">
          <p class="eyebrow">AUTHOR PROFILE</p>
          <h1>{{ profile.username }}</h1>

          <div class="profile-badges">
            <span class="count-pill">{{ profile.role }}</span>
            <span class="count-pill">加入于 {{ joinedText }}</span>
            <span class="count-pill">最近活跃 {{ activeText }}</span>
          </div>

          <p class="lead">
            {{ profile.bio || `${profile.username} 还没有写下个人简介，先看看 Ta 已经公开发布的作品吧。` }}
          </p>
        </div>
      </div>

      <section class="profile-bio-panel cute-panel">
        <div class="section-heading cute-heading profile-section-heading">
          <div>
            <p class="eyebrow">BIO NOTE</p>
            <h2>个人简介</h2>
          </div>
        </div>

        <form v-if="isOwner" class="profile-bio-form" @submit.prevent="saveBio">
          <textarea
            v-model="bioDraft"
            maxlength="1000"
            placeholder="写一点关于你的创作偏好、灵感来源，或者想对读者说的话吧。"
          ></textarea>
          <div class="profile-bio-actions">
            <span>{{ bioDraft.length }}/1000</span>
            <button class="primary cute-primary" :disabled="saving">
              {{ saving ? '保存中…' : '保存简介' }}
            </button>
          </div>
        </form>

        <div v-else class="profile-bio-readonly">
          {{ profile.bio || '这个作者还没有留下个人简介。' }}
        </div>
      </section>

      <div class="profile-stats profile-stats-upgraded">
        <article class="cute-panel">
          <span>公开作品</span>
          <strong>{{ profile.publishedNovelCount }}</strong>
        </article>
        <article class="cute-panel">
          <span>连载小说</span>
          <strong>{{ profile.serialNovelCount }}</strong>
        </article>
        <article class="cute-panel">
          <span>微小说</span>
          <strong>{{ profile.microNovelCount }}</strong>
        </article>
        <article class="cute-panel">
          <span>写作章节</span>
          <strong>{{ profile.publishedChapterCount }}</strong>
        </article>
        <article class="cute-panel" :class="{ 'profile-stat-clickable': isOwner }" @click="openInbox('all')">
          <span>收到评论</span>
          <strong>{{ profile.receivedCommentCount }}</strong>
        </article>
        <article class="cute-panel" :class="{ 'profile-stat-clickable': isOwner }" @click="openInbox('popular')">
          <span>收到点赞</span>
          <strong>{{ profile.receivedLikeCount }}</strong>
        </article>
      </div>

      <section class="profile-works">
        <div class="section-heading cute-heading">
          <div>
            <p class="eyebrow">RECENT WORKS</p>
            <h2>最新发布</h2>
          </div>
          <span class="count-pill">{{ profile.recentNovels.length }} 部展示中</span>
        </div>

        <div v-if="!profile.recentNovels.length" class="empty-state cute-empty">这个账号还没有公开发布的小说。</div>
        <div v-else class="novel-grid">
          <NovelCard v-for="novel in profile.recentNovels" :key="novel.id" :novel="novel" />
        </div>
      </section>

      <section class="profile-works">
        <div class="section-heading cute-heading">
          <div>
            <p class="eyebrow">SERIAL SHELF</p>
            <h2>连载作品</h2>
          </div>
          <span class="count-pill">{{ profile.serialNovelCount }} 部</span>
        </div>

        <div v-if="!profile.serialNovels.length" class="empty-state cute-empty">还没有连载作品公开展示。</div>
        <div v-else class="novel-grid">
          <NovelCard v-for="novel in profile.serialNovels" :key="novel.id" :novel="novel" />
        </div>
      </section>

      <section class="profile-works">
        <div class="section-heading cute-heading">
          <div>
            <p class="eyebrow">MICRO COLLECTION</p>
            <h2>微小说收藏夹</h2>
          </div>
          <span class="count-pill">{{ profile.microNovelCount }} 部</span>
        </div>

        <div v-if="!profile.microNovels.length" class="empty-state cute-empty">还没有微小说公开展示。</div>
        <div v-else class="novel-grid">
          <NovelCard v-for="novel in profile.microNovels" :key="novel.id" :novel="novel" />
        </div>
      </section>

    </section>

    <p v-else class="empty-state cute-empty">{{ error || '正在加载个人主页…' }}</p>
  </main>
</template>
