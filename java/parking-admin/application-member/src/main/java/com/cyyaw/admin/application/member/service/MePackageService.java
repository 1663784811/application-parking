package com.cyyaw.admin.application.member.service;

import com.cyyaw.admin.entity.module.member.MePackage;

import java.util.List;

public interface MePackageService {

    MePackage findById(Long id);

    MePackage save(MePackage mePackage);

    void delete(Long id);

    List<MePackage> findAll();

}
