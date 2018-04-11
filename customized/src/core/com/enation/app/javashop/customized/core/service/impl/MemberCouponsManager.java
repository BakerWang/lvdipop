package com.enation.app.javashop.customized.core.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.order.service.IStoreOrderManager;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.service.IStoreManager;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.StringUtil;

/**
 * 会员优惠券管理接口实现类
 * 
 * @author DMRain
 * @date 2016-9-27
 * @since v61
 * @version 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Component
public class MemberCouponsManager implements IMemberCouponsManager {

	@Autowired
	private IDaoSupport<MemberCoupons> daoSupport;

	@Autowired
	private IStoreMemberManager storeMemberManager;

	@Autowired
	private IStoreManager storeManager;

	@Autowired
	private IStoreOrderManager storeOrderManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IMemberCouponsManager#
	 * add(com.enation.app.javashop.customized.core.model.coupons.MemberCoupons)
	 */
	@Override
	public void add(MemberCoupons memberCoupons) {
		this.daoSupport.insert("es_member_coupons", memberCoupons);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IMemberCouponsManager#
	 * checkCode(java.lang.String)
	 */
	@Override
	public int checkCode(String code) {
		String sql = "select count(0) from es_member_coupons where mcoup_code = ?";
		int result = this.daoSupport.queryForInt(sql, code);
		result = result > 0 ? 1 : 0;
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IMemberCouponsManager#
	 * getMemberCouponsNum(java.lang.Integer, java.lang.Integer,
	 * java.lang.Integer)
	 */
	@Override
	public int getMemberCouponsNum(Integer coupons_id, Integer store_id, Integer member_id) {
		String sql = "select count(0) from es_member_coupons where get_type = 0 and coupons_id = ? and store_id = ? and member_id = ?";
		return this.daoSupport.queryForInt(sql, coupons_id, store_id, member_id);
	}
	
	/* (non Javadoc) 
	 * @see com.enation.app.javashop.customized.core.service.IMemberCouponsManager#
	 * getMemberCouponsNum(java.lang.Integer, java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年10月13日 上午10:28:08
	 */
	@Override
	public int getMemberCouponsNum(Integer coupons_id, Integer member_id) {
		String sql = "select count(0) from es_member_coupons where get_type = 0 and coupons_id = ? and member_id = ?";
		return this.daoSupport.queryForInt(sql, coupons_id, member_id);
	}
	
	/* (non Javadoc) 
	 * @param coupons_id
	 * @param member_mobile
	 * @see com.enation.app.javashop.customized.core.service.IMemberCouponsManager#getMemberCouponsNum(java.lang.Integer, java.lang.String) 
	 * @author： liuyulei
	 * @date：2016年10月14日 下午5:48:24
	 */
	@Override
	public int getMemberCouponsNum(Integer coupons_id, Integer store_id, String member_mobile) {
		String sql = "select count(0) from es_member_coupons where get_type = 0 and coupons_id = ? and member_mobile = ? and store_id = ? ";
		return this.daoSupport.queryForInt(sql, coupons_id, member_mobile, store_id);
	}

	@Override
	public int getMemberCouponsNum(Integer coupons_id) {
		String sql = "select count(0) from es_member_coupons where is_received = 0 and status = 0 and coupons_id = ? and mcoup_end_date > ?";
		return this.daoSupport.queryForInt(sql, coupons_id, DateUtil.getDateline());
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IMemberCouponsManager#receive(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void receive(Integer coupons_id, Integer store_id, Integer member_id, Integer get_type) {
		String sql = "";
		List<MemberCoupons> couponsList = this.getCouponsList(coupons_id, store_id);

		//如果优惠券集合不为空
		if (couponsList != null) {
			MemberCoupons memberCoupons = couponsList.get(0);
			StoreMember member = this.storeMemberManager.getMember(member_id);

			sql = "update es_member_coupons set is_received = 1,received_date = ?,member_id = ?,get_type = ?,member_name = ?,member_mobile = ? where mcoup_id = ?";
			this.daoSupport.execute(sql, DateUtil.getDateline(), member_id, get_type, member.getUname(),member.getMobile(),
					memberCoupons.getMcoup_id());

			sql = "update es_store_coupons set received_num = (received_num + 1) where coupons_id = ? and store_id = ?";
			this.daoSupport.execute(sql, coupons_id, store_id);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IMemberCouponsManager#
	 * getMemberCouponsList(int, int, java.lang.Integer)
	 */
	@Override
	public Page getMemberCouponsList(int pageNo, int pageSize, Integer member_id) {
		String sql = "select c.*,s.store_name from es_member_coupons c left join es_store s on c.store_id = s.store_id where c.is_deleted = 0 and c.member_id = ? order by c.received_date desc";
		Page page = this.daoSupport.queryForPage(sql, pageNo, pageSize, member_id);

		List list = (List) page.getResult();
		Map map = new HashMap();

		// 遍历获取的当前页的记录
		for (int i = 0; i < list.size(); i++) {
			map = (Map) list.get(i);
			String stateStr = this.getCurrentStatus((Long) (map.get("mcoup_start_date")),
					(Long) (map.get("mcoup_end_date")), (Integer) (map.get("status")));
			map.put("state", stateStr);
		}

		return page;
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IMemberCouponsManager#getCouponsList(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public List<MemberCoupons> getCouponsList(Integer coupons_id,
			Integer store_id) {
		String sql = "select * from es_member_coupons where is_received = 0 and status = 0 and "
				+ "mcoup_end_date > ? and coupons_id = ? and store_id = ?";
		List<MemberCoupons> couponsList = this.daoSupport.queryForList(sql, MemberCoupons.class, DateUtil.getDateline(),
				coupons_id, store_id);
		return couponsList;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IMemberCouponsManager#
	 * getCouponsList(java.lang.Integer, java.lang.Integer, java.lang.Double)
	 */
	@Override
	public List getCouponsList(Integer member_id, Integer store_id, Double min_order_money) {
		// 根据店铺id获取店铺信息
		Store store = this.storeManager.getStore(store_id);

		String sql = "select * from es_member_coupons where status = 0 and is_used = 0 and mcoup_start_date < ? and mcoup_end_date > ? and "
				+ "(min_order_money <= ? or mcoup_type = 2) and member_id = ? and store_id = ?";
		List list = this.daoSupport.queryForList(sql, DateUtil.getDateline(), DateUtil.getDateline(), min_order_money,
				member_id, store_id);
		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IMemberCouponsManager#
	 * get(java.lang.Integer)
	 */
	@Override
	public MemberCoupons get(Integer mcoup_id) {
		String sql = "select * from es_member_coupons where mcoup_id = ?";
		return this.daoSupport.queryForObject(sql, MemberCoupons.class, mcoup_id);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IMemberCouponsManager#get(java.lang.String)
	 */
	@Override
	public MemberCoupons get(String coupons_code) {
		String sql = "select * from es_member_coupons where mcoup_code = ?";
		return this.daoSupport.queryForObject(sql, MemberCoupons.class, coupons_code);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IMemberCouponsManager#
	 * useCoupons(java.lang.Integer, java.lang.Integer, java.lang.Integer,
	 * java.lang.Integer, java.lang.String)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void useCoupons(Integer mcoup_id, Integer coupons_id, Integer member_id, Integer order_id, String order_sn) {
		String sql = "update es_member_coupons set is_used = 1, used_date = ?";
		
		//如果订单id和订单编号不为空，证明优惠券是线上使用的
		if (order_id != null && !StringUtil.isEmpty(order_sn)) {
			sql += ", order_id = " + order_id + ", order_sn = '" + order_sn + "'";
		}
		
		sql += " where mcoup_id = ? and member_id = ?";
		
		this.daoSupport.execute(sql, DateUtil.getDateline(), mcoup_id, member_id);

		sql = "update es_store_coupons set used_num = (used_num + 1) where coupons_id = ?";
		this.daoSupport.execute(sql, coupons_id);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IMemberCouponsManager#
	 * returned(java.lang.Integer)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void returned(Integer order_id) {
		MemberCoupons memberCoupons = this.daoSupport
				.queryForObject("select * from es_member_coupons where order_id = ?", MemberCoupons.class, order_id);

		if(memberCoupons !=null){
			String sql = "update es_member_coupons set is_used = 0, used_date = null, order_id = null, order_sn = null,is_check =0 where order_id = ?";
			this.daoSupport.execute(sql, order_id);
			
			sql = "update es_store_coupons set used_num = (used_num - 1) where coupons_id = ?";
			this.daoSupport.execute(sql, memberCoupons.getCoupons_id());
		}
		
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.javashop.customized.core.service.IMemberCouponsManager#
	 * getMemberCouponsList(int, int, java.util.Map)
	 */
	@Override
	public Page getMemberCouponsList(int pageNo, int pageSize, Map params) {
		String sql = "select c.* from es_member_coupons c left join es_member m on c.member_id = m.member_id where c.is_deleted = 0 ";

		String mobile = params.get("mobile") == null ? "" : params.get("mobile").toString();
		Integer status = Integer.parseInt(params.get("status").toString());

		if (!StringUtil.isEmpty(mobile)) {
			sql += " and m.mobile = '" + mobile + "'";
		}
		
		if(status != -1){
				if(status != 2){  // 使用状态    
					sql += " and c.is_used = " + status;
				}
				
				if(status == 2){  // 判断是否超过有效期
					sql += " and c.mcoup_end_date < " + System.currentTimeMillis() / 1000;
				}
		}
		sql += " order by c.received_date desc";
		Page page = this.daoSupport.queryForPage(sql, pageNo, pageSize);

		List list = (List) page.getResult();
		Map map = new HashMap();

		// 遍历获取的当前页的记录
		for (int i = 0; i < list.size(); i++) {
			map = (Map) list.get(i);
			String stateStr = this.getCurrentStatus((Long) (map.get("mcoup_start_date")),
					(Long) (map.get("mcoup_end_date")), (Integer) (map.get("status")));
			map.put("state", stateStr);
		}
		return page;
	}

	/**
	 * 获取当前状态
	 * 
	 * @param startTime
	 *            优惠券生效时间
	 * @param endTime
	 *            优惠券失效时间
	 * @param status
	 *            优惠券状态
	 * @return
	 */
	private String getCurrentStatus(Long startTime, Long endTime, Integer status) {
		String state = "";
		long currentTime = System.currentTimeMillis() / 1000; // 获取当前时间秒数
		if (startTime > currentTime) {
			state = "正常";
		} else if ((startTime <= currentTime) && (endTime >= currentTime)) {
			state = "正常";
		} else if (endTime < currentTime) {
			state = "已失效";
		}
		if (status.intValue() == 1) {
			state = "已失效";
		}
		return state;
	}

	@Override
	public void checkCoupons(MemberCoupons couponus, Integer isCheck) {
		String sql = "update es_member_coupons set is_check = ? where mcoup_id = ?";
		this.daoSupport.execute(sql,isCheck,couponus.getMcoup_id());
	}

	@Override
	public void releaseCoupons(Integer memberId, Integer storeid) {
		String sql = "update es_member_coupons set is_check = 0 where member_id = ? and  is_check = 1 and is_used = 0 and store_id =?";
		this.daoSupport.execute(sql, memberId,storeid);
	}

	@Override
	public MemberCoupons getCheckCoupons(Integer member_id, Integer sotreid) {
		String sql = "select * from es_member_coupons where status = 0 and is_used=0 and "
				+ "mcoup_end_date > ? and member_id = ? and store_id = ? and is_check = 1";
		return this.daoSupport.queryForObject(sql, MemberCoupons.class, DateUtil.getDateline(),member_id,sotreid);
	}

	@Override
	public void releaseCouponsAll(Integer memberId) {
		String sql = "update es_member_coupons set is_check = 0 where member_id = ? and  is_check = 1 and is_used = 0";
		this.daoSupport.execute(sql, memberId);
	}
	
	
	

}
