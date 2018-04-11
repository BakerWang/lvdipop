package com.enation.app.javashop.solr.plugin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.javashop.solr.service.IGoodsIndexManager;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.plugin.order.IOrderRogconfirmEvent;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.plugin.AutoRegisterPlugin;

/**
 * 订单插件
 * @author Sylow
 * @version v1.0,2015-11-18
 * @since v5.2
 */
@Component
public class OrderPlugin extends AutoRegisterPlugin implements IOrderRogconfirmEvent {

	@Autowired
	private IDaoSupport daoSupport;
	@Autowired
	private IGoodsIndexManager goodsIndexManager;
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.plugin.order.IOrderRogconfirmEvent#rogConfirm(com.enation.app.shop.core.model.Order)
	 */
	public void rogConfirm(Order order) {
		// TODO Auto-generated method stub
		
		String getGoodsIdsSql = "SELECT i.goods_id FROM es_order_items i WHERE i.order_id = " + order.getOrder_id();
		List<Map> list = daoSupport.queryForList(getGoodsIdsSql);
		
		//遍历goodsId
		for (Map map : list) {
			String goodsId = map.get("goods_id").toString();
			String getGoodsSql = "SELECT * FROM es_goods g WHERE g.goods_id = " + goodsId;
			Map goods = daoSupport.queryForMap(getGoodsSql);
			goodsIndexManager.updateIndex(goods);
		}
		
		
	}

	
}
