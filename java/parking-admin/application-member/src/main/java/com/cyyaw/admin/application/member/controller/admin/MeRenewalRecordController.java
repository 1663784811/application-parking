package com.cyyaw.admin.application.member.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.member.service.MeRenewalRecordService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.member.MeRenewalRecord;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "会员续费记录")
@RestController
@RequestMapping("/admin/member/renewal")
public class MeRenewalRecordController {

    @Autowired
    private MeRenewalRecordService meRenewalRecordService;

    @Operation(summary = "查询续费记录", description = "根据ID查询续费记录")
    @GetMapping("/find/{id}")
    public BaseResult<MeRenewalRecord> findById(@PathVariable Long id) {
        return BaseResult.ok(meRenewalRecordService.findById(id));
    }

    @Operation(summary = "续费记录列表", description = "分页查询续费记录")
    @GetMapping("/list")
    public BaseResult<List<MeRenewalRecord>> list(@RequestParam(defaultValue = "1") Integer page,
                                                  @RequestParam(defaultValue = "10") Integer size,
                                                  @RequestParam(required = false) String plate,
                                                  @RequestParam(required = false) Integer cardType,
                                                  @RequestParam(required = false) String startTime,
                                                  @RequestParam(required = false) String endTime) {
        QueryWrapper<MeRenewalRecord> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(plate)) {
            wrapper.like("plate", plate);
        }
        if (cardType != null) {
            wrapper.eq("card_type", cardType);
        }
        if (StringUtils.hasText(startTime)) {
            wrapper.ge("renewal_time", startTime + " 00:00:00");
        }
        if (StringUtils.hasText(endTime)) {
            wrapper.le("renewal_time", endTime + " 23:59:59");
        }
        wrapper.orderByDesc("renewal_time");
        Page<MeRenewalRecord> pageResult = meRenewalRecordService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "会员续费", description = "为会员续费并生成续费记录，顺延会员到期时间")
    @PostMapping
    public BaseResult<MeRenewalRecord> renewal(@RequestBody Map<String, Object> body) {
        Object memberIdVal = body.get("memberId");
        if (memberIdVal == null) {
            throw new RuntimeException("缺少会员ID");
        }
        Long memberId = Long.valueOf(memberIdVal.toString());
        return BaseResult.ok(meRenewalRecordService.renewal(memberId));
    }

}
