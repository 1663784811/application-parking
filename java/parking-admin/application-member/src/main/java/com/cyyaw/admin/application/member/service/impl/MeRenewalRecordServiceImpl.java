package com.cyyaw.admin.application.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyyaw.admin.application.member.service.MeRenewalRecordService;
import com.cyyaw.admin.dao.member.MeMemberDao;
import com.cyyaw.admin.dao.member.MePackageDao;
import com.cyyaw.admin.dao.member.MeRenewalRecordDao;
import com.cyyaw.admin.entity.module.member.MeMember;
import com.cyyaw.admin.entity.module.member.MePackage;
import com.cyyaw.admin.entity.module.member.MeRenewalRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MeRenewalRecordServiceImpl implements MeRenewalRecordService {

    @Autowired
    private MeRenewalRecordDao meRenewalRecordDao;

    @Autowired
    private MeMemberDao meMemberDao;

    @Autowired
    private MePackageDao mePackageDao;

    @Override
    public MeRenewalRecord findById(Long id) {
        return meRenewalRecordDao.selectById(id);
    }

    @Override
    public Page<MeRenewalRecord> findPage(Integer page, Integer size, QueryWrapper<MeRenewalRecord> wrapper) {
        return meRenewalRecordDao.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public MeRenewalRecord renewal(Long memberId) {
        MeMember member = meMemberDao.selectById(memberId);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        Integer cardType = member.getCardType();
        // 查找该卡类型对应的套餐，复用套餐价格与有效期天数
        List<MePackage> pkgs = mePackageDao.selectList(new QueryWrapper<MePackage>().eq("type", cardType));
        if (pkgs == null || pkgs.isEmpty()) {
            throw new RuntimeException("未找到该卡类型对应的套餐配置");
        }
        MePackage pkg = pkgs.get(0);
        BigDecimal amount = pkg.getPrice() == null ? BigDecimal.ZERO : pkg.getPrice();
        Integer validDays = pkg.getValidDays() == null ? 0 : pkg.getValidDays();

        LocalDateTime now = LocalDateTime.now();
        // 若仍在有效期内则从原到期时间顺延，否则从当前时间起算
        LocalDateTime base = (member.getExpireTime() != null && member.getExpireTime().isAfter(now))
                ? member.getExpireTime() : now;
        member.setExpireTime(base.plusDays(validDays));
        BigDecimal total = member.getTotalAmount() == null ? BigDecimal.ZERO : member.getTotalAmount();
        member.setTotalAmount(total.add(amount));
        member.setFrozen(0);
        meMemberDao.save(member);

        MeRenewalRecord record = new MeRenewalRecord();
        record.setOrderNo("R" + System.currentTimeMillis());
        record.setMemberId(memberId);
        record.setPlate(member.getPlate());
        record.setName(member.getName());
        record.setCardType(cardType);
        record.setAmount(amount);
        record.setOperator("管理员");
        record.setRenewalTime(now);
        return meRenewalRecordDao.save(record);
    }

}
