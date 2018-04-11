package com.enation.app.javashop.customized.front.tag;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.javashop.customized.core.service.IStoreCouponsManager;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.taglib.BaseFreeMarkerTag;
import com.enation.framework.util.StringUtil;

import freemarker.template.TemplateModelException;

/**
 * 获取店铺优惠券信息标签
 * @author DMRain
 * @date 2016-9-26
 * @since v61
 * @version 1.0
 */
@Component
public class StoreCouponsTag extends BaseFreeMarkerTag{

	@Autowired
	private IStoreCouponsManager storeCouponsManager;
	
	@Autowired
	private IStoreMemberManager storeMemberManager;
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String coupons_id = request.getParameter("coupons_id");
		
		//如果优惠券id为空
		if (StringUtil.isEmpty(coupons_id)) {
			coupons_id = params.get("coupons_id").toString();
		}
		
		//获取当前登录的店铺会员信息
		StoreMember member = this.storeMemberManager.getStoreMember();
		if (member == null) {
			throw new TemplateModelException("未登陆不能使用此标签[StoreCouponsTag]");
		}
		
		StoreCoupons storeCoupons = this.storeCouponsManager.get(Integer.parseInt(coupons_id), member.getStore_id());
		return storeCoupons;
	}

}
