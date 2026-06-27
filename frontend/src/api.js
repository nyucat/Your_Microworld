async function request(path, options = {}) {
  const token = localStorage.getItem('microworld-token')
  const response = await fetch(`/api${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...options.headers
    },
    ...options
  })

  const raw = await response.text()
  let body = null

  if (raw) {
    try {
      body = JSON.parse(raw)
    } catch {
      throw new Error(raw || `请求失败（${response.status}）`)
    }
  }

  if (!response.ok) {
    if (body?.message) throw new Error(body.message)
    if (response.status === 401 || response.status === 403) {
      throw new Error('登录状态已失效，请重新登录后再试。')
    }
    throw new Error(`请求失败（${response.status}）`)
  }

  if (!body?.success) {
    throw new Error(body?.message || '请求失败，请稍后重试')
  }

  return body.data
}

export const getHome = () => request('/home')
export const getHealth = () => request('/health')
export const register = (payload) => request('/auth/register', { method: 'POST', body: JSON.stringify(payload) })
export const login = (payload) => request('/auth/login', { method: 'POST', body: JSON.stringify(payload) })
export const getNovels = (page = 0, size = 12, tag = '', category = '') =>
  request(
    `/novels?page=${page}&size=${size}` +
      `${tag ? `&tag=${encodeURIComponent(tag)}` : ''}` +
      `${category ? `&category=${encodeURIComponent(category)}` : ''}`
  )
export const getCategoryOverview = (category = '') =>
  request(`/novels/category-overview${category ? `?category=${encodeURIComponent(category)}` : ''}`)
export const getNovel = (id) => request(`/novels/${id}`)
export const createNovel = (payload) => request('/novels', { method: 'POST', body: JSON.stringify(payload) })
export const updateNovel = (novelId, payload) =>
  request(`/novels/${novelId}`, { method: 'PATCH', body: JSON.stringify(payload) })
export const deleteNovel = (novelId) => request(`/novels/${novelId}`, { method: 'DELETE' })
export const addChapter = (novelId, payload) => request(`/novels/${novelId}/chapters`, { method: 'POST', body: JSON.stringify(payload) })
export const getChapter = (id) => request(`/chapters/${id}`)
export const updateChapter = (chapterId, payload) =>
  request(`/chapters/${chapterId}`, { method: 'PATCH', body: JSON.stringify(payload) })
export const getChapterComments = (chapterId) => request(`/chapters/${chapterId}/comments`)
export const createComment = (chapterId, payload) =>
  request(`/chapters/${chapterId}/comments`, { method: 'POST', body: JSON.stringify(payload) })
export const toggleCommentLike = (commentId) =>
  request(`/comments/${commentId}/like`, { method: 'POST' })
export const deleteComment = (commentId) =>
  request(`/comments/${commentId}`, { method: 'DELETE' })
export const getTags = () => request('/tags')
export const getUserProfile = (id) => request(`/users/${id}`)
export const updateMyProfile = (payload) =>
  request('/users/me/profile', { method: 'PATCH', body: JSON.stringify(payload) })
export const getMyInteractions = () => request('/users/me/interactions')
export const updateInteractionReadState = (commentId, payload) =>
  request(`/users/me/interactions/${commentId}/read-state`, { method: 'PATCH', body: JSON.stringify(payload) })
export const markAllInteractionsRead = () =>
  request('/users/me/interactions/read-all', { method: 'PATCH' })
