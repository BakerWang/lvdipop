package com.enation.app.service.core.servicegoods.action;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.enation.app.service.core.servicegoods.model.BookingGoods;
import com.enation.app.service.core.servicegoods.service.IBookingGoodsManager;
import com.enation.framework.action.GridController;
import com.enation.framework.action.GridJsonResult;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.JsonResultUtil;

/**
 * 
 * @ClassName: SelfServiceGoodsController 
 * @Description: 自营服务商品 Controller
 * @author: liuyulei
 * @date: 2016年9月20日 上午10:42:57 
 * @since:v61
 */

@Scope("prototype")
@Controller
@RequestMapping("/b2b2c/admin/self-service-goods")
public class SelfServiceGoodsController extends GridController {
	
	@Autowired
	private IBookingGoodsManager bookingGoodsManager;
	
	/**
	 * 
	 * @Title: getServiceGoodCheckCodesHtml 
	 * @Description: TODO   跳转到服务商品列表页面
	 * @return
	 * @return: ModelAndView
	 * @author： liuyulei
	 * @date：2016年9月20日 上午10:23:55
	 */
	@RequestMapping("/get-booking-html")
	public ModelAndView getServiceGoodCheckCodesHtml() {
		ModelAndView view = getGridModelAndView();
		view.setViewName("/b2b2c/admin/self/goods/booking_list");
		return view;
	}
	/**
	 * 
	 * @Title: getBookingGoodsListHtml 
	 * @Description: TODO  获取预定商品信息列表
	 * @return
	 * @return: ModelAndView
	 * @author： liuyulei
	 * @date：2016年9月26日 下午4:23:39
	 */
	@RequestMapping("/get-booking-info-html")
	public ModelAndView getBookingGoodsListHtml(){
		ModelAndView view = getGridModelAndView();
		view.setViewName("/b2b2c/admin/self/goods/booking_info_list");
		return view;
	}
	
	/**
	 * 获取预约信息html
	 * @param bookingid 预约Id,Integer
	 */
	@RequestMapping(value="/get-booking-status-html")
	public ModelAndView getStockDialogHtml( Integer bookingid) {
		ModelAndView view=new ModelAndView();
		view.addObject("bookingid",bookingid);
		BookingGoods bGoods = this.bookingGoodsManager.queryByBookingGoodsId(bookingid);
		view.addObject("booking_status", bGoods.getBooking_status());
		view.setViewName("/b2b2c/admin/self/goods/booking_dialog");
		return view;
	}
	
	
	
	
	/**
	 * 
	 * @Title: listJson 
	 * @Description: TODO  预约信息列表
	 * @param catid
	 * @param stype
	 * @param keyword
	 * @param name
	 * @param sn
	 * @return
	 * @return: GridJsonResult
	 * @author： liuyulei
	 * @date：2016年9月26日 下午4:35:29
	 */
	@ResponseBody
	@RequestMapping(value="/list-json")
	public GridJsonResult listJson(Integer catid,Integer stype,String keyword,String name,String sn) {
		Map goodsMap = new HashMap();
		if(stype!=null){
			if(stype==0){
				goodsMap.put("stype", stype);
				goodsMap.put("keyword", keyword);
			}else if(stype==1){
				goodsMap.put("stype", stype);
				goodsMap.put("name", name);
				goodsMap.put("sn", sn);
				goodsMap.put("catid", catid);
			}
		}
		webpage =this.bookingGoodsManager.searchBookingGoods(this.getPage(), this.getPageSize(), goodsMap);
		return JsonResultUtil.getGridJson(webpage);
	}
	/**
	 * 
	 * @Title: esitBookingStatus 
	 * @Description: TODO   修改预约信息
	 * @param booking_id    预约id
	 * @param booking_status  预约状态
	 * @return
	 * @return: JsonResult
	 * @author： liuyulei
	 * @date：2016年9月27日 下午1:05:14
	 */
	@ResponseBody
	@RequestMapping("/edit-booking-status")
	public JsonResult editBookingStatus(Integer booking_id,Integer booking_status) {
		try {
			if(booking_id == null){
				return JsonResultUtil.getErrorJson("参数失败！");
			}
			BookingGoods bGoods = this.bookingGoodsManager.queryByBookingGoodsId(booking_id);
			bGoods.setBooking_status(booking_status==null?0:booking_status);
			this.bookingGoodsManager.edit(bGoods);
			return JsonResultUtil.getSuccessJson("修改成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("修改失败！");
		}
	}
	
	
	
}
