package com.enation.app.javashop.customized.front.tag;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.base.core.model.Member;
import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.eop.sdk.context.UserConext;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

/**
 * 获取会员当前拥有的满足订单条件的店铺优惠券集合标签
 * @author DMRain
 * @date 2016-10-10
 * @since v61
 * @version 1.0
 */
@Component
public class MemberStoreCouponsTag extends BaseFreeMarkerTag{

	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		Integer store_id = (Integer) params.get("store_id");
		Double storeprice = (Double) params.get("storeprice");
		Member member = UserConext.getCurrentMember();
		List list = this.memberCouponsManager.getCouponsList(member.getMember_id(), store_id, storeprice);
		return list;
	}

}
