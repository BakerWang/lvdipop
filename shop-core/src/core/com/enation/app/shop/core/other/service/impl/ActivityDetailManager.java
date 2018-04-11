package com.enation.app.shop.core.other.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.shop.core.other.model.ActivityDetail;
import com.enation.app.shop.core.other.service.IActivityDetailManager;
import com.enation.framework.database.IDaoSupport;

/**
 * 促销活动优惠详细管理接口实现类
 * 2016-5-27
 * @author DMRain
 * @version 1.0
 */
@Service("activityDetailManager")
public class ActivityDetailManager implements IActivityDetailManager{

	@Autowired
	private IDaoSupport<ActivityDetail> daoSupport;
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityDetailManager#add(com.enation.app.shop.core.other.model.ActivityDetail)
	 */
	@Override
	public void add(ActivityDetail detail) {
		this.daoSupport.insert("es_activity_detail", detail);
		
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityDetailManager#edit(com.enation.app.shop.core.other.model.ActivityDetail)
	 */
	@Override
	public void edit(ActivityDetail detail) {
		this.daoSupport.update("es_activity_detail", detail, "detail_id="+detail.getDetail_id());
		
	}

	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityDetailManager#get(java.lang.Integer)
	 */
	@Override
	public ActivityDetail get(Integer detail_id) {
		String sql = "select * from es_activity_detail where detail_id = ?";
		return this.daoSupport.queryForObject(sql, ActivityDetail.class, detail_id);
	}

	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityDetailManager#getDetail(java.lang.Integer)
	 */
	@Override
	public ActivityDetail getDetail(Integer activity_id) {
		String sql = "select * from es_activity_detail where activity_id = ?";
		return this.daoSupport.queryForObject(sql, ActivityDetail.class, activity_id);
	}
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.other.service.IActivityDetailManager#listDetail(java.lang.Integer)
	 */
	@Override
	public List listDetail(Integer activity_id) {
		String sql = "select * from es_activity_detail where activity_id = ?";
		List list = this.daoSupport.queryForList(sql, activity_id);
		return list;
	}


}
