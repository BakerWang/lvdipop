package com.enation.app.service.core.servicegoods.action;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.enation.framework.action.GridController;

/**
 * 
 * @ClassName: ServiceGoodsListController 
 * @Description: 自营预约服务商品
 * @author: liuyulei
 * @date: 2016年9月26日 下午3:51:08 
 * @since:v61
 */
@Scope("prototype")
@Controller
@RequestMapping("/b2b2c/admin/service-goods")
public class ServiceGoodsListController extends GridController {
	
	/**
	 * 
	 * @Title: getServiceGoodCheckCodesHtml 
	 * @Description: TODO   跳转到服务商品列表页面
	 * @return
	 * @return: ModelAndView
	 * @author： liuyulei
	 * @date：2016年9月20日 上午10:23:55
	 */
	@RequestMapping("/get-servicegoods-html")
	public ModelAndView getServiceGoodCheckCodesHtml() {
		ModelAndView view = getGridModelAndView();
		view.setViewName("/b2b2c/admin/goods/service_goods_list");
		return view;
	}
	
	/**
	 * 
	 * @Title: getBookingGoodsHtml 
	 * @Description: TODO  跳转到预约服务商品列表
	 * @return
	 * @return: ModelAndView
	 * @author： liuyulei
	 * @date：2016年9月24日 下午3:02:51
	 */
	@RequestMapping("/get-booking-html")
	public ModelAndView getBookingGoodsHtml() {
		ModelAndView view = getGridModelAndView();
		view.setViewName("/b2b2c/admin/goods/booking_list");
		return view;
	}
	
}
