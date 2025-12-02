import dayjs from 'dayjs'
import 'dayjs/locale/ko'

dayjs.locale('ko')

export const formatDate = (date, format = 'YYYY-MM-DD') => {
  return dayjs(date).format(format)
}

export const formatDateKorean = (date) => {
  return dayjs(date).format('YYYY년 M월 D일')
}

export const getToday = () => {
  return dayjs().format('YYYY-MM-DD')
}

export const isToday = (date) => {
  return dayjs(date).isSame(dayjs(), 'day')
}