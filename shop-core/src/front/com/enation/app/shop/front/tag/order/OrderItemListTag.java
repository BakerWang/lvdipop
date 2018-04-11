package com.enation.app.shop.front.tag.order;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.enation.app.shop.core.order.model.OrderItem;
import com.enation.app.shop.core.order.service.IOrderManager;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;


/**
 * 订单货物列表标签
 * @author kingapex
 *2013-7-28下午3:54:32
 */
@Component
@Scope("prototype")
public class OrderItemListTag extends BaseFreeMarkerTag {
	@Autowired
	private IOrderManager orderManager;

	
	/**
	 * 订单货物列表标签
	 * @param orderid:订单id，int型
	 * @return 订单货物列表 ,List<OrderItem>型
	 * {@link OrderItem}
	 */
	@Override
	public Object exec(Map params) throws TemplateModelException {
		Integer orderid  =(Integer)params.get("orderid");
		if(orderid==null){
			throw new TemplateModelException("必须传递orderid参数");
		}
		
		List<OrderItem> itemList  =orderManager.listGoodsItems(orderid);
		return itemList;
	}

	
}
