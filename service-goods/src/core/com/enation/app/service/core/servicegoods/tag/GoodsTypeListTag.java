package com.enation.app.service.core.servicegoods.tag;

import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.service.core.utils.GoodsType;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

/**
 * 
 * @ClassName: GoodsTypeTag
 * @Description: 商品类型tag
 * @author: liuyulei
 * @date: 2016年9月22日 下午1:55:25
 * @since:v61
 */
@Component
@Scope("prototype")
public class GoodsTypeListTag extends BaseFreeMarkerTag {

	/**
	 * 返回商品类型list return list
	 */
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		return GoodsType.getGoodsTypeList();
	}

}
