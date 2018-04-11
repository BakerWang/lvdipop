package com.enation.app.service.component.plugin;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.shop.ShopApp;
import com.enation.app.shop.core.goods.model.Cat;
import com.enation.app.shop.core.goods.plugin.IGetGoodsAddHtmlEvent;
import com.enation.app.shop.core.goods.plugin.IGetGoodsEditHtmlEvent;
import com.enation.app.shop.core.goods.plugin.IGoodsAfterAddEvent;
import com.enation.app.shop.core.goods.plugin.IGoodsTabShowEvent;
import com.enation.app.shop.core.goods.service.IGoodsCatManager;
import com.enation.eop.processor.core.freemarker.FreeMarkerPaser;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.framework.plugin.AutoRegisterPlugin;
import com.enation.framework.util.StringUtil;

/**
 * 
 * @ClassName: GoodsServicePlugin
 * @Description: 服务商品插件
 * @author: liuyulei
 * @date: 2016年9月14日 上午9:48:49
 */
@Component
public class GoodsServicePlugin extends AutoRegisterPlugin
		implements IGetGoodsAddHtmlEvent, IGetGoodsEditHtmlEvent, IGoodsAfterAddEvent, IGoodsTabShowEvent {

	@Autowired
	private IGoodsCatManager goodsCatManager;

	
	/**
	 * 商品添加     商品类型tab页面
	 */
	@Override
	public String getAddHtml(HttpServletRequest request) {

		// 由request中读取所属分类id
		int catid = StringUtil.toInt(request.getParameter("catid"), true);

		// 找到所有的父，以便在页面显示
		List<Cat> parentList = this.goodsCatManager.getParents(catid);

		// 找到当前的父，以便确定商品类型id
		Cat currentCat = parentList.get(parentList.size() - 1);

		FreeMarkerPaser freeMarkerPaser = FreeMarkerPaser.getInstance();
		freeMarkerPaser.setPageName("servicegoods_add");
		freeMarkerPaser.putData("typeid", currentCat.getType_id());
		freeMarkerPaser.putData("catid", catid);

		freeMarkerPaser.putData("optype", request.getParameter("optype")); // 操作类型
		freeMarkerPaser.putData("parentList", parentList);

		if ("b2b2c".equals(EopSetting.PRODUCT)) { //如果是多店
			freeMarkerPaser.putData("self_store", "yes");
			freeMarkerPaser.putData("store_id", ShopApp.self_storeid);
		}
		return freeMarkerPaser.proessPageContent();
	}

	/**
	 * 商品修改     商品类型tab页面
	 */
	@Override
	public String getEditHtml(Map goods, HttpServletRequest request) {

		int catid = StringUtil.toInt(goods.get("cat_id").toString(), true);

		// 找到所有的父，以便在页面显示
		List<Cat> parentList = this.goodsCatManager.getParents(catid);

		// 找到当前的父，以便确定商品类型id
		Cat currentCat = parentList.get(parentList.size() - 1);

		FreeMarkerPaser freeMarkerPaser = FreeMarkerPaser.getInstance();
		freeMarkerPaser.putData("goodsView", goods);
		freeMarkerPaser.putData("parentList", parentList);
		freeMarkerPaser.putData("typeid", currentCat.getType_id());
		freeMarkerPaser.putData("catid", catid);
		freeMarkerPaser.putData("optype", request.getParameter("optype"));
		freeMarkerPaser.setPageName("servicegoods_edit");
		if ("b2b2c".equals(EopSetting.PRODUCT)) {  //如果是多店
			freeMarkerPaser.putData("self_store", "yes");
			freeMarkerPaser.putData("store_id", ShopApp.self_storeid);
		}
		return freeMarkerPaser.proessPageContent();
	}

	@Override
	public void onAfterGoodsAdd(Map goods, HttpServletRequest request) throws RuntimeException {
		
	}

	@Override
	public String getTabName() {
		// TODO Auto-generated method stub
		return "类型";
	}

	@Override
	public int getOrder() {
		// TODO Auto-generated method stub
		return 6;
	}

}
