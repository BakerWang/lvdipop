package com.enation.app.javashop.customized.core.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.javashop.customized.core.service.IStoreCouponsManager;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.jms.EopJmsMessage;
import com.enation.framework.jms.EopProducer;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.StringUtil;

/**
 * 店铺优惠券管理接口实现类
 * @author DMRain
 * @date 2016-9-22
 * @since v61
 * @version 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Component
public class StoreCouponsManager implements IStoreCouponsManager{

	@Autowired
	private IDaoSupport<StoreCoupons> daoSupport;

	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	
	@Autowired
	private MemberCouponsCreateProcessor memberCouponsCreateProcessor;
	
	@Autowired
	private EopProducer eopProducer;
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#getStoreCouponsList(int, int, java.lang.Integer, java.util.Map)
	 */
	@Override
	public Page getStoreCouponsList(int pageNo, int pageSize, Integer store_id,
			Map result) {
		String sql = "select * from es_store_coupons where is_deleted = 0 and store_id = ?";
		
		String keyword = (String) result.get("keyword");
		//如果搜索关键字不为空
		if (!StringUtil.isEmpty(keyword)) {
			sql += " and coupons_name like '%" + keyword + "%'";
		}
		
		String coupons_type = (String) result.get("coupons_type");
		//如果优惠券类型不为空
		if (!StringUtil.isEmpty(coupons_type)) {
			sql += " and coupons_type = " + coupons_type + "";
		}
		
		String coupons_status = (String) result.get("coupons_status");
		//如果优惠券状态不为空
		if (!StringUtil.isEmpty(coupons_status)) {
			//如果优惠券状态为未生效
			if (coupons_status.equals("0")) {
				sql += " and status = 0 and start_date > " + DateUtil.getDateline() + "";
			}
			
			//如果优惠券状态为已生效
			if (coupons_status.equals("1")) {
				sql += " and status = 0 and start_date <= " + DateUtil.getDateline() + " and end_date >= " + DateUtil.getDateline() + "";
			}
			
			//如果优惠券状态为已失效
			if (coupons_status.equals("2")) {
				sql += " and (status = 1 or end_date < " + DateUtil.getDateline() + ")";
			}
		}
		
		String send_status = (String) result.get("send_status");
		//如果优惠券发放状态不为空
		if (!StringUtil.isEmpty(send_status)) {
			sql += " and send_status = " + send_status + "";
		}
		
		Integer province_id = (Integer) result.get("province_id");
		//如果优惠券使用地区不为空
		if (province_id != null) {
			//如果优惠券使用地区不为0
			if (!province_id.equals(0)) {
				sql += " and province_id = " + province_id + "";
			}
		}
		
		String start_date = (String) result.get("start_date");
		//如果优惠券生效日期不为空
		if (!StringUtil.isEmpty(start_date)) {
			sql += " and start_date >= " + DateUtil.getDateline(start_date + " 00:00:00", "yyyy-MM-dd HH:mm:ss") + "";
		}
		
		String end_date = (String) result.get("end_date");
		//如果优惠券失效日期不为空
		if (!StringUtil.isEmpty(end_date)) {
			sql += " and end_date <= " + DateUtil.getDateline(end_date + " 23:59:59", "yyyy-MM-dd HH:mm:ss") + "";
		}
		
		sql += " order by create_date desc";
		
		Page page = this.daoSupport.queryForPage(sql, pageNo, pageSize, store_id);
		
		List list = (List) page.getResult();
		Map map = new HashMap();
		
		//遍历获取的当前页的记录
		for(int i = 0; i < list.size(); i++){
			map = (Map) list.get(i);
			String stateStr = this.getCurrentStatus((Long)(map.get("start_date")), (Long)(map.get("end_date")), (Integer)(map.get("status")));
            map.put("state", stateStr);
		}
		return page;
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#add(com.enation.app.javashop.customized.core.model.coupons.StoreCoupons)
	 */
	@Override
	public void add(StoreCoupons storeCoupons) {
		this.daoSupport.insert("es_store_coupons", storeCoupons);
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#get(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public StoreCoupons get(Integer coupons_id, Integer store_id) {
		String sql = "select * from es_store_coupons where coupons_id = ? and store_id = ?";
		return this.daoSupport.queryForObject(sql, StoreCoupons.class, coupons_id, store_id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#getCoupons(java.lang.Integer)
	 */
	@Override
	public StoreCoupons getCoupons(Integer coupons_id) {
		String sql = "select * from es_store_coupons where coupons_id = ?";
		return this.daoSupport.queryForObject(sql, StoreCoupons.class, coupons_id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#get(java.lang.Integer)
	 */
	@Override
	public Map get(Integer coupons_id) {
		String sql = "select * from es_store_coupons where coupons_id = ?";
		return this.daoSupport.queryForMap(sql, coupons_id);
	}
	
	/*
     * (non-Javadoc)
     * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#edit(com.enation.app.javashop.customized.core.model.coupons.StoreCoupons)
     */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void edit(StoreCoupons storeCoupons) {
		String sql = "";
		
		//如果修改后的优惠券类型为满减券，则将原优惠券中的优惠券折扣一项置为null，
		//如果修改后的优惠券类型为折扣券，则将原优惠券中的优惠券金额一项置为null，
		//如果修改后的优惠券类型为现金券，则将原优惠券中的最小订单金额和优惠券折扣两项置为null，这样做是为了保证优惠券只为一种类型
		if (storeCoupons.getCoupons_type().equals(0)) {
			sql = "update es_store_coupons set coupons_discount = null where coupons_id = ? and store_id = ?";
		} else if (storeCoupons.getCoupons_type().equals(1)) {
			sql = "update es_store_coupons set coupons_money = null where coupons_id = ? and store_id = ?";
		} else {
			sql = "update es_store_coupons set coupons_discount = null,min_order_money = null where coupons_id = ? and store_id = ?";
		}
		
		this.daoSupport.execute(sql, storeCoupons.getCoupons_id(), storeCoupons.getStore_id());
		
		this.daoSupport.update("es_store_coupons", storeCoupons, "coupons_id="+storeCoupons.getCoupons_id());
		
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#invalid(java.lang.Integer, java.lang.Integer)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void invalid(Integer coupons_id, Integer store_id) {
		String sql = "update es_store_coupons set status = 1 where coupons_id = ? and store_id = ?";
		this.daoSupport.execute(sql, coupons_id, store_id);
		
		//获取优惠券信息
		StoreCoupons coupons = this.get(coupons_id, store_id);
		
		//如果优惠券已发放，则将还没有被领取的优惠券设置为无效
		if (coupons.getSend_status() == 1) {
			sql = "update es_member_coupons set status = 1 where coupons_id = ? and store_id = ? and is_received = 0";
			this.daoSupport.execute(sql, coupons_id, store_id);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#delete(java.lang.Integer, java.lang.Integer)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void delete(Integer coupons_id, Integer store_id) {
		String sql = "update es_store_coupons set is_deleted = 1 where coupons_id = ? and store_id = ?";
		this.daoSupport.execute(sql, coupons_id, store_id);
		
		//获取优惠券信息
		StoreCoupons coupons = this.get(coupons_id, store_id);
		
		//如果优惠券已发放，则将还没有被领取的优惠券设置已删除
		if (coupons.getSend_status() == 1) {
			sql = "update es_member_coupons set is_deleted = 1 where coupons_id = ? and store_id = ? and is_received = 0";
			this.daoSupport.execute(sql, coupons_id, store_id);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#send(java.lang.Integer, java.lang.Integer)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void send(Integer coupons_id, Integer store_id) {
		StoreCoupons storeCoupons = this.get(coupons_id, store_id);
		
		MemberCoupons memberCoupons = this.getMemberCoupons(storeCoupons);
		
		int num = storeCoupons.getCoupons_stock();
		
		//this.sendMemberCoupons(store_id, memberCoupons, num);
		// 修改创建优惠券为消息机制
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("storeId", store_id);
		map.put("memberCoupons", memberCoupons);
		map.put("num", num);
		
		//this.memberCouponsCreateProcessor.process(map);
		
		EopJmsMessage jmsMessage = new EopJmsMessage();
		jmsMessage.setData(map);
		jmsMessage.setProcessorBeanId("memberCouponsCreateProcessor");
		eopProducer.send(jmsMessage);
		
		
		//成功发放后将原店铺优惠券的发放状态改为已发放
		this.daoSupport.execute("update es_store_coupons set send_status = 1 where coupons_id = ? and store_id = ?", coupons_id, store_id);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#append(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void append(Integer coupons_id, Integer store_id, Integer append_num) {
		StoreCoupons storeCoupons = this.get(coupons_id, store_id);
		
		MemberCoupons memberCoupons = this.getMemberCoupons(storeCoupons);
		
		//this.sendMemberCoupons(store_id, memberCoupons, append_num);
		// 修改创建优惠券为消息机制
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("storeId", store_id);
		map.put("memberCoupons", memberCoupons);
		map.put("num", append_num);
		
		//this.memberCouponsCreateProcessor.process(map);
		
		EopJmsMessage jmsMessage = new EopJmsMessage();
		jmsMessage.setData(map);
		jmsMessage.setProcessorBeanId("memberCouponsCreateProcessor");
		eopProducer.send(jmsMessage);
		
		//成功追加后，要修改原店铺优惠券的库存数量
		this.daoSupport.execute("update es_store_coupons set coupons_stock=(coupons_stock + ?) where coupons_id = ? and store_id = ?", append_num, coupons_id, store_id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#listSendCoupons(int, int, java.lang.Integer, java.lang.Integer, java.util.Map)
	 */
	@Override
	public Page listSendCoupons(int pageNo, int pageSize, Integer coupons_id,
			Integer store_id, Map result) {
		String sql = "select * from es_member_coupons where is_deleted = 0 and coupons_id = ? and store_id = ?";
		
		String is_received = (String) result.get("is_received");
		//如果领取状态不为空
		if (!StringUtil.isEmpty(is_received)) {
			sql += " and is_received = "+ is_received +"";
		}
		
		String is_used = (String) result.get("is_used");
		//如果使用状态不为空
		if (!StringUtil.isEmpty(is_used)) {
			sql += " and is_used = " + is_used + "";
		}
		
		sql += " order by mcoup_create_date desc";
		
		Page page = this.daoSupport.queryForPage(sql, pageNo, pageSize, coupons_id, store_id);
		
		List list = (List) page.getResult();
		Map map = new HashMap();
		
		//遍历获取的当前页的记录
		for(int i = 0; i < list.size(); i++){
			map = (Map) list.get(i);
			String stateStr = this.getCurrentStatus((Long)(map.get("mcoup_start_date")), (Long)(map.get("mcoup_end_date")), (Integer)(map.get("status")));
            map.put("state", stateStr);
		}
		
		return page;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#invalidCoupons(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void invalidCoupons(Integer mcoup_id, Integer coupons_id, Integer store_id) {
		String sql = "update es_member_coupons set status = 1 where mcoup_id = ? and coupons_id = ? and store_id = ?";
		this.daoSupport.execute(sql, mcoup_id, coupons_id, store_id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#deleteCoupons(java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public void deleteCoupons(Integer mcoup_id, Integer coupons_id, Integer store_id) {
		String sql = "update es_member_coupons set is_deleted = 1 where mcoup_id = ? and coupons_id = ? and store_id = ?";
		this.daoSupport.execute(sql, mcoup_id, coupons_id, store_id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#listCoupons(java.lang.Integer)
	 */
	@Override
	public List<StoreCoupons> listCoupons(Integer store_id) {
		String sql = "select * from es_store_coupons where send_status = 1 and status = 0 and is_deleted = 0 and end_date > ? and store_id = ?";
		List<StoreCoupons> list = this.daoSupport.queryForList(sql, StoreCoupons.class, DateUtil.getDateline(), store_id);
		return list;
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#getMemberLvNum(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public int getMemberLvNum(Integer lv_id, Integer member_id) {
		String sql = "select count(0) from es_member where lv_id = ? and member_id != ?";
		return this.daoSupport.queryForInt(sql, lv_id, member_id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.javashop.customized.core.service.IStoreCouponsManager#getMemberRegionNum(java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public int getMemberRegionNum(Integer province_id, Integer city_id,
			Integer region_id, Integer member_id) {
		String sql = "select count(0) from es_member where member_id != ?";
		
		//如果省份id不等于0
		if (province_id.intValue() != 0) {
			sql += " and province_id = "+province_id+"";
		}
		
		//如果城市id不等于0
		if (city_id.intValue() != 0) {
			sql += " and city_id = "+city_id+"";
		}
		
		//如果地区（县）id不等于0
		if (region_id.intValue() != 0) {
			sql += " and region_id = "+region_id+"";
		}
		
		sql += " order by province_id asc";
		
		int result = this.daoSupport.queryForInt(sql, member_id);
		return result;
	}
	
	/**
	 * 填充并获取会员优惠券信息
	 * @param storeCoupons 店铺优惠券信息
	 * @return
	 */
	private MemberCoupons getMemberCoupons(StoreCoupons storeCoupons){
		MemberCoupons memberCoupons = new MemberCoupons();
		
		memberCoupons.setCoupons_id(storeCoupons.getCoupons_id());
		memberCoupons.setStore_id(storeCoupons.getStore_id());
		memberCoupons.setMcoup_name(storeCoupons.getCoupons_name());
		memberCoupons.setMcoup_type(storeCoupons.getCoupons_type());
		
		//如果店铺优惠券的类型为满减券 0：满减券，1：折扣券，2：现金券
		if (storeCoupons.getCoupons_type().intValue() == 0) {
			memberCoupons.setMcoup_money(storeCoupons.getCoupons_money());
		} else if (storeCoupons.getCoupons_type().intValue() == 1) {
			memberCoupons.setMcoup_discount(storeCoupons.getCoupons_discount());
		} else {
			memberCoupons.setMcoup_money(storeCoupons.getCoupons_money());
		}
		
		//如果优惠券类型不是现金券
		if (storeCoupons.getCoupons_type().intValue() != 2) {
			memberCoupons.setMin_order_money(storeCoupons.getMin_order_money());
		}
		
		memberCoupons.setMcoup_create_date(DateUtil.getDateline());
		memberCoupons.setMcoup_start_date(storeCoupons.getStart_date());
		memberCoupons.setMcoup_end_date(storeCoupons.getEnd_date());
		memberCoupons.setMcoup_province_id(storeCoupons.getProvince_id());
		memberCoupons.setMcoup_province_name(storeCoupons.getProvince_name());
		memberCoupons.setIs_received(0);
		memberCoupons.setIs_used(0);
		memberCoupons.setStatus(storeCoupons.getStatus());
		memberCoupons.setIs_deleted(storeCoupons.getIs_deleted());
		memberCoupons.setCoupons_describe(storeCoupons.getCoupons_describe());
		
		return memberCoupons;
	}
	
	
	/**
     * 获取当前状态
     * @param startTime 优惠券生效时间
     * @param endTime 优惠券失效时间
     * @param status 优惠券状态
     * @return
     */
    private String getCurrentStatus(Long startTime, Long endTime, Integer status) {
        String state = "";
        long currentTime = System.currentTimeMillis() / 1000; //获取当前时间秒数
        if (startTime > currentTime) {
        	state = "未生效";
        } else if ((startTime <= currentTime) && (endTime >= currentTime)) {
        	state = "已生效";
        } else if (endTime < currentTime) {
        	state = "已失效";
        }
        
        if (status.intValue() == 1) {
			state = "已失效";
		}
        
        return state;
    }

}
