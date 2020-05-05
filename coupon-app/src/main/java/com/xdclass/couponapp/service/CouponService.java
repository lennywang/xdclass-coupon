package com.xdclass.couponapp.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.xdclass.couponapp.constant.Constant;
import com.xdclass.couponapp.domain.TCoupon;
import com.xdclass.couponapp.domain.TCouponExample;
import com.xdclass.couponapp.mapper.TCouponMapper;
import com.xdclass.userapi.service.IUserService;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 *
 **/
@Service
public class CouponService {

    @Resource
    private TCouponMapper tCouponMapper;

    @Reference
    private IUserService iUserService;

    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    LoadingCache<Integer, List<TCoupon>> couponCache = CacheBuilder.newBuilder().expireAfterWrite(10, TimeUnit.MINUTES)
            .refreshAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<Integer, List<TCoupon>>() {
                @Override
                public List<TCoupon> load(Integer integer) throws Exception {
                    return loadCoupon(integer);
                }
            });

    com.github.benmanes.caffeine.cache.LoadingCache<Integer,List<TCoupon>> couponCaffeine= Caffeine.newBuilder()
            .expireAfterWrite(10,TimeUnit.MINUTES)
            .refreshAfterWrite(5,TimeUnit.MINUTES)
            .build(new com.github.benmanes.caffeine.cache.CacheLoader<Integer,List<TCoupon>>(){
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

    public List<TCoupon> getCouponList() {
        List<TCoupon> tCoupons = Lists.newArrayList();
        try {
            couponCache.get(1);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return tCoupons;
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
