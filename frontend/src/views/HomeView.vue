<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getHealth, getNovels, getTags } from '../api'
import { NOVEL_CATEGORIES } from '../constants/novelCategories'
import TopNav from '../components/TopNav.vue'
import NovelCard from '../components/NovelCard.vue'
import AnimatedBook from '../components/AnimatedBook.vue'

const props = defineProps({
  pageType: { type: String, default: 'home' }
})

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))
const healthy = ref(false)
const novels = ref([])
const allTags = ref([])
const loading = ref(true)
const searchKeyword = ref('')
const activeTag = ref('')
const activeCategory = ref('')

const isHomePage = computed(() => props.pageType === 'home')
const isPublishedPage = computed(() => props.pageType === 'published')
const isCategoriesPage = computed(() => props.pageType === 'categories')

const pageTitle = computed(() => {
  if (isPublishedPage.value) return '已发布'
  if (isCategoriesPage.value) return '分类浏览'
  return '最新发布'
})

async function load() {
  loading.value = true
  try {
    const category = isCategoriesPage.value ? activeCategory.value : ''
    const tag = isCategoriesPage.value ? activeTag.value : ''

    const [health, list, tags] = await Promise.all([
      getHealth(),
      getNovels(0, 12, tag, category),
      getTags()
    ])
    healthy.value = health.status === 'UP'
    novels.value = list.content
    allTags.value = tags
  } finally {
    loading.value = false
  }
}

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
    [novel.title, novel.description, novel.authorName, novel.category, ...(novel.tags || [])]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(keyword))
  )
})

watch(
  () => [props.pageType, activeCategory.value, activeTag.value],
  () => {
    if (!isCategoriesPage.value) {
      activeCategory.value = ''
      activeTag.value = ''
    }
    load()
  },
  { immediate: false }
)

onMounted(load)
</script>

<template>
  <main class="page-shell">
    <TopNav v-model="searchKeyword" mode="full" @logout="logout" />

    <section v-if="isHomePage" class="hero hero-cute container">
      <div class="hero-copy floating-card paper-unfold">
        <span class="bookmark-ribbon" aria-hidden="true"></span>
        <span class="paper-sheet" aria-hidden="true"></span>

        <p class="eyebrow">协作式叙事社区</p>
        <h1>让故事不止<br><em>一条可能。</em></h1>
        <p class="intro">
          在这里发布你的小说、写下第一章，也让每一次创作都像收集一颗会发光的小宇宙。
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
          <h2>{{ pageTitle }}</h2>
        </div>
        <span class="count-pill">{{ filteredNovels.length }} 部小说</span>
      </div>

      <template v-if="isCategoriesPage">
        <div class="filter-bar">
          <button class="filter-chip" :class="{ active: activeCategory === '' }" @click="activeCategory = ''">全部分类</button>
          <button
            v-for="category in NOVEL_CATEGORIES"
            :key="category"
            class="filter-chip"
            :class="{ active: activeCategory === category }"
            @click="activeCategory = category"
          >
            {{ category }}
          </button>
        </div>

        <div v-if="allTags.length" class="filter-bar filter-bar-sub">
          <button class="filter-chip" :class="{ active: activeTag === '' }" @click="activeTag = ''">全部标签</button>
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
      </template>

      <div v-if="loading" class="empty-state cute-empty">正在加载小说…</div>
      <div v-else-if="!novels.length" class="empty-state cute-empty">还没有已发布的小说，先来写下第一部吧。</div>
      <div v-else-if="!filteredNovels.length" class="empty-state cute-empty">没有找到符合条件的小说，换个筛选试试。</div>

      <div v-else class="novel-grid">
        <NovelCard v-for="novel in filteredNovels" :key="novel.id" :novel="novel" />
      </div>
    </section>
  </main>
</template>
