/**
 * 
 */
package com.enation.app.base.core.tag;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.enation.app.base.core.model.ClusterSetting;
import com.enation.eop.SystemSetting;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

/**
 * solr是否开启检测标签 
 * 如果开启返回ON，没有开启返回OFF
 * @author zh
 *2015-5-20
 */
@Component
public class SolrCheckTag extends BaseFreeMarkerTag {

	/* (non-Javadoc)
	 * @see com.enation.framework.taglib.BaseFreeMarkerTag#exec(java.util.Map)
	 */
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		 
		return ClusterSetting.getSolr_open()==1?"ON":"OFF";
	}
 

}
