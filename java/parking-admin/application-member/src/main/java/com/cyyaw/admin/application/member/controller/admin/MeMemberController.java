package com.cyyaw.admin.application.member.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.member.service.MeMemberService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.dto.member.MeMemberQueryDTO;
import com.cyyaw.admin.entity.module.member.MeMember;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Tag(name = "会员管理")
@RestController
@RequestMapping("/admin/member")
public class MeMemberController {

    @Autowired
    private MeMemberService meMemberService;

    @Operation(summary = "查询会员", description = "根据ID查询会员")
    @GetMapping("/find/{id}")
    public BaseResult<MeMember> findById(@PathVariable Long id) {
        return BaseResult.ok(meMemberService.findById(id));
    }

    @Operation(summary = "会员列表", description = "分页查询会员；以 MeMemberQueryDTO 接收查询参数")
    @GetMapping("/list")
    public BaseResult<List<MeMember>> list(MeMemberQueryDTO query) {
        Integer page = query.getPage();
        Integer size = query.getSize();
        String plate = query.getPlate();
        String phone = query.getPhone();
        Integer status = query.getStatus();
        QueryWrapper<MeMember> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(plate)) {
            wrapper.like("plate", plate);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like("phone", phone);
        }
        if (status != null) {
            // 前端 status 为派生值：1正常 2即将到期 3已过期 4已冻结
            LocalDateTime now = LocalDateTime.now();
            switch (status) {
                case 4:
                    wrapper.eq("frozen", 1);
                    break;
                case 3:
                    wrapper.eq("frozen", 0).lt("expire_time", now);
                    break;
                case 2:
                    wrapper.eq("frozen", 0).ge("expire_time", now).le("expire_time", now.plusDays(7));
                    break;
                case 1:
                    wrapper.eq("frozen", 0).gt("expire_time", now.plusDays(7));
                    break;
                default:
                    break;
            }
        }
        wrapper.orderByDesc("create_time");
        Page<MeMember> pageResult = meMemberService.findPage(page, size, wrapper);
        BaseResult.Result result = new BaseResult.Result(page, size, pageResult.getTotal());
        return BaseResult.ok(pageResult.getRecords(), result);
    }

    @Operation(summary = "保存会员", description = "新增或更新会员")
    @PostMapping("/save")
    public BaseResult<MeMember> save(@RequestBody MeMember member) {
        return BaseResult.ok(meMemberService.save(member));
    }

    @Operation(summary = "删除会员", description = "根据ID删除会员")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        meMemberService.delete(id);
        return BaseResult.ok();
    }

    @Operation(summary = "冻结会员", description = "根据ID冻结会员")
    @PostMapping("/freeze/{id}")
    public BaseResult<MeMember> freeze(@PathVariable Long id) {
        return BaseResult.ok(meMemberService.freeze(id, true));
    }

    @Operation(summary = "解冻会员", description = "根据ID解冻会员")
    @PostMapping("/unfreeze/{id}")
    public BaseResult<MeMember> unfreeze(@PathVariable Long id) {
        return BaseResult.ok(meMemberService.freeze(id, false));
    }

    @Operation(summary = "到期提醒", description = "根据ID发送到期提醒")
    @PostMapping("/notice/expire/{id}")
    public BaseResult<Void> sendExpireNotice(@PathVariable Long id) {
        meMemberService.sendExpireNotice(id);
        return BaseResult.ok();
    }

    @Operation(summary = "会员统计", description = "会员数量统计")
    @GetMapping("/stats")
    public BaseResult<Map<String, Object>> stats() {
        return BaseResult.ok(meMemberService.stats());
    }

}
