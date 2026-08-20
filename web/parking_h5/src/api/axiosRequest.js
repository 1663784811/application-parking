import axios from 'axios'
import { loginInfo } from '@/stores/loginInfo.js'

const instance = axios.create();

instance.defaults.withCredentials = true
instance.defaults.headers['X-Requested-With'] = 'XMLHttpRequest'
instance.defaults.headers.post['Content-Type'] = 'application/json'

const requestConfig = function (config) {
    const userStore = loginInfo();
    config.headers['Authorization'] = userStore.token;
    return config
}

const responseConfig = async function (rest) {
    if (typeof rest.data !== 'object') {
        console.error('网络错误', rest)
        return Promise.reject(rest)
    }
    if (rest.data && rest.data.code !== 2000) {
        return Promise.reject(rest)
    }
    return rest.data
}

/**
 * 请求拦截器
 */
instance.interceptors.request.use(requestConfig, error => Promise.reject(error));
/**
 * 响应拦截器
 */
instance.interceptors.response.use(async (rest) => {
    if (typeof rest.data !== 'object') {
        console.error('网络错误', rest)
        return Promise.reject(rest)
    }
    if (rest.data && rest.data.code !== 2000) {
        if (rest.data && (rest.data.code === 6010 || rest.data.code === 6001)) {
            console.log('token 过期============================')
            const userStore = loginInfo();
            const returnToken = await userStore.refreshShortToken();
            if (returnToken) {
                // 重试
                const config = rest.config
                config.headers.Authorization = userStore.token;
                return instance(config)
            }
        }
        return Promise.reject(rest.data)
    }
    return rest.data
}, async error => {
    console.log('错误', error);
    return Promise.reject(error);
});
export default instance


// ====================================================================

const upLoad = axios.create();
//请求拦截器
upLoad.interceptors.request.use(requestConfig, error => Promise.reject(error))
// 响应拦截器
upLoad.interceptors.response.use(responseConfig, error => Promise.reject(error))
export const upLoadFile = upLoad;
