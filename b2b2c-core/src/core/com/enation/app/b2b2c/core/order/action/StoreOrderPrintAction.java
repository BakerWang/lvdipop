package com.enation.app.b2b2c.core.order.action;

import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.order.service.IStoreOrderPrintManager;
/**
 * 店铺发货订单Action
 * @author fenlongli
 *
 */
public class StoreOrderPrintAction {
	private IStoreOrderPrintManager storeOrderPrintManager;
	private Integer order_id;
	
	/**
	 * 打印发货单
	 * @param script 打印的script,String
	 * @return 发货单的script
	 */
	public String shipScript() {
		String script= storeOrderPrintManager.getShipScript(order_id);
//		this.json=script;
//		return this.JSON_MESSAGE;
		return "";
	}

}
