package com.enation.app.base.core.model.cluster;

import java.io.Serializable;
import java.util.Map;

import com.enation.app.base.core.service.ISettingService;
import com.enation.framework.context.spring.SpringContextHolder;

/**
 * 
 * 更新solr设置
 * @author zh
 * @version v1.0
 * @since v6.1
 * 2016年10月13日 下午3:39:42
 */
public class SolrSetting implements Serializable {
	
	private static String solr;//solr 
	private static int solr_open=0;//是否开启solr
	public static final String cluster_key="cluster"; //系统设置中的分组
	
	

	/**
	 * 集群设置缓存更新
	 */
	public static void load(){
		ISettingService settingService= SpringContextHolder.getBean("settingDbService");
		Map<String,String> clusters = settingService.getSetting(cluster_key);
		if(clusters != null ){
			solr_open=Integer.parseInt(clusters.get("solr_open"));
			solr=clusters.get("solr");
		}
		
	}
	
	
	public static String getSolr() {
		return solr;
	}

	public static void setSolr(String solr) {
		SolrSetting.solr = solr;
	}


	public static int getSolr_open() {
		return solr_open;
	}


	public static void setSolr_open(int solr_open) {
		SolrSetting.solr_open = solr_open;
	}
	
	
	
	
}
