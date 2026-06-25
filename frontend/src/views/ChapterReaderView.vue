<script setup>
import { onMounted, ref } from 'vue'
import { getChapter } from '../api'

const props = defineProps({ id: String })
const chapter = ref(null)
const error = ref('')

onMounted(async () => {
  try {
    chapter.value = await getChapter(props.id)
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <main>
    <nav class="nav container">
      <RouterLink class="brand" to="/"><span>✦</span> Your Microworld</RouterLink>
      <RouterLink v-if="chapter" class="nav-login" :to="`/novels/${chapter.novelId}`">返回小说</RouterLink>
    </nav>

    <article v-if="chapter" class="reader container">
      <p class="eyebrow">{{ chapter.novelTitle }} · 第 {{ chapter.sequenceNo }} 章</p>
      <h1>{{ chapter.title }}</h1>
      <p class="reader-author">
        <RouterLink :to="`/users/${chapter.authorId}`" class="author-link">{{ chapter.authorName }}</RouterLink>
        <span> · {{ new Date(chapter.createdAt).toLocaleDateString('zh-CN') }}</span>
      </p>
      <div class="content">
        <p v-for="(paragraph, index) in chapter.content.split(/\n\s*\n|\r?\n/)" :key="index">{{ paragraph }}</p>
      </div>
    </article>

    <p v-else class="empty-state">{{ error || '正在打开章节…' }}</p>
  </main>
</template>
