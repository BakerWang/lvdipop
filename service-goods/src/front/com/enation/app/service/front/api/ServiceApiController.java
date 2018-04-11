package com.enation.app.service.front.api;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.service.core.servicegoods.model.ServiceGoods;
import com.enation.app.service.core.servicegoods.service.IServiceGoodsManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonResultUtil;


/**
 * 
 * @ClassName: ServiceApiController 
 * @Description: 卡密信息管理API
 * @author: liuyulei
 * @date: 2016年9月19日 下午3:28:00 
 * @since:v61
 */
@Controller
@RequestMapping("/api/b2b2c/store-goods-service-code")
public class ServiceApiController {
	
	
	@Autowired
	private IServiceGoodsManager serviceGoodsManager;
	
	protected Logger logger=Logger.getLogger(getClass());
	
	/**
	 * 修改卡密状态
	 * @param service_id 卡密Id,Integer型
	 * @param status 卡密状态,Integer型
	 * @return	返回json串
	 * result 	为1表示调用成功0表示失败
	 * 
	 */
	@ResponseBody
	@RequestMapping(value="/edit-status", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult editServiceCodeStatus(Integer service_id,Integer status){
		try {
			
			Map map = new HashMap();
			
			map.put("service_id", service_id);
			Map serviceGoodsMap = this.serviceGoodsManager.getServiceGoods(map);
			
			ServiceGoods serviceGoods = new ServiceGoods();
			serviceGoods.setCode((String)serviceGoodsMap.get("code"));
			serviceGoods.setCreate_time((Long)map.get("create_time"));
			serviceGoods.setEnable_time((Long)map.get("enable_time"));
			serviceGoods.setItem_id((Integer)map.get("item_id"));
			serviceGoods.setValid_term((Integer)map.get("valid_term"));
			serviceGoods.setUseDate(DateUtil.getDateline()+"");
			serviceGoods.setStatus(status);
			serviceGoods.setService_id(service_id);
			this.serviceGoodsManager.edit(serviceGoods);
			return JsonResultUtil.getSuccessJson("修改状态成功");
		} catch (Exception e) {
			this.logger.error("修改状态失败:"+e);
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("修改状态失败");
		}
	}
	
	
	
	
	
}
