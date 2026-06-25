<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getHealth, getNovels } from '../api'

const router = useRouter()
const route = useRoute()
const user = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))
const healthy = ref(false)
const novels = ref([])
const loading = ref(true)
const searchKeyword = ref('')

const isPublishedPage = computed(() => route.path === '/published')

onMounted(async () => {
  try {
    const [health, list] = await Promise.all([getHealth(), getNovels()])
    healthy.value = health.status === 'UP'
    novels.value = list.content
  } finally {
    loading.value = false
  }
})

function logout() {
  localStorage.removeItem('microworld-user')
  localStorage.removeItem('microworld-token')
  user.value = null
  router.push('/login')
}

function openNovel(id) {
  router.push(`/novels/${id}`)
}

const filteredNovels = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return novels.value

  return novels.value.filter((novel) =>
    [novel.title, novel.description, novel.authorName]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(keyword))
  )
})
</script>

<template>
  <main>
    <nav class="topbar container">
      <RouterLink class="brand" to="/"><span>✦</span> Your Microworld</RouterLink>

      <div class="topbar-menu">
        <RouterLink class="topbar-link" to="/published">已发布</RouterLink>
        <span class="topbar-divider" aria-hidden="true"></span>
        <span class="topbar-link muted">分类</span>
        <span class="topbar-divider" aria-hidden="true"></span>

        <label class="topbar-search" aria-label="搜索小说">
          <input v-model.trim="searchKeyword" type="text" placeholder="搜索小说 / 作者" />
          <span class="search-icon" aria-hidden="true">⌕</span>
        </label>

        <template v-if="user">
          <span class="topbar-divider" aria-hidden="true"></span>
          <RouterLink class="topbar-link" to="/novels/create">发布小说</RouterLink>
          <span class="topbar-divider" aria-hidden="true"></span>
          <RouterLink class="topbar-link" :to="`/users/${user.userId}`">我的主页</RouterLink>
          <span class="topbar-divider" aria-hidden="true"></span>
          <button class="topbar-link topbar-button" @click="logout">退出登录</button>
        </template>

        <template v-else>
          <span class="topbar-divider" aria-hidden="true"></span>
          <RouterLink class="topbar-link" to="/login">登录</RouterLink>
          <span class="topbar-divider" aria-hidden="true"></span>
          <RouterLink class="topbar-link" to="/register">注册</RouterLink>
        </template>
      </div>
    </nav>

    <section v-if="!isPublishedPage" class="hero compact container">
      <div>
        <p class="eyebrow">协作式叙事社区</p>
        <h1>让故事不止<br><em>一条可能。</em></h1>
        <p class="intro">发布你的小说，写下第一章；故事从此有了被阅读、被延续的地方。</p>
        <p class="hero-status">
          <i :class="{ on: healthy }"></i>
          {{ healthy ? '服务在线' : '连接中' }}
        </p>
        <RouterLink v-if="user" class="hero-button" to="/novels/create">发布一部小说</RouterLink>
        <RouterLink v-else class="hero-button" to="/register">开始创作</RouterLink>
      </div>
    </section>

    <section class="container library">
      <div class="section-heading">
        <div>
          <p class="eyebrow">NOVEL LIBRARY</p>
          <h2>{{ isPublishedPage ? '已发布' : '最新发布' }}</h2>
        </div>
        <span>{{ filteredNovels.length }} 部小说</span>
      </div>

      <div v-if="loading" class="empty-state">正在加载小说…</div>
      <div v-else-if="!novels.length" class="empty-state">还没有小说，成为第一个落笔的人吧。</div>
      <div v-else-if="!filteredNovels.length" class="empty-state">没有找到相关小说，换个关键词试试。</div>

      <div v-else class="novel-grid">
        <article
          v-for="novel in filteredNovels"
          :key="novel.id"
          class="novel-card clickable-card"
          @click="openNovel(novel.id)"
        >
          <span class="card-mark">✦</span>
          <RouterLink :to="`/users/${novel.authorId}`" class="eyebrow author-link" @click.stop>
            {{ novel.authorName }}
          </RouterLink>
          <h3>{{ novel.title }}</h3>
          <p>{{ novel.description || '作者尚未添加简介。' }}</p>
          <small>{{ new Date(novel.createdAt).toLocaleDateString('zh-CN') }}</small>
        </article>
      </div>
    </section>
  </main>
</template>
