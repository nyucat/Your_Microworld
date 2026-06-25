<script setup>
import { onMounted, ref } from 'vue'
import { getUserProfile } from '../api'
import TopNav from '../components/TopNav.vue'
import NovelCard from '../components/NovelCard.vue'

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
  <main class="page-shell">
    <TopNav mode="simple" back-to="/" back-label="返回首页" />

    <section v-if="profile" class="profile container">
      <div class="profile-hero cute-panel">
        <div class="profile-avatar">{{ profile.username.slice(0, 1).toUpperCase() }}</div>
        <div>
          <p class="eyebrow">USER PROFILE</p>
          <h1>{{ profile.username }}</h1>
          <p class="lead">这里展示 {{ profile.username }} 的创作身份、加入时间，以及已经公开发布的作品。</p>
        </div>
      </div>

      <div class="profile-stats">
        <article class="cute-panel">
          <span>身份</span>
          <strong>{{ profile.role }}</strong>
        </article>
        <article class="cute-panel">
          <span>加入时间</span>
          <strong>{{ new Date(profile.joinedAt).toLocaleDateString('zh-CN') }}</strong>
        </article>
        <article class="cute-panel">
          <span>已发布小说</span>
          <strong>{{ profile.publishedNovelCount }}</strong>
        </article>
        <article class="cute-panel">
          <span>已写章节</span>
          <strong>{{ profile.publishedChapterCount }}</strong>
        </article>
      </div>

      <section class="profile-works">
        <div class="section-heading cute-heading">
          <div>
            <p class="eyebrow">PUBLIC WORKS</p>
            <h2>公开作品</h2>
          </div>
          <span class="count-pill">{{ profile.recentNovels.length }} 部展示中</span>
        </div>

        <div v-if="!profile.recentNovels.length" class="empty-state cute-empty">这个账号还没有公开发布的小说。</div>
        <div v-else class="novel-grid">
          <NovelCard v-for="novel in profile.recentNovels" :key="novel.id" :novel="novel" />
        </div>
      </section>
    </section>

    <p v-else class="empty-state cute-empty">{{ error || '正在加载个人主页…' }}</p>
  </main>
</template>
