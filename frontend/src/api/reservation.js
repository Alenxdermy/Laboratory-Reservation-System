import request from '@/utils/request'

export default {
  submit(data) {
    return request.post('/reservations', data)
  },
  my() {
    return request.get('/reservations/my')
  },
  list(params) {
    return request.get('/reservations', { params })
  },
  audit(id, status) {
    return request.put(`/reservations/${id}/audit`, { status })
  },
  cancel(id) {
    return request.delete(`/reservations/${id}`)
  }
}
