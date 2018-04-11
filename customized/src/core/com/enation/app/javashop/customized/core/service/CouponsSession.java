package com.enation.app.javashop.customized.core.service;

import java.util.HashMap;
import java.util.Map;

import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.framework.context.webcontext.ThreadContextHolder;

/**
 * 优惠券session操作类
 * @author DMRain
 * @date 2016-10-10
 * @since v61
 * @version 1.0
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class CouponsSession {

	public static final String B2B2C_COUPONS_SESSIONKEY ="b2b2c_coupons_session_key";
	
	//不可实例化
	private CouponsSession(){
		
	}
	
	/**
	 * 多店结算页面使用红包
	 * @param store_id 店铺id
	 * @param coupons 会员优惠券信息
	 */
	public static void use(int store_id,MemberCoupons coupons){
		 
		//添加到session
		Map<Integer,MemberCoupons> couponsMap = (Map)ThreadContextHolder.getSession().getAttribute(B2B2C_COUPONS_SESSIONKEY);
		
		if (couponsMap == null) {
			couponsMap = new HashMap<Integer, MemberCoupons>();
		}
		
		couponsMap.put(store_id,coupons); 
		ThreadContextHolder.getSession().setAttribute(B2B2C_COUPONS_SESSIONKEY, couponsMap);
	}
	
	/**
	 * 结算页面取消某个店铺的优惠券
	 * @param store_id 店铺id
	 */
	public static void cancelB2b2cCoupons(int store_id){

		Map<Integer,MemberCoupons> couponsMap = (Map)ThreadContextHolder.getSession().getAttribute(B2B2C_COUPONS_SESSIONKEY);
		
		//如果优惠券map集合不为空 add_by DMRain 2016-7-14
		if (couponsMap != null && !couponsMap.isEmpty()) {
			couponsMap.remove(store_id);
			ThreadContextHolder.getSession().setAttribute(B2B2C_COUPONS_SESSIONKEY, couponsMap); 
		}
	}
	
	
}
