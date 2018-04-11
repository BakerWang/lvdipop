package com.enation.app.javashop.customized.component.coupons.plugin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.order.model.StoreOrder;
import com.enation.app.b2b2c.core.order.service.IStoreOrderManager;
import com.enation.app.javashop.customized.core.service.CouponsSession;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.OrderMeta;
import com.enation.app.shop.core.order.model.OrderMetaEnumKey;
import com.enation.app.shop.core.order.model.support.CartItem;
import com.enation.app.shop.core.order.plugin.order.IAfterOrderCreateEvent;
import com.enation.app.shop.core.order.plugin.order.IOrderCanelEvent;
import com.enation.app.shop.core.order.service.IOrderMetaManager;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.plugin.AutoRegisterPlugin;

/**
 * 优惠券订单插件
 * @author DMRain
 * @date 2016-10-10
 * @since v61
 * @version 1.0
 */
@Component
@Scope("prototype")
public class CouponsOrderDiscountPlugin extends AutoRegisterPlugin implements IAfterOrderCreateEvent, IOrderCanelEvent{

	@Autowired
	private IStoreOrderManager storeOrderManager;
	
	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	
	@Autowired
	private IOrderMetaManager orderMetaManager;
	
	@Override
	public void canel(Order order) {
		//取消订单，会员使用的优惠券退回
		this.memberCouponsManager.returned(order.getOrder_id());
		
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void onAfterOrderCreate(Order order, List<CartItem> itemList,
			String sessionid) {
		StoreOrder storeOrder = this.storeOrderManager.get(order.getOrder_id());
		
		//如果订单属于子订单
		if (storeOrder.getParent_id() != null) {
			Map<Integer, MemberCoupons> map = (Map) ThreadContextHolder.getSession().getAttribute(CouponsSession.B2B2C_COUPONS_SESSIONKEY);
			//如果优惠券集合不为空
			if (map != null) {
				for (MemberCoupons memberCoupons : map.values()) {
					//如果会员优惠券不为空
					if (memberCoupons != null) {
						if (memberCoupons.getStore_id().equals(storeOrder.getStore_id())) {
							int mcoup_id = memberCoupons.getMcoup_id();
							int coupons_id = memberCoupons.getCoupons_id();
							this.memberCouponsManager.useCoupons(mcoup_id, coupons_id, storeOrder.getMember_id(), storeOrder.getOrder_id(), storeOrder.getSn());
							
							//添加订单拓展信息
							OrderMeta meta = new OrderMeta();
							meta.setOrderid(storeOrder.getOrder_id());
							meta.setMeta_key(OrderMetaEnumKey.coupons_discount.toString());
							meta.setMeta_value(storeOrder.getDiscount().toString());
							this.orderMetaManager.add(meta);
						}
					}
				}
			}
		}
	}

}
