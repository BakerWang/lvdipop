package com.enation.app.javashop.customized.front.tag;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.store.model.StoreCoupons;
import com.enation.app.javashop.customized.core.service.IStoreCouponsManager;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

/**
 * 获取店铺所有已发放并且未失效的优惠券集合标签
 * @author DMRain
 * @date 2016-10-9
 * @since v61
 * @version 1.0
 */
@Component
public class StoreHomeCouponsListTag extends BaseFreeMarkerTag{

	@Autowired
	private IStoreCouponsManager storeCouponsManager;
	
	@Autowired
	private IStoreMemberManager storeMemberManager;
	
	@SuppressWarnings("rawtypes")
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		Integer store_id = (Integer) params.get("store_id");
		
		//如果店铺id为空
		if (store_id == null) {
			StoreMember storeMember = storeMemberManager.getStoreMember();
			store_id = storeMember.getStore_id();
		}
		
		List<StoreCoupons> couponsList = this.storeCouponsManager.listCoupons(store_id);
		return couponsList;
	}

}
