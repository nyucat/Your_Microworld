async function request(path, options = {}) {
  const token = localStorage.getItem('microworld-token')
  const response = await fetch(`/api${path}`, {
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers },
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
