package com.cyyaw.admin.application.user.service;


import com.cyyaw.admin.entity.dto.user.login.LoginRest;
import com.cyyaw.admin.entity.dto.user.login.StoreLoginRequest;

public interface StoreLoginService {


    LoginRest login(StoreLoginRequest storeLoginRequest);


    LoginRest createTokenByStoreAdminId(Long id);

}
