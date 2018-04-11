package com.enation.app.b2b2c.core.store.service;

import java.util.List;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.b2b2c.core.store.model.StoreBonus;
import com.enation.app.b2b2c.core.store.model.StoreCoupons;


/**
 * 店铺促销管理接口
 * @author xulipeng
 * 2015年1月12日23:07:19
 */
public interface IStorePromotionManager {

	/**
	 * 添加满减优惠
	 * @return
	 */
	public void add_FullSubtract(StoreBonus bonus);
	
	/**
	 * 会员领取优惠卷
	 * @param memberid	会员id
	 * @param storeid	店铺id
	 * @param type_id	优惠卷id
	 */
	public void receive_bonus(Integer memberid,Integer storeid,Integer type_id );
	
	/**
	 * 获取优惠劵
	 * @param type_id
	 * @return
	 */
	public StoreBonus getBonus(Integer type_id);
	
	/**
	 * 获取会员领取的优惠劵的数量
	 * @param type_id
	 * @return
	 */
	public int getmemberBonus(Integer type_id,Integer memberid);
	
	/**
	 * 修改优惠劵
	 * @param bonus
	 */
	public void edit_FullSubtract(StoreBonus bonus);
	
	/**
	 * 删除优惠劵
	 * @param bonus
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteBonus(Integer type_id);
	
	/**
	 * 获取店铺所有有效的优惠券List
	 * @author DMRain
	 * @date 2016-1-19
	 * @param store_id 店铺ID
	 * @return
	 */
	public List<StoreBonus> getList(Integer store_id);
	
	/**
	 * 获取优惠券已被领取的数量
	 * @param type_id 优惠券ID
	 * @return
	 */
	public int getCountBonus(Integer type_id);
	
	/**
	 * 新版获取店铺所有有效并且已发放的优惠券集合
	 * @author DMRain 
	 * @date 2016-10-11
	 * @param store_id 店铺id
	 * @return
	 */
	public List<StoreCoupons> getCouponsList(Integer store_id);
}
