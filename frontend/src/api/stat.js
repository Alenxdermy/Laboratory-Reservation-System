import request from '@/utils/request'

export default {
  weeklyUsage() {
    return request.get('/stats/weekly-usage')
  }
}
