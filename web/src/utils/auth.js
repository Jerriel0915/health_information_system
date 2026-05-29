const TokenKey = 'Admin-Token'

// 获取 token
export function getToken() {
    const nameEQ = TokenKey + '='
    const ca = document.cookie.split(';')
    for (let i = 0; i < ca.length; i++) {
        let c = ca[i].trim()
        if (c.indexOf(nameEQ) === 0) {
            return c.substring(nameEQ.length, c.length)
        }
    }
    return null
}

// 设置 token
export function setToken(token) {
    document.cookie = TokenKey + '=' + token + '; path=/'
}

// 移除 token
export function removeToken() {
    document.cookie = TokenKey + '=; path=/; expires=Thu, 01 Jan 1970 00:00:00 GMT'
}
