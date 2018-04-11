/**
 * 版权：Copyright (C) 2015  易族智汇（北京）科技有限公司.
 * 本系统是商用软件,未经授权擅自复制或传播本程序的部分或全部将是非法的.
 * 描述：收藏api  
 * 修改人：  
 * 修改时间：
 * 修改内容：
 */
package com.enation.app.shop.mobile.action.goods;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.enation.app.base.core.model.Member;
import com.enation.app.shop.core.member.model.Favorite;
import com.enation.app.shop.core.member.service.IFavoriteManager;
import com.enation.app.shop.mobile.service.ApiFavoriteManager;
import com.enation.app.shop.mobile.utils.CommonRequest;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.framework.action.JsonResult;
import com.enation.framework.database.Page;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.TestUtil;

/**
 * 收藏API 提供收藏 增删改查
 * 
 * @author hoser
 * @version v1.1 , 2016-04-10
 * @since v1.1
 */
@SuppressWarnings("serial")
@Controller
@Scope("prototype")
@RequestMapping("/api/mobile/favorite")
public class FavoriteMobileApiController {
	protected final Logger logger = Logger.getLogger(FavoriteMobileApiController.class);
	@Autowired
	private IFavoriteManager favoriteManager;
	
	@Autowired
	private ApiFavoriteManager apiFavoriteManager;
	
	private final int PAGE_SIZE = 20;

	/**
	 * 添加收藏
	 * @param id 商品id  必填
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/add.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult add(String id) {
		try {
			Integer member_id = CommonRequest.getMemberID();
			if (member_id == null) {
				return JsonResultUtil.getErrorJson("未登录不能进行此操作！");
			}
			int goods_id = NumberUtils.toInt(id);
			if(apiFavoriteManager.isFavorited(goods_id, member_id)){
				return JsonResultUtil.getSuccessJson("商品已收藏！");
			}
			apiFavoriteManager.add(goods_id, member_id);
			return JsonResultUtil.getSuccessJson("收藏成功！");
		} catch (RuntimeException e) {
			TestUtil.print(e);
			this.logger.error("添加收藏出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
		
	}

	/**
	 * 删除一个会员收藏
	 * 
	 * get参数favoriteid ：要删除的会员收藏地址id,String型 返回的json : result 为1表示添加成功，0表示失败
	 * ，int型 返回的json : message 为提示信息 ，String型
	 */
	@ResponseBody
	@RequestMapping(value="/delete.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult delete(HttpServletRequest request) {
		try {
			Integer member_id = CommonRequest.getMemberID();
			if (member_id == null) {
				return JsonResultUtil.getErrorJson("未登录不能进行此操作！");
			}
			int favorite_id = NumberUtils.toInt(request.getParameter("favoriteId"), 0);
			if (favorite_id > 0) {
				try {
					this.apiFavoriteManager.delete(favorite_id, member_id);
					return JsonResultUtil.getSuccessJson("删除成功");
				} catch (Exception e) {
					if (this.logger.isDebugEnabled()) {
						logger.error(e.getStackTrace());
					}
					return JsonResultUtil.getErrorJson("删除失败！");
				}
			}

			int goods_id = NumberUtils.toInt(request.getParameter("id"), 0);
			if (goods_id > 0) {
				Favorite favorite = apiFavoriteManager.get(goods_id, member_id);
				if (favorite == null) {
					return JsonResultUtil.getErrorJson("您没有权限进行此项操作！");
				}
				try {
					this.apiFavoriteManager.delete(goods_id, member_id);
					return JsonResultUtil.getSuccessJson("删除成功");
				} catch (Exception e) {
					if (this.logger.isDebugEnabled()) {
						logger.error(e.getStackTrace());
					}
					return JsonResultUtil.getErrorJson("删除失败！");
				}
			}
			return JsonResultUtil.getSuccessJson("删除成功");
		} catch (RuntimeException e) {
			this.logger.error("删除出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage() + "");
		}
	}
	
	
	@ResponseBody
	@RequestMapping(value = "/list", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonResult list(@RequestParam(value = "page", required = false, defaultValue = "1") Integer page
			, @RequestParam(value = "pageSize", required = false, defaultValue = "10") Integer pageSize) {
		Integer member_id = CommonRequest.getMemberID();
		if (member_id == null) {
			return JsonResultUtil.getErrorJson("请登录后再进行此项操作！");
		}
		if (page == null || page <= 0)
			page = 1;

		Page webpage = apiFavoriteManager.list(member_id, page, pageSize);
		List list = (List) webpage.getResult();
		for (int i = 0; i < list.size(); i++) {
			Map map = (Map) list.get(i);
			map.put("thumbnail", StaticResourcesUtil.convertToUrl(map.get("thumbnail").toString()));
			map.put("small", StaticResourcesUtil.convertToUrl(map.get("small").toString()));
			map.put("big", StaticResourcesUtil.convertToUrl(map.get("big").toString()));
		}
		webpage.setCurrentPageNo(page);
		return JsonResultUtil.getObjectJson(webpage);
	}


}
