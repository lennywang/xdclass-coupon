package com.xdclass.couponapp;

import com.xdclass.couponapp.service.CouponService;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 *
 **/

public class JMHSpringBootTest {

    private ConfigurableApplicationContext context;
    private CouponService couponService;

    public static void main(String[] args) {

    }


    public void init() {
        String arg = "";
        context = SpringApplication.run(CouponAppApplication.class);
        couponService=context.getBean(CouponService.class);
    }

    public void test(){
        System.out.println(couponService.getCouponList());
    }

    public void testDB(){
        System.out.println(couponService.loadCoupon(1));
    }

    public void testMap(){
//        System.out.println(couponService.get);
    }

}
