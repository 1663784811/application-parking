import axios from "./axiosRequest";
import { upLoadFile } from "./axiosRequest";

const baseUrl = import.meta.env.VITE_BASE_URL;

// 通用查询
export function commonQuery(params = {}) { return axios.get(`${baseUrl}/api/common/sql/query`, { params }); }
// 通用保存
export function commonSave(params = {}) { return axios.post(`${baseUrl}/api/common/sql/save`, params); }
// 通用删除
export const commonDel = (params) => { return axios.post(`${baseUrl}/api/common/sql/del`, params); }
// 获取页面设置
export const pageSetting = (params = {}) => { return axios.get(`${baseUrl}/api/common/page/pageSetting`, { params }); }
export const settingQuery = (params = {}) => { return axios.get(`${baseUrl}/api/common/page/setting`, { params }); }

// 登录
export const logInFn = (params = {}) => { return axios.post(`${baseUrl}/api/admin/login/login`, params); }
// 门店管理员登录（用户名 + 密码 + 验证码 + 验证码编码Key）
export const storeAdminLogin = (params = {}) => { return axios.post(`${baseUrl}/api/admin/login/storeAdminLogin`, params); }
// 注册
export const register = (params = {}) => { return axios.post(`${baseUrl}/api/admin/login/register`, params); }
// 刷新 token
export const refreshTokenRequest = (params) => { return axios.post(`${baseUrl}/api/common/token/refreshToken`, params); }
// 用户信息
export const userInfo = () => { return axios.get(`${baseUrl}/api/common/token/findUserInfo`); }
// 获取验证码
export const getVerifyCode = (params) => { return axios.post(`${baseUrl}/api/common/verify/getVerifyCode`, params); }

// 文件上传
export const uploadFile = (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return upLoadFile.post(`${baseUrl}/api/common/file/upload`, formData);
};
