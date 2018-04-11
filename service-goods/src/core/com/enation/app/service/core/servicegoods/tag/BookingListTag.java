package com.enation.app.service.core.servicegoods.tag;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.service.core.servicegoods.model.BookingGoods;
import com.enation.app.service.core.servicegoods.service.IBookingGoodsManager;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

/**
 * 
 * @ClassName: BookingListTag
 * @Description: 预约信息列表标签
 * @author: liuyulei
 * @date: 2016年9月23日 下午4:52:12
 * @since:v61
 */
@Component
@Scope("prototype")
public class BookingListTag extends BaseFreeMarkerTag {

	@Autowired
	private IBookingGoodsManager bookingGoodsManager;

	@Autowired
	private IStoreMemberManager storeMemberManager;

	@Override
	protected Object exec(Map params) throws TemplateModelException {
		// session中获取会员信息,判断用户是否登陆
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		StoreMember member = storeMemberManager.getStoreMember();
		if (member == null) {
			throw new TemplateModelException("未登陆不能使用此标签[BookingListTag]");
		}
		Map result = new HashMap();
		String page = request.getParameter("page");
		String keyword = request.getParameter("keyword") == null ? "" : request.getParameter("keyword");
		String isStore = params.get("store") == null ? "0" : params.get("store").toString();

		page = (page == null || page.equals("")) ? "1" : page;
		int pageSize = 10;
		result.put("member_id", member.getMember_id());
		result.put("keyword", keyword);
		result.put("store_id", member.getStore_id());
		result.put("isStore", isStore);
		Page bookingPage = this.bookingGoodsManager.searchBookingGoods(Integer.valueOf(page), pageSize, result);

		Long totalCount = bookingPage.getTotalCount();

		Integer bookingid = Integer
				.parseInt(params.get("bookingid") == null ? "0" : params.get("bookingid").toString());
		BookingGoods bGoods = this.bookingGoodsManager.queryByBookingGoodsId(bookingid);
		if (bGoods != null) {
			result.put("bGoods", bGoods);
		}
		result.put("totalCount", totalCount);
		result.put("pageSize", pageSize);
		result.put("page", page);
		result.put("bookingList", bookingPage);
		return result;

	}

}
