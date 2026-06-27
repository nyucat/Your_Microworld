<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  addChapter,
  createComment,
  deleteComment,
  deleteNovel,
  getChapterComments,
  getNovel,
  toggleCommentLike,
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
const deletingNovel = ref(false)
const editingNovel = ref(false)
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
const parsedEditTags = computed(() =>
  editForm.tagInput
    .split(/[，,]/)
    .map((tag) => tag.trim())
    .filter(Boolean)
    .slice(0, 5)
)

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
  } catch (e) {
    error.value = e.message
  } finally {
    savingEdit.value = false
  }
}

async function handleDeleteNovel() {
  if (!window.confirm('确认删除这部小说吗？删除后书库中将不再显示。')) return

  deletingNovel.value = true
  error.value = ''
  try {
    await deleteNovel(props.id)
    router.push('/published')
  } catch (e) {
    error.value = e.message
  } finally {
    deletingNovel.value = false
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

async function handleDeleteComment(commentId) {
  if (!window.confirm('确认删除这条评论吗？')) return

  try {
    error.value = ''
    await deleteComment(commentId)
    await refreshMicroComments()
  } catch (e) {
    error.value = e.message
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

        <div v-if="owner" class="owner-action-bar">
          <button class="filter-chip" @click="editingNovel = !editingNovel">
            {{ editingNovel ? '收起编辑' : '编辑小说信息' }}
          </button>
          <button class="filter-chip danger-chip" :disabled="deletingNovel" @click="handleDeleteNovel">
            {{ deletingNovel ? '删除中…' : '删除小说' }}
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
