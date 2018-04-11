package com.enation.app.b2b2c.core.store.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.b2b2c.core.store.model.StoreBonus;
import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.b2b2c.core.store.service.IStorePromotionManager;
import com.enation.app.shop.component.bonus.service.IBonusTypeManager;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.util.DateUtil;

/**
 * 店铺促销管理 manager
 * @author xulipeng
 * 2015年1月12日23:14:29
 */
@SuppressWarnings({"rawtypes", "unused", "unchecked"})
@Service("storePromotionManager")
public class StorePromotionManager  implements IStorePromotionManager {
	
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IBonusTypeManager bonusTypeManager;
	
	@Override
	public void add_FullSubtract(StoreBonus bonus) {
		this.daoSupport.insert("es_bonus_type", bonus);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void receive_bonus(Integer memberid, Integer storeid, Integer type_id) {
		StoreBonus bonus =	this.getBonus(type_id);
		
		while (memberid != null) {
			String sn = this.createSn(bonus.getType_id()+"");
			int c = this.daoSupport.queryForInt("select count(0) from es_member_bonus where bonus_sn=?", sn);
			
			if (c == 0) {
				this.daoSupport.execute("insert into es_member_bonus(bonus_type_id,bonus_sn,type_name,bonus_type,create_time,member_id)values(?,?,?,?,?,?)", type_id,sn,bonus.getType_name(),bonus.getSend_type(),DateUtil.getDateline(),memberid);
				
				//修改优惠券已被领取的数量 by_DMRain 2016-6-24
				this.daoSupport.execute("update es_bonus_type set received_num = (received_num + 1) where type_id = ?", type_id);
				return;
			} else {
				System.out.println("有相同的sn码,在生成一个sn码");
			}
		}
	}
	
	private String createSn(String prefix){
		
		StringBuffer sb = new StringBuffer();
		sb.append(prefix);
		sb.append( DateUtil.toString(new Date(), "yyMM"));
		sb.append( createRandom() );
		
		return sb.toString();
	}
	
	private String createRandom(){
		Random random  = new Random();
		StringBuffer pwd=new StringBuffer();
		for(int i=0;i<6;i++){
			pwd.append(random.nextInt(9));
			 
		}
		return pwd.toString();
	}
	
	@Override
	public StoreBonus getBonus(Integer type_id) {
		String sql ="select * from es_bonus_type  where type_id =?";
		return (StoreBonus) this.daoSupport.queryForObject(sql, StoreBonus.class, type_id);
	}
	
	@Override
	public int getmemberBonus(Integer type_id,Integer memberid) {
		String sql = "select count(0) from es_member_bonus where bonus_type_id=? and member_id=?";
		int num = this.daoSupport.queryForInt(sql, type_id,memberid);
		return num;
	}
	
	@Override
	public void edit_FullSubtract(StoreBonus bonus) {
		this.daoSupport.update("es_bonus_type", bonus, "type_id="+bonus.getType_id());
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteBonus(Integer type_id) {
		String sql ="select use_end_date from es_bonus_type where type_id="+type_id;
		long use_end_date = this.daoSupport.queryForLong(sql);
		if(DateUtil.getDateline()<use_end_date){
			throw new RuntimeException("此优惠劵未过期不能删除!");
		}else{
			int result = this.checkForActivity(type_id);
			if (result == 1) {
				throw new RuntimeException("此优惠券已经关联了促销活动，不可删除");
			} else {
				this.daoSupport.execute("delete from es_bonus_type where type_id="+type_id);
				
				//商家删除优惠券后，也将会员领取的此优惠券删除 add_by DMRain 2016-7-28
				this.daoSupport.execute("delete from es_member_bonus where bonus_type_id = ?", type_id);
				
				//将已经结束的促销活动关联的优惠券信息去除 by_DMRain 2016-6-16
				sql = "update es_activity_detail set is_send_bonus = 0,bonus_id = null where bonus_id = ?";
				this.daoSupport.execute(sql, type_id);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.IStorePromotionManager#getList(java.lang.Integer)
	 */
	@Override
	public List<StoreBonus> getList(Integer store_id) {
		long curTime = DateUtil.getDateline();
		String sql = "select * from es_bonus_type where store_id = ? and create_num > 0 and use_end_date > ?";
		List<StoreBonus> list = this.daoSupport.queryForList(sql, store_id, curTime);
		return list;
	}

	/* (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.store.service.IStorePromotionManager#getCountBonus(java.lang.Integer)
	 */
	@Override
	public int getCountBonus(Integer type_id) {
		String queryBonusCreate = "select count(0) from es_member_bonus where bonus_type_id = "+type_id;
		int count = this.daoSupport.queryForInt(queryBonusCreate);
		return count;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.store.service.IStorePromotionManager#getCouponsList(java.lang.Integer)
	 */
	@Override
	public List<StoreCoupons> getCouponsList(Integer store_id) {
		String sql = "select * from es_store_coupons where status = 0 and send_status = 1 and coupons_stock > received_num and end_date > ? and store_id = ?";
		List<StoreCoupons> couponsList = this.daoSupport.queryForList(sql, StoreCoupons.class, DateUtil.getDateline(), store_id);
		return couponsList;
	}
	
	/**
	 * 检查优惠券是否已经关联了促销活动
	 * add by DMRain 2016-6-16
	 * @param type_id 优惠券ID
	 * @return result 0：否，1：是
	 */
	private int checkForActivity(Integer type_id) {
		String sql = "select count(0) from es_activity a inner join es_activity_detail ad on a.activity_id = ad.activity_id "
				+ "where a.disabled = 0 and a.end_time > ? and ad.bonus_id = ?";
		int result = this.daoSupport.queryForInt(sql, DateUtil.getDateline(), type_id);
		result = result > 0 ? 1 : 0;
		return result;
	}


}
