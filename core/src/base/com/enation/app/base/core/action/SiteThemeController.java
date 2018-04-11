package com.enation.app.base.core.action;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.enation.eop.resource.IThemeManager;
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
 * @author Kanon 2015-11-16 version 1.1 添加注释
 */
@Controller
@RequestMapping("/core/admin/user/siteTheme")
public class SiteThemeController  {

	@Autowired
	private IThemeManager themeManager;
	
	/**
	 * 获取站点主题列表
	 * @param ctx 虚拟目录
	 * @param site 站点信息
	 * @param themeinfo 当前模板
	 * @param listTheme 模板列表
	 * @param previewpath 模板主题图片
	 */
	@RequestMapping(value="/info")
	public ModelAndView info() throws Exception {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String ctx = request.getContextPath();
		EopSite site  =EopSite.getInstance();
		String previewBasePath = ctx+ "/themes/";

		Theme themeinfo = themeManager.getTheme( site.getThemeid());
		List<Theme> listTheme = themeManager.list();
		String previewpath = previewBasePath + themeinfo.getPath() + "/preview.png";
		
		
		ModelAndView view=new ModelAndView();
		view.addObject("previewBasePath", previewBasePath);
		view.addObject("themeinfo", themeinfo);
		view.addObject("listTheme", listTheme);
		view.addObject("previewpath", previewpath);
		view.setViewName("/core/admin/user/sitetheme");
		return view;
	}
	
	/**
	 * 跳转至添加前台模板页面
	 * @return 添加模板页面
	 */
	@RequestMapping(value="/add")
	public String add(){
		return "/core/admin/user/add_site_theme";
	}
	
	/**
	 * 保存模板主题
	 * @param theme 模板
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/save")
	public JsonResult save(Theme theme){
		try {
			themeManager.add(theme, true);
			return JsonResultUtil.getSuccessJson("模板主题添加成功");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("模板主题添加失败");
		}
	}
	
	
	/**
	 * 更换主题
	 * @param themeid 模板id
	 */
	@ResponseBody
	@RequestMapping(value="/change")
	public JsonResult change(Integer themeid)throws Exception {
		
		try {
			themeManager.changetheme(themeid);
			return JsonResultUtil.getSuccessJson("更换主题成功");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("更换主题失败");
		}
	}

}
