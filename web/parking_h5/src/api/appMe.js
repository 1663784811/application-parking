import axios from "./axiosRequest";

// 「我的」页面接口。
// 注意：项目约定 src/api/app.js 与 axiosRequest.js 是禁改文件，新接口单独放这里，
// 复用同一个 axios 实例与拦截器（token、信封解包、token 过期重试都在拦截器里）。
// 拦截器已把 BaseResult 解包，业务数据统一在 res.data 里。
const baseUrl = import.meta.env.VITE_BASE_URL;

// 「我的」整页数据：头部三格统计 + 三格列表统计 + 四张列表一次拿齐。
// 页面挂载时只调这一个，省掉连发 6 个请求。
export const getMeBoard = (params = {}) => {
    return axios.get(`${baseUrl}/api/app/user/me/board`, { params });
};

// 我的车辆列表（车辆页进来了但没带数据时用，或保存/删除后单独刷新）
export const getMeVehicleList = (params = {}) => {
    return axios.get(`${baseUrl}/api/app/user/me/vehicle/list`, { params });
};

// 保存车辆：id 为空是新增，有值是按记录更新。
// isDefault 是 Boolean，后端会转成 1/0。
export const saveMeVehicle = (data = {}) => {
    return axios.post(`${baseUrl}/api/app/user/me/vehicle/save`, data);
};

// 删除车辆（后端软删除）
export const deleteMeVehicle = (id, params = {}) => {
    return axios.delete(`${baseUrl}/api/app/user/me/vehicle/delete/${id}`, { params });
};

// 优惠券列表
export const getMeCouponList = (params = {}) => {
    return axios.get(`${baseUrl}/api/app/user/me/coupon/list`, { params });
};

// 订单列表
export const getMeOrderList = (params = {}) => {
    return axios.get(`${baseUrl}/api/app/user/me/order/list`, { params });
};
