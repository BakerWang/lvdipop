package com.enation.app.shop.mobile.action.store;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.core.store.service.IStoreClassifyManager;
import com.enation.app.shop.mobile.action.goods.AdvMobileApiController;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;

/**
 * 
 * @ClassName: StoreClassifyMobileApiController 
 * @Description: 店铺分类API
 * 				 店铺分类（标签）
 * @author: liuyulei
 * @date: 2016年10月10日 下午7:10:04 
 * @since:v61
 */
@Controller
@Scope("prototype")
@RequestMapping("/api/mobile/store/classify")
public class StoreClassifyMobileApiController {
	protected final Logger logger = Logger.getLogger(AdvMobileApiController.class);
	@Autowired
	private IStoreClassifyManager storeClassifyManager;
	
	
	/**
	 * 
	 * @Title: classifyList 
	 * @Description: TODO  获取店铺分类列表
	 * @param   city_name	[String]	城市名称
	 * @param   longitude	[Double]	经度
	 * @param   latitude	[Double]	纬度
	 * 
	 * @return  返回    分类id     分类名称
	 * @return: JsonResult
	 * @author： liuyulei
	 * @date：2016年10月10日 下午7:18:53
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(value="/classify-list",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult classifyList() {
		try {
			Map result = new HashMap();
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String city_name  =request.getParameter("city_name");
			String longitude  =request.getParameter("longitude");
			String latitude  =request.getParameter("latitude");
			result.put("city_name", city_name);
			result.put("longitude", longitude);
			result.put("latitude", latitude);
			List list = storeClassifyManager.getStoreClassifyList(result);
			return JsonResultUtil.getObjectJson(list);
		} catch (RuntimeException e) {
			if (this.logger.isDebugEnabled()) {
				this.logger.error(e.getStackTrace());
			}
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("获取店铺分类失败！");
		}
	}
	
}
