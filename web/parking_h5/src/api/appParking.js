import axios from "./axiosRequest";

// 首页「附近停车场」接口。
// 注意：项目约定 src/api/app.js 与 axiosRequest.js 是禁改文件，新接口单独放这里，
// 复用同一个 axios 实例与拦截器（token、信封解包、token 过期重试都在拦截器里）。
const baseUrl = import.meta.env.VITE_BASE_URL;

// 附近停车场列表。
// lng/lat 由浏览器定位提供（可选）：都传了后端才按距离排序并返回 distance，
// 缺省则按创建时间倒序、无距离字段。
export const getParkingList = (params = {}) => {
    return axios.get(`${baseUrl}/api/app/parking/list`, { params });
};