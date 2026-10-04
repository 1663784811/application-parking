package com.cyyaw.admin.dao.parking;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

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
     * 按车牌聚合「已出场」的停车次数，供「我的车辆」展示每辆车的停车次数。
     * <p>
     * plates 为空（车主还没登记任何车辆）时不加车牌条件，退化成按企业维度统计 ——
     * 单账号企业里这些记录本来就都属于这个车主，别让头部统计一片 0。
     * 注意 status 是 MySQL 保留字，必须加反引号。
     */
    @Select("<script>" +
            "select car_number as carNumber, count(*) as `timesCount` from pk_car_log " +
            "where del_time = 0 and `status` = 1 and en_id = #{enId} " +
            "<if test='plates != null and plates.size() > 0'>" +
            " and car_number in <foreach collection='plates' item='p' open='(' separator=',' close=')'>#{p}</foreach>" +
            "</if> group by car_number</script>")
    List<Map<String, Object>> selectPlateTimes(Long enId, List<String> plates);

    /**
     * 「已出场」记录的累计停车时长（秒），供「我的」页面头部展示累计小时数。
     * 口径与 {@link #selectPlateTimes} 完全一致，只是这里聚合成一个数。
     */
    @Select("<script>" +
            "select coalesce(sum(TIMESTAMPDIFF(SECOND, entry_time, out_time)), 0) as seconds " +
            "from pk_car_log " +
            "where del_time = 0 and `status` = 1 and en_id = #{enId} " +
            "and entry_time is not null and out_time is not null " +
            "<if test='plates != null and plates.size() > 0'>" +
            " and car_number in <foreach collection='plates' item='p' open='(' separator=',' close=')'>#{p}</foreach>" +
            "</if></script>")
    Map<String, Object> selectTotalOutSeconds(Long enId, List<String> plates);

    /**
     * 查询某出场通道当前正在等待缴费出场的车辆（最近的 1 条）。
     * 出场摄像头识别后写入 out_channel_id / out_device_code，缴费放行前 status 仍为 0。
     */
    @Select("select * from pk_car_log where out_channel_id = #{channelId} and `status` = 0 " +
            "order by out_recognize_time desc limit 1")
    PkCarLog selectWaitingExitByChannelId(Long channelId);

}