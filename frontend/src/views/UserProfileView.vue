<script setup>
import { onMounted, ref } from 'vue'
import { getUserProfile } from '../api'

const props = defineProps({ id: String })
const profile = ref(null)
const error = ref('')

onMounted(async () => {
  try {
    profile.value = await getUserProfile(props.id)
  } catch (e) {
    error.value = e.message
  }
})
</script>

<template>
  <main>
    <nav class="nav container">
      <RouterLink class="brand" to="/"><span>✦</span> Your Microworld</RouterLink>
      <RouterLink class="nav-login" to="/">返回首页</RouterLink>
    </nav>

    <section v-if="profile" class="profile container">
      <div class="profile-hero">
        <div class="profile-avatar">{{ profile.username.slice(0, 1).toUpperCase() }}</div>
        <div>
          <p class="eyebrow">USER PROFILE</p>
          <h1>{{ profile.username }}</h1>
          <p class="lead">这是 {{ profile.username }} 的个人主页，这里会展示创作身份、加入时间和公开发布的作品。</p>
        </div>
      </div>

      <div class="profile-stats">
        <article>
          <span>身份</span>
          <strong>{{ profile.role }}</strong>
        </article>
        <article>
          <span>加入时间</span>
          <strong>{{ new Date(profile.joinedAt).toLocaleDateString('zh-CN') }}</strong>
        </article>
        <article>
          <span>已发布小说</span>
          <strong>{{ profile.publishedNovelCount }}</strong>
        </article>
        <article>
          <span>已写章节</span>
          <strong>{{ profile.publishedChapterCount }}</strong>
        </article>
      </div>

      <section class="profile-works">
        <div class="section-heading">
          <div>
            <p class="eyebrow">PUBLIC WORKS</p>
            <h2>公开作品</h2>
          </div>
          <span>{{ profile.recentNovels.length }} 部展示中</span>
        </div>

        <div v-if="!profile.recentNovels.length" class="empty-state">这个账号还没有公开发布的小说。</div>
        <div v-else class="novel-grid">
          <RouterLink v-for="novel in profile.recentNovels" :key="novel.id" :to="`/novels/${novel.id}`" class="novel-card">
            <span class="card-mark">✦</span>
            <p class="eyebrow">{{ novel.authorName }}</p>
            <h3>{{ novel.title }}</h3>
            <p>{{ novel.description || '作者尚未添加简介。' }}</p>
            <small>{{ new Date(novel.createdAt).toLocaleDateString('zh-CN') }}</small>
          </RouterLink>
        </div>
      </section>
    </section>

    <p v-else class="empty-state">{{ error || '正在加载个人主页…' }}</p>
  </main>
</template>
