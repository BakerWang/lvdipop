package com.enation.app.javashop.solr.plugin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.javashop.solr.service.IGoodsIndexManager;
import com.enation.app.shop.core.goods.plugin.IGoodsAfterAddEvent;
import com.enation.app.shop.core.goods.plugin.IGoodsAfterEditEvent;
import com.enation.app.shop.core.goods.plugin.IGoodsDeleteEvent;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.plugin.AutoRegisterPlugin;
import com.enation.framework.util.StringUtil;

/**
 * 商品索引插件
 * @author kingapex
 *2015-4-16
 */
@Component
public class GoodsIndexPlugin extends AutoRegisterPlugin implements
		IGoodsAfterAddEvent, IGoodsAfterEditEvent,IGoodsDeleteEvent {
	@Autowired
	private IDaoSupport daoSupport;
	@Autowired
	private IGoodsIndexManager goodsIndexManager;
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.plugin.goods.IGoodsAfterEditEvent#onAfterGoodsEdit(java.util.Map, javax.servlet.http.HttpServletRequest)
	 */
	public void onAfterGoodsEdit(Map goods, HttpServletRequest arg1) {
		this.goodsIndexManager.updateIndex(goods);
	}
 
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.plugin.goods.IGoodsAfterAddEvent#onAfterGoodsAdd(java.util.Map, javax.servlet.http.HttpServletRequest)
	 */
	public void onAfterGoodsAdd(Map goods, HttpServletRequest arg1)
			throws RuntimeException {
		this.goodsIndexManager.addIndex(goods);
	}

	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.plugin.goods.IGoodsDeleteEvent#onGoodsDelete(java.lang.Integer[])
	 */
	public void onGoodsDelete(Integer[] ids) {
		String id_str = StringUtil.arrayToString(ids, ",");
		String sql = "select goods_id,name from es_goods  where goods_id in ("
				+ id_str + ")";
		List<Map> goodsList = this.daoSupport.queryForList(sql);
		for (Map map : goodsList) {
			this.goodsIndexManager.deleteIndex(map);
		}
	}
	
	
	

	
	
}
