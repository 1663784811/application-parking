package com.cyyaw.admin.application.member.service.impl;

import com.cyyaw.admin.application.member.service.MePackageService;
import com.cyyaw.admin.dao.member.MePackageDao;
import com.cyyaw.admin.entity.module.member.MePackage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MePackageServiceImpl implements MePackageService {

    @Autowired
    private MePackageDao mePackageDao;

    @Override
    public MePackage findById(Long id) {
        return mePackageDao.selectById(id);
    }

    @Override
    public MePackage save(MePackage mePackage) {
        return mePackageDao.save(mePackage);
    }

    @Override
    public void delete(Long id) {
        mePackageDao.deleteById(id);
    }

    @Override
    public List<MePackage> findAll() {
        return mePackageDao.selectList();
    }

}
