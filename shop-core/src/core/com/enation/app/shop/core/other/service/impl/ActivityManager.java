package com.enation.app.shop.core.other.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.shop.core.other.model.Activity;
import com.enation.app.shop.core.other.model.ActivityDetail;
import com.enation.app.shop.core.other.model.ActivityGoods;
import com.enation.app.shop.core.other.service.IActivityDetailManager;
import com.enation.app.shop.core.other.service.IActivityManager;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.StringUtil;

/**
 * 促销活动管理接口实现类
 * 2016-5-23
 * @author DMRain
 * @version 1.0
 */
@Service("activityManager")
public class ActivityManager implements IActivityManager{

	@Autowired
	private IDaoSupport<Activity> daoSupport;
	
	@Autowired
	private IActivityDetailManager activityDetailManager;
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#list(java.util.Map, int, int)
	 */
	@Override
	public Page list(Map actMap, int page, int pageSize) {
		Integer stype = (Integer) actMap.get("stype");
		Integer status = (Integer) actMap.get("status");
		String activity_name = (String) actMap.get("activity_name");
		Integer activity_type = (Integer) actMap.get("activity_type");
		Integer range_type = (Integer) actMap.get("range_type");
		String start_time = (String) actMap.get("start_time");
		String end_time = (String) actMap.get("end_time");
		
		String sql = "select * from es_activity where disabled = 0";
		
		//如果搜索类型不为空
		if (stype != null) {
			
			//如果搜索类型为0，也就是普通搜索
			if (stype == 0) {
				
				//对促销活动状态进行判断 0：全部，1：进行中，2：未开始，3：已结束
				if (status != 0) {
					if (status == 1) {
						sql += " and start_time < " + DateUtil.getDateline() + " and end_time > " + DateUtil.getDateline() + "";
					} else if (status == 2) {
						sql += " and start_time > " + DateUtil.getDateline() + "";
					} else if (status == 3) {
						sql += " and end_time < " + DateUtil.getDateline() + "";
					}
				}
			//如果搜索类型为1，也就是高级搜索
			} else if (stype == 1) {
				
				//促销活动名称不为空
				if (activity_name != null && !StringUtil.isEmpty(activity_name)) {
					sql += " and activity_name like '%" + activity_name + "%'";
				}
				
				//促销活动类型不等于0
				if (activity_type != 0) {
					sql += " and activity_type = " + activity_type + "";
				}
				
				//商品参加促销活动形式不等于0
				if (range_type != 0) {
					sql += " and range_type = " + range_type + "";
				}
				
				//促销活动开始时间不为空
				if (start_time != null && !StringUtil.isEmpty(start_time)) {			
					long stime = DateUtil.getDateline(start_time, "yyyy-MM-dd HH:mm:ss");
					sql += " and start_time >= " + stime;
				}
				
				//促销活动结束时间不为空
				if (end_time != null && !StringUtil.isEmpty(end_time)) {			
					long etime = DateUtil.getDateline(end_time, "yyyy-MM-dd HH:mm:ss");
					sql += " and end_time <= " + etime;
				}
			}
		}
		
		sql += " order by activity_id desc";
		
		Page webpage = this.daoSupport.queryForPage(sql, page, pageSize);
		
		List list = (List) webpage.getResult();
		Map map = new HashMap();
		
		//遍历获取的当前页的记录
		for(int i = 0; i < list.size(); i++){
			map = (Map) list.get(i);
			String statusStr = this.getCurrentStatus((Long)(map.get("start_time")), (Long)(map.get("end_time")));
			
            map.put("status", statusStr);
		}
		
		return webpage;
	}

	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#listGoods(java.lang.String, java.lang.Integer, int, int)
	 */
	@Override
	public Page listGoods(String keyword, Integer activity_id, int page, int pageSize) {
		String sql = "";
		
		//如果促销活动ID不等于0
		if (activity_id != 0) {
			sql = "select g.*,ag.activity_id from es_goods g left join es_activity_goods ag on g.goods_id = ag.goods_id where g.disabled = 0 and g.market_enable = 1 and ag.activity_id = " + activity_id + "";
			int total = this.getGoodsTotal(activity_id);
			
			//如果参加促销活动的商品总数大于10
			if (total > 10) {
				pageSize = total;
			}
			
			sql += " order by g.create_time desc";
		} else {
			sql = "select * from es_goods where disabled = 0 and market_enable = 1";
			
			//如果搜索关键字不为空
			if (keyword != null && !StringUtil.isEmpty(keyword)) {
				sql += " and (name like '%" + keyword + "%' or sn like '%" + keyword + "%')";
			}
			
			sql += " order by create_time desc";
		}
		
		Page webpage = this.daoSupport.queryForPage(sql, page, pageSize);
		return webpage;
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#add(com.enation.app.shop.core.other.model.Activity, com.enation.app.shop.core.other.model.ActivityDetail, java.lang.Integer[])
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void add(Activity activity, ActivityDetail detail, Integer[] goods_ids) {
		this.daoSupport.insert("es_activity", activity);
		Integer activity_id = this.daoSupport.getLastId("es_activity");
		
		detail.setActivity_id(activity_id);
		this.activityDetailManager.add(detail);
		
		//如果商品参加促销活动形式为部分商品参加
		if (activity.getRange_type() == 2) {
			ActivityGoods activityGoods = new ActivityGoods();
			for(int i = 0; i < goods_ids.length; i++){
				activityGoods.setActivity_id(activity_id);
				activityGoods.setGoods_id(goods_ids[i]);
				this.daoSupport.insert("es_activity_goods", activityGoods);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#edit(com.enation.app.shop.core.other.model.Activity, com.enation.app.shop.core.other.model.ActivityDetail, java.lang.Integer[])
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void edit(Activity activity, ActivityDetail detail, Integer[] goods_ids) {
		this.daoSupport.update("es_activity", activity, "activity_id="+activity.getActivity_id());
		
		detail.setIs_full_minus(detail.getIs_full_minus() == null ? 0 : detail.getIs_full_minus());
		detail.setMinus_value(detail.getMinus_value() == null ? 0 : detail.getMinus_value());
		detail.setIs_send_point(detail.getIs_send_point() == null ? 0 : detail.getIs_send_point());
		detail.setPoint_value(detail.getPoint_value() == null ? 0 : detail.getPoint_value());
		detail.setIs_free_ship(detail.getIs_free_ship() == null ? 0 : detail.getIs_free_ship());
		detail.setIs_send_gift(detail.getIs_send_gift() == null ? 0 : detail.getIs_send_gift());
		detail.setIs_send_bonus(detail.getIs_send_bonus() == null ? 0 : detail.getIs_send_bonus());
		this.activityDetailManager.edit(detail);
		
		//如果商品参加促销活动形式为全部商品参加 1：全部商品参加，2：部分商品参加 
		if (activity.getRange_type() == 1) {
			this.deleteGoods(activity.getActivity_id());
		} else if(activity.getRange_type() == 2) {
			this.deleteGoods(activity.getActivity_id());
			ActivityGoods activityGoods = new ActivityGoods();
			for(int i = 0; i < goods_ids.length; i++){
				activityGoods.setActivity_id(activity.getActivity_id());
				activityGoods.setGoods_id(goods_ids[i]);
				this.daoSupport.insert("es_activity_goods", activityGoods);
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#delete(java.lang.Integer)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public void delete(Integer activity_id) {
		String sql = "update es_activity set disabled = 1 where activity_id = ?";
		this.daoSupport.execute(sql, activity_id);
		
		this.deleteGoods(activity_id);
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#get(java.lang.Integer)
	 */
	@Override
	public Activity get(Integer activity_id) {
		String sql = "select * from es_activity where activity_id = ?";
		Activity activity = this.daoSupport.queryForObject(sql, Activity.class, activity_id);
		return activity;
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#checkActByDate(java.lang.Long, java.lang.Long, java.lang.Integer)
	 */
	@Override
	public int checkActByDate(Long startTime, Long endTime, Integer activity_id) {
		Long currentTime = DateUtil.getDateline();	//获取当前时间
		String sql = "select start_time,end_time from es_activity where disabled = 0 and end_time > ?";
		
		//如果促销活动ID不为空或着不等于0
		if(activity_id != null && activity_id != 0){
			sql += " and activity_id != " + activity_id + "";
		}
		
		List<Map> list = this.daoSupport.queryForList(sql, currentTime);
		
		//循环判断添加或修改的促销活动时间是否不于其它促销活动冲突，只要有一个冲突就返回1
		int result = 0;
		if(list.size() != 0){
			for(Map map : list){
				if(startTime >= (Long)map.get("start_time") && startTime <= (Long)map.get("end_time")){
					result = 1;
					break;
				}
				if(endTime >= (Long)map.get("start_time") && endTime <= (Long)map.get("end_time")){
					result = 1;
					break;
				}
			}
		}
		
		return result;
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#getCurrentAct()
	 */
	@Override
	public Activity getCurrentAct() {
		long currentTime = DateUtil.getDateline();
		String sql = "select * from es_activity where start_time < "+currentTime+" and end_time > "+currentTime+" and disabled = 0";
		Activity activity = this.daoSupport.queryForObject(sql, Activity.class);
		return activity;
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#checkGoodsAct(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public int checkGoodsAct(Integer goods_id, Integer activity_id) {
		String sql = "select count(0) from es_activity_goods where goods_id = ? and activity_id = ?";
		int num = this.daoSupport.queryForInt(sql, goods_id, activity_id);
		num = num > 0 ? 1 : 0;
		return num;
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#getActMap(java.lang.Integer)
	 */
	@Override
	public Map getActMap(Integer activity_id) {
		String sql = "select * from es_activity a left join es_activity_detail ad on a.activity_id = ad.activity_id where a.activity_id = ? and a.disabled = 0";
		Map map = this.daoSupport.queryForMap(sql, activity_id);
		return map;
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#getCurrActDetail()
	 */
	@Override
	public List<Map> getCurrActDetail() {
		long currentTime = DateUtil.getDateline();
		String sql = "select * from es_activity a left join es_activity_detail ad on a.activity_id = ad.activity_id where a.start_time < "+currentTime+" and a.end_time > "+currentTime+" and a.disabled = 0";
		List<Map> list = this.daoSupport.queryForList(sql);
		return list;
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityManager#checkActivity(java.lang.Integer)
	 */
	@Override
	public int checkActivity(Integer activity_id) {
		Activity activity = this.get(activity_id);
		if (activity.getDisabled() == 1 || activity.getEnd_time() < DateUtil.getDateline()) {
			return 1;
		}
		return 0;
	}
	
	/**
     * 获取当前促销活动状态
     * @param startTime 活动开始时间
     * @param endTime 活动结束时间
     * @return
     */
    private String getCurrentStatus(Long startTime, Long endTime) {
        String status = "";
        long currentTime = System.currentTimeMillis() / 1000; //获取当前时间秒数
        if (startTime > currentTime) {
            status = "未开始";
        } else if ((startTime <= currentTime) && (endTime >= currentTime)) {
            status = "进行中";
        } else if (endTime < currentTime) {
            status = "已结束";
        }
        return status;
    }

    /**
     * 根据促销活动ID删除参与促销活动的商品
     * @param activity_id 促销活动ID
     */
    private void deleteGoods(Integer activity_id){
    	String sql = "delete from es_activity_goods where activity_id = ?";
    	this.daoSupport.execute(sql, activity_id);
    }

    /**
	 * 获取参加促销活动的商品总数
	 * @param activity_id 促销活动id
	 * @return
	 */
	private int getGoodsTotal(Integer activity_id){
		String sql = "select count(0) from es_activity_goods where activity_id = ?";
		return this.daoSupport.queryForInt(sql, activity_id);
	}

}
