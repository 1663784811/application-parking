package com.cyyaw.admin.dao.parking;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PkCarLogDao extends BaseMapperPlus<PkCarLogDao, PkCarLog> {

    @Select("select count(*) from pk_car_log where parking_id = #{parkingId} and `status` = #{status}")
    int selectStatusCount(Long parkingId, int status);

    @Select("select * from pk_car_log where parking_id = #{parkingId} and car_number = #{carNumber} and `status` = 0 limit 1")
    PkCarLog selectByParkingIdAndCarNumber(Long parkingId, String carNumber);

    @Select("select count(*) from pk_car_log where entry_time >= CURDATE()")
    int selectTodayCount();

    @Select("select * from pk_car_log where parking_id = #{parkingId} and car_number = #{carNumber} and `status` = 0 ")
    List<PkCarLog>  selectUnfinishedLog(Long parkingId, String carNumber);

    /**
     * 查询某出场通道当前正在等待缴费出场的车辆（最近的 1 条）。
     * 出场摄像头识别后写入 out_channel_id / out_device_code，缴费放行前 status 仍为 0。
     */
    @Select("select * from pk_car_log where out_channel_id = #{channelId} and `status` = 0 " +
            "order by out_recognize_time desc limit 1")
    PkCarLog selectWaitingExitByChannelId(Long channelId);

}