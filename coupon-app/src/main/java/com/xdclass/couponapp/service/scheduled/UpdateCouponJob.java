package com.xdclass.couponapp.service.scheduled;

import com.xdclass.couponapp.service.CouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 *
 **/
@Service
public class UpdateCouponJob {
    @Resource
    private CouponService couponService;

    private static final Logger logger = LoggerFactory.getLogger(UpdateCouponJob.class);

    @Scheduled(cron = "0/5 * * * * ?")//秒分时日月年
    public void updateCoupon() {
        System.out.println("enter update coupon job");
        couponService.updateCouponMap();
    }
}
