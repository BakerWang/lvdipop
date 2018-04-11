package com.enation.app.javashop.customized.core.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.framework.database.Page;

/**
 * 店铺优惠券管理接口
 * @author DMRain
 * @date 2016-9-22
 * @since v61
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public interface IStoreCouponsManager {

	public static final Integer SELF_STORE_ID_KEY = 1;
	
	/**
	 * 获取店铺优惠券分页列表集合
	 * @param pageNo 页数
	 * @param pageSize 每页记录数
	 * @param store_id 店铺id
	 * @param result 参数集合
	 * @return
	 */
	public Page getStoreCouponsList(int pageNo, int pageSize, Integer store_id, Map result);
	
	/**
	 * 新增店铺优惠券
	 * @param storeCoupons 店铺优惠券信息
	 */
	public void add(StoreCoupons storeCoupons);
	
	/**
	 * 根据优惠券id和店铺id获取一条店铺优惠券信息
	 * @param coupons_id 优惠券id
	 * @param store_id 店铺id
	 * @return storeCoupons 店铺优惠券信息
	 */
	public StoreCoupons get(Integer coupons_id, Integer store_id);
	
	/**
	 * 根据店铺优惠券id获取一条店铺优惠券信息
	 * @param coupons_id 店铺优惠券id
	 * @return
	 */
	public StoreCoupons getCoupons(Integer coupons_id);
	
	/**
	 * 
	 * @Title: get  根据优惠券id 获取一条店铺优惠券信息
	 * @param coupons_id  优惠券id
	 * @return
	 * @return: StoreCoupons  店铺优惠券信息
	 * @author： liuyulei
	 * @date：2016年10月11日 下午5:19:58
	 */
	public Map get(Integer coupons_id);
	
	/**
	 * 修改优惠券信息
	 * @param storeCoupons 店铺优惠券信息
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void edit(StoreCoupons storeCoupons);
	
	/**
	 * 将店铺优惠券设置为失效状态
	 * @param coupons_id 优惠券id
	 * @param store_id 店铺id
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void invalid(Integer coupons_id, Integer store_id);
	
	/**
	 * 删除店铺优惠券
	 * @param coupons_id 优惠券id
	 * @param store_id 店铺id
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(Integer coupons_id, Integer store_id);
	
	/**
	 * 发放优惠券
	 * @param coupons_id 优惠券id
	 * @param store_id 店铺id
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void send(Integer coupons_id, Integer store_id);
	
	/**
	 * 追加发放优惠券
	 * @param coupons_id 优惠券id
	 * @param store_id 店铺id
	 * @param append_num 追加发放的数量
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void append(Integer coupons_id, Integer store_id, Integer append_num);
	
	/**
	 * 获取已发放的店铺优惠券列表
	 * @param pageNo 页数
	 * @param pageSize 每页记录数
	 * @param coupons_id 店铺优惠券id
	 * @param store_id 优惠券所属店铺id
	 * @param result 搜索参数集合
	 * @return
	 */
	public Page listSendCoupons(int pageNo, int pageSize, Integer coupons_id, Integer store_id, Map result);
	
	/**
	 * 将已经发放的某个优惠券置为无效
	 * @param mcoup_id 已发放的优惠券id
	 * @param coupons_id 优惠券id
	 * @param store_id 店铺id
	 */
	public void invalidCoupons(Integer mcoup_id, Integer coupons_id, Integer store_id);
	
	/**
	 * 将已经发放的某个优惠券删除
	 * @param mcoup_id 已发放的优惠券id
	 * @param coupons_id 优惠券id
	 * @param store_id 店铺id
	 */
	public void deleteCoupons(Integer mcoup_id, Integer coupons_id, Integer store_id);
	
	/**
	 * 获取店铺当前所有已发放并且未失效的优惠券集合
	 * @param store_id 店铺id
	 * @return
	 */
	public List<StoreCoupons> listCoupons(Integer store_id);
	
	/**
	 * 根据会员等级id获取，除当前店铺会员之外的所有会员数量
	 * @param lv_id 会员等级id
	 * @param member_id 会员id
	 * @return result 会员数量
	 */
	public int getMemberLvNum(Integer lv_id, Integer member_id);
	
	/**
	 * 根据会员所在地区id获取，除当前店铺会员之外的所有会员数量
	 * @param province_id 所在省份id
	 * @param city_id 所在城市id
	 * @param region_id 所在区域（县）id
	 * @param member_id 会员id
	 * @return result 会员数量
	 */
	public int getMemberRegionNum(Integer province_id, Integer city_id, Integer region_id, Integer member_id);
	
}
