/**
 * 版权：Copyright (C) 2015  易族智汇（北京）科技有限公司.
 * 本系统是商用软件,未经授权擅自复制或传播本程序的部分或全部将是非法的.
 * 描述：地区api
 * 修改人：Sylow  
 * 修改时间：
 * 修改内容：
 */
package com.enation.app.shop.mobile.action.member;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.base.core.service.IRegionsManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;

/**
 * 地区api
 * @author Sylow
 * @version v1.0 , 2015-08-24
 * @since v1.0
 */
@Scope("prototype")
@Controller
@RequestMapping("/api/mobile/region")
public class RegionMobileApiController{
	protected final Logger logger = Logger.getLogger(getClass());
	@Autowired
	private IRegionsManager regionsManager;

	/**
	 * 省级单位列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/list-province",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult listProvince(){

		try {
			List list = new ArrayList();
			list = regionsManager.listProvince();
			return JsonResultUtil.getObjectJson(list);
		} catch(RuntimeException e) {
			this.logger.error("获取省级地区列表出错", e);
			return JsonResultUtil.getErrorJson("获取省级地区列表出错[" + e.getMessage() + "]");
		}
	}

	/**
	 * 根据parentid获取地区列表
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/list",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult list(){
		try {
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			int parentId = NumberUtils.toInt(request.getParameter("parentid"));
			List list =regionsManager.listChildrenByid(parentId);
			return JsonResultUtil.getObjectJson(list);

		} catch (RuntimeException e) {
			this.logger.error("获取地区列表出错", e);
			return JsonResultUtil.getErrorJson("获取地区列表出错[" + e.getMessage() + "]");
		}
	}

	public IRegionsManager getRegionsManager() {
		return regionsManager;
	}

	public void setRegionsManager(IRegionsManager regionsManager) {
		this.regionsManager = regionsManager;
	}
}
