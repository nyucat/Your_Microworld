<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getCategoryOverview, getHealth, getNovels, getTags } from '../api'
import { NOVEL_CATEGORIES } from '../constants/novelCategories'
import TopNav from '../components/TopNav.vue'
import NovelCard from '../components/NovelCard.vue'
import AnimatedBook from '../components/AnimatedBook.vue'
import fantasyBg from '../assets/category-backgrounds/fantasy.png'
import scifiBg from '../assets/category-backgrounds/scifi.png'
import campusBg from '../assets/category-backgrounds/campus.png'
import urbanBg from '../assets/category-backgrounds/urban.png'
import mysteryBg from '../assets/category-backgrounds/mystery.png'
import ancientBg from '../assets/category-backgrounds/ancient.png'
import healingBg from '../assets/category-backgrounds/healing.png'
import adventureBg from '../assets/category-backgrounds/adventure.png'
import otherBg from '../assets/category-backgrounds/other.png'

const props = defineProps({
  pageType: { type: String, default: 'home' }
})

const PAGE_SIZE = 12

const CATEGORY_DESCRIPTIONS = {
  奇幻: '魔法、异族、古老传说与未知大陆，在这里适合阅读更有世界感的故事。',
  科幻: '未来科技、星际文明、人工智能与时间想象，适合寻找脑洞和设定感带来的惊喜。',
  校园: '青春、社团、成长、暗恋与毕业季，把最轻盈也最敏感的情绪留在这里。',
  都市: '日常、职场、情感与城市角落，那些贴近现实的情绪和人物会更容易共鸣。',
  悬疑: '谜题、反转、追踪与秘密，让读者在一段段文字里被一步步牵引下去。',
  古风: '山河、朝堂、江湖与旧梦，适合收藏更有氛围感与人物命运感的作品。',
  治愈: '温柔、陪伴、成长与和解，适合想让情绪慢慢安静下来的阅读时刻。',
  冒险: '旅途、伙伴、挑战与未知地图，适合节奏更明快、更有探索感的故事。',
  其他: '不被单一标签束缚的灵感，也许会在这里遇见最特别的故事分支。'
}

const CATEGORY_BACKGROUNDS = {
  奇幻: fantasyBg,
  科幻: scifiBg,
  校园: campusBg,
  都市: urbanBg,
  悬疑: mysteryBg,
  古风: ancientBg,
  治愈: healingBg,
  冒险: adventureBg,
  其他: otherBg
}

const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))
const healthy = ref(false)
const novels = ref([])
const allTags = ref([])
const categoryOverview = ref(null)
const loading = ref(true)
const loadingMore = ref(false)
const searchKeyword = ref('')
const activeTag = ref('')
const activeCategory = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const totalNovels = ref(0)

const isHomePage = computed(() => props.pageType === 'home')
const isPublishedPage = computed(() => props.pageType === 'published')
const isCategoriesPage = computed(() => props.pageType === 'categories')
const currentCategoryBackground = computed(() => CATEGORY_BACKGROUNDS[activeCategory.value] || otherBg)
const categoryHeroStyle = computed(() => ({
  '--category-hero-bg': `url(${currentCategoryBackground.value})`
}))
const hasMore = computed(() => currentPage.value + 1 < totalPages.value)
const hasActiveCategoryFilters = computed(() => Boolean(activeCategory.value || activeTag.value))
const filterSummaryText = computed(() => {
  const parts = []
  if (activeCategory.value) parts.push(`当前分类：${activeCategory.value}`)
  if (activeTag.value) parts.push(`当前标签：${activeTag.value}`)
  return parts.join(' / ')
})

const pageTitle = computed(() => {
  if (isPublishedPage.value) return '已发布'
  if (isCategoriesPage.value) return '分类专题'
  return '最新发布'
})

const currentCategoryDescription = computed(() =>
  activeCategory.value
    ? CATEGORY_DESCRIPTIONS[activeCategory.value]
    : '先选择一个分类，看看这个题材下最近被发布和被讨论的故事。'
)

const filteredNovels = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()
  if (!keyword) return novels.value

  return novels.value.filter((novel) =>
    [novel.title, novel.description, novel.authorName, novel.category, ...(novel.tags || [])]
      .filter(Boolean)
      .some((value) => value.toLowerCase().includes(keyword))
  )
})

const countText = computed(() => {
  const total = totalNovels.value || 0
  return searchKeyword.value.trim() ? `${filteredNovels.value.length} / ${total} 部小说` : `${total} 部小说`
})

const categoryStats = computed(() => {
  const overview = categoryOverview.value || {
    totalNovels: 0,
    serialNovels: 0,
    microNovels: 0,
    authorCount: 0
  }

  return [
    { label: '当前作品数', value: `${overview.totalNovels}` },
    { label: '连载小说', value: `${overview.serialNovels}` },
    { label: '微小说', value: `${overview.microNovels}` },
    { label: '参与作者', value: `${overview.authorCount}` }
  ]
})

const popularTags = computed(() => categoryOverview.value?.popularTags || [])

