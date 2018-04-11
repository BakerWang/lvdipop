package com.enation.app.shop.component.decorate;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.base.core.service.ISiteMenuManager;
import com.enation.app.base.core.service.dbsolution.DBSolutionFactory;
import com.enation.app.base.core.service.impl.cache.SiteMenuCacheProxy;
import com.enation.app.base.core.service.solution.IInstaller;
import com.enation.app.base.core.service.solution.Installer;
import com.enation.eop.processor.facade.ThemePathGeterFactory;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.framework.cache.CacheFactory;
import com.enation.framework.cache.ICache;
import com.enation.framework.component.IComponent;
import com.enation.framework.component.IComponentStartAble;
import com.enation.framework.component.IComponentStopAble;
import com.enation.framework.context.spring.SpringContextHolder;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.util.StringUtil;
/**
 * 装修组件
 * @author    jianghongyan
 * @version   1.0.0,2016年6月20日
 * @since     v6.1
 */
@Component
public class DecorateComponent implements IComponent,IComponentStartAble,IComponentStopAble{

	@Autowired
	private ISiteMenuManager siteMenuDbManager;
	
	@Autowired
	private IDaoSupport daoSupport;
	
	public void install() {
		DBSolutionFactory.dbImport("file:com/enation/app/shop/component/decorate/decorate_install.xml","es_");
	}

	public void unInstall() {
		DBSolutionFactory.dbImport("file:com/enation/app/shop/component/decorate/decorate_uninstall.xml","es_");
	}
	
	/**
	 * 安装后台菜单
	 */
	public void installMenu(){
		String sql="insert into es_menu (pid,title,url,sorder,menutype,selected,deleteflag,canexp,is_display,icon) values (?,?,?,49,2,0,0,1,0,?)";
		this.daoSupport.execute(sql, 0,"装修","/core/admin/decoration.do","/adminthemes/new/images/menu_07.gif");
		int id=this.daoSupport.queryForInt("select id from es_menu where  title=?", "装修");
		sql="insert into es_menu (pid,title,url,sorder,menutype,selected,deleteflag,canexp,is_display) values (?,?,?,49,2,0,0,1,0)";
		this.daoSupport.execute(sql, id,"装修","");
//		id=this.daoSupport.getLastId("es_menu");
		id=this.daoSupport.queryForInt("select id from es_menu where pid!=? and title=?", 0,"装修");
		sql="insert into es_menu (pid,title,url,sorder,menutype,selected,deleteflag,canexp,is_display) values (?,?,?,50,2,0,0,1,0)";
		this.daoSupport.execute(sql, id,"首页楼层管理","/core/admin/floor/list.do");
		this.daoSupport.execute(sql, id,"橱窗管理","/core/admin/showcase/list.do");
		this.daoSupport.execute(sql, id,"专题管理","/core/admin/subject/list.do");
	}
	
	/**
	 * 卸载后台菜单
	 */
	public void uninstallMenu(){
		String sql="delete from es_menu where title=?";
		this.daoSupport.execute(sql, "装修");
		this.daoSupport.execute(sql, "首页楼层管理");
		this.daoSupport.execute(sql, "橱窗管理");
		this.daoSupport.execute(sql, "专题管理");
	}

	
	public void uninstallSiteMenu(){
		String sql="delete from es_site_menu where name=?";
		this.daoSupport.execute(sql,"专题");
		
	}
	
	public void installSubjectSiteMenu(){
		String sql="insert into es_site_menu (parentid,name,url,target,sort) values (?,?,?,?,?)";
		this.daoSupport.execute(sql, 0,"专题","subject/subject-index.html","",5);
	}
	/**
	 * 停用方法
	 */
	@Override
	public void stop() {
		this.uninstallMenu();
		if("b2b2c".equals(EopSetting.PRODUCT)){
			this.uninstallSiteMenu();
		}
		this.uninstallSubjectThemeUri();
		ICache cache=CacheFactory.getCache(SiteMenuCacheProxy.MENU_LIST_CACHE_KEY);
		List menuList = this.siteMenuDbManager.list(0);
		cache.put(SiteMenuCacheProxy.MENU_LIST_CACHE_KEY,menuList);
	}

	/**
	 * 启动方法
	 */
	@Override
	public void start() {
		this.uninstallMenu();
		if("b2b2c".equals(EopSetting.PRODUCT)){
			this.uninstallSiteMenu();
			this.installSubjectSiteMenu();
		}
		this.uninstallSubjectThemeUri();
		this.initSubjectThemeUri();
		this.installMenu();
		ICache cache=CacheFactory.getCache(SiteMenuCacheProxy.MENU_LIST_CACHE_KEY);
		List menuList = this.siteMenuDbManager.list(0);
		cache.put(SiteMenuCacheProxy.MENU_LIST_CACHE_KEY,menuList);
	}
	
	public void initSubjectThemeUri(){
		String sql="insert into es_themeuri (uri,path,pagename,point) values (?,?,?,?)";
		this.daoSupport.execute(sql, "/subject-(\\d+).html","/subject/subject.html","专题页面",0);
		this.daoSupport.execute(sql, "/subject-index.html","/subject/subject-index.html","专题首页",0);
	}
	
	public void uninstallSubjectThemeUri(){
		String sql="delete from es_themeuri where pagename like '%专题%'";
		this.daoSupport.execute(sql);
	}
}
