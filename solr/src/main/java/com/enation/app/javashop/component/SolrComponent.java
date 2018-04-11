package com.enation.app.javashop.component;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.base.core.service.dbsolution.DBSolutionFactory;
import com.enation.framework.component.IComponent;
import com.enation.framework.database.IDaoSupport;
/**
 * 
 * sorl组件类
 * @author zh
 * @version v1.0
 * @since v6.1
 * 2016年10月13日 上午11:02:57
 */
@Component
public class SolrComponent implements IComponent{
	@Autowired
	private IDaoSupport daoSupport;
	/**
	 * 安装方法
	 */
	public void install() {
		
//		//创建附件表
		this.daoSupport.execute("CREATE TABLE IF NOT EXISTS `es_attachment` ("
				+ "`id` int(11) NOT NULL AUTO_INCREMENT,"
				+ "`file_key` varchar(50) DEFAULT NULL,"
				+ "`group_name` varchar(255) DEFAULT NULL ,"
				+ "`remote_name` varchar(255) DEFAULT NULL,"
				+ "`size` bigint(20) DEFAULT NULL,"
				+ "`url` varchar(255) DEFAULT NULL,"
				+ "`real_name` varchar(255) DEFAULT NULL,"
				+ "`local_path` varchar(255) DEFAULT NULL,"
				+ "PRIMARY KEY (`id`),"
				+ "UNIQUE KEY `es_attachment_key` (`file_key`))"
				+ " ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;");
		
		//添加菜单
		if(this.daoSupport.queryForInt("SELECT COUNT(0) FROM `es_menu` WHERE title='集群设置'") <= 0){
			this.daoSupport.execute("INSERT INTO `es_menu`(`appid`,`pid`,`title`,`url`,`target`,`sorder`,`menutype`,"
					+ "`datatype`,`selected`,`deleteflag`,`canexp`,`icon`,`icon_hover`) VALUES('', '63', '集群设置', '/shop/admin/cluster.do', "
					+ "null, '50', '2', null, '0', '0', '1', null, null)");
		}
		
		//添加配置
		if(this.daoSupport.queryForInt("SELECT COUNT(0) FROM `es_settings` WHERE cfg_group='cluster'") <= 0){
			this.daoSupport.execute("INSERT INTO `es_settings`(`code`,`cfg_value`,`cfg_group`) VALUES ('', '{\"solr\":\"http://192.168.190.137:8983/solr/gettingstarted/\",\"solr_open\":\"0\"}', 'cluster');");
		}
		
		DBSolutionFactory.dbImport("file:com/enation/app/javashop/solr/solr_install.xml","es_");
		
	}
	/**
	 * 卸载方法
	 */
	public void unInstall() {
		this.daoSupport.execute("DELETE FROM `es_settings` WHERE cfg_group=?", "cluster");
		this.daoSupport.execute("DELETE FROM `es_menu` WHERE title=?", "集群设置");
	}

}
