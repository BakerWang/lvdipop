package com.enation.app.javashop.customized.front.tag;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.javashop.customized.core.service.IStoreCouponsManager;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

/**
 * 获取店铺优惠券分页列表标签
 * @author DMRain
 * @date 2016-9-22
 * @since v61
 * @version 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Component
public class StoreCouponsListTag extends BaseFreeMarkerTag{

	@Autowired
	private IStoreCouponsManager storeCouponsManager;
	
	@Autowired
	private IStoreMemberManager storeMemberManager;
	
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		HttpServletRequest request=ThreadContextHolder.getHttpRequest();
		Integer store_id = (Integer) params.get("store_id");
		
		//如果店铺id为空
		if (store_id == null) {
			StoreMember storeMember = this.storeMemberManager.getStoreMember();
			store_id = storeMember.getStore_id();
		}

		//获得优惠券参数
		int pageSize = 10;
		String page = request.getParameter("page") == null ? "1" : request.getParameter("page");
		String search_type = request.getParameter("search_type");
		String keyword = request.getParameter("keyword");
		String coupons_type = request.getParameter("coupons_type");
		String coupons_status = request.getParameter("coupons_status");
		String send_status = request.getParameter("send_status");
		String province_id = request.getParameter("province_id") == null ? "0" : request.getParameter("province_id");
		String start_date = request.getParameter("start_date");
		String end_date = request.getParameter("end_date");
		
		
		Map result = new HashMap();
		result.put("search_type", search_type);
		result.put("keyword", keyword);
		result.put("coupons_type", coupons_type);
		result.put("coupons_status", coupons_status);
		result.put("send_status", send_status);
		result.put("province_id", Integer.parseInt(province_id));
		result.put("start_date", start_date);
		result.put("end_date", end_date);
		
		Page couponsList = this.storeCouponsManager.getStoreCouponsList(Integer.parseInt(page), pageSize,store_id, result);
		
		//获取总记录数
		Long totalCount = couponsList.getTotalCount();

		result.put("page", page);
		result.put("pageSize", pageSize);
		result.put("totalCount", totalCount);
		result.put("couponsList", couponsList);
		return result;
	}

}
