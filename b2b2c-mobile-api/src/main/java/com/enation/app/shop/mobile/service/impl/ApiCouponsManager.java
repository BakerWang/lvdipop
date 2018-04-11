package com.enation.app.shop.mobile.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.app.shop.mobile.service.IApiCouponsManager;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.StringUtil;

/**
 * 
 * @Description: mobile优惠券管理
 * @author: liuyulei
 * @date: 2016年10月24日 下午12:08:24 
 * @since:v61
 */
@Service
public class ApiCouponsManager implements IApiCouponsManager {

	
	@SuppressWarnings("rawtypes")
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	
	
	/* (non Javadoc) 
	 * @param pageNo
	 * @param pageSize
	 * @param result
	 * @return 
	 * @see com.enation.app.shop.mobile.service.IApiCouponsManager#getStoreCouponsList(int, int, java.util.Map) 
	 * @author： liuyulei
	 * @date：2016年10月24日 下午12:18:17
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Page getStoreCouponsList(int pageNo, int pageSize, 
			Map result) {
		String sql = "select c.coupons_id,c.coupons_type,c.coupons_money,c.coupons_discount,c.min_order_money, c.coupons_describe,c.start_date,"
				+ " c.end_date,c.coupons_stock,c.received_num,c.status "
				+ " from es_store_coupons c where c.is_deleted = 0 and c.store_id = ? and c.send_status = 1"
				+ " and (c.status = 0 or c.end_date < " + DateUtil.getDateline() + ") ";  // 添加筛选条件    在失效日期之前   且 状态正常的优惠券
		
		//获取店铺id
		String store_id = result.get("store_id").toString();
		
		//获取手机号码
		String mobile = result.get("mobile").toString();
		
		
		sql += " order by create_date desc";
		
		Page page = this.daoSupport.queryForPage(sql, pageNo, pageSize,store_id);
		page.setCurrentPageNo(pageNo);
		
		if(page.getResult() == null ){
			return page;
		}
		
		List list = (List) page.getResult();
		Map map = new HashMap();
		
		
		//遍历获取的当前页的记录
		for (Object object : list) {
			 map = (Map) object;
			 //获取优惠券id
			 Integer coupons_id = org.apache.commons.lang3.math.NumberUtils.toInt(map.get("coupons_id").toString());
			 //根据会员手机号码、优惠券id和会员手机号码   获取会员领取该优惠券数量
			 int receivedNum = this.memberCouponsManager.getMemberCouponsNum(coupons_id, Integer.parseInt(store_id), mobile);
			
			 //判断领用状态
			 if(receivedNum == 0){  
				 map.put("receive_state", 0);  //未领取
			 }else{
				 map.put("receive_state", 1);   //已领取
			 }
			 //设置优惠券状态
			 String stateStr = this.getCurrentStatus((Long)(map.get("start_date")), (Long)(map.get("end_date")), (Integer)(map.get("status")));
             map.put("state", stateStr);
		}
		return page;
	}

	/* (non Javadoc) 
	 *  
	 * @see com.enation.app.shop.mobile.service.IApiCouponsManager#updateStatus() 
	 * @author： liuyulei
	 * @date：2016年10月24日 下午12:18:47
	 */
	@Override
	public void  updateStatus(){
		//如果优惠券超过有效期   修改状态为失效
		String sql = "UPDATE es_member_coupons SET status = 2 WHERE mcoup_end_date < ?";
		this.daoSupport.execute(sql, System.currentTimeMillis() / 1000);
	}

