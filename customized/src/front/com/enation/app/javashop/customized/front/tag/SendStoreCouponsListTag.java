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
import com.enation.framework.util.StringUtil;

import freemarker.template.TemplateModelException;

/**
 * 获取店铺已发放的优惠券分页列表标签
 * @author DMRain
 * @date 2016-9-27
 * @since v61
 * @version 1.0
 */
@Component
public class SendStoreCouponsListTag extends BaseFreeMarkerTag {

	@Autowired
	private IStoreCouponsManager storeCouponsManager;

	@Autowired
	private IStoreMemberManager storeMemberManager;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String coupons_id = request.getParameter("coupons_id");
		
		//如果优惠券id为空
		if (StringUtil.isEmpty(coupons_id)) {
			coupons_id = params.get("coupons_id").toString();
		}
		
		StoreMember storeMember = this.storeMemberManager.getStoreMember();
		//如果当前登录的店铺会员信息为空
		if (storeMember == null) {
			throw new TemplateModelException("未登陆不能使用此标签[SendStoreCouponsListTag]");
		}

		// 获得优惠券参数
		int pageSize = 10;
		String page = request.getParameter("page") == null ? "1" : request.getParameter("page");
		String received = request.getParameter("received");
		String used = request.getParameter("used");
		
		Map result = new HashMap();
		result.put("is_received", received);
		result.put("is_used", used);
		
		Page sendList = this.storeCouponsManager.listSendCoupons(Integer.parseInt(page), pageSize, Integer.parseInt(coupons_id), storeMember.getStore_id(), result);

		// 获取总记录数
		Long totalCount = sendList.getTotalCount();

		result.put("page", page);
		result.put("pageSize", pageSize);
		result.put("totalCount", totalCount);
		result.put("sendList", sendList);
		return result;
	}

}
