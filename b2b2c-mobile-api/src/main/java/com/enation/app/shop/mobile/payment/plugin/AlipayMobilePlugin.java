package com.enation.app.shop.mobile.payment.plugin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import com.enation.app.shop.component.payment.plugin.alipay.sdk33.util.UtilDate;
import com.enation.app.shop.core.order.model.PayCfg;
import com.enation.app.shop.core.order.model.PayEnable;
import com.enation.app.shop.core.order.plugin.payment.AbstractPaymentPlugin;
import com.enation.app.shop.core.order.plugin.payment.IPaymentEvent;
import com.enation.app.shop.core.order.plugin.payment.PaymentPluginBundle;
import com.enation.app.shop.mobile.payment.alipay.config.AlipayConfig;
import com.enation.app.shop.mobile.payment.alipay.util.AlipayNotify;
import com.enation.app.shop.mobile.payment.alipay.util.AlipaySubmit;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonUtil;
import com.enation.framework.util.StringUtil;

@Component("alipayMobilePlugin")
public class AlipayMobilePlugin extends AbstractPaymentPlugin implements IPaymentEvent{
	//支付插件桩
	private PaymentPluginBundle paymentPluginBundle;
		
	@Override
	public String onCallBack(String ordertype) {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		Map<String,String> params = new HashMap<String,String>();
		Map requestParams = request.getParameterMap();
		for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
			String name = (String) iter.next();
			String[] values = (String[]) requestParams.get(name);
			String valueStr = "";
			for (int i = 0; i < values.length; i++) {
				valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
			}
			// 乱码解决，这段代码在出现乱码时使用。如果mysign和sign不相等也可以使用这段代码转化
			// valueStr = new String(valueStr.getBytes("ISO-8859-1"), "gbk");
			// valueStr = StringUtil.toUTF8(valueStr);
			params.put(name, valueStr);
		}
		//商户订单号
		 String out_trade_no = StringUtil.toUTF8(request.getParameter("out_trade_no") );
		//支付宝交易号
		String trade_no =StringUtil.toUTF8(request.getParameter("trade_no") );// new String(request.getParameter("trade_no").getBytes("ISO-8859-1"),"UTF-8");
		//交易状态
		String trade_status = StringUtil.toUTF8(request.getParameter("trade_status") );//new String(request.getParameter("trade_status").getBytes("ISO-8859-1"),"UTF-8");
		
		if(AlipayNotify.verify(params)){//验证成功
			if(trade_status.equals("TRADE_FINISHED")){
				this.paySuccess(out_trade_no,trade_no,"", ordertype);
			} else if (trade_status.equals("TRADE_SUCCESS")){
				this.paySuccess(out_trade_no,trade_no,"", ordertype);
			}
			return ("success");
		}else{//验证失败
			return ("fail");
		}
	}
	
	
	@Override
	public String onPay(PayCfg payCfg, PayEnable order) {
		try {
			// 支付宝支付参数
//			Map<String, String> cfgparams = paymentManager.getConfigParams(this.getId());
//			AlipayConfig.partner = cfgparams.get("partner");
//			AlipayConfig.private_key = cfgparams.get("rsa_private");
//			AlipayConfig.seller_id = cfgparams.get("seller_email");
			// 商户订单号
			String out_trade_no = new String(order.getSn().getBytes("ISO-8859-1"), "utf-8");
			// 订单名称
			String subject = out_trade_no;
			subject = new String(subject.getBytes("ISO-8859-1"), "utf-8");
			// 必填
			String body = ("orderNo-" + out_trade_no);
			body = new String(body.getBytes("ISO-8859-1"), "utf-8");
			// 付款金额
			String total_amount = new String(String.valueOf(order.getNeedPayMoney()).getBytes("ISO-8859-1"), "utf-8");
			Map<String, String> orderParam = new HashMap<String, String>();
			// 支付业务请求参数
			orderParam.put("out_trade_no", out_trade_no); // 商户订单号
			orderParam.put("total_amount", total_amount);// 交易金额
			orderParam.put("subject", subject); // 订单标题
			orderParam.put("body", body);// 对交易或商品的描述
			orderParam.put("product_code", "QUICK_MSECURITY_PAY");//销售产品码
			orderParam.put("seller_id", AlipayConfig.seller_id);
			// 统一请求参数
			Map<String, String> sinParams = new HashMap<String, String>();
			sinParams.put("app_id", AlipayConfig.app_id);
			sinParams.put("method", "alipay.trade.app.pay");
			sinParams.put("charset", AlipayConfig.input_charset);
			sinParams.put("timestamp", UtilDate.getDateFormatter());
			sinParams.put("version", "1.0");
			sinParams.put("biz_content", JsonUtil.MapToJson(orderParam));
			sinParams.put("notify_url",this.getNotifyUrl(payCfg,order));
			sinParams.put("payment_type", AlipayConfig.payment_type);
			String alipaySign =AlipaySubmit.buildRequestPara(sinParams);
			return alipaySign;
		} catch (Exception e) {
			return "request is error";
		}
	}
	
	
	@Override
	public String onReturn(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getId() {
		return "alipayMobilePlugin";
	}
	
	@Override
	public String getName() {
		return "支付宝移动支付接口";
	}

	public PaymentPluginBundle getPaymentPluginBundle() {
		return paymentPluginBundle;
	}

	public void setPaymentPluginBundle(PaymentPluginBundle paymentPluginBundle) {
		this.paymentPluginBundle = paymentPluginBundle;
	}
	
	protected String getNotifyUrl(PayCfg payCfg,PayEnable order){
		System.out.println("ordertype" + order.getOrderType() );
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String serverName =request.getServerName();
		int port = request.getServerPort();
		String portstr = "";
		if(port!=80){
			portstr = ":"+port;
		}
		//http://pop.greenlandjs.com:8095/pop/api/payment/b_alipayMobilePlugin_payment-callback/notifypay.do
		String contextPath = request.getContextPath();
		return "http://"+serverName+portstr+contextPath+"/api/payment/"+order.getOrderType()+"_"+payCfg.getType() +"_payment-callback/notifypay.do";
	}

}