	/* (non Javadoc) 
	 * @param pageNo
	 * @param pageSize
	 * @param params
	 * @return 
	 * @see com.enation.app.shop.mobile.service.IApiCouponsManager#getMemberCouponsList(int, int, java.util.Map) 
	 * @author： liuyulei
	 * @date：2016年10月24日 下午12:19:26
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Page getMemberCouponsList(int pageNo, int pageSize, Map params) {
		String sql = "select c.coupons_id,c.mcoup_name,c.coupons_describe,c.mcoup_money,c.mcoup_discount,c.mcoup_type,c.mcoup_code,c.min_order_money,c.mcoup_start_date,c.mcoup_end_date,"
				+ " c.status,s.store_id,s.store_name,c.is_used "
				+ " from es_member_coupons c "
				+ " left join es_store_coupons sc on sc.coupons_id = c.coupons_id "
				+ " left join es_store s on s.store_id = c.store_id where sc.status = 0 ";
		List<Object> paramsList = new ArrayList<Object>();
		//获取会员手机号码   
		String mobile = params.get("mobile") == null ? "-1" : params.get("mobile").toString();
		
		//获取参数列表中的优惠券状态
		Integer status = Integer.parseInt(params.get("status").toString());
		
		//判断手机号码是否为空
		if(!StringUtil.isEmpty(mobile)){
			sql += " and c.member_mobile = ? ";
			paramsList.add(mobile);
		}
		
		//判断使用状态是否传参
		if(status != -1){
				if(status != 2){  // 使用状态    
					sql += " and c.is_used = ? ";
					paramsList.add(status);
				}
				
				if(status == 2){  // 判断是否超过有效期
					sql += " and c.mcoup_end_date < ?";
					paramsList.add(System.currentTimeMillis() / 1000);
				}
		}
		sql += " order by c.received_date desc";
		int size = paramsList.size();
		Object[] args = paramsList.toArray(new Object[size]);
		Page page = this.daoSupport.queryForPage(sql, pageNo, pageSize,args);
		page.setCurrentPageNo(pageNo);
		List list = (List) page.getResult();
		Map map = new HashMap();
		// 遍历获取的当前页的记录
		for (int i = 0; i < list.size(); i++) {
			map = (Map) list.get(i);
			String stateStr = this.getCurrentStatus((Long) (map.get("mcoup_start_date")),
					(Long) (map.get("mcoup_end_date")), (Integer) (map.get("status")));
			map.put("state", stateStr);
			
			if("超过有效期".equals(stateStr)) {
				map.put("status", 2);
			}
		}
		return page;
	}

	/* (non Javadoc) 
	 * @param coupons_id
	 * @return 
	 * @see com.enation.app.shop.mobile.service.IApiCouponsManager#get(java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年10月24日 下午12:21:06
	 */
	@SuppressWarnings("rawtypes")
	@Override
	public Map get(Integer coupons_id) {
		String sql = "select c.coupons_id,c.coupons_type,c.coupons_money,c.coupons_discount,c.coupons_describe,c.min_order_money,c.start_date,"
				+ " c.end_date,c.status,s.store_id,s.store_name,"
				+ " s.attr,s.tel,s.business_start_hours,s.business_end_hours "
				+ " from es_store_coupons c  "
				+ " left join es_store s on s.store_id = c.store_id "
				+ " where 1=1 and c.is_deleted = 0 and c.coupons_id = ?";
		return this.daoSupport.queryForMap(sql, coupons_id);
	}

	/* (non Javadoc) 
	 * @param coupons_id
	 * @param store_id
	 * @param member_mobile
	 * @param get_type 
	 * @see com.enation.app.shop.mobile.service.IApiCouponsManager#receive(java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年10月24日 下午12:21:12
	 */
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void receive(Integer coupons_id, Integer store_id, String member_mobile, Integer get_type,Integer member_id,String member_name) {
		String sql = "select * from es_member_coupons where is_received = 0 and is_used = 0 and status = 0 and "
				+ "is_deleted = 0 and mcoup_end_date > ? and coupons_id = ? and store_id = ?";
		//根据优惠券id和店铺id   查询 可用优惠券列表
		List<MemberCoupons> couponsList = this.daoSupport.queryForList(sql, MemberCoupons.class, DateUtil.getDateline(),
				coupons_id, store_id);

		//判断了用优惠券是否为空
		if (couponsList != null && couponsList.size() > 0) {
			//如果不为空
			MemberCoupons memberCoupons = couponsList.get(0);
			//设置会员领取优惠券
			sql = "update es_member_coupons set is_received = 1,received_date = ?,member_mobile = ?,member_id = ?,member_name = ?,get_type = ? where mcoup_id = ?";
			this.daoSupport.execute(sql, DateUtil.getDateline(), member_mobile,member_id,member_name,get_type, 
					memberCoupons.getMcoup_id());
			//修改优惠券领用数量
			sql = "update es_store_coupons set received_num = (received_num + 1) where coupons_id = ? and store_id = ?";
			this.daoSupport.execute(sql, coupons_id, store_id);
		}

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
        	state = "超过有效期";
        }
        
        if (status.intValue() == 1) {
			state = "已失效";
		}
        
        return state;
    }
	
}
