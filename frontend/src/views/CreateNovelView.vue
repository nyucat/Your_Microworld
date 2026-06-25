<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { createNovel } from '../api'

const router = useRouter()
const saving = ref(false)
const error = ref('')
const form = reactive({
  title: '',
  description: '',
  worldSetting: '',
  outlineContent: '',
  allowIfBranch: true,
  allowBid: true,
  firstChapterTitle: '第一章',
  firstChapterContent: ''
})

async function submit() {
  saving.value = true
  error.value = ''
  try {
    const novel = await createNovel(form)
    router.push(`/novels/${novel.id}`)
  } catch (e) {
    error.value = e.message
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <main class="editor-page">
    <nav class="nav container">
      <RouterLink class="brand" to="/"><span>✦</span> Your Microworld</RouterLink>
      <RouterLink class="nav-login" to="/">返回书库</RouterLink>
    </nav>

    <form class="editor container" @submit.prevent="submit">
      <header>
        <p class="eyebrow">NEW NOVEL</p>
        <h1>发布你的小说</h1>
        <p>先为读者打开一扇门，再用第一章邀请他们走进来。</p>
      </header>

      <label>
        小说标题
        <input v-model.trim="form.title" required maxlength="200" placeholder="给故事一个名字" />
      </label>

      <label>
        简介
        <textarea v-model.trim="form.description" maxlength="5000" placeholder="这部小说讲述了什么？"></textarea>
      </label>

      <details>
        <summary>世界观与大纲（可选）</summary>

        <label>
          世界观
          <textarea v-model.trim="form.worldSetting" maxlength="20000" placeholder="人物、时代、规则……"></textarea>
        </label>

        <label>
          故事大纲
          <textarea v-model.trim="form.outlineContent" maxlength="20000" placeholder="可公开的大纲内容"></textarea>
        </label>
      </details>

      <div class="checks">
        <label><input v-model="form.allowIfBranch" type="checkbox" /> 允许未来创建 IF 分支</label>
        <label><input v-model="form.allowBid" type="checkbox" /> 允许未来发起主线竞标</label>
      </div>

      <hr />

      <p class="eyebrow">FIRST CHAPTER</p>

      <label>
        第一章标题
        <input v-model.trim="form.firstChapterTitle" required maxlength="200" />
      </label>

      <label>
        第一章正文
        <textarea
          v-model="form.firstChapterContent"
          class="story-input"
          required
          maxlength="100000"
          placeholder="在这里写下故事的开端……"
        ></textarea>
      </label>

      <button class="primary publish" :disabled="saving">
        {{ saving ? '正在发布…' : '发布小说与第一章' }}
      </button>

      <p v-if="error" class="form-message">{{ error }}</p>
    </form>
  </main>
</template>
