package com.cyyaw.admin.dao.iot;

import com.cyyaw.admin.dao.BaseMapperPlus;
import com.cyyaw.admin.entity.module.iot.IotDevice;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface IotDeviceDao extends BaseMapperPlus<IotDeviceDao, IotDevice> {


    @Select("select * from iot_device where code = #{code}")
    IotDevice findByCode(@Param("code") String code);


}
