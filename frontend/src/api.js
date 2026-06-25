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
  const body = await response.json()
  if (!response.ok || !body.success) throw new Error(body.message || '请求失败，请稍后重试')
  return body.data
}

export const getHome = () => request('/home')
export const getHealth = () => request('/health')
export const register = (payload) => request('/auth/register', { method: 'POST', body: JSON.stringify(payload) })
export const login = (payload) => request('/auth/login', { method: 'POST', body: JSON.stringify(payload) })
export const getNovels = (page = 0, size = 12) => request(`/novels?page=${page}&size=${size}`)
export const getNovel = (id) => request(`/novels/${id}`)
export const createNovel = (payload) => request('/novels', { method: 'POST', body: JSON.stringify(payload) })
export const addChapter = (novelId, payload) => request(`/novels/${novelId}/chapters`, { method: 'POST', body: JSON.stringify(payload) })
export const getChapter = (id) => request(`/chapters/${id}`)
export const getUserProfile = (id) => request(`/users/${id}`)
