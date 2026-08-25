package com.cyyaw.admin.dao.parking;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.parking.PkSpace;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDate;
import java.util.Map;

public interface PkSpaceDao extends BaseMapperPlus<PkSpaceDao, PkSpace> {

    /**
     * 车位状态统计：{ free(空闲), fixed(固定占用), temp(临时占用), fault(故障) }
     */
    @Select("SELECT " +
            "(SELECT COUNT(*) FROM pk_space WHERE parking_id = #{parkingId} AND status = 0) AS free, " +
            "(SELECT COUNT(*) FROM pk_space WHERE parking_id = #{parkingId} AND status = 1 AND space_type = 1) AS fixed, " +
            "(SELECT COUNT(*) FROM pk_space WHERE parking_id = #{parkingId} AND status = 1 AND space_type = 2) AS temp, " +
            "(SELECT COUNT(*) FROM pk_space WHERE parking_id = #{parkingId} AND status = 2) AS fault")
    Map<String, Object> selectStats(@Param("parkingId") Long parkingId);

    /**
     * 分配车位：绑定会员/车牌/有效期，状态置为占用
     */
    @Update("UPDATE pk_space SET member_id = #{memberId}, member_name = #{memberName}, plate = #{plate}, expire_date = #{expireDate}, status = 1 WHERE id = #{id}")
    int assign(@Param("id") Long id, @Param("memberId") Long memberId, @Param("memberName") String memberName, @Param("plate") String plate, @Param("expireDate") LocalDate expireDate);

    /**
     * 解绑车位：清除绑定，状态置为空闲
     */
    @Update("UPDATE pk_space SET member_id = NULL, member_name = NULL, plate = NULL, expire_date = NULL, status = 0 WHERE id = #{id}")
    int unbind(@Param("id") Long id);

    /**
     * 车位报修：状态置为故障
     */
    @Update("UPDATE pk_space SET status = 2 WHERE id = #{id}")
    int repair(@Param("id") Long id);

}