async function load(reset = false) {
  const requestPage = reset ? 0 : currentPage.value + 1

  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const category = isCategoriesPage.value ? activeCategory.value : ''
    const tag = isCategoriesPage.value ? activeTag.value : ''
    const includeTags = reset || !allTags.value.length
    const [health, list, tags] = await Promise.all([
      getHealth(),
      getNovels(requestPage, PAGE_SIZE, tag, category),
      includeTags ? getTags() : Promise.resolve(allTags.value)
    ])

    healthy.value = health.status === 'UP'
    novels.value = reset ? list.content : [...novels.value, ...list.content]
    currentPage.value = list.number
    totalPages.value = list.totalPages
    totalNovels.value = list.totalElements
    allTags.value = tags

    if (isCategoriesPage.value) {
      categoryOverview.value = await getCategoryOverview(category).catch(() => ({
        category: category || null,
        totalNovels: list.totalElements,
        serialNovels: novels.value.filter((novel) => novel.type === 'SERIAL').length,
        microNovels: novels.value.filter((novel) => novel.type === 'MICRO').length,
        authorCount: new Set(novels.value.map((novel) => novel.authorId)).size,
        popularTags: []
      }))
    } else {
      categoryOverview.value = null
    }
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

async function loadMore() {
  if (!hasMore.value || loadingMore.value || loading.value) return
  await load(false)
}

function logout() {
  localStorage.removeItem('microworld-user')
  localStorage.removeItem('microworld-token')
  user.value = null
  router.push('/login')
}

watch(
  () => props.pageType,
  () => {
    if (!isCategoriesPage.value) {
      activeCategory.value = ''
      activeTag.value = ''
    }
    load(true)
  }
)

watch(
  () => [activeCategory.value, activeTag.value],
  () => {
    if (isCategoriesPage.value) {
      load(true)
    }
  }
)

onMounted(() => load(true))
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
          <span>♥</span>
          <strong>从灵感到发布</strong>
          <p>从首页、发布页到阅读页，统一成一套更高级也更灵动的界面语言。</p>
        </article>
      </aside>
    </section>

    <section v-if="isCategoriesPage" class="container category-feature">
      <div class="category-feature-hero floating-card" :style="categoryHeroStyle">
        <div>
          <p class="eyebrow">CATEGORY SPOTLIGHT</p>
          <h1>{{ activeCategory || '分类专题推荐' }}</h1>
          <p class="intro">{{ currentCategoryDescription }}</p>

          <div class="hero-status status-pill">
            <i :class="{ on: healthy }"></i>
            {{ healthy ? '社区正在更新' : '专题载入中' }}
          </div>
        </div>

        <div class="category-feature-side">
          <div class="feature-mini-card floating-card">
            <strong>热门标签区</strong>
            <div v-if="popularTags.length" class="feature-tag-cloud">
              <button
                v-for="tag in popularTags"
                :key="tag.name"
                class="tag-cloud-chip"
                :class="{ active: activeTag === tag.name }"
                @click="activeTag = activeTag === tag.name ? '' : tag.name"
              >
                <span># {{ tag.name }}</span>
                <small>{{ tag.count }}</small>
              </button>
            </div>
            <p v-else class="feature-empty-copy">这个分类下还没有形成明显的热门标签。</p>
          </div>
        </div>
      </div>

      <div class="category-stats-grid">
        <article v-for="item in categoryStats" :key="item.label" class="floating-card category-stat-card">
          <span>{{ item.label }}</span>
          <strong>{{ item.value }}</strong>
        </article>
      </div>
    </section>

    <section class="container library">
      <div class="section-heading cute-heading">
        <div>
          <p class="eyebrow">NOVEL LIBRARY</p>
          <h2>{{ pageTitle }}</h2>
        </div>
        <span class="count-pill">{{ countText }}</span>
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
            @click="activeTag = activeTag === tag ? '' : tag"
          >
            {{ tag }}
          </button>
        </div>

        <div v-if="hasActiveCategoryFilters" class="filter-summary-bar floating-card">
          <span class="filter-summary-text">{{ filterSummaryText }}</span>
          <button class="filter-summary-clear" @click="activeCategory = ''; activeTag = ''">清空筛选</button>
        </div>
      </template>

      <div v-if="loading" class="empty-state cute-empty">正在加载小说…</div>
      <div v-else-if="!novels.length" class="empty-state cute-empty">还没有已发布的小说，先来写下第一部吧。</div>
      <div v-else-if="!filteredNovels.length" class="empty-state cute-empty">没有找到符合条件的小说，换个关键词试试。</div>

      <template v-else>
        <div class="novel-grid">
          <NovelCard v-for="novel in filteredNovels" :key="novel.id" :novel="novel" />
        </div>

        <div v-if="hasMore && !searchKeyword.trim()" class="library-more">
          <button class="hero-button cute-secondary sparkle-button library-more-button" :disabled="loadingMore" @click="loadMore">
            <span>{{ loadingMore ? '加载中…' : '加载更多' }}</span>
            <i class="hover-particle star"></i>
            <i class="hover-particle heart"></i>
          </button>
        </div>
      </template>
    </section>
  </main>
</template>
