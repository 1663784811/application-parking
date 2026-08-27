package com.cyyaw.netty.mqtt.ctl.service.impl;

import com.cyyaw.netty.mqtt.ctl.ParkingMqtt;
import com.cyyaw.netty.mqtt.ctl.enity.CarInfo;
import com.cyyaw.netty.mqtt.ctl.service.DeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private ParkingMqtt parkingMqtt;


    @Override
    public void detectCamera(CarInfo carInfo) {
//        IotDevice device = iotDeviceDao.findByCode(carInfo.getCode());
//        Integer locationType = device.getLocationType();
//        if (locationType == 1) {
//            carIn(device, carInfo);
//        } else if (locationType == 2) {
//            carOut(device, carInfo);
//        }
    }
//
//    @Transactional
//    public void carIn(IotDevice device, CarInfo carInfo) {
//        String code = device.getCode();
//        // 入口
//        // 判断车位是否满
//        Long businessId = device.getBusinessId();
//        Long enId = device.getEnId();
//        PkParking pkParking = pkParkingDao.selectById(businessId);
//        Integer openingUp = pkParking.getOpeningUp();
//        if (null != openingUp && openingUp == 1) {
//            // 封闭式停车场, 查会员, 开闸
//            parkingMqtt.showDisplay(code, "欢迎 粤C12345 进场");
//            parkingMqtt.openTheGate(code);
//            parkingMqtt.showDisplay(code, "抱歉此停车场不对外开放");
//            parkingMqtt.openTheGate(code);
//        } else {
//            Integer capacity = pkParking.getCapacity();
//            // 查在场车辆
//            int count = pkCarLogDao.selectStatusCount(businessId, 0);
//            if (capacity - count > 0) {
//                // 有空车位
//                // 记录停车日志、减少车位
//                PkCarLog pkCarLog = new PkCarLog();
//                pkCarLog.setAppId(pkParking.getAppId());
//                pkCarLog.setParkingId(pkParking.getId());
//                pkCarLog.setCarNumber(carInfo.getCarNumber());
//                pkCarLog.setEntryTime(LocalDateTime.now());
//                pkCarLog.setCarType(carInfo.getCarType());
//                pkCarLog.setStatus(0);
//                pkCarLog.setEnId(enId);
//                pkCarLogDao.insert(pkCarLog);
//                // 生成订单、
//                OrOrder orOrder = new OrOrder();
//                orOrder.setAppId(pkParking.getAppId());
//                orOrder.setPayStatus(0);
//                orOrder.setOrderStatus(0);
//                orOrder.setPayTime(LocalDateTime.now());
//                orOrder.setDeliveryTime(LocalDateTime.now());
//                orOrder.setCompleteTime(LocalDateTime.now());
//                orOrder.setCancelTime(LocalDateTime.now());
//                orOrder.setRemark("停车场");
//                orOrder.setEnId(pkParking.getEnId());
//                List<OrOrderDetail> orderDetailList = new ArrayList<>();
//                OrOrderDetail orOrderDetail = new OrOrderDetail();
//                orOrderDetail.setBusinessId(pkCarLog.getId());
//                orOrderDetail.setProductName(pkCarLog.getCarNumber());
//                orOrderDetail.setProductImage("");
//                orOrderDetail.setQuantity(BigDecimal.ONE);
//                orderDetailList.add(orOrderDetail);
//                orOrder.setOrderDetailList(orderDetailList);
//                orOrderService.createOrder(orOrder);
//                // 开闸、播放语音: 欢迎 粤C12345
//                parkingMqtt.showDisplay(code, "欢迎 粤C12345 进场");
//                parkingMqtt.openTheGate(code);
//            } else {
//                // 没有空车位，播放语音: 车位已满,请等待
//                parkingMqtt.showDisplay(code, "车位已满,请等待");
//            }
//        }
//    }
//
//    /**
//     * 出场
//     * 1. 出闸摄像头拍到车牌则结束订单, 以免跟车出场产生过多费用
//     */
//    public void carOut(IotDevice device, CarInfo carInfo) {
//        String code = device.getCode();
//        // 查停车场的车
//        Long parkingId = device.getBusinessId();
//        String carNumber = carInfo.getCarNumber();
//        PkCarLog pkCarLog = pkCarLogDao.selectByParkingIdAndCarNumber(parkingId, carNumber);
//        Long pkCarLogId = pkCarLog.getId();
//        // 结算停车费用
//
//
//        OrOrder order = orOrderDao.findByBusinessId(pkCarLogId);
//        // 判断订单是否已支付
//        Integer payStatus = order.getPayStatus();
//        if (payStatus == 1) {
//            // 已支付 、 产生的费用为0 : 开闸、播放语音: 一路顺风
//            parkingMqtt.showDisplay(code, "粤C12345 一路顺风");
//            parkingMqtt.openTheGate(code);
//
//        } else {
//            // 未支付: 播放语音: 粤C12345 停车1小时30分,请支付10元
//            parkingMqtt.showDisplay(code, "粤C12345 停车1小时30分,请支付10元");
//        }
//    }


}
