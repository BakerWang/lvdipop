package com.enation.app.shop.component.bonus.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enation.app.shop.component.bonus.model.MemberBonus;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.CurrencyUtil;
import com.enation.framework.util.StringUtil;

public final class BonusSession {
	private static final String list_sessionkey ="bonus_list_session_key";
	private static final String one_sessionkey ="bonus_one_session_key";
	public static final String B2B2C_SESSIONKEY ="b2b2c_session_key";
	
	public static final String B2B2C_COUPONS_SESSIONKEY ="b2b2c_coupons_session_key";
	
	//不可实例化
	private BonusSession(){
		
	}

	/**
	 * 使用红包
	 * @param bonus
	 */
	public static void use(MemberBonus bonus){
		
		//如果存在跳过
		if(isExists(bonus)){
			return;
		}
		
		//添加到session
		List<MemberBonus> bounsList= (List)ThreadContextHolder.getSession().getAttribute(list_sessionkey);
		if(bounsList==null){
			bounsList= new ArrayList<MemberBonus>();
		}
		
		bounsList.add(bonus);
		
		ThreadContextHolder.getSession().setAttribute(list_sessionkey, bounsList);
		 
	}

	/**
	 * 版本：v1.0
	 * 描述：多店版使用红包 
	 * Author：liushuai
	 * date：2015年9月23日15:39:52 
	 */
	public static void use(int sotreid,MemberBonus bonus){
		 
		//添加到session
		Map<Integer,MemberBonus> bonusMap = (Map)ThreadContextHolder.getSession().getAttribute(B2B2C_SESSIONKEY);
		if(bonusMap==null){
			bonusMap= new HashMap<Integer, MemberBonus>();
		}
		bonusMap.put(sotreid,bonus); 
		ThreadContextHolder.getSession().setAttribute(B2B2C_SESSIONKEY, bonusMap);
	}
	


	/**
	 * 取消某个店铺的优惠券
	 * @param store_id
	 */
	public static void cancelB2b2cBonus(int store_id){

		Map<Integer,MemberBonus> bonusMap = (Map)ThreadContextHolder.getSession().getAttribute(B2B2C_SESSIONKEY);
		
		//如果优惠券map集合不为空 add_by DMRain 2016-7-14
		if (bonusMap!=null&&!bonusMap.isEmpty()) {
			bonusMap.remove(store_id);
			ThreadContextHolder.getSession().setAttribute(B2B2C_SESSIONKEY, bonusMap); 
		}
	}
	/**
	 * 取消某个店铺的优惠券
	 * @param store_id
	 */
	public static MemberBonus getB2b2cBonus(int store_id){

		Map<Integer,MemberBonus> bonusMap = (Map)ThreadContextHolder.getSession().getAttribute(B2B2C_SESSIONKEY);
		
		//如果不为空，则返回对应店铺的优惠券
		if (bonusMap != null && !bonusMap.isEmpty()) {
			if(bonusMap.containsKey(store_id)) {
				return bonusMap.get(store_id); 
			}
		}
		return null;
		
	}
	
	/**
	 * 获取店铺的优惠券(新版方法)
	 * add_by DMRain 2016-10-10
	 * @param store_id 店铺id
	 */
	public static MemberCoupons getB2b2cCoupons(int store_id){

		Map<Integer,MemberCoupons> couponsMap = (Map)ThreadContextHolder.getSession().getAttribute(B2B2C_COUPONS_SESSIONKEY);
		
		//如果不为空，则返回对应店铺的优惠券
		if (couponsMap != null && !couponsMap.isEmpty()) {
			if(couponsMap.containsKey(store_id)) {
				return couponsMap.get(store_id); 
			}
		}
		return null;
		
	}
	
	
	/**
	 * 同步红包集合
	 */
	public static void syncBounsList(){

		Map<Integer,MemberBonus> bonusMap = (Map)ThreadContextHolder.getSession().getAttribute(B2B2C_SESSIONKEY);
		List<MemberBonus> bounsList = new ArrayList<MemberBonus>();
		for (MemberBonus memberBonus : bonusMap.values()) { 
			bounsList.add(memberBonus);
		} 
		ThreadContextHolder.getSession().setAttribute(list_sessionkey, bounsList); 
	}
	
	/**
	 * 用编号取消一个优惠卷的使用
	 * @param sn
	 */
	public static void cancel(String sn){
		
		if(StringUtil.isEmpty(sn)){
			
			return;
		}
		
		List<MemberBonus> bounsList= (List)ThreadContextHolder.getSession().getAttribute(list_sessionkey);
		if(bounsList==null){
			return;
		}
		
		List<MemberBonus> newBounsList = new ArrayList<MemberBonus>();
		for (MemberBonus memberBonus : bounsList) {
			if(sn.equals( memberBonus.getBonus_sn() )){
				continue;
			}
			newBounsList.add(memberBonus);
		}
		
		ThreadContextHolder.getSession().setAttribute(list_sessionkey,newBounsList);
		
	}
	
	
	
	/**
	 * 使用红包
	 * @param bonus
	 */
	public static void useOne(MemberBonus bonus){
		
		ThreadContextHolder.getSession().setAttribute(one_sessionkey, bonus);
		 
	}
	
	
	
	
	/**
	 * 获取已使用的红包
	 * @return
	 */
	public static List<MemberBonus> get(){
		
		return  (List)ThreadContextHolder.getSession().getAttribute(list_sessionkey);
	}
	
	/**
	 * 获取只能使用一个的红包
	 * @return
	 */
	public static MemberBonus getOne(){
		
		return  (MemberBonus)ThreadContextHolder.getSession().getAttribute(one_sessionkey);
	}
	
//	RIPERIPERIPE

	/**
	 * 获取已使用红包的金额
	 * @return
	 */
	public static double getUseMoney(){
		List<MemberBonus> bonusList =get();
		double moneyCount = 0;

		if(bonusList!=null){
						
			for (MemberBonus memberBonus : bonusList) {
				double bonusMoney = memberBonus.getBonus_money(); //红包金额
				moneyCount= CurrencyUtil.add(moneyCount,bonusMoney);//累加所有的红包金额
				
			}
			

		}
		MemberBonus memberBonus = getOne();
		if(memberBonus!=null){
			double bonusMoney = memberBonus.getBonus_money(); //红包金额
			moneyCount= CurrencyUtil.add(moneyCount,bonusMoney);//累加所有的红包金额
		}
		return  moneyCount;
	}
	
	public static void clean(){
		ThreadContextHolder.getSession().removeAttribute(one_sessionkey);
	}
	public static void cleanAll(){
		ThreadContextHolder.getSession().removeAttribute(one_sessionkey);
		ThreadContextHolder.getSession().removeAttribute(list_sessionkey);
	}
	public static boolean isExists(MemberBonus bonus){
		 List<MemberBonus> bounsList= (List)ThreadContextHolder.getSession().getAttribute(list_sessionkey);
		 if(bounsList==null) return false;
		 for (MemberBonus memberBonus : bounsList) {
			if(memberBonus.getBonus_id() == bonus.getBonus_id()){
				return true;
			}
		}
		return false;
	}
	
}
