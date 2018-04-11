package com.enation.app.shop.mobile.action.coupons;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.base.core.model.Member;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.javashop.customized.core.service.IStoreCouponsManager;
import com.enation.app.shop.core.member.service.impl.MemberManager;
import com.enation.app.shop.mobile.service.IApiCouponsManager;
import com.enation.app.shop.mobile.utils.CommonRequest;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * 
 * @ClassName: MemberCouponsMobileApiController
 * @Description: 会员优惠券api 提供会员优惠券列表api、会员领取优惠券api
 * @author: liuyulei
 * @date: 2016年10月10日 下午1:50:51
 * @since:v61
 */
@Controller
@Scope("prototype")
@RequestMapping("/api/mobile/coupons")
public class MemberCouponsMobileApiController {
	protected final Logger logger = Logger.getLogger(MemberCouponsMobileApiController.class);

	@Autowired
	private IMemberCouponsManager memberCouponsManager;

	@Autowired
	private IStoreCouponsManager storeCouponsManager;
	
	@Autowired
	private IApiCouponsManager apiCouponsManager;
	
	@Autowired
	private MemberManager memberManager;

	/**
	 * 
     * 会员优惠券列表
     * @param mobile [String] 手机号码
     * @param status [Integer] 优惠券状态
	 * @return
	 * @return: JsonResult @author： liuyulei
	 * @date：2016年10月10日 下午1:58:15
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@ResponseBody
	@RequestMapping(value = "/member-coupons-list", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult couponsList() {
		try {
			// 更新优惠券状态  add by Sylow  临时添加
			this.apiCouponsManager.updateStatus();
			Map params = new HashMap();
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String pageNo = request.getParameter("pageNo");
			String pageSize = request.getParameter("pageSize");
			String mobile = request.getParameter("mobile");
			Integer status = Integer
					.parseInt(request.getParameter("status") == null ? "-1" : request.getParameter("status")); // 优惠券状态   (全部:-1/未使用:0/已使用:1/已过期:2)
			pageNo = (pageNo == null || pageNo.equals("")) ? "1" : pageNo;
			pageSize = (pageSize == null || pageSize.equals("")) ? "10" : pageSize;
			params.put("mobile", mobile);
			params.put("status", status);
			Page page = this.apiCouponsManager.getMemberCouponsList(Integer.parseInt(pageNo), Integer.parseInt(pageSize), params);
			return JsonResultUtil.getObjectJson(page);
		} catch (RuntimeException e) {
			if (this.logger.isDebugEnabled()) {
				this.logger.error(e.getStackTrace());
			}
			return JsonResultUtil.getErrorJson("获取会员优惠券列表失败!");
		}
	}

	/**
	 * 
	 * 会员领券接口
	 * @param mobile
	 *            [String] 手机号码
	 * @param mcoup_id
	 *            [Integer] 会员优惠券id
	 * @param store_id
	 *            [integer] 店鋪id
	 * @return
	 * @return: JsonResult @author： liuyulei
	 * @date：2016年10月10日 下午1:53:19
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(value = "/receive-coupons", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult receiveCoupons() {
		try {
			
//			Member member = UserConext.getCurrentMember();
			Integer memberId = CommonRequest.getMemberID();
			Member member = memberManager.get(memberId);
			if (member == null) {
				return JsonResultUtil.getErrorJson("未登录不能进行此操作！");
			}
			 
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			Integer coupons_id = Integer
					.parseInt(request.getParameter("coupons_id") == null ? "-1" : request.getParameter("coupons_id"));

			String mobile = request.getParameter("mobile");
			
			if (StringUtil.isEmpty(mobile)) {
				return JsonResultUtil.getErrorJson("缺少参数mobile！");
			}
			if (coupons_id == -1) {
				return JsonResultUtil.getErrorJson("缺少参数coupons_id！");
			}
			StoreCoupons storeCoupons = this.storeCouponsManager.getCoupons(coupons_id);
			Integer store_id = storeCoupons.getStore_id();
			if (storeCoupons.getEnd_date() < DateUtil.getDateline() || storeCoupons.getStatus().equals(1)) {
				return JsonResultUtil.getErrorJson("此优惠券已经失效，无法领取！");
			}
			int limitNum = storeCoupons.getLimit_num();
			int stock = storeCoupons.getCoupons_stock();
			int receivedNum = storeCoupons.getReceived_num();
			if (receivedNum >= stock) {
				return JsonResultUtil.getErrorJson("此优惠券已被领完！");
			}
			Map result = new HashMap();
			int residualNum = storeCoupons.getCoupons_stock() - storeCoupons.getReceived_num(); // 计算优惠券剩余数量
			int memberNum = this.memberCouponsManager.getMemberCouponsNum(coupons_id, store_id, mobile);
			if (memberNum >= limitNum) {
				return JsonResultUtil.getErrorJson("此优惠券限领"+limitNum+"个，不可再次领取！");
			}
			this.apiCouponsManager.receive(coupons_id, store_id, mobile, 0,member.getMember_id(),member.getUname());
			result.put("residualNum", residualNum);
			result.put("msg", "领券成功！");// 领取成功后返回提示信息
			return JsonResultUtil.getObjectJson(result);
		} catch (RuntimeException e) {
			if (this.logger.isDebugEnabled()) {
				this.logger.error(e.getStackTrace());
			}
			return JsonResultUtil.getErrorJson("领取优惠券失败!");
		}
	}
	
	/**
	 * 
	 * @Title: getCouponsInfo 
	 * @Description: TODO  获取优惠券详情
	 * @param    coupons_id		[Integer]		优惠券id
	 * @param    mobile			[String]		用户手机号码
	 * @return   
	 * @return: JsonResult
	 * @author： liuyulei
	 * @date：2016年10月11日 下午4:49:44
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
	@ResponseBody
	@RequestMapping(value = "/get-coupons-info", produces = MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getCouponsInfo(){
		try{
			Map params = new HashMap();
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			Integer coupons_id = Integer.parseInt(request.getParameter("coupons_id") == null ? "-1" : request.getParameter("coupons_id") );
			String mobile = request.getParameter("mobile") == null ? "-1" : request.getParameter("mobile") ;
			if(coupons_id == -1){
				return JsonResultUtil.getErrorJson("缺少参数coupons_id!");
			}
			Map storeCouponsMap = this.apiCouponsManager.get(coupons_id);  //根据优惠券id    获取优惠券信息
			StoreCoupons storeCoupons = this.storeCouponsManager.getCoupons(coupons_id);
			Integer store_id = storeCoupons.getStore_id();
			int num = this.memberCouponsManager.getMemberCouponsNum(coupons_id, store_id, mobile);
			if(num == 0 ){
				storeCouponsMap.put("is_received", 0);  // 是否已领取      0 ：未领取
			}else{
				storeCouponsMap.put("is_received", 1);  // 是否已领取      1 ：已领取
			}
			return JsonResultUtil.getObjectJson(storeCouponsMap);
		}catch (RuntimeException e) {
			if (this.logger.isDebugEnabled()) {
				this.logger.error(e.getStackTrace());
			}
			return JsonResultUtil.getErrorJson("获取优惠券详情失败!");
		}
	}
	
}
