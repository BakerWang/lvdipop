/**
 *  版权：Copyright (C) 2015  易族智汇（北京）科技有限公司.
 *  本系统是商用软件,未经授权擅自复制或传播本程序的部分或全部将是非法的.
 *  描述：商品评论插件
 *  修改人：Sylow
 *  修改时间：2015-11-05
 *  修改内容：制定初版
 */
package com.enation.app.javashop.solr.plugin;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.chenlb.mmseg4j.analysis.MMSegAnalyzer;
import com.enation.app.javashop.solr.service.IGoodsIndexManager;
import com.enation.app.shop.core.goods.plugin.IGoodsCommentsAddEvent;
import com.enation.app.shop.core.goods.service.IGoodsManager;
import com.enation.app.shop.core.member.model.MemberComment;
import com.enation.framework.plugin.AutoRegisterPlugin;


/**
 * 商品评论插件
 * @author Sylow
 * @version v1.0,2015-11-05
 * @since v5.2
 */
@Component
public class GoodsCommentsPlugin extends AutoRegisterPlugin implements IGoodsCommentsAddEvent {
	@Autowired
	private IGoodsManager goodsManager;
	@Autowired
	private IGoodsIndexManager goodsIndexManager;
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.plugin.goods.IGoodsCommentsAddEvent#beforeAdd(com.enation.app.shop.core.model.Comments)
	 */
	public void beforeAdd(MemberComment memberComment) {
		// TODO Auto-generated method stub
		MMSegAnalyzer a;
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.plugin.goods.IGoodsCommentsAddEvent#afterAdd(com.enation.app.shop.core.model.Comments)
	 */
	public void afterAdd(MemberComment memberComment) {
		// TODO Auto-generated method stub
		MMSegAnalyzer analyzer = null;
		int goodsId = memberComment.getGoods_id();
		Map<String, Object> goods = this.goodsManager.get(goodsId);
		this.goodsIndexManager.updateIndex(goods);
		
	}

	
}
