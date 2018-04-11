package com.enation.app.shop.mobile.service;

import java.util.Map;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.framework.database.Page;

/**
 * 
 * @Description: mobile优惠券管理API
 * @author: liuyulei
 * @date: 2016年10月24日 下午12:02:39 
 * @since:v61
 */
public interface IApiCouponsManager {

	
	/**
	 * 
	 * @Description: 店铺优惠券列表
	 * @param pageNo
	 * @param pageSize
	 * @param result  参数列表        {store_id：店铺id,mobile:手机号码}
	 * @return
	 * @return: Page
	 * @author： liuyulei
	 * @date：2016年10月24日 下午12:03:27
	 */
	@SuppressWarnings("rawtypes")
	public Page getStoreCouponsList(int pageNo, int pageSize, Map result);
	
	/**
	 * 
	 * @Description: 修改会员优惠券状态   失效    
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年10月24日 下午12:04:14
	 */
	public void  updateStatus();
	
	/**
	 * 
	 * @Description: 会员优惠券列表
	 * @param pageNo
	 * @param pageSize
	 * @param params    参数列表  {mobile：有机号码 ， status：状态}
	 * @return
	 * @return: Page
	 * @author： liuyulei
	 * @date：2016年10月24日 下午12:05:03
	 */
	@SuppressWarnings("rawtypes")
	public Page getMemberCouponsList(int pageNo, int pageSize, Map params) ;
	
	/**
	 * 
	 * @Description: 获取优惠券详情
	 * @param coupons_id
	 * @return
	 * @return: Map
	 * @author： liuyulei
	 * @date：2016年10月24日 下午12:05:42
	 */
	@SuppressWarnings("rawtypes")
	public Map get(Integer coupons_id);
	
	/**
	 * 
	 * @Description: 领取优惠券
	 * @param coupons_id  优惠券id
	 * @param store_id    店铺id
	 * @param member_mobile  会员手机号码
	 * @param get_type        领取类型
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年10月24日 下午12:06:33
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void receive(Integer coupons_id, Integer store_id, String member_mobile, Integer get_type,Integer member_id,String member_name) ;
	
	
	
}
