package com.enation.app.service.front.api;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.service.core.servicegoods.model.BookingGoods;
import com.enation.app.service.core.servicegoods.service.IBookingGoodsManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonResultUtil;

/**
 * 
 * @ClassName: BookingApiController
 * @Description: 预约信息API
 * @author: liuyulei
 * @date: 2016年9月23日 上午10:20:30
 * @since:v61
 */
@Controller
@RequestMapping("/api/b2b2c/booking")
public class BookingApiController {

	@Autowired
	private IBookingGoodsManager bookingGoodsManager;

	@Autowired
	private IStoreMemberManager storeMemberManager;

	protected Logger logger = Logger.getLogger(getClass());

	/**
	 * 
	 * @Title: addBooking
	 * @Description: TODO 立即预约
	 * @param goods_id
	 *            商品 id 必填
	 * @param name
	 *            预约名称 必填
	 * @param mobile
	 *            预约手机号 必填
	 * @param age
	 *            年龄 非必填项
	 * @param sex
	 *            性别 非必填
	 * @param store_id
	 *            店铺id 必填
	 * @return 预约信息
	 * @return: JsonResult @author： liuyulei
	 * @date：2016年9月26日 下午1:08:01
	 */
	@ResponseBody
	@RequestMapping(value = "/add-booking", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult addBooking(Integer goods_id, String name, String mobile,
			@RequestParam(value = "age", required = false) Integer age, Integer sex, Integer store_id) {
		// session中获取会员信息,判断用户是否登陆
		StoreMember member = storeMemberManager.getStoreMember();
		try {
			BookingGoods bookingGoods = new BookingGoods();
			bookingGoods.setGoods_id(goods_id);
			bookingGoods.setBooking_name(name);
			bookingGoods.setAge(age == null ? 0 : age);
			bookingGoods.setSex(sex == null ? 0 : sex);
			bookingGoods.setStore_id(store_id);
			bookingGoods.setBooking_mobile(mobile);
			bookingGoods.setCreate_time(DateUtil.getDateline());
			bookingGoods.setMember_id(member.getMember_id());
			bookingGoods.setBooking_status(0); // 添加预约信息 默认状态为申请预约状态 0
			this.bookingGoodsManager.add(bookingGoods);
			return JsonResultUtil.getSuccessJson("预约成功!");
		} catch (Exception e) {
			this.logger.error("添加预约信息出错！");
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("预约失败！");
		}

	}

	/**
	 * 
	 * @Title: editBookingStatus
	 * @Description: TODO 修改预约状态
	 * @param bookingId
	 *            预约信息id
	 * @param status
	 *            状态
	 * @return 修改结果
	 * @return: JsonResult @author： liuyulei
	 * @date：2016年9月26日 下午3:19:31
	 */
	@ResponseBody
	@RequestMapping(value = "/edit-booking-status", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult editBookingStatus(Integer bookingid, Integer status) {
		
		try {
			if(bookingid == null ){
				return JsonResultUtil.getErrorJson("参数有误！");
			}
			
			BookingGoods bGoods  = this.bookingGoodsManager.queryByBookingGoodsId(bookingid);
			bGoods.setBooking_status((status == null ? 2 : status));
			this.bookingGoodsManager.edit(bGoods);
			return JsonResultUtil.getSuccessJson("取消成功！");
		} catch (Exception e) {
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("取消失败！");
		}
		
	}

}
