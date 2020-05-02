package com.xdclass.couponapp.service;

import com.xdclass.userapi.service.IUserService;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.stereotype.Service;

/**
 *
 **/
@Service
public class CouponService {

    @Reference
    private IUserService iUserService;

    public String getUserById(Integer id) {
        return iUserService.getUserById(id).toString();
    }
}
