package com.enation.app.shop.mobile.action.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.eop.SystemSetting;
import com.enation.framework.util.JsonUtil;

/**
 * 系统配置Api 
 * 提供系统参数js】
 * @author hoser
 * @version v1.1 2016-04-10
 * @since v1.1
 */
@SuppressWarnings("serial")
@Controller
@Scope("prototype")
@RequestMapping("/api/mobile/config")
public class ConfigMobileApiController{
	protected final Logger logger = Logger.getLogger(ConfigMobileApiController.class);
	
	private String  default_img_url; //默认图片路径
	
	/**
	 * 获得一些系统配置
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/get.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public String get(){
		
		
		// 默认图片url
		Map<String,String> map = new HashMap<String,String>();
		
		map.put("static_server_domain", SystemSetting.getStatic_server_domain());
		map.put("default_img_url", SystemSetting.getDefault_img_url());
		
//		this.json = "var APP_SETTING = " + JsonUtil.MapToJson(map);
		return "var APP_SETTING = "+JsonUtil.MapToJson(map);
		
	}
	
	public String getDefault_img_url() {
		return default_img_url;
	}

	public void setDefault_img_url(String default_img_url) {
		this.default_img_url = default_img_url;
	}

}
