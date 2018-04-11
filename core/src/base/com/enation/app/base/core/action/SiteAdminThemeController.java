package com.enation.app.base.core.action;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.enation.eop.resource.IAdminThemeManager;
import com.enation.eop.resource.model.AdminTheme;
import com.enation.eop.resource.model.EopSite;
import com.enation.eop.resource.model.Theme;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;

/**
 * 站点主题管理
 * 
 * @author lzf
 *         <p>
 *         2009-12-30 上午11:01:08
 *         </p>
 * @version 1.0
 */
@Controller
@RequestMapping("/core/admin/user/siteAdminTheme")
public class SiteAdminThemeController  {

	@Autowired
	private IAdminThemeManager adminThemeManager;
	
	@RequestMapping(value="/info")
	public ModelAndView info() throws Exception {
		String contextPath = ThreadContextHolder.getHttpRequest().getContextPath();
		String previewBasePath =  contextPath+ "/adminthemes/";
		AdminTheme adminTheme = adminThemeManager.get( EopSite.getInstance().getAdminthemeid());
		List<AdminTheme> listTheme = adminThemeManager.list();
		String previewpath = previewBasePath + adminTheme.getPath() + "/preview.png";
		
		ModelAndView view=new ModelAndView();
		view.addObject("contextPath", contextPath);
		view.addObject("previewBasePath", previewBasePath);
		view.addObject("adminTheme", adminTheme);
		view.addObject("listTheme", listTheme);
		view.addObject("previewpath", previewpath);
		view.setViewName("/core/admin/user/siteadmintheme");
		return view;
	}
	
	/**
	 * 更换主题
	 * @param themeid 模板id
	 */
	@ResponseBody
	@RequestMapping(value="/change")
	public JsonResult change(Integer themeid)throws Exception {
		try {
			adminThemeManager.changeTheme(themeid);
			return JsonResultUtil.getSuccessJson("更换主题成功");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("更换主题失败");
		}

	}

	/**
	 * 跳转至添加后台模板页面
	 * @return 添加模板页面
	 */
	@RequestMapping(value="/add")
	public String add(){
		return "/core/admin/user/add_site_admin_theme";
	}
	
	/**
	 * 保存模板主题
	 * @param theme 模板
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/save")
	public JsonResult save(AdminTheme theme){
		try {
			adminThemeManager.add(theme, true);
			return JsonResultUtil.getSuccessJson("模板主题添加成功");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("模板主题添加失败");
		}
	}

}
