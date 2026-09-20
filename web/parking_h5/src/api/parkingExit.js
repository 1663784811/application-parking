import axios from "./axiosRequest";

// 扫码缴费出场接口（业务流程_h5.md）。
// 注意：项目约定 src/api/app.js 与 axiosRequest.js 是禁改文件，本流程的接口单独放这里，
// 复用同一个 axios 实例与拦截器（token、信封解包、token 过期重试都在拦截器里）。
const baseUrl = import.meta.env.VITE_BASE_URL;

// 接口1：查询通道当前要出场的车辆（页面加载时调用）
export const getChannelVehicle = (params = {}) => {
    return axios.get(`${baseUrl}/api/app/parking/exit/channelVehicle`, { params });
};

// 接口2：查询当前停车场车辆停车费用订单（车牌输入完整时调用）
export const getExitOrder = (params = {}) => {
    return axios.get(`${baseUrl}/api/app/parking/exit/order`, { params });
};

// 接口3：支付停车费用（支付宝、微信）
export const payExitOrder = (params = {}) => {
    return axios.post(`${baseUrl}/api/app/parking/exit/pay`, params);
};
