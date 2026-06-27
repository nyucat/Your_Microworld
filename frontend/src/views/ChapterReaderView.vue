<script setup>
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { createComment, deleteComment, getChapter, getChapterComments, getNovel, toggleCommentLike, updateChapter } from '../api'
import TopNav from '../components/TopNav.vue'
import AnimatedBook from '../components/AnimatedBook.vue'
import ParagraphCommentPanel from '../components/ParagraphCommentPanel.vue'

const props = defineProps({ id: String })
const router = useRouter()
const user = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))
const hasToken = computed(() => Boolean(localStorage.getItem('microworld-token')))
const chapter = ref(null)
const novel = ref(null)
const comments = ref([])
const error = ref('')
const readingProgress = ref(0)
const activeParagraphIndex = ref(null)
const submittingParagraph = ref(null)
const likingCommentIds = ref([])
const editingChapter = ref(false)
const savingChapter = ref(false)
const editForm = reactive({ title: '', content: '' })

const owner = computed(() => user.value?.userId === chapter.value?.authorId)
const paragraphs = computed(() =>
  chapter.value ? chapter.value.content.split(/\n\s*\n|\r?\n/).map((item) => item.trim()).filter(Boolean) : []
)

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

const activeParagraph = computed(() =>
  activeParagraphIndex.value == null ? '' : paragraphs.value[activeParagraphIndex.value] || ''
)

const activeComments = computed(() =>
  activeParagraphIndex.value == null
    ? []
    : comments.value.filter((item) => item.paragraphIndex === activeParagraphIndex.value)
)

function syncEditForm() {
  editForm.title = chapter.value?.title || ''
  editForm.content = chapter.value?.content || ''
}

function commentsOf(index) {
  return comments.value.filter((item) => item.paragraphIndex === index)
}

async function load() {
  error.value = ''
  chapter.value = null
  novel.value = null
  comments.value = []
  activeParagraphIndex.value = null
  readingProgress.value = 0
  editingChapter.value = false
  window.scrollTo({ top: 0, behavior: 'smooth' })

  try {
    const currentChapter = await getChapter(props.id)
    chapter.value = currentChapter
    syncEditForm()
    const [novelDetail, chapterComments] = await Promise.all([
      getNovel(currentChapter.novelId),
      getChapterComments(currentChapter.id)
    ])
    novel.value = novelDetail
    comments.value = chapterComments
    updateProgress()
  } catch (e) {
    error.value = e.message
  }
}

async function refreshComments() {
  if (!chapter.value) return
  comments.value = await getChapterComments(chapter.value.id)
}

function openChapter(chapterId) {
  if (!chapterId || String(chapterId) === String(props.id)) return
  router.push(`/chapters/${chapterId}`)
}

function toggleParagraph(index) {
  activeParagraphIndex.value = activeParagraphIndex.value === index ? null : index
}

async function handleSubmitComment({ paragraphIndex, content, parentCommentId, reset }) {
  const text = content.trim()
  if (!text) return

  submittingParagraph.value = paragraphIndex
  try {
    error.value = ''
    const saved = await createComment(chapter.value.id, {
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

async function handleDeleteComment(commentId) {
  if (!window.confirm('确认删除这条评论吗？')) return

  try {
    error.value = ''
    await deleteComment(commentId)
    await refreshComments()
  } catch (e) {
    error.value = e.message
  }
}

async function saveChapterEdit() {
  savingChapter.value = true
  try {
    error.value = ''
    const updated = await updateChapter(props.id, {
      title: editForm.title,
      content: editForm.content
    })
    chapter.value = updated
    editingChapter.value = false
    syncEditForm()
    if (novel.value?.chapters?.length) {
      novel.value = {
        ...novel.value,
        chapters: novel.value.chapters.map((item) => (item.id === updated.id ? { ...item, title: updated.title } : item))
      }
    }
    updateProgress()
  } catch (e) {
    error.value = e.message
  } finally {
    savingChapter.value = false
  }
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

      <section class="reader-layout container reader-shell">
        <article class="reader floating-card reader-card reader-animated">
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

          <div v-if="owner" class="owner-action-bar">
            <button class="filter-chip" @click="editingChapter = !editingChapter">
              {{ editingChapter ? '收起编辑' : '编辑本章' }}
            </button>
          </div>

          <form v-if="owner && editingChapter" class="owner-edit-panel cute-panel" @submit.prevent="saveChapterEdit">
            <label>
              章节标题
              <input v-model.trim="editForm.title" required maxlength="200" />
            </label>

            <label>
              章节正文
              <textarea v-model="editForm.content" class="story-input" required maxlength="100000"></textarea>
            </label>

            <div class="owner-action-bar end">
              <button class="primary cute-primary" :disabled="savingChapter">
                {{ savingChapter ? '保存中…' : '保存章节修改' }}
              </button>
            </div>
          </form>

          <div class="content">
            <section
              v-for="(paragraph, index) in paragraphs"
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
        </article>

        <aside class="reader-comment-panel floating-card">
          <ParagraphCommentPanel
            :active-paragraph-index="activeParagraphIndex"
            :active-paragraph="activeParagraph"
            :comments="activeComments"
            :author-name="novel?.authorName || chapter?.authorName || ''"
            :error="error"
            :user="user"
            :has-token="hasToken"
            :submitting-paragraph="submittingParagraph"
            :liking-comment-ids="likingCommentIds"
            @submit-comment="handleSubmitComment"
            @toggle-like="handleToggleLike"
            @delete-comment="handleDeleteComment"
          />
        </aside>
      </section>

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
