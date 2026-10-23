import request from '@/utils/request'

export default {
  list() {
    return request.get('/labs')
  },
  create(data) {
    return request.post('/labs', data)
  },
  update(id, data) {
    return request.put(`/labs/${id}`, data)
  },
  remove(id) {
    return request.delete(`/labs/${id}`)
  }
}
