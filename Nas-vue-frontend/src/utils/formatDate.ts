export const formatDate = (isoString:string | Date) => {
    if(!isoString) return ''
    const date = new Date(isoString)

    //获取年月日
    const y = date.getFullYear()
    const m = (date.getMonth() + 1).toString().padStart(2,'0')
    const d = date.getDay().toString().padStart(2,'0')
    const hh = date.getHours().toString().padStart(2,'0')
    const mm = date.getMinutes().toString().padStart(2, '0');
    const ss = date.getSeconds().toString().padStart(2, '0'); 

    return `${y}-${m}-${d} ${hh}:${mm}`; // 返回格式：2026-01-27 10:13
}