import axios from "./axiosRequest";

// 第三方登录（微信公众号 / 支付宝），流程见 微信H5授权流程.md。
// 注意：项目约定 src/api/app.js 与 axiosRequest.js 是禁改文件，本流程的接口单独放这里，
// 复用同一个 axios 实例与拦截器（token、信封解包、token 过期重试都在拦截器里）。
//
// 这里只有登录：拿 code / auth_code 换登录态。取授权链接（/wechatMpAuthUrl、/alipayAuthUrl）
// 目前前端没有调用方。
const baseUrl = import.meta.env.VITE_BASE_URL;

// 微信公众号网页授权回跳带回的 code，换登录态，返回 { jwtToken, refreshToken }
export const wechatMpLogin = (params = {}) => {
    return axios.post(`${baseUrl}/api/app/login/wechatMpLogin`, params);
};

// 支付宝授权回跳带回的 auth_code（后端接口的字段名仍叫 code）
export const alipayLogin = (params = {}) => {
    return axios.post(`${baseUrl}/api/app/login/alipayLogin`, params);
};
