package com.enation.app.shop.front.api.order;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.shop.core.order.model.DlyType;
import com.enation.app.shop.core.order.model.support.OrderPrice;
import com.enation.app.shop.core.order.service.ICartManager;
import com.enation.app.shop.core.order.service.IDlyTypeManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;
/**
 * 结算api
 * @author kingapex
 *2013-7-25上午10:38:13
 * @version 2.0,2016-02-18 Sylow v60版本改造
 * @since v6.0
 */
@Controller 
@RequestMapping("/api/shop/checkout")
public class CheckoutApiController {
	
	@Autowired
	private ICartManager cartManager;
	@Autowired
	private IDlyTypeManager dlyTypeManager;
	
	/**
	 * 获取配送方式
	 * @param regionid 收货地区id
	 * @return
	 */
	@ResponseBody  
	@RequestMapping(value="/get-shiping-type", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getShipingType(){
		
		HttpServletRequest request  = ThreadContextHolder.getHttpRequest();
		String sessionid  = request.getSession().getId();
		Double orderPrice = cartManager.countGoodsTotal(sessionid);
		Double weight = cartManager.countGoodsWeight(sessionid);
		
		List<DlyType> dlyTypeList = this.dlyTypeManager.list(weight, orderPrice, request.getParameter("regionid"));
		return JsonResultUtil.getObjectJson(dlyTypeList);
	}
	
	
	/**
	 * 显示订单价格信息
	 * @param typeId 配送方式Id
	 * @param regionId 地区Id
	 * @return
	 */
	@ResponseBody  
	@RequestMapping(value="/show-order-total", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult showOrderTotal(int typeId, String regionId){
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String sessionid = request.getSession().getId();
		OrderPrice orderPrice = this.cartManager.countPrice(cartManager.listGoods(sessionid), typeId, regionId);
		return JsonResultUtil.getObjectJson(orderPrice);
	}
}
