package com.enation.app.javashop.customized.component.coupons.plugin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.order.model.StoreOrder;
import com.enation.app.b2b2c.core.order.service.IStoreOrderManager;
import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.javashop.customized.core.service.IStoreCouponsManager;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.plugin.order.IOrderConfirmPayEvent;
import com.enation.framework.plugin.AutoRegisterPlugin;

/**
 * 确认付款发放促销活动赠送优惠券插件
 * @author DMRain
 * @date 2016-10-11
 * @version 1.0
 */
@Component
public class OrderPaySendCouponsPlugin extends AutoRegisterPlugin implements IOrderConfirmPayEvent {

	@Autowired
	private IStoreOrderManager storeOrderManager;
	
	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	
	@Autowired
	private IStoreCouponsManager storeCouponsManager;
	
	@Override
	public void confirmPay(Order order) {
		StoreOrder storeOrder = this.storeOrderManager.get(order.getOrder_id());
		
		//如果订单中的优惠券id不等于0或者不为空
		if (storeOrder.getBonus_id() != null && storeOrder.getBonus_id() != 0) {
			StoreCoupons storeCoupons = this.storeCouponsManager.getCoupons(storeOrder.getBonus_id());
			
			//如果店铺的优惠券领取数量小于发放数量
			if (storeCoupons.getReceived_num() < storeCoupons.getCoupons_stock()) {
				this.memberCouponsManager.receive(storeOrder.getBonus_id(), storeOrder.getStore_id(), storeOrder.getMember_id(), 1);
			}
		}
		
	}

}
