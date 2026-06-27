<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  addChapter,
  createComment,
  deleteComment,
  deleteNovel,
  getChapter,
  getChapterComments,
  getNovel,
  toggleCommentLike,
  updateChapter,
  updateNovel
} from '../api'
import { NOVEL_CATEGORIES } from '../constants/novelCategories'
import TopNav from '../components/TopNav.vue'
import ParagraphCommentPanel from '../components/ParagraphCommentPanel.vue'

const props = defineProps({ id: String })
const router = useRouter()

const novel = ref(null)
const error = ref('')
const publishing = ref(false)
const savingEdit = ref(false)
const editingNovel = ref(false)
const user = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))
const hasToken = computed(() => Boolean(localStorage.getItem('microworld-token')))
const comments = ref([])
const activeParagraphIndex = ref(null)
const submittingParagraph = ref(null)
const likingCommentIds = ref([])
const successMessage = ref('')
const selectedChapterEditId = ref(null)
const chapterEditLoading = ref(false)
const savingChapterEdit = ref(false)
let successTimer = null

const form = reactive({ title: '', content: '' })
const editForm = reactive({
  title: '',
  description: '',
  category: '奇幻',
  tagInput: '',
  microContent: '',
  worldSetting: '',
  outlineContent: '',
  allowIfBranch: true,
  allowBid: true
})
const chapterEditForm = reactive({ title: '', content: '' })
const confirmState = reactive({
  open: false,
  title: '',
  description: '',
  confirmText: '确认',
  loading: false,
  onConfirm: null
})

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
const parsedEditTags = computed(() =>
  editForm.tagInput
    .split(/[，,]/)
    .map((tag) => tag.trim())
    .filter(Boolean)
    .slice(0, 5)
)

function showSuccess(message) {
  successMessage.value = message
  if (successTimer) clearTimeout(successTimer)
  successTimer = setTimeout(() => {
    successMessage.value = ''
    successTimer = null
  }, 2400)
}

function openConfirm({ title, description, confirmText = '确认', onConfirm }) {
  confirmState.open = true
  confirmState.title = title
  confirmState.description = description
  confirmState.confirmText = confirmText
  confirmState.loading = false
  confirmState.onConfirm = onConfirm
}

function closeConfirm() {
  if (confirmState.loading) return
  confirmState.open = false
  confirmState.title = ''
  confirmState.description = ''
  confirmState.confirmText = '确认'
  confirmState.onConfirm = null
}

async function confirmAction() {
  if (!confirmState.onConfirm || confirmState.loading) return
  confirmState.loading = true
  try {
    await confirmState.onConfirm()
    closeConfirm()
  } catch (e) {
    error.value = e.message
    confirmState.loading = false
  }
}

function syncEditForm() {
  if (!novel.value) return
  editForm.title = novel.value.title || ''
  editForm.description = novel.value.description || ''
  editForm.category = novel.value.category || '奇幻'
  editForm.tagInput = (novel.value.tags || []).join('，')
  editForm.microContent = novel.value.microContent || ''
  editForm.worldSetting = novel.value.worldSetting || ''
  editForm.outlineContent = novel.value.outlineContent || ''
  editForm.allowIfBranch = Boolean(novel.value.allowIfBranch)
  editForm.allowBid = Boolean(novel.value.allowBid)
}

function commentsOf(index) {
  return comments.value.filter((item) => item.paragraphIndex === index)
}

async function load() {
  try {
    error.value = ''
    novel.value = await getNovel(props.id)
    syncEditForm()
    comments.value = []
    activeParagraphIndex.value = null
    selectedChapterEditId.value = null

    if (novel.value?.type === 'MICRO' && novel.value?.microChapterId) {
      comments.value = await getChapterComments(novel.value.microChapterId)
    }
  } catch (e) {
    error.value = e.message
  }
}

async function refreshMicroComments() {
  if (novel.value?.type === 'MICRO' && novel.value?.microChapterId) {
    comments.value = await getChapterComments(novel.value.microChapterId)
  }
}

async function ensureMicroChapterId() {
  if (novel.value?.microChapterId) return novel.value.microChapterId

  const refreshed = await getNovel(props.id)
  novel.value = refreshed
  syncEditForm()

  if (refreshed?.microChapterId) return refreshed.microChapterId

  throw new Error('微小说评论通道还没有就绪，请稍后再试。')
}

