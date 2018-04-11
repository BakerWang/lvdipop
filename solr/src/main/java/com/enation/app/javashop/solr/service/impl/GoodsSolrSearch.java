/**
 * 
 */
package com.enation.app.javashop.solr.service.impl;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.enation.app.javashop.solr.service.IGoodsIndexManager;
import com.enation.app.shop.core.goods.service.IGoodsSearchManager;
import com.enation.framework.database.Page;

/**
 * 基于lucene全文检索的搜索器
 * @author zh
 * @version v1.0
 * @since v6.1
 * 2016年10月13日 下午12:09:38
 */
@Component
public class GoodsSolrSearch implements IGoodsSearchManager {
	
	private IGoodsIndexManager goodsIndexManager;
	
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.service.IGoodsSearchManager#getSelector(com.enation.app.shop.core.model.Cat)
	 */
	public Map<String,Object> getSelector() {
		return this.goodsIndexManager.createSelector();
	}
 
	/* (non-Javadoc)
	 * @see com.enation.app.shop.core.service.IGoodsSearchManager#search(int, int)
	 */
	public Page search(int pageNo,int pageSize) {
		return this.goodsIndexManager.search(pageNo, pageSize);
	}

	public IGoodsIndexManager getGoodsIndexManager() {
		return goodsIndexManager;
	}

	public void setGoodsIndexManager(IGoodsIndexManager goodsIndexManager) {
		this.goodsIndexManager = goodsIndexManager;
	}
 

}
