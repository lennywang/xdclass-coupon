package com.xdclass.couponapp.controller;

import com.xdclass.couponapp.domain.TCoupon;
import com.xdclass.couponapp.service.CouponService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 *
 **/
@RequestMapping("/api/v1/coupon/")
@RestController
public class CouponController {

    @Resource
    private CouponService couponService;

    @RequestMapping("test")
    public String test() {
        return "test1";
    }

    @RequestMapping("getUserById")
    public String getUserById(Integer id) {
        return couponService.getUserById(id);
    }

    @RequestMapping("getCouponListByIds")
    public List<TCoupon> getCouponListByIds(String ids)
    {
        return couponService.getCouponListByIds(ids);
    }

}
