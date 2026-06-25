<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addChapter, getNovel } from '../api'

const props = defineProps({ id: String })
const router = useRouter()
const novel = ref(null)
const error = ref('')
const publishing = ref(false)
const form = reactive({ title: '', content: '' })
const user = ref(JSON.parse(localStorage.getItem('microworld-user') || 'null'))

const owner = computed(() => user.value?.userId === novel.value?.authorId)

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
  <main>
    <nav class="nav container">
      <RouterLink class="brand" to="/"><span>✦</span> Your Microworld</RouterLink>
      <RouterLink class="nav-login" to="/">返回书库</RouterLink>
    </nav>

    <section v-if="novel" class="detail container">
      <RouterLink :to="`/users/${novel.authorId}`" class="eyebrow author-link">{{ novel.authorName }} 创作</RouterLink>
      <h1>{{ novel.title }}</h1>
      <p class="lead">{{ novel.description || '作者尚未添加简介。' }}</p>

      <details v-if="novel.worldSetting || novel.outlineContent" class="meta">
        <summary>世界观与大纲</summary>
        <p v-if="novel.worldSetting">{{ novel.worldSetting }}</p>
        <p v-if="novel.outlineContent">{{ novel.outlineContent }}</p>
      </details>

      <section class="chapter-list">
        <div class="section-heading">
          <h2>主线章节</h2>
          <span>{{ novel.chapters.length }} 章</span>
        </div>
        <article v-for="chapter in novel.chapters" :key="chapter.id" class="chapter-row clickable-card" @click="openChapter(chapter.id)">
          <span>第 {{ chapter.sequenceNo }} 章</span>
          <strong>{{ chapter.title }}</strong>
          <RouterLink :to="`/users/${chapter.authorId}`" class="author-link inline-author" @click.stop>{{ chapter.authorName }}</RouterLink>
        </article>
      </section>

      <form v-if="owner" class="chapter-form" @submit.prevent="submitChapter">
        <p class="eyebrow">CONTINUE WRITING</p>
        <h2>续写主线</h2>
        <label>章节标题<input v-model.trim="form.title" required maxlength="200" /></label>
        <label>正文<textarea v-model="form.content" class="story-input" required maxlength="100000"></textarea></label>
        <button class="primary publish" :disabled="publishing">{{ publishing ? '正在发布…' : '发布下一章' }}</button>
      </form>
    </section>

    <p v-else-if="error" class="empty-state">{{ error }}</p>
  </main>
</template>
