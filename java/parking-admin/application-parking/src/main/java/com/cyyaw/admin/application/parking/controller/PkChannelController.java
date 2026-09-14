package com.cyyaw.admin.application.parking.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.parking.service.PkChannelService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.parking.PkChannelQueryDTO;
import com.cyyaw.admin.entity.module.parking.PkChannel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "停车场通道")
@RestController
@RequestMapping("/admin/parking/channel")
public class PkChannelController {

    @Autowired
    private PkChannelService pkChannelService;

    @Operation(summary = "查询通道", description = "根据ID查询通道")
    @GetMapping("/find/{id}")
    public BaseResult<PkChannel> findById(@PathVariable Long id) {
        return BaseResult.ok(pkChannelService.findById(id));
    }

    @Operation(summary = "通道列表", description = "分页查询通道列表；以 PkChannelQueryDTO 接收查询参数")
    @GetMapping("/list")
    public BaseResult<List<PkChannel>> list(PkChannelQueryDTO query) {
        Integer page = query.getPage();
        Integer size = query.getSize();
        String type = query.getType();
        Integer status = query.getStatus();
        Long parkingId = query.getParkingId();
        String keyword = query.getKeyword();
        QueryWrapper<PkChannel> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(type)) {
            wrapper.eq("type", type);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        if (parkingId != null) {
            wrapper.eq("parking_id", parkingId);
        }
        if (StringUtils.hasText(keyword)) {
            // 关键字匹配通道名称或通道编号
            wrapper.and(w -> w.like("name", keyword).or().like("code", keyword));
        }
        wrapper.orderByDesc("create_time");
        Page<PkChannel> pageResult = pkChannelService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "保存通道", description = "新增或更新通道")
    @PostMapping("/save")
    public BaseResult<PkChannel> save(@RequestBody PkChannel channel) {
        return BaseResult.ok(pkChannelService.save(channel));
    }

    @Operation(summary = "删除通道", description = "根据ID删除通道")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        pkChannelService.delete(id);
        return BaseResult.ok();
    }

}
