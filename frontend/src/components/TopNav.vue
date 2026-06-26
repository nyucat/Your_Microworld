<script setup>
import { computed, ref, watch } from 'vue'

const props = defineProps({
  mode: { type: String, default: 'full' },
  modelValue: { type: String, default: '' },
  backTo: { type: String, default: '/' },
  backLabel: { type: String, default: '返回首页' }
})

const emit = defineEmits(['update:modelValue', 'logout'])
const user = ref(null)

const search = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

watch(
  () => props.mode,
  () => {
    user.value = JSON.parse(localStorage.getItem('microworld-user') || 'null')
  },
  { immediate: true }
)
</script>

<template>
  <nav class="topbar container">
    <RouterLink class="brand brand-cute" to="/"><span>✨</span> Your Microworld</RouterLink>

    <div v-if="mode === 'full'" class="topbar-menu topbar-menu-cute">
      <RouterLink class="topbar-link" to="/published">已发布</RouterLink>
      <span class="topbar-divider" aria-hidden="true"></span>
      <RouterLink class="topbar-link" to="/categories">分类</RouterLink>
      <span class="topbar-divider" aria-hidden="true"></span>

      <label class="topbar-search cute-search" aria-label="搜索小说">
        <input v-model.trim="search" type="text" placeholder="搜索小说、作者、灵感" />
        <span class="search-icon" aria-hidden="true">⌕</span>
      </label>

      <template v-if="user">
        <span class="topbar-divider" aria-hidden="true"></span>
        <RouterLink class="topbar-link" to="/novels/create">发布小说</RouterLink>
        <span class="topbar-divider" aria-hidden="true"></span>
        <RouterLink class="topbar-link" :to="`/users/${user.userId}`">我的主页</RouterLink>
        <span class="topbar-divider" aria-hidden="true"></span>
        <button class="topbar-link topbar-button" @click="$emit('logout')">退出登录</button>
      </template>

      <template v-else>
        <span class="topbar-divider" aria-hidden="true"></span>
        <RouterLink class="topbar-link" to="/login">登录</RouterLink>
        <span class="topbar-divider" aria-hidden="true"></span>
        <RouterLink class="topbar-link" to="/register">注册</RouterLink>
      </template>
    </div>

    <div v-else class="topbar-menu topbar-menu-compact">
      <RouterLink class="nav-chip" :to="backTo">{{ backLabel }}</RouterLink>
    </div>
  </nav>
</template>
