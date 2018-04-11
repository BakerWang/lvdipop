package com.enation.app.javashop.customized.core.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.framework.database.Page;

/**
 * 会员优惠券管理接口
 * @author DMRain
 * @date 2016-9-27
 * @since v61
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public interface IMemberCouponsManager {

	/**
	 * 添加会员优惠券
	 * @param memberCoupons 会员优惠券信息
	 */
	public void add(MemberCoupons memberCoupons);
	
	/**
	 * 检查优惠券识别码是否重复
	 * @param code 识别码
	 * @return result 0:不重复，1：重复
	 */
	public int checkCode(String code);
	
	/**
	 * 获取会员已经领取的店铺优惠券数量
	 * @param coupons_id 店铺优惠券id
	 * @param store_id 店铺id
	 * @param member_id 会员id
	 * @return
	 */
	public int getMemberCouponsNum(Integer coupons_id, Integer store_id, Integer member_id);
	
	/**
	 * 获取会员已经领取的优惠券数量
	 * @param coupons_id 店铺优惠券id
	 * @param member_id  会员id
	 * @return
	 * add by liuyulei  2016-10-13
	 */
	public int getMemberCouponsNum(Integer coupons_id, Integer member_id);
	
	/**
	 * 获取会员已经领取的优惠券数量
	 * @param coupons_id 店铺优惠券id
	 * @param member_mobile  手机号码
	 * @return
	 * add by liuyulei  2016-10-13
	 */
	public int getMemberCouponsNum(Integer coupons_id, Integer store_id,String member_mobile);
	
	/**
	 * 根据店铺会员优惠券获取会员优惠券有效并且没有被领取的优惠券
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	public int getMemberCouponsNum(Integer coupons_id);
	
	/**
	 * 会员领取店铺优惠券
	 * @param coupons_id 店铺优惠券id
	 * @param store_id 店铺id
	 * @param member_id 会员id
	 * @param get_type 优惠券获取途径 0：店铺领取，1：活动赠送, 2：店铺发放
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void receive(Integer coupons_id, Integer store_id, Integer member_id, Integer get_type);
	
	/**
	 * 获取会员优惠券分页列表
	 * @param pageNo 页数
	 * @param pageSize 每页记录数
	 * @param member_id 会员id
	 * @return
	 */
	public Page getMemberCouponsList(int pageNo, int pageSize, Integer member_id);
	
	/**
	 * 
	 * @param pageNo  页数 
	 * @param pageSize 每页记录数
	 * @param params   参数列表
	 * @return
	 */
	public Page getMemberCouponsList(int pageNo, int pageSize, Map params);
	
	/**
	 * 根据店铺优惠券id和店铺id获取店铺已经发放并且有效，而且未被领取的优惠券集合
	 * @param coupons_id 店铺优惠券id
	 * @param store_id 店铺id
	 * @return
	 */
	public List<MemberCoupons> getCouponsList(Integer coupons_id, Integer store_id);
	
	/**
	 * 获取会员有效未使用并且属于购买商品店铺的优惠券集合
	 * @param member_id 会员id
	 * @param store_id 店铺id
	 * @param min_order_money 订单金额
	 * @return
	 */
	public List getCouponsList(Integer member_id, Integer store_id,Double min_order_money);
	
	/**
	 * 根据会员优惠券id获取会员优惠券信息
	 * @param mcoup_id 会员优惠券id
	 * @return
	 */
	public MemberCoupons get(Integer mcoup_id);
	
	/**
	 * 根据会员优惠券识别码获取会员优惠券信息
	 * @param coupons_code 优惠券唯一识别码
	 * @return
	 */
	public MemberCoupons get(String coupons_code);
	
	/**
	 * 会员使用一个优惠券
	 * @param mcoup_id 会员优惠券id
	 * @param coupons_id 店铺优惠券id
	 * @param member_id 会员id
	 * @param order_id 订单id
	 * @param order_sn 订单编号
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void useCoupons(Integer mcoup_id, Integer coupons_id, Integer member_id, Integer order_id, String order_sn);
	
	/**
	 * 订单取消，优惠券恢复未使用状态
	 * @param order_id 订单id
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void returned(Integer order_id);
	
	
	/**
	 * 购物车选中和取消会员优惠券
	 * @param couponus
	 * @param isCheck
	 */
	public void checkCoupons(MemberCoupons couponus,Integer isCheck);
	
	
	/**
	 * 释放优惠券
	 * @param memberId
	 * @param storeid
	 */
	public void releaseCoupons(Integer memberId,Integer storeid);
	
	
	
	/**
	 * 释放选中的所有优惠券
	 * @param memberId
	 * @param storeid
	 */
	public void releaseCouponsAll(Integer memberId);
	
	
	/**
	 * 查询用户选中的coupons 
	 * @param member_id
	 * @return
	 */
	public MemberCoupons getCheckCoupons(Integer member_id,Integer sotreid);
	
	
}
