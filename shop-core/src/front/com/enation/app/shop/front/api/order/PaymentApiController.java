package com.enation.app.shop.front.api.order;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.base.core.service.IRegionsManager;
import com.enation.app.shop.core.member.model.MemberAddress;
import com.enation.app.shop.core.member.service.IMemberAddressManager;
import com.enation.app.shop.core.order.model.Order;
import com.enation.app.shop.core.order.model.PayCfg;
import com.enation.app.shop.core.order.plugin.payment.IPaymentEvent;
import com.enation.app.shop.core.order.service.IOrderManager;
import com.enation.app.shop.core.order.service.IPaymentManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.spring.SpringContextHolder;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;


/**
 * 支付api
 * @author kingapex
 * 2013-9-4下午7:21:31
 * @author Sylow
 * @version v2.0,2016年2月20日
 * @since v6.0
 */
@Controller
@RequestMapping("/api/shop/payment")
public class PaymentApiController {
	
	@Autowired
	private IPaymentManager paymentManager;
	@Autowired
	private IOrderManager orderManager;
	@Autowired
	private IMemberAddressManager memberAddressManager;
	@Autowired
	private IRegionsManager regionsManager;

	
	/**
	 * 跳转到第三方支付页面
	 * @param orderid 订单Id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/execute")
	public String execute(){
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		
		//订单id参数
		Integer orderId=  StringUtil.toInt( request.getParameter("orderid") ,null);
		if(orderId == null ){
			return "必须传递orderid参数";
		}
		
		//支付方式id参数
		Integer paymentId=  StringUtil.toInt( request.getParameter("paymentid") ,null);
		Order order = this.orderManager.get(orderId);
		
		if(order==null){
			return "该订单不存在";
		}
		
		//如果没有传递支付方式id，则使用订单中的支付方式
		if(paymentId==null){
			paymentId = order.getPayment_id(); 
		}
		
		PayCfg payCfg = this.paymentManager.get(paymentId);

		IPaymentEvent paymentPlugin = SpringContextHolder.getBean(payCfg.getType());
		String payhtml = paymentPlugin.onPay(payCfg, order);

		// 用户更换了支付方式，更新订单的数据
		if (order.getPayment_id().intValue() != paymentId.intValue()) {
			this.orderManager.updatePayMethod(orderId, paymentId, payCfg.getType(), payCfg.getName());
		}
		return payhtml;
	}
	


	/**
	 * 检查是否支持货到付款
	 * @param addrid 地区id 必填
	 * @return result result 1.支持.0.不支持
	 */
	@ResponseBody
	@RequestMapping(value="/check-support-cod")
	public JsonResult checkSupportCod(int addrid) {
		MemberAddress memberAddress = memberAddressManager.getAddress(addrid);
		try {
			if (regionsManager.get(memberAddress.getRegion_id()).getCod() == 1) {
				return JsonResultUtil.getSuccessJson("支持货到付款");
			} else {
				return JsonResultUtil.getErrorJson("不支持货到付款");
			}
		} catch (RuntimeException e) {  
			return JsonResultUtil.getErrorJson("不支持货到付款");
		}
	}
}
