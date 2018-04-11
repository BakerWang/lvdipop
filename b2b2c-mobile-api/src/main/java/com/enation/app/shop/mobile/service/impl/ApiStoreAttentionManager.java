package com.enation.app.shop.mobile.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.shop.mobile.service.IApiStoreAttentionManager;
import com.enation.framework.database.IDaoSupport;

@Service
public class ApiStoreAttentionManager implements IApiStoreAttentionManager {
	@Autowired
	private IDaoSupport daoSupport;
	
	@Override
	public String queryStoreIdByInvokeNum(String invokeNum) {
		return  daoSupport.queryForString("select storeid from es_store_attention_config where invokenum = " + invokeNum);
	}

	@Override
	public boolean hasNewCreateOrder(String storeId) {
		// 普通商品逻辑
		int result = daoSupport.queryForInt("select count(1) from es_order where status = 2 and store_id = ?", storeId);

		// 服务商品逻辑
		/*int serviceResult = daoSupport
				.queryForInt("select count(1) from es_goods_servicegoods where status = 0 and store_id = ?", storeId);*/
		return result >=1 ? true :false;
//		return (result >= 1 ? true : false) || (serviceResult >= 1 ? true : false);
		
	}

}
