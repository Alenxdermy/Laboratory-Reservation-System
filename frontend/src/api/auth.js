import request from '@/utils/request'

export default {
  login(data) {
    return request.post('/auth/login', data)
  },
  register(data) {
    return request.post('/auth/register', data)
  },
  getCurrentUser() {
    return request.get('/auth/me')
  }
}
