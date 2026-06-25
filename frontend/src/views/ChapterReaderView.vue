<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { getChapter, getNovel } from '../api'
import TopNav from '../components/TopNav.vue'
import AnimatedBook from '../components/AnimatedBook.vue'

const props = defineProps({ id: String })
const router = useRouter()
const chapter = ref(null)
const novel = ref(null)
const error = ref('')
const readingProgress = ref(0)

const currentIndex = computed(() => {
  if (!chapter.value || !novel.value?.chapters?.length) return -1
  return novel.value.chapters.findIndex((item) => item.id === chapter.value.id)
})

const previousChapter = computed(() =>
  currentIndex.value > 0 ? novel.value.chapters[currentIndex.value - 1] : null
)

const nextChapter = computed(() =>
  currentIndex.value >= 0 && currentIndex.value < novel.value.chapters.length - 1
    ? novel.value.chapters[currentIndex.value + 1]
    : null
)

const progressText = computed(() => `${Math.round(readingProgress.value)}%`)

async function load() {
  error.value = ''
  chapter.value = null
  novel.value = null
  readingProgress.value = 0
  window.scrollTo({ top: 0, behavior: 'smooth' })

  try {
    const currentChapter = await getChapter(props.id)
    chapter.value = currentChapter
    novel.value = await getNovel(currentChapter.novelId)
    updateProgress()
  } catch (e) {
    error.value = e.message
  }
}

function openChapter(chapterId) {
  if (!chapterId || String(chapterId) === String(props.id)) return
  router.push(`/chapters/${chapterId}`)
}

function updateProgress() {
  const scrollTop = window.scrollY || document.documentElement.scrollTop || 0
  const viewport = window.innerHeight || document.documentElement.clientHeight || 0
  const fullHeight = document.documentElement.scrollHeight || document.body.scrollHeight || 0
  const readable = Math.max(fullHeight - viewport, 1)
  readingProgress.value = Math.min(100, Math.max(0, (scrollTop / readable) * 100))
}

onMounted(() => {
  load()
  window.addEventListener('scroll', updateProgress, { passive: true })
  window.addEventListener('resize', updateProgress)
})

onUnmounted(() => {
  window.removeEventListener('scroll', updateProgress)
  window.removeEventListener('resize', updateProgress)
})

watch(() => props.id, load)
</script>

<template>
  <main class="page-shell">
    <TopNav
      mode="simple"
      :back-to="chapter ? `/novels/${chapter.novelId}` : '/published'"
      :back-label="chapter ? '返回小说' : '返回书库'"
    />

    <template v-if="chapter">
      <aside class="chapter-side-fixed">
        <div class="chapter-side-nav floating-card">
          <button
            class="chapter-switch switch-prev sparkle-button"
            :class="{ disabled: !previousChapter }"
            :disabled="!previousChapter"
            @click="openChapter(previousChapter?.id)"
          >
            <i class="hover-particle star"></i>
            <i class="hover-particle heart"></i>
            <span class="switch-label">上一章</span>
            <strong>{{ previousChapter ? `第 ${previousChapter.sequenceNo} 章` : '已经是第一章' }}</strong>
          </button>

          <button
            class="chapter-switch switch-next sparkle-button"
            :class="{ disabled: !nextChapter }"
            :disabled="!nextChapter"
            @click="openChapter(nextChapter?.id)"
          >
            <i class="hover-particle star"></i>
            <i class="hover-particle heart"></i>
            <span class="switch-label">下一章</span>
            <strong>{{ nextChapter ? `第 ${nextChapter.sequenceNo} 章` : '已经是最后一章' }}</strong>
          </button>
        </div>
      </aside>

      <article class="reader container floating-card reader-card reader-animated">
        <div class="reader-topbar">
          <div>
            <p class="eyebrow">{{ chapter.novelTitle }} · 第 {{ chapter.sequenceNo }} 章</p>
            <h1>{{ chapter.title }}</h1>
            <p class="reader-author">
              <RouterLink :to="`/users/${chapter.authorId}`" class="author-link">{{ chapter.authorName }}</RouterLink>
              <span> · {{ new Date(chapter.createdAt).toLocaleDateString('zh-CN') }}</span>
            </p>
          </div>

          <AnimatedBook />
        </div>

        <div class="content">
          <p v-for="(paragraph, index) in chapter.content.split(/\n\s*\n|\r?\n/)" :key="index">{{ paragraph }}</p>
        </div>
      </article>

      <div class="reading-progress-shell">
        <div class="reading-progress-card">
          <span class="progress-caption">阅读进度</span>
          <div class="reading-progress-track">
            <div class="reading-progress-fill" :style="{ width: `${readingProgress}%` }"></div>
          </div>
          <span class="progress-value">{{ progressText }}</span>
        </div>
      </div>
    </template>

    <p v-else class="empty-state cute-empty">{{ error || '正在打开章节…' }}</p>
  </main>
</template>
