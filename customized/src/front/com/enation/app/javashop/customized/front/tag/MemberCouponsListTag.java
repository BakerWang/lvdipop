package com.enation.app.javashop.customized.front.tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

/**
 * 获取会员优惠券列表标签
 * @author DMRain
 * @date 2016-10-9
 * @since v61
 * @version 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
@Component
public class MemberCouponsListTag extends BaseFreeMarkerTag{

	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	
	@Autowired
	private IStoreMemberManager storeMemberManager;
	
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		StoreMember member = this.storeMemberManager.getStoreMember();
		
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		
		Map result = new HashMap();
		String page = request.getParameter("page");
		page = (page == null || page.equals("")) ? "1" : page;
		int pageSize = 10;
		
		Page webpage = this.memberCouponsManager.getMemberCouponsList(Integer.valueOf(page), pageSize, member.getMember_id());
		Long totalCount = webpage.getTotalCount();
		
		List couponsList = (List) webpage.getResult();
		couponsList = couponsList == null ? new ArrayList() : couponsList;

		result.put("totalCount", totalCount);
		result.put("pageSize", pageSize);
		result.put("page", page);
		result.put("couponsList", couponsList);
		
		return result;
	}

}
