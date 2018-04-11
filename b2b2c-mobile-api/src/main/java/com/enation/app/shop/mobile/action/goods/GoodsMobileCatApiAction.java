/**
 * 版权：Copyright (C) 2015  易族智汇（北京）科技有限公司.
 * 本系统是商用软件,未经授权擅自复制或传播本程序的部分或全部将是非法的.
 * 描述：商品api  
 * 修改人：  
 * 修改时间：
 * 修改内容：
 */
package com.enation.app.shop.mobile.action.goods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.shop.core.goods.model.Cat;
import com.enation.app.shop.core.goods.service.IGoodsCatManager;
import com.enation.app.shop.core.goods.service.impl.GoodsManager;
import com.enation.app.shop.mobile.model.ApiCat;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * 商品分类api 获取商品分类
 * 
 * @author hoser
 * @version v1.1 , 2016-04-10
 * @since v1.1
 */
@SuppressWarnings("serial")
@Controller
@Scope("prototype")
@RequestMapping("/api/mobile/goodscat")
public class GoodsMobileCatApiAction {
	protected final Logger logger = Logger.getLogger(GoodsMobileCatApiAction.class);
	
	@Autowired
	private IGoodsCatManager goodsCatManager;

	/**
	 * 获取商品分类
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/list.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult list(String level) {
		try {
			List<Cat> cat_tree = goodsCatManager.listAllChildren(0);
			List<ApiCat> apiCatTree = new ArrayList<ApiCat>();
			List<Cat> levelCatTree = new ArrayList<Cat>();
			if(StringUtils.isEmpty(level)){
				for (Cat cat : cat_tree) {
					apiCatTree.add(toApiCat(cat));
				}
				return JsonResultUtil.getObjectJson(apiCatTree);
			}else{
				for(Cat cat : cat_tree){
					getCatByLevel(cat,Integer.valueOf(level),levelCatTree);
				}
				return JsonResultUtil.getObjectJson(levelCatTree);
			}

		} catch (RuntimeException e) {
			this.logger.error("获取商品分类出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
			
		}
	}

	/**
	 * 把Cat转换为ApiCat
	 * 
	 * @param cat
	 * @return
	 */
	private ApiCat toApiCat(Cat cat) {
		ApiCat apiCat = new ApiCat();
		apiCat.setName(cat.getName());
		apiCat.setCat_id(cat.getCat_id());
		apiCat.setImage(cat.getImage());
		apiCat.setLevel(cat.getCat_path().split("\\|").length - 1);
		apiCat.setParent_id(cat.getParent_id());
		if (cat.getHasChildren()) {
			for (Cat subcat : cat.getChildren()) {
				apiCat.getChildren().add(toApiCat(subcat));
			}
		}
		return apiCat;
	}
	
	private Cat getCatByLevel(Cat cat,Integer level,List<Cat> result){
		if (cat.getHasChildren()) {
			for (Cat subcat : cat.getChildren()) {
				if(subcat.getCat_path().split("\\|").length - 1 == level){
					result.add(subcat);
				}else{
					return getCatByLevel(subcat, level,result);
				}
			}
		}else{
			if(cat.getCat_path().split("\\|").length - 1 == level){
				result.add(cat);
			}
		}
		return null;
	}

	

	/**
	 * 获得商品分类
	 * 
	 * @author Sylow
	 * @param <b>parentid</b>:父级id.int型,可为空
	 * @param <b>catimage</b>:大概是
	 *            是否显示图片的参数 我也没看太懂，String型,可为空, on表示是
	 * @return 返回json串 <br />
	 *         <b>result</b>: 1表示添加成功0表示失败 ，int型 <br />
	 *         <b>message</b>: 提示信息 <br />
	 *         <b>data</b>: 商品分类数据
	 */
	@ResponseBody
	@RequestMapping(value="/goods-cat-list.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult goodsCatList(HttpServletRequest request) {
		try {
			String parentIdStr = request.getParameter("parentid");
			int parentid = 0;
			if (parentIdStr != null) {
				parentid = Integer.parseInt(parentIdStr);
			}
			List<Cat> cat_tree = goodsCatManager.listAllChildren(parentid);
			String catimage = request.getParameter("catimage");
			boolean showimage = catimage != null && catimage.equals("on") ? true : false;

			String imgPath = "";
			if (!cat_tree.isEmpty()) {
				for (Cat cat : cat_tree) {

					if (cat.getImage() != null && !StringUtil.isEmpty(cat.getImage())) {
						imgPath = StaticResourcesUtil.convertToUrl(cat.getImage());
						cat.setImage(imgPath);
					}

				}
			}

			Map<String, Object> data = new HashMap<String, Object>();
			data.put("showimg", showimage);// 是否显示分类图片
			data.put("cat_tree", cat_tree);// 分类列表数据
			return JsonResultUtil.getObjectJson(data);
		} catch (RuntimeException e) {
			this.logger.error("获取商品分类出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}

	
	@ResponseBody
	@RequestMapping(value="/goods-cat-level.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult goodsCatLevel(Integer level) {
		try{
			if(level == null || level.equals(""))
				level = 0;
			List<Map> listCat = goodsCatManager.listCatByLevel(level);
			return JsonResultUtil.getObjectJson(listCat);
	 	} catch (RuntimeException e) {
			this.logger.error("获取商品分类出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}
	
}
