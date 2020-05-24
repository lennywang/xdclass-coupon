package com.xdclass.couponapp;

import com.xdclass.couponapp.service.CouponService;
import com.xdclass.couponserviceapi.dto.UserCouponDto;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class CouponAppApplicationTests {

	@Resource
	private CouponService couponService;

	@Test
	void contextLoads() {
	}

	@Test
	public void testSaveUserCoupon(){
		UserCouponDto dto = new UserCouponDto();
		dto.setUserId(1234);
		dto.setCouponId(1);
		dto.setOrderId(10086);
		System.err.println(couponService.saveUserCoupon(dto));
	}
}
