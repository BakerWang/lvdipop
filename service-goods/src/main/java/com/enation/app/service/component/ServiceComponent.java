package com.enation.app.service.component;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.base.core.service.ISiteMenuManager;
import com.enation.app.base.core.service.dbsolution.DBSolutionFactory;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.framework.component.IComponent;
import com.enation.framework.component.IComponentStartAble;
import com.enation.framework.component.IComponentStopAble;
import com.enation.framework.database.IDaoSupport;


/**
 * 
 * @ClassName: ServiceComponent 
 * @Description: 服务商品组件
 * @author: liuyulei
 * @date: 2016年9月13日 下午4:23:48
 */
@Component
public class ServiceComponent implements IComponent, IComponentStartAble, IComponentStopAble {

	@Autowired
	private IDaoSupport daoSupport;

	@Autowired
	private ISiteMenuManager siteMenuDbManager;

	@Override
	public void install() {
		DBSolutionFactory.dbImport("file:com/enation/app/service/component/servicegoods_install.xml", "es_");
	}

	@Override
	public void unInstall() {
		DBSolutionFactory.dbImport("file:com/enation/app/service/component/servicegoods_uninstall.xml", "es_");
	}

	/**
	 * 安装后台菜单
	 */
	public void installMenu() {
		
		Integer id = this.daoSupport.queryForInt("select id from es_menu where pid=? and title=? ", 0, "自营");;
		String sql = "insert into es_menu (pid,title,url,sorder,menutype,selected,deleteflag,canexp,is_display) values (?,?,?,50,2,0,0,1,0)";
		this.daoSupport.execute(sql, id, "服务商品管理", "");
		
		 id = this.daoSupport.queryForInt("select id from es_menu where pid!=? and title=? ", 0, "服务商品管理");
		if("b2b2c".equals(EopSetting.PRODUCT)){  //   如果是多店
			this.daoSupport.execute(sql, id, "服务商品列表", "/b2b2c/admin/service-goods/get-servicegoods-html.do?self_store=yes");
			this.daoSupport.execute(sql, id, "卡密列表", "/shop/admin/service-goods/service-goods-list.do?self_store=yes");
			this.daoSupport.execute(sql, id, "服务商品验证", "/shop/admin/service-goods/get-servicegoods-checkcode-html.do?self_store=yes");
			this.daoSupport.execute(sql, id, "预约服务列表", "/b2b2c/admin/self-service-goods/get-booking-html.do");
			this.daoSupport.execute(sql, id, "预约服务信息", "/b2b2c/admin/self-service-goods/get-booking-info-html.do");
		}else{
			this.daoSupport.execute(sql, id, "服务商品验证", "/shop/admin/service-goods/get-servicegoods-checkcode-html.do");
			this.daoSupport.execute(sql, id, "服务商品列表", "/shop/admin/service-goods/get-service-goods-list.do");
			this.daoSupport.execute(sql, id, "卡密列表", "/shop/admin/service-goods/service-goods-list.do");
		}
		
	}

	/**
	 * 卸载后台菜单
	 */
	public void uninstallMenu() {
		String sql = "delete from es_menu where title=?";
		this.daoSupport.execute(sql, "服务商品验证");
		this.daoSupport.execute(sql, "服务商品列表");
		this.daoSupport.execute(sql, "服务商品管理");
		this.daoSupport.execute(sql, "卡密列表");
		this.daoSupport.execute(sql, "预约服务列表");
		this.daoSupport.execute(sql, "预约服务信息");

	}

	@Override
	public void stop() {
		this.uninstallMenu();
	}

	@Override
	public void start() {
		
		this.uninstallMenu();
		this.installMenu();
		
	}

}
