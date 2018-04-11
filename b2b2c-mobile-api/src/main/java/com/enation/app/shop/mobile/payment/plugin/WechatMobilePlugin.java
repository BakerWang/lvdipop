package com.enation.app.shop.mobile.payment.plugin;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.stereotype.Component;

import com.enation.app.shop.core.order.model.PayCfg;
import com.enation.app.shop.core.order.model.PayEnable;
import com.enation.app.shop.core.order.plugin.payment.AbstractPaymentPlugin;
import com.enation.app.shop.core.order.plugin.payment.IPaymentEvent;
import com.enation.app.shop.core.order.plugin.payment.PaymentPluginBundle;
import com.enation.app.shop.mobile.payment.WeixinUtil;
import com.enation.app.shop.mobile.payment.wechatpay.util.PayCommonUtil;
import com.enation.framework.context.webcontext.ThreadContextHolder;

@Component("wechatMobilePlugin")
public class WechatMobilePlugin extends AbstractPaymentPlugin implements IPaymentEvent {
		//支付插件桩
		private PaymentPluginBundle paymentPluginBundle;
		@Override
	public String onCallBack(String ordertype) {
		Map<String, String> cfgparams = paymentManager.getConfigParams(this.getId());
		String key = cfgparams.get("key");
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		Map map = new HashMap();
		try {
			SAXReader saxReadr = new SAXReader();
			Document document = saxReadr.read(request.getInputStream());
			/** 调试时可以打开下面注释 ，以观察通知的xml内容 **/
			String docstr = WeixinUtil.doc2String(document);
			 this.logger.debug("--------post xml-------");
			 this.logger.debug(docstr);
			 this.logger.debug("--------end-------");
			Map<String, String> params = WeixinUtil.xmlToMap(document);
			String return_code = params.get("return_code");
			String result_code = params.get("result_code");
			if ("SUCCESS".equals(return_code) && "SUCCESS".equals(result_code)) {
				String ordersn = params.get("out_trade_no");
				String sign = WeixinUtil.createSign(params, key);
				if (sign.equals(params.get("sign"))) {
					this.paySuccess(ordersn, "", "", ordertype);
					map.put("return_code", "SUCCESS");
					this.logger.debug("签名校验成功");
				} else {
					 this.logger.debug("-----------签名校验失败---------");
					 this.logger.debug("weixin sign:" + params.get("sign"));
					 this.logger.debug("my sign:" + sign);
					map.put("return_code", "FAIL");
					map.put("return_msg", "签名失败");
				}
			} else {
				map.put("return_code", "FAIL");
				System.out.println("微信通知的结果为失败");
			}
		} catch (IOException e) {
			map.put("return_code", "FAIL");
			map.put("return_msg", "");
			e.printStackTrace();
		} catch (DocumentException e) {
			map.put("return_code", "FAIL");
			map.put("return_msg", "");
			e.printStackTrace();
		}
		HttpServletResponse response = ThreadContextHolder.getHttpResponse();
		response.setHeader("Content-Type", "text/xml");
		return WeixinUtil.mapToXml(map);
	}
		/**
		 * 微信支付返回预支付订单
		 */
		@Override
		public String onPay(PayCfg payC, PayEnable order) {
			Map<String,String> params = paymentManager.getConfigParams(this.getId());
			String sn = order.getSn();
			BigDecimal totalAmount = new BigDecimal(String.valueOf(order.getNeedPayMoney()));
			String description = sn;
			String appid = params.get("appid");
			String mch_id = params.get("mchid");
			String api_key = params.get("key");
			String notify_url = this.getNotifyUrl(payC, order);//回调函数
		    String payResult = PayCommonUtil.ceratePrePay(sn, totalAmount, description, appid, mch_id, notify_url, api_key);
		    System.out.println(payResult);
			return payResult;
		}
		
		@Override
		public String onReturn(String arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		public String getId() {
			return "wechatMobilePlugin";
		}
		
		@Override
		public String getName() {
			return "微信移动支付接口";
		}

		public PaymentPluginBundle getPaymentPluginBundle() {
			return paymentPluginBundle;
		}

		public void setPaymentPluginBundle(PaymentPluginBundle paymentPluginBundle) {
			this.paymentPluginBundle = paymentPluginBundle;
		}
		
		protected String getNotifyUrl(PayCfg payCfg,PayEnable order){
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String serverName =request.getServerName();
			int port = request.getServerPort();
			String portstr = "";
			if(port!=80){
				portstr = ":"+port;
			}
			String contextPath = request.getContextPath();
			return "http://"+serverName+portstr+contextPath+"/api/payment/"+order.getOrderType()+"_"+payCfg.getType() +"_payment-callback/notifypay.do";
		}
		
}
