package com.cyyaw.admin.application.parking.controller.internal;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.parking.service.PkCarLogService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.InternalParkingQueryDTO;
import com.cyyaw.admin.entity.module.parking.PkCarLog;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 停车场内部接口（供门口端 parking-gate 等内部服务调用）。
 * <p>
 * 门口端不持有停车记录数据，通行记录统一由 admin 落库与查询。本接口把
 * 最近通行记录开放给内部服务，复用 {@link PkCarLogService#findPage} 的分页查询。
 * <p>
 * 路径 /internal/** 在 SecurityConfig 中放行（仅供内部服务调用，
 * 生产环境建议绑定 loopback 或加共享密钥）。
 */
@RestController
@RequestMapping("/internal/parking/carLog")
@RequiredArgsConstructor
public class InternalParkingController {

    private final PkCarLogService pkCarLogService;

    @GetMapping("/list")
    public BaseResult<List<PkCarLog>> list(InternalParkingQueryDTO query) {
        Integer page = query.getPage();
        Integer size = query.getSize();
        Long parkingId = query.getParkingId();
        QueryWrapper<PkCarLog> wrapper = new QueryWrapper<>();
        if (parkingId != null) {
            wrapper.eq("parking_id", parkingId);
        }
        wrapper.orderByDesc("entry_time");
        Page<PkCarLog> pageResult = pkCarLogService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

}
