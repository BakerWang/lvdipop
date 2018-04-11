package com.enation.app.shop.component.activity.plugin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.OrderGift;
import com.enation.app.shop.core.order.model.OrderMeta;
import com.enation.app.shop.core.order.model.OrderMetaEnumKey;
import com.enation.app.shop.core.order.model.support.CartItem;
import com.enation.app.shop.core.order.plugin.order.IAfterOrderCreateEvent;
import com.enation.app.shop.core.order.service.IOrderGiftManager;
import com.enation.app.shop.core.order.service.IOrderMetaManager;
import com.enation.app.shop.core.other.model.ActivityGift;
import com.enation.app.shop.core.other.service.IActivityGiftManager;
import com.enation.framework.plugin.AutoRegisterPlugin;

/**
 * 创建订单后添加赠品信息
 * @author DMRain
 * @date 2016-6-8
 * @version 1.0
 */
@Component
public class OrderAfterGiftBonusPlugin extends AutoRegisterPlugin implements IAfterOrderCreateEvent{

	@Autowired
	private IActivityGiftManager activityGiftManager;
	
	@Autowired
	private IOrderGiftManager orderGiftManager;
	
	@Autowired
	private IOrderMetaManager orderMetaManager;
	
	@Override
	public void onAfterOrderCreate(Order order, List<CartItem> itemList, String sessionid) {
		Integer gift_id = order.getGift_id();
		Integer get_point = order.getActivity_point();
		Double act_discount = order.getAct_discount();
		
		//如果订单中的赠品ID不为0并且不为空，也就证明此订单中有赠送的赠品
		if(gift_id != null && gift_id != 0){
			ActivityGift gift = this.activityGiftManager.get(gift_id);
			
			//如果赠品的可用库存大于0
			if (gift.getEnable_store() > 0) {
				OrderGift orderGift = new OrderGift();
				orderGift.setOrder_id(order.getOrder_id());
				orderGift.setOrder_sn(order.getSn());
				orderGift.setGift_id(gift_id);
				orderGift.setGift_name(gift.getGift_name());
				orderGift.setGift_price(gift.getGift_price());
				orderGift.setGift_type(gift.getGift_type());
				orderGift.setGift_img(gift.getGift_img());
				orderGift.setGift_status(0);
				
				this.orderGiftManager.addOrderGift(orderGift, gift.getEnable_store());
			}
		}
		
		//如果订单获取的积分不等于0并且不为空
		if (get_point != null && get_point != 0) {
			OrderMeta meta = new OrderMeta();
			meta.setOrderid(order.getOrder_id());
			meta.setMeta_key(OrderMetaEnumKey.get_point.toString());
			meta.setMeta_value(get_point.toString());
			this.orderMetaManager.add(meta);
		}
		
		//如果促销活动优惠不等于0并且不为空
		if (act_discount != null && act_discount != 0) {
			OrderMeta meta = new OrderMeta();
			meta.setOrderid(order.getOrder_id());
			meta.setMeta_key(OrderMetaEnumKey.activity_reduce_price.toString());
			meta.setMeta_value(act_discount.toString());
			this.orderMetaManager.add(meta);
		}
		
	}

}
