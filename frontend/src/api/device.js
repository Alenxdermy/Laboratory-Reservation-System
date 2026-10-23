import request from '@/utils/request'

export default {
  list(params) {
    return request.get('/devices', { params })
  },
  create(data) {
    return request.post('/devices', data)
  },
  update(id, data) {
    return request.put(`/devices/${id}`, data)
  },
  remove(id) {
    return request.delete(`/devices/${id}`)
  }
}
