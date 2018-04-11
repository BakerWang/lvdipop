package com.enation.app.shop.core.other.service;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Service;

import com.enation.eop.SystemSetting;
import com.enation.eop.processor.facade.IThemePathGeter;
import com.enation.eop.resource.IThemeManager;
import com.enation.eop.resource.model.EopSite;
import com.enation.eop.resource.model.Theme;
import com.enation.framework.context.spring.SpringContextHolder;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.StringUtil;
/***
 * b2c模板处理器
 * 处理b2b2c不同的店铺模板 访问
 * @author Kanon
 *
 */
@Service("b2cThemePathGeter")
public class B2cThemePathGeter implements IThemePathGeter{

	@Override
	public String getThemespath(String url) {
		if(this.isMobile()){
			return SystemSetting.getWap_folder();
		}
		
		EopSite site = EopSite.getInstance();
		IThemeManager themeManager = SpringContextHolder.getBean("themeManager");
		Integer themeid  = site.getThemeid();
		if(themeid==null){
			System.out.println("发生 theme id 为空！！");
			System.out.println(" themeid暂时重置为 1");
			themeid=1;
		}
		Theme theme = themeManager.getTheme(themeid);
		return theme.getPath();
	}

	@Override
	public String getTplFileName(String url) {
		// TODO Auto-generated method stub
		return url;
	}
	
	
	/**
	 * 检测是不是手机访问
	 * @return
	 */
	private static boolean isMobile(){
		
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		
		//判断请求是否为空
		if(request==null){
			return false;
		}
		
		String user_agent = request.getHeader("user-agent");
		
		//判断user-agent是否为空
		if(StringUtil.isEmpty(user_agent)){
			 return false;
		}
		 
		String userAgent = user_agent.toLowerCase();

		if(userAgent.contains("android" ) || userAgent.contains("iphone")){
			return true;
		}
		
		return false;
	}
}
