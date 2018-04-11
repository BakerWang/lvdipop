package com.enation.app.b2b2c.core.order.service;

import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.enation.app.b2b2c.core.order.model.StoreOrder;
import com.enation.app.b2b2c.core.store.model.Store;
import com.enation.app.b2b2c.core.store.service.IStoreManager;
import com.enation.app.shop.core.order.model.PaymentDetail;
import com.enation.app.shop.core.order.plugin.payment.IPaySuccessProcessor;
import com.enation.app.shop.core.order.service.IOrderFlowManager;
import com.enation.app.shop.core.order.service.IOrderReportManager;
import com.enation.app.shop.core.order.service.OrderStatus;
import com.enation.app.shop.core.util.MessageTool;
import com.enation.framework.database.IDaoSupport;
/**
 * 店铺订单支付成功处理器
 * @author LiFenLong
 *
 */
@Service("b2b2cOrderPaySuccessProcessor")
public class B2b2cOrderPaySuccessProcessor implements IPaySuccessProcessor {
	private final static String SENDMESSAGE = "绿地会提醒您，您有新的订单啦，订单号%s，请及时处理";
	
	@Autowired
	private IOrderFlowManager orderFlowManager;
	
	@Autowired
	private IOrderReportManager orderReportManager;
	
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IStoreOrderManager storeOrderManager;
	
	
	@Autowired
	private IStoreManager storeManager;
	
	@Override
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.shop.core.plugin.payment.IPaySuccessProcessor#paySuccess(java.lang.String, java.lang.String, java.lang.String)
	 */
	public void paySuccess(String ordersn, String tradeno,String payment_account, String ordertype) {
		StoreOrder order=storeOrderManager.get(ordersn);
		
		if( order.getPay_status().intValue()== OrderStatus.PAY_YES ){ //如果是已经支付的，不要再支付
			return ;
		}
		this.payConfirmOrder(order,payment_account);
		if(order.getParent_id()==null){
			//获取子订单列表
			List<StoreOrder> cOrderList= storeOrderManager.storeOrderList(order.getOrder_id());
			for (StoreOrder storeOrder : cOrderList) {
				this.payConfirmOrder(storeOrder, payment_account);
				// TODO 发短信通知
				Store store = storeManager.getStore(storeOrder.getStore_id());
				if (StringUtils.isNotEmpty(store.getNotify_phone())) {
					MessageTool.sendMessage(String.format(SENDMESSAGE, storeOrder.getSn()), store.getNotify_phone());
				}
			}
		}
	}
	
	/**
	 * 订单确认收款
	 * @param order 订单
	 */
	private void payConfirmOrder(StoreOrder order,String payment_account){
		orderFlowManager.payConfirm(order.getOrder_id());
		Double needPayMoney= order.getNeed_pay_money(); //在线支付的金额
		int paymentid = orderReportManager.getPaymentLogId(order.getOrder_id());
		PaymentDetail paymentdetail=new PaymentDetail();
		paymentdetail.setAdmin_user("系统");
		paymentdetail.setPay_date(new Date().getTime());
		paymentdetail.setPay_money(needPayMoney);
		paymentdetail.setPayment_id(paymentid);
		orderReportManager.addPayMentDetail(paymentdetail);
		//修改订单状态为已付款付款
		this.daoSupport.execute("update es_payment_logs set paymoney=paymoney+? where payment_id=?",needPayMoney,paymentid);
		//如果订单为子订单，则改变子订单中的paymoney的值 add by DMRain 2016-7-12
		if (this.daoSupport.queryForInt("select count(0) from es_order where parent_id = ?", order.getOrder_id()) == 0) {
			//更新订单的已付金额
			this.daoSupport.execute("update es_order set paymoney=paymoney+?,payment_account=? where order_id=?",needPayMoney,payment_account,order.getOrder_id());
		}
	}
	
}
