package com.enation.app.javashop.customized.front.tag;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.base.core.service.IRegionsManager;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

/**
 * 获取省级地区集合标签
 * @author DMRain
 * @date 2016-9-22
 * @since v61
 * @version 1.0
 */
@Component
public class ProvinceListTag extends BaseFreeMarkerTag{

	@Autowired
	private IRegionsManager regionsManager;
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		List list = this.regionsManager.listProvince();
		return list;
	}

}
