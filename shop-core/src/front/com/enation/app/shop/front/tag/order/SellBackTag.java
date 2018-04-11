package com.enation.app.shop.front.tag.order;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.shop.core.order.service.ISellBackManager;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;
/**
 * 查看退货申请标签
 * @author fenlongli
 *
 */
@Component
public class SellBackTag extends BaseFreeMarkerTag{
	
	@Autowired
	private ISellBackManager sellBackManager;
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		if(params.get("order_id")!=null&&!params.get("order_id").toString().equals("0")){
			Integer order_id=Integer.parseInt(params.get("order_id").toString());
			return sellBackManager.getSellBack(order_id);
		}
		Integer id = Integer.parseInt(params.get("id").toString());
		return sellBackManager.get(id);
	}
}
