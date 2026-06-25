<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addChapter, getNovel } from '../api'
import TopNav from '../components/TopNav.vue'

const props = defineProps({ id: String })
const router = useRouter()
const novel = ref(null)
const error = ref('')
const publishing = ref(false)
const form = reactive({ title: '', content: '' })
const user = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))

const owner = computed(() => user.value?.userId === novel.value?.authorId)
const isMicro = computed(() => novel.value?.type === 'MICRO')
const typeLabel = computed(() => (isMicro.value ? '微小说' : '连载小说'))

async function load() {
  try {
    novel.value = await getNovel(props.id)
  } catch (e) {
    error.value = e.message
  }
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
        <p class="lead">{{ novel.description || '作者暂时还没有添加简介。' }}</p>

        <details v-if="novel.worldSetting || novel.outlineContent" class="meta cute-meta">
          <summary>世界观与大纲</summary>
          <p v-if="novel.worldSetting">{{ novel.worldSetting }}</p>
          <p v-if="novel.outlineContent">{{ novel.outlineContent }}</p>
        </details>

        <section v-if="isMicro" class="micro-reader">
          <div class="section-heading cute-heading">
            <h2>正文</h2>
          </div>

          <div class="content micro-content">
            <p v-for="(paragraph, index) in (novel.microContent || '').split(/\n\s*\n|\r?\n/)" :key="index">
              {{ paragraph }}
            </p>
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
