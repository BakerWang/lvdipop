package com.enation.app.b2b2c.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.base.core.service.dbsolution.DBSolutionFactory;
import com.enation.app.base.core.service.solution.IInstaller;
import com.enation.framework.component.IComponent;
import com.enation.framework.context.spring.SpringContextHolder;
import com.enation.framework.database.IDaoSupport;
@Component
/**
 * b2b2c组件
 */
public class B2b2cComponent implements IComponent{
	
	@Autowired
	private IDaoSupport daoSupport;
	@Override
	public void install() {
		DBSolutionFactory.dbImport("file:com/enation/app/b2b2c/component/b2b2c_install.xml", "es_");
		
		/**
		 * 安装b2b2c数据库索引
		 */
		DBSolutionFactory.dbImport("file:com/enation/app/b2b2c/core/b2b2c_index.xml", "es_");
		
		//修改后台显示菜单
		daoSupport.execute("update es_index_item set url='/b2b2c/admin/b2b2c-index-item/order.do' where id=2 ");
		
		
		IInstaller installer  = SpringContextHolder.getBean("storeSettingInstaller");
		
		installer.install("b2b2c", null);	
	}

	@Override
	public void unInstall() {
		DBSolutionFactory.dbImport("file:com/enation/app/b2b2c/component/b2b2c_uninstall.xml", "es_");
	}

}
