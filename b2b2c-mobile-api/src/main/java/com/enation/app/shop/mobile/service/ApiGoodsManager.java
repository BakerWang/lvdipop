package com.enation.app.shop.mobile.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.enation.app.shop.core.goods.model.Cat;
import com.enation.app.shop.core.goods.service.IGoodsCatManager;
import com.enation.eop.sdk.database.BaseSupport;
import com.enation.framework.database.Page;

@Service
public class ApiGoodsManager extends BaseSupport {
	private IGoodsCatManager goodsCatManager;
	
	public List listByCat(Integer catid, int number) {
		String sql = "SELECT g.* from " + this.getTableName("goods") + " g WHERE g.disabled=0 and g.market_enable=1 ";
		if (catid.intValue() != 0) {
			Cat cat = this.goodsCatManager.getById(catid);
			sql += " and  g.cat_id in(";
			sql += "select c.cat_id from " + this.getTableName("goods_cat")
					+ " c where c.cat_path like '" + cat.getCat_path()
					+ "%') ";
		}
		sql += " ORDER BY g.goods_id asc limit " + number;
		return this.daoSupport.queryForList(sql);
	}
	
	public Map getSeckillGoods(int goodsid, int act_id){
		return this.daoSupport.queryForMap("SELECT * FROM " + getTableName("seckill_goods") + " WHERE goods_id=? AND act_id=?", goodsid, act_id);
	}
	
	public Page getSeckillGoodsList(int pageNo,int pageSize){
		long timestamp = System.currentTimeMillis() / 1000;
		List goodslist = this.daoSupport.queryForListPage("select g.* from " + getTableName("tag_rel") + " r LEFT JOIN " + getTableName("goods") 
				+ " g ON g.goods_id=r.rel_id where g.disabled=0 and g.market_enable=1 AND r.tag_id in (SELECT act_tag_id FROM " + getTableName("seckill_active") 
				+ " WHERE start_time<? AND end_time>?) order by r.ordernum desc", pageNo, pageSize, timestamp, timestamp);
		
		int count = this.daoSupport.queryForInt("select COUNT(0) from " + getTableName("tag_rel") + " r LEFT JOIN " + getTableName("goods") 
				+ " g ON g.goods_id=r.rel_id where g.disabled=0 and g.market_enable=1 AND r.tag_id in (SELECT act_tag_id FROM " + getTableName("seckill_active") 
				+ " WHERE start_time<? AND end_time>?) order by r.ordernum desc", timestamp, timestamp);
		return new Page(0, count, pageSize, goodslist);
	}
	
	/**
	 * 获取团购商品详情
	 * @param goodsid
	 * @param groupby_id
	 * @return
	 */
	public Map getGroupbuyGoods(int goodsid, int groupby_id){
		return this.daoSupport.queryForMap("SELECT * FROM " + getTableName("groupbuy_goods") + " WHERE goods_id=? AND act_id=?", goodsid, groupby_id);
	}
	
	public int getStore(int productId){
		return this.daoSupport.queryForInt("SELECT SUM(store) FROM " + getTableName("product_store") + " WHERE productid=?", productId);
	}

	public IGoodsCatManager getGoodsCatManager() {
		return goodsCatManager;
	}

	public void setGoodsCatManager(IGoodsCatManager goodsCatManager) {
		this.goodsCatManager = goodsCatManager;
	}
	
}
