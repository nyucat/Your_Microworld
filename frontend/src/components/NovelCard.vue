<script setup>
import { computed } from 'vue'

const props = defineProps({
  novel: { type: Object, required: true }
})

const typeLabel = computed(() => (props.novel.type === 'MICRO' ? '微小说' : '连载'))
</script>

<template>
  <RouterLink :to="`/novels/${novel.id}`" class="novel-card cute-card sparkle-surface">
    <i class="hover-particle star"></i>
    <i class="hover-particle heart"></i>

    <div class="novel-card-top">
      <span class="card-mark">✦</span>
      <div class="card-badge-group">
        <span class="card-badge">{{ typeLabel }}</span>
        <span class="card-badge soft">已发布</span>
      </div>
    </div>

    <RouterLink :to="`/users/${novel.authorId}`" class="eyebrow author-link" @click.stop>
      {{ novel.authorName }}
    </RouterLink>

    <h3>{{ novel.title }}</h3>
    <p>{{ novel.description || '作者暂时还没有留下简介。' }}</p>

    <div v-if="novel.tags?.length" class="card-tags">
      <span v-for="tag in novel.tags.slice(0, 3)" :key="tag" class="tag-chip">{{ tag }}</span>
    </div>

    <div class="novel-card-meta">
      <small>{{ new Date(novel.createdAt).toLocaleDateString('zh-CN') }}</small>
      <span class="card-arrow">→</span>
    </div>
  </RouterLink>
</template>
