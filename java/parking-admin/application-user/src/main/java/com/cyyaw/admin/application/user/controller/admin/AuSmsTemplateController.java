package com.cyyaw.admin.application.user.controller.admin;

import com.cyyaw.admin.application.user.service.AuSmsTemplateService;
import com.cyyaw.admin.common.BaseResult;
import com.cyyaw.admin.entity.module.user.AuSmsTemplate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "短信模板")
@RestController
@RequestMapping("/system/sms/template")
public class AuSmsTemplateController {

    @Autowired
    private AuSmsTemplateService auSmsTemplateService;

    @Operation(summary = "查询短信模板", description = "根据ID查询短信模板")
    @GetMapping("/find/{id}")
    public BaseResult<AuSmsTemplate> findById(@PathVariable Long id) {
        return BaseResult.ok(auSmsTemplateService.findById(id));
    }

    @Operation(summary = "短信模板列表", description = "查询全部短信模板（按创建时间倒序）")
    @GetMapping("/list")
    public BaseResult<List<AuSmsTemplate>> list() {
        return BaseResult.ok(auSmsTemplateService.findAll());
    }

    @Operation(summary = "保存短信模板", description = "新增或更新短信模板")
    @PostMapping("/save")
    public BaseResult<AuSmsTemplate> save(@RequestBody AuSmsTemplate smsTemplate) {
        return BaseResult.ok(auSmsTemplateService.save(smsTemplate));
    }

    @Operation(summary = "删除短信模板", description = "根据ID删除短信模板")
    @DeleteMapping("/delete/{id}")
    public BaseResult<Void> delete(@PathVariable Long id) {
        auSmsTemplateService.delete(id);
        return BaseResult.ok();
    }
}
