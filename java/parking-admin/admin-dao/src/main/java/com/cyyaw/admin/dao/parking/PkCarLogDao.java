package com.cyyaw.admin.dao.parking;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import org.apache.ibatis.annotations.Select;

public interface PkCarLogDao extends BaseMapperPlus<PkCarLogDao, PkCarLog> {

    @Select("select count(*) from pk_car_log where parking_id = #{parkingId} and `status` = #{status}")
    int selectStatusCount(Long parkingId, int status);

    @Select("select * from pk_car_log where parking_id = #{parkingId} and car_number = #{carNumber} and `status` = 0 limit 1")
    PkCarLog selectByParkingIdAndCarNumber(Long parkingId, String carNumber);

    @Select("select count(*) from pk_car_log where entry_time >= CURDATE()")
    int selectTodayCount();

}