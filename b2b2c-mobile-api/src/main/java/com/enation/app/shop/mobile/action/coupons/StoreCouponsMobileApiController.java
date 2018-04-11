/**
 * 版权：Copyright (C) 2015  易族智汇（北京）科技有限公司.
 * 本系统是商用软件,未经授权擅自复制或传播本程序的部分或全部将是非法的.
 * 描述：店铺优惠券api  
 * 添加人：liuyulei  
 * 添加时间：2016-10-10
 * 添加内容：店铺优惠券列表api
 */
package com.enation.app.shop.mobile.action.coupons;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.shop.mobile.service.IApiCouponsManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.util.JsonResultUtil;

/**
 * 
 * @ClassName: StoreCouponsMobileApiController 
 * @Description: 店铺优惠券api  
 * 				 提供店铺优惠券列表api	
 * @author: liuyulei
 * @date: 2016年10月10日 下午12:59:53 
 * @since:v61
 */
@Controller
@Scope("prototype")
@RequestMapping("/api/mobile/store/coupons")
public class StoreCouponsMobileApiController {
	
	protected final Logger logger = Logger.getLogger(StoreCouponsMobileApiController.class);
	
	@Autowired
	private IApiCouponsManager apiCouponsManager;
	
	/**
	 * 
	 * @Title: couponList 
	 * @Description: TODO  获取店铺优惠券列表
	 * @param   store_id [Integer]   店铺id
	 * @param   page     [Integer]   页码
	 * @param   mobile   [String]    手机号码 
	 * @param   mobile   [String]    手机号码 
	 * @return   
	 * @return: JsonResult
	 * @author： liuyulei
	 * @date：2016年10月10日 下午1:07:29
	 */
	@SuppressWarnings("rawtypes")
	@ResponseBody
	@RequestMapping(value="/coupon-list",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult couponList() {
		try {
			Map result = new HashMap();
			
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			
			String pageNo = request.getParameter("page");
			String pageSize = request.getParameter("pageSize");
			String mobile = request.getParameter("mobile") == null ? "-1" : request.getParameter("mobile") ;
			Integer store_id = NumberUtils.toInt(request.getParameter("store_id"), 0);

			pageNo = (pageNo == null || pageNo.equals("")) ? "1" : pageNo;
			pageSize = (pageSize == null || pageSize.equals("")) ? "10" : pageSize;
			
			if(store_id == 0){//判断传参是否有效
				return JsonResultUtil.getErrorJson("获取店铺优惠券列表失败，参数店铺id为空！");
			}
			
			result.put("store_id", store_id);
			result.put("mobile", mobile);
			
			Page page = this.apiCouponsManager.getStoreCouponsList(NumberUtils.toInt(pageNo), NumberUtils.toInt(pageSize), result);
			return JsonResultUtil.getObjectJson(page);
		} catch (RuntimeException e) {
			if (this.logger.isDebugEnabled()) {
				this.logger.error(e.getStackTrace());
			}
			return JsonResultUtil.getErrorJson("获取店铺优惠券列表失败！");
		}
	}
}
