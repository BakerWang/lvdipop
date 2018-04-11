package com.enation.app.javashop.customized.component.coupons.plugin;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.javashop.customized.core.service.IStoreCouponsManager;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.OrderBonus;
import com.enation.app.shop.core.order.model.support.CartItem;
import com.enation.app.shop.core.order.plugin.order.IAfterOrderCreateEvent;
import com.enation.app.shop.core.order.service.IOrderBonusManager;
import com.enation.framework.plugin.AutoRegisterPlugin;
import com.enation.framework.util.DateUtil;

/**
 * 创建订单后添加订单优惠券信息
 * @author DMRain
 * @date 2016-10-11
 * @since v61
 * @version 1.0
 */
@Component
public class OrderAfterAddCouponsPlugin extends AutoRegisterPlugin implements IAfterOrderCreateEvent{

	@Autowired
	private IStoreCouponsManager storeCouponsManager;
	
	@Autowired
	private IOrderBonusManager orderBonusManager;
	
	@Override
	public void onAfterOrderCreate(Order order, List<CartItem> itemList,
			String sessionid) {
		Integer coupons_id = order.getBonus_id();
		
		//如果订单中的优惠券ID不为0并且不为空，也就证明此订单中有赠送的优惠券
		if(coupons_id != null && coupons_id != 0){
			StoreCoupons coupons = this.storeCouponsManager.getCoupons(coupons_id);
				
			//如果优惠券创建数量大于优惠券已被领取的数量
			if (coupons.getCoupons_stock() > coupons.getReceived_num()) {
				
				//如果赠送的优惠券最后使用期限大于当前时间
				if (coupons.getEnd_date() > DateUtil.getDateline()) {
					OrderBonus orderBonus = new OrderBonus();
					orderBonus.setOrder_id(order.getOrder_id());
					orderBonus.setOrder_sn(order.getSn());
					orderBonus.setBonus_id(coupons_id);
					orderBonus.setBonus_name(coupons.getCoupons_name());
					orderBonus.setType(coupons.getCoupons_type());
					
					//如果优惠券类型为满减券或现金券（0：满减券，1：折扣券，2：现金券）
					if (coupons.getCoupons_type() == 0 || coupons.getCoupons_type() == 2) {
						orderBonus.setBonus_money(coupons.getCoupons_money());
					} else {
						orderBonus.setBonus_discount(coupons.getCoupons_discount());
					}
					
					orderBonus.setMin_goods_amount(coupons.getMin_order_money());
					orderBonus.setUse_start_date(coupons.getStart_date());
					orderBonus.setUse_end_date(coupons.getEnd_date());
					
					this.orderBonusManager.add(orderBonus);
				}
			}
		}
	}

}