async function submitChapter() {
  publishing.value = true
  error.value = ''
  try {
    const chapter = await addChapter(props.id, form)
    form.title = ''
    form.content = ''
    showSuccess('新章节已经发布成功啦')
    router.push(`/chapters/${chapter.id}`)
  } catch (e) {
    error.value = e.message
  } finally {
    publishing.value = false
  }
}

async function saveNovelEdit() {
  savingEdit.value = true
  error.value = ''
  try {
    novel.value = await updateNovel(props.id, {
      title: editForm.title,
      description: editForm.description,
      category: editForm.category,
      tags: parsedEditTags.value,
      microContent: editForm.microContent,
      worldSetting: editForm.worldSetting,
      outlineContent: editForm.outlineContent,
      allowIfBranch: editForm.allowIfBranch,
      allowBid: editForm.allowBid
    })
    syncEditForm()
    editingNovel.value = false
    await refreshMicroComments()
    showSuccess('小说信息保存成功')
  } catch (e) {
    error.value = e.message
  } finally {
    savingEdit.value = false
  }
}

function handleDeleteNovel() {
  openConfirm({
    title: '删除这部小说？',
    description: '删除后书库中将不再显示这部作品，但你之后仍可以继续扩展成回收站等功能。',
    confirmText: '确认删除',
    onConfirm: async () => {
      error.value = ''
      await deleteNovel(props.id)
      router.push('/published')
    }
  })
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

function handleDeleteComment(commentId) {
  openConfirm({
    title: '删除这条评论？',
    description: '删除后这条评论将不再对读者展示。',
    confirmText: '删除评论',
    onConfirm: async () => {
      error.value = ''
      await deleteComment(commentId)
      await refreshMicroComments()
      showSuccess('评论已删除')
    }
  })
}

function toggleParagraph(index) {
  activeParagraphIndex.value = activeParagraphIndex.value === index ? null : index
}

function openChapter(id) {
  router.push(`/chapters/${id}`)
}

async function startQuickEdit(chapterId) {
  if (selectedChapterEditId.value === chapterId) {
    selectedChapterEditId.value = null
    return
  }

  chapterEditLoading.value = true
  try {
    error.value = ''
    const chapter = await getChapter(chapterId)
    chapterEditForm.title = chapter.title
    chapterEditForm.content = chapter.content
    selectedChapterEditId.value = chapterId
  } catch (e) {
    error.value = e.message
  } finally {
    chapterEditLoading.value = false
  }
}

async function saveQuickEdit(chapterId) {
  savingChapterEdit.value = true
  try {
    error.value = ''
    const updated = await updateChapter(chapterId, {
      title: chapterEditForm.title,
      content: chapterEditForm.content
    })
    novel.value = {
      ...novel.value,
      chapters: novel.value.chapters.map((item) =>
        item.id === updated.id ? { ...item, title: updated.title, content: updated.content } : item
      )
    }
    selectedChapterEditId.value = null
    showSuccess(`第 ${updated.sequenceNo} 章保存成功`)
  } catch (e) {
    error.value = e.message
  } finally {
    savingChapterEdit.value = false
  }
}

onMounted(load)

onBeforeUnmount(() => {
  if (successTimer) clearTimeout(successTimer)
})
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

        <div v-if="owner" class="owner-action-bar">
          <button class="filter-chip" @click="editingNovel = !editingNovel">
            {{ editingNovel ? '收起编辑' : '编辑小说信息' }}
          </button>
          <button class="filter-chip danger-chip" @click="handleDeleteNovel">
            删除小说
          </button>
        </div>

        <form v-if="owner && editingNovel" class="owner-edit-panel cute-panel" @submit.prevent="saveNovelEdit">
          <div class="owner-edit-grid">
            <label>
              小说标题
              <input v-model.trim="editForm.title" required maxlength="200" />
            </label>

            <label>
              小说分类
              <select v-model="editForm.category" required>
                <option v-for="category in NOVEL_CATEGORIES" :key="category" :value="category">
                  {{ category }}
                </option>
              </select>
            </label>
          </div>

          <label>
            简介
            <textarea v-model.trim="editForm.description" maxlength="5000"></textarea>
          </label>

          <label>
            小说标签
            <input v-model.trim="editForm.tagInput" maxlength="120" placeholder="例如：校园，奇幻，成长（最多 5 个）" />
          </label>

          <div v-if="parsedEditTags.length" class="tag-preview">
            <span v-for="tag in parsedEditTags" :key="tag" class="tag-chip">{{ tag }}</span>
          </div>

          <template v-if="isMicro">
            <label>
              微小说正文
              <textarea v-model="editForm.microContent" class="story-input" required maxlength="100000"></textarea>
            </label>
          </template>

          <template v-else>
            <label>
              世界观
              <textarea v-model.trim="editForm.worldSetting" maxlength="20000"></textarea>
            </label>

            <label>
              故事大纲
              <textarea v-model.trim="editForm.outlineContent" maxlength="20000"></textarea>
            </label>

            <div class="checks">
              <label><input v-model="editForm.allowIfBranch" type="checkbox" /> 允许 IF 分支</label>
              <label><input v-model="editForm.allowBid" type="checkbox" /> 允许主线竞标</label>
            </div>
          </template>

          <div class="owner-action-bar end">
            <button class="primary cute-primary" :disabled="savingEdit">
              {{ savingEdit ? '保存中…' : '保存修改' }}
            </button>
          </div>
        </form>

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
                @delete-comment="handleDeleteComment"
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

          <div class="chapter-manage-list">
            <div v-for="chapter in novel.chapters" :key="chapter.id" class="chapter-manage-item">
              <article class="chapter-row cute-row">
                <button class="chapter-row-main" type="button" @click="openChapter(chapter.id)">
                  <span>第 {{ chapter.sequenceNo }} 章</span>
                  <strong>{{ chapter.title }}</strong>
                </button>

                <div class="chapter-row-actions">
                  <RouterLink :to="`/users/${chapter.authorId}`" class="author-link inline-author" @click.stop>
                    {{ chapter.authorName }}
                  </RouterLink>
                  <button
                    v-if="owner"
                    class="filter-chip chapter-inline-edit"
                    type="button"
                    :disabled="chapterEditLoading && selectedChapterEditId !== chapter.id"
                    @click.stop="startQuickEdit(chapter.id)"
                  >
                    {{ selectedChapterEditId === chapter.id ? '收起本章编辑' : '直接编辑本章' }}
                  </button>
                </div>
              </article>

              <form
                v-if="owner && selectedChapterEditId === chapter.id"
                class="owner-edit-panel cute-panel chapter-inline-editor"
                @submit.prevent="saveQuickEdit(chapter.id)"
              >
                <label>
                  章节标题
                  <input v-model.trim="chapterEditForm.title" required maxlength="200" />
                </label>

                <label>
                  章节正文
                  <textarea v-model="chapterEditForm.content" class="story-input" required maxlength="100000"></textarea>
                </label>

                <div class="owner-action-bar end">
                  <button class="primary cute-primary" :disabled="savingChapterEdit">
                    {{ savingChapterEdit ? '保存中…' : '保存本章修改' }}
                  </button>
                </div>
              </form>
            </div>
          </div>
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

    <Transition name="toast-pop">
      <div v-if="successMessage" class="success-toast">
        <span class="success-icon">✓</span>
        <div>
          <strong>保存成功</strong>
          <p>{{ successMessage }}</p>
        </div>
        <span class="success-heart">♡</span>
      </div>
    </Transition>

    <Transition name="comment-fold">
      <div v-if="confirmState.open" class="cute-confirm-backdrop" @click.self="closeConfirm">
        <div class="cute-confirm-dialog cute-panel">
          <p class="eyebrow">CONFIRM ACTION</p>
          <h3>{{ confirmState.title }}</h3>
          <p>{{ confirmState.description }}</p>
          <div class="cute-confirm-actions">
            <button class="filter-chip" type="button" @click="closeConfirm">先想想</button>
            <button class="primary cute-primary" type="button" :disabled="confirmState.loading" @click="confirmAction">
              {{ confirmState.loading ? '处理中…' : confirmState.confirmText }}
            </button>
          </div>
        </div>
      </div>
    </Transition>
  </main>
</template>
