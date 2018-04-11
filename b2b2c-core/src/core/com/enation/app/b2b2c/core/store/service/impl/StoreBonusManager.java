package com.enation.app.b2b2c.core.store.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.b2b2c.core.store.model.StoreBonus;
import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.b2b2c.core.store.service.IStoreBonusManager;
import com.enation.eop.sdk.database.BaseSupport;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.StringUtil;

/**
 * 店铺优惠卷查询
 * @author xulipeng
 *
 */
@Service("storeBonusManager")
public class StoreBonusManager  implements IStoreBonusManager {
	
	@Autowired
	private IDaoSupport daoSupport;
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.IStoreBonusManager#getBonusList(java.lang.Integer)
	 */
	@Override
	public List getBonusList(Integer store_id) {
		List list = this.daoSupport.queryForList("select * from es_bonus_type where store_id=?", store_id);
		return list;
	}

	@Override
	public List<Map> getMemberBonusList(Integer memberid, Integer store_id,Double min_goods_amount) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		String date = format.format(new Date());
		long l = DateUtil.getDateline(date+" 23:59:59");
		
		StringBuffer sql = new StringBuffer("select * from es_member_bonus m left join es_bonus_type b on b.type_id = m.bonus_type_id  where m.used=0 ");
		sql.append(" and m.member_id="+memberid);
		sql.append(" and b.store_id="+store_id);
		sql.append(" and b.min_goods_amount<="+min_goods_amount);
		sql.append(" and b.use_start_date <=" +l);
		sql.append(" and b.use_end_date>="+l);
		
//		System.out.println(sql.toString());
		List list = this.daoSupport.queryForList(sql.toString());
		return list;
	}

	@Override
	public StoreBonus get(Integer bonusid) {
		String sql ="select * from es_bonus_type where type_id=?";
		StoreBonus bonus = (StoreBonus) this.daoSupport.queryForObject(sql, StoreBonus.class, bonusid);
		return bonus;
	}

	@Override
	public Page getBonusListBymemberid(int pageNo,int pageSize,Integer memberid) { 

		
		String sql = "select m.*,b.type_id,b.type_money,b.send_type,b.min_amount,b.max_amount,b.send_start_date,b.send_end_date,b.use_start_date,"
				+"b.use_end_date,b.min_goods_amount,b.use_num,b.create_num,b.recognition"
				+",s.store_name from es_member_bonus m left join es_bonus_type b on b.type_id = m.bonus_type_id"
				+ " left join es_store s on b.store_id=s.store_id where m.member_id="+memberid;
		Page webPage = this.daoSupport.queryForPage(sql, pageNo, pageSize);
		return webPage;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void setBonusUsed(Integer bonus_id,Integer member_id) {
		this.daoSupport.execute("update es_member_bonus set used=1 where bonus_type_id="+bonus_id+" and member_id="+member_id);
		this.daoSupport.execute("update es_bonus_type set use_num=(use_num+1) where type_id="+bonus_id);
	}
	
	
	@Override
	public Page getConditionBonusList(Integer pageNo, Integer pageSize,Integer store_id,Map map){
		String keyword=String.valueOf(map.get("keyword"));
		String add_time_from=String.valueOf(map.get("add_time_from"));
		String add_time_to=String.valueOf(map.get("add_time_to"));
		StringBuffer sql =new StringBuffer("select * from es_bonus_type where store_id= "+ store_id);
		String sign_time=String.valueOf(map.get("sign_time"));
		
		if(!StringUtil.isEmpty(keyword)&&!keyword.equals("null")){
			sql.append(" AND type_name like '%" + keyword + "%'");
		}
		
		if(!StringUtil.isEmpty(add_time_from)&&!add_time_from.equals("null")){
			sql.append(" AND use_start_date >"+DateUtil.getDateline(add_time_from));
		}
		if(!StringUtil.isEmpty(add_time_to)&&!add_time_to.equals("null")){
			sql.append(" AND use_end_date <"+DateUtil.getDateline(add_time_to));
		}
		if(!StringUtil.isEmpty(sign_time)&&!sign_time.equals("null")){
			sql.append(" AND use_end_date >"+DateUtil.getDateline(sign_time));
		}
		sql.append(" order by type_id ");
		Page rpage = this.daoSupport.queryForPage(sql.toString(),pageNo, pageSize, StoreBonus.class);
		 
		return rpage;
		
	}

	@Override
	public StoreCoupons getCoupons(Integer coupons_id) {
		String sql = "select * from es_store_coupons where coupons_id = ?";
		return (StoreCoupons) this.daoSupport.queryForObject(sql, StoreCoupons.class, coupons_id);
	}

}
