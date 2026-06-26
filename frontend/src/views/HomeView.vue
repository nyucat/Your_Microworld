<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getHealth, getNovels, getTags } from '../api'
import TopNav from '../components/TopNav.vue'
import NovelCard from '../components/NovelCard.vue'
import AnimatedBook from '../components/AnimatedBook.vue'

const router = useRouter()
const route = useRoute()
const user = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))
const healthy = ref(false)
const novels = ref([])
const allTags = ref([])
const loading = ref(true)
const searchKeyword = ref('')
const activeTag = ref('')

const isPublishedPage = computed(() => route.path === '/published')

async function load() {
  loading.value = true
  try {
    const [health, list, tags] = await Promise.all([
      getHealth(),
      getNovels(0, 12, activeTag.value),
      getTags()
    ])
    healthy.value = health.status === 'UP'
    novels.value = list.content
    allTags.value = tags
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(activeTag, load)

function logout() {
  localStorage.removeItem('microworld-user')
  localStorage.removeItem('microworld-token')
  user.value = null
  router.push('/login')
}

const filteredNovels = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return novels.value

  return novels.value.filter((novel) =>
    [novel.title, novel.description, novel.authorName, ...(novel.tags || [])]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(keyword))
  )
})
</script>

<template>
  <main class="page-shell">
    <TopNav v-model="searchKeyword" mode="full" @logout="logout" />

    <section v-if="!isPublishedPage" class="hero hero-cute container">
      <div class="hero-copy floating-card paper-unfold">
        <span class="bookmark-ribbon" aria-hidden="true"></span>
        <span class="paper-sheet" aria-hidden="true"></span>

        <p class="eyebrow">协作式叙事社区</p>
        <h1>让故事长出<br><em>温柔又闪亮的分支。</em></h1>
        <p class="intro">
          在这里发布你的小说、写下第一章，也让每一次创作都像收藏一颗会发光的小宇宙。
        </p>

        <div class="hero-status status-pill">
          <i :class="{ on: healthy }"></i>
          {{ healthy ? '服务在线' : '连接中' }}
        </div>

        <div class="hero-cta-group">
          <RouterLink v-if="user" class="hero-button cute-primary sparkle-button" to="/novels/create">
            <span>发布一部小说</span>
            <i class="hover-particle star"></i>
            <i class="hover-particle heart"></i>
          </RouterLink>
          <RouterLink v-else class="hero-button cute-primary sparkle-button" to="/register">
            <span>开始创作</span>
            <i class="hover-particle star"></i>
            <i class="hover-particle heart"></i>
          </RouterLink>
          <RouterLink class="hero-button cute-secondary sparkle-button" to="/published">
            <span>查看已发布</span>
            <i class="hover-particle star"></i>
            <i class="hover-particle heart"></i>
          </RouterLink>
        </div>
      </div>

      <aside class="hero-side">
        <article class="floating-card feature-bubble pink feature-bubble-book">
          <AnimatedBook />
          <strong>翻页时刻</strong>
          <p>打开页面时像一本小书轻轻翻开，让阅读氛围更灵动。</p>
        </article>

        <article class="floating-card feature-bubble purple">
          <span>♡</span>
          <strong>从灵感到发布</strong>
          <p>从首页、发布页到阅读页，统一成一套更高级也更灵动的界面语言。</p>
        </article>
      </aside>
    </section>

    <section class="container library">
      <div class="section-heading cute-heading">
        <div>
          <p class="eyebrow">NOVEL LIBRARY</p>
          <h2>{{ isPublishedPage ? '已发布' : '最新发布' }}</h2>
        </div>
        <span class="count-pill">{{ filteredNovels.length }} 部小说</span>
      </div>

      <div v-if="allTags.length" class="filter-bar">
        <button class="filter-chip" :class="{ active: activeTag === '' }" @click="activeTag = ''">全部</button>
        <button
          v-for="tag in allTags"
          :key="tag"
          class="filter-chip"
          :class="{ active: activeTag === tag }"
          @click="activeTag = tag"
        >
          {{ tag }}
        </button>
      </div>

      <div v-if="loading" class="empty-state cute-empty">正在加载小说…</div>
      <div v-else-if="!novels.length" class="empty-state cute-empty">还没有小说，来写下第一颗发光的故事种子吧。</div>
      <div v-else-if="!filteredNovels.length" class="empty-state cute-empty">没有找到相关小说，换个关键词试试。</div>

      <div v-else class="novel-grid">
        <NovelCard v-for="novel in filteredNovels" :key="novel.id" :novel="novel" />
      </div>
    </section>
  </main>
</template>
