package com.xdclass.couponapp.service;

import com.alibaba.druid.support.json.JSONUtils;
//import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.xdclass.couponapp.constant.Constant;
import com.xdclass.couponapp.domain.TCoupon;
import com.xdclass.couponapp.domain.TCouponExample;
import com.xdclass.couponapp.domain.TUserCoupon;
import com.xdclass.couponapp.mapper.TCouponMapper;
import com.xdclass.couponapp.mapper.TUserCouponMapper;
import com.xdclass.couponapp.util.SnowflakeIdWorker;
import com.xdclass.couponserviceapi.dto.CouponDto;
import com.xdclass.couponserviceapi.dto.UserCouponDto;
import com.xdclass.couponserviceapi.dto.UserCouponInfoDto;
import com.xdclass.couponserviceapi.service.ICouponService;
import com.xdclass.userapi.service.IUserService;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
//import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 *
 **/
//@Service
@Service
public class CouponService implements ICouponService {

    @Resource
    private TCouponMapper tCouponMapper;

    @Reference
    private IUserService iUserService;

    @Resource
    private TUserCouponMapper tUserCouponMapper;

    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    LoadingCache<Integer, List<TCoupon>> couponCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
            .refreshAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<Integer, List<TCoupon>>() {
                @Override
                public List<TCoupon> load(Integer integer) throws Exception {
                    return loadCoupon(integer);
                }
            });

    com.github.benmanes.caffeine.cache.LoadingCache<Integer, List<TCoupon>> couponCaffeine = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .refreshAfterWrite(5, TimeUnit.MINUTES)
            .build(new com.github.benmanes.caffeine.cache.CacheLoader<Integer, List<TCoupon>>() {
                @Override
                public List<TCoupon> load(Integer integer) throws Exception {
                    return loadCoupon(integer);
                }
            });

    public List<TCoupon> loadCoupon(Integer o) {
        TCouponExample example = new TCouponExample();
        example.createCriteria().andStatusEqualTo(Constant.USERFUL).andStartTimeLessThan(new Date()).andEndTimeGreaterThan(new Date());
        return tCouponMapper.selectByExample(example);
    }

    LoadingCache<Integer, TCoupon> couponIdsCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES).refreshAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<Integer, TCoupon>() {
                @Override
                public TCoupon load(Integer integer) throws Exception {
                    return loadIdCoupon(integer);
                }
            });

    private TCoupon loadIdCoupon(Integer integer) {
        return tCouponMapper.selectByPrimaryKey(integer);
    }

    public String getUserById(Integer id) {
        return iUserService.getUserById(id).toString();
    }

    public List<TCoupon> getCouponListByIds(String ids) {
        String[] idArr = ids.split(",");
        List<Integer> loadFromDB = Lists.newArrayList();
        List<TCoupon> tCoupons = Lists.newArrayList();
        List<String> idList = Lists.newArrayList(idArr);

        for (String id : idList) {
            TCoupon tCoupon = couponIdsCache.getIfPresent(id);
            if (tCoupon == null) {
                loadFromDB.add(Integer.parseInt(id));
            } else {
                tCoupons.add(tCoupon);
            }
        }
        List<TCoupon> tCouponsList = couponByIds(loadFromDB);
        Map<Integer, TCoupon> tCouponMap = tCouponsList.stream().collect(Collectors.toMap(TCoupon::getId, TCoupon -> TCoupon));
        tCoupons.addAll(tCouponsList);
        couponIdsCache.putAll(tCouponMap);
        return tCoupons;
    }

    private List<TCoupon> couponByIds(List<Integer> ids) {
        TCouponExample tCouponExample = new TCouponExample();
        tCouponExample.createCriteria().andIdIn(ids);
        return tCouponMapper.selectByExample(tCouponExample);
    }

//    public List<TCoupon> getCouponList() {
//        List<TCoupon> tCoupons = Lists.newArrayList();
//        try {
//            couponCache.get(1);
//        } catch (ExecutionException e) {
//            e.printStackTrace();
//        }
//        return tCoupons;
//    }

    @Override
    public List<CouponDto> getCouponList() {
        return null;
    }

    @Override
    public String saveUserCoupon(UserCouponDto dto) {
        String result = check(dto);

        if (result != null) {
            return result;
        }

        //TCoupon coupon = tCouponMapper.selectByPrimaryKey(dto.getCouponId());
        TCoupon coupon = null;
        try {
            coupon = tCouponMapper.finudCouponById(dto.getCouponId());
        } catch (Exception ex) {
            logger.error("查询优惠券发生异常", ex);
        }
        if (coupon == null) {
            return "coupon无效";
        }

        return save2DB(dto, coupon);
    }

    private String check(UserCouponDto dto) {
        Integer couponId = dto.getCouponId();
        Integer userId = dto.getUserId();
        if (couponId == null || userId == null) {
            return "couponId或者userId为空";
        }
        return null;
    }


    private String save2DB(UserCouponDto dto, TCoupon coupon) {
        TUserCoupon userCoupon = new TUserCoupon();
        BeanUtils.copyProperties(dto, userCoupon);
        userCoupon.setPicUrl(coupon.getPicUrl());
        userCoupon.setCreateTime(new Date());
        SnowflakeIdWorker worker = new SnowflakeIdWorker(0, 0);
        userCoupon.setUserCouponCode(worker.nextId() + "");
        tUserCouponMapper.insertSelective(userCoupon);
        logger.info("save coupon success:{}", JSON.toJSONString(dto));
        return "领取成功";
    }

    @Override
    public List<UserCouponInfoDto> userCouponList(Integer userId) {
        return null;
    }

    private Map couponMap = new ConcurrentHashMap();

    public void updateCouponMap() {
        Map couponMapTmp = new ConcurrentHashMap();
        List<TCoupon> tCoupons = Lists.newArrayList();
        try {
            tCoupons = this.loadCoupon(1);
            couponMapTmp.put(1, tCoupons);
            couponMap = couponMapTmp;
            logger.info("update coupon list :{},coupon list size:{}", JSONUtils.toJSONString(tCoupons), tCoupons.size());
        } catch (Exception e) {
            logger.error("update coupon list :{},coupon list size:{}", JSONUtils.toJSONString(tCoupons), tCoupons.size(), e);
        }
    }


}
