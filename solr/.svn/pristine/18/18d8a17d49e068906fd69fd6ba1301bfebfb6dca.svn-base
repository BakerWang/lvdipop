package com.enation.app.javashop.solr.plugin;

import org.springframework.stereotype.Component;

import com.enation.app.base.core.model.ClusterSetting;
import com.enation.app.base.core.model.cluster.SolrSetting;
import com.enation.app.base.core.plugin.setting.IOnSettingInputShow;
import com.enation.app.base.core.plugin.setting.IOnSettingSaveEnvent;
import com.enation.framework.plugin.AutoRegisterPlugin;
/**
 * 
 * solr设置选项
 * @author zh
 * @version v1.0
 * @since v6.1
 * 2016年10月13日 下午2:40:07
 */
@Component
public class SolrSettingPlugin extends AutoRegisterPlugin implements IOnSettingInputShow, IOnSettingSaveEnvent {

	public void onSave() {
		SolrSetting.load();
	}

	public String onShow() {
		return "edit";
	}

	public String getSettingGroupName() {
		return ClusterSetting.cluster_key;
	}

	public String getTabName() {
		return "solr设置";
	}

	public int getOrder() {
		return 7;
	}

}
