package com.enation.app.service.core.servicegoods.tag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.service.core.servicegoods.service.IServiceGoodsManager;
import com.enation.eop.processor.core.UrlNotFoundException;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.Page;
import com.enation.framework.taglib.BaseFreeMarkerTag;

import freemarker.template.TemplateModelException;

/**
 * 
 * @ClassName: ServiceGoodsListTag
 * @Description: 卡密列表标签
 * @author: liuyulei
 * @date: 2016年9月19日 下午1:15:34
 * @since:v61
 */
@Component
@Scope("prototype")
public class ServiceGoodsListTag extends BaseFreeMarkerTag {

	@Autowired
	private IServiceGoodsManager serviceGoodsManager;

	@Autowired
	private IStoreMemberManager storeMemberManager;

	/**
	 * 根据参数获取卡密列表
	 * 
	 * @param keyword:搜索关键字
	 * @param goodsnum:要读取的商品数量，必填项。
	 * @return 卡密列表
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected Object exec(Map params) throws TemplateModelException {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		// session中获取会员信息,判断用户是否登陆
		StoreMember member = storeMemberManager.getStoreMember();
		if (member == null) {
			HttpServletResponse response = ThreadContextHolder.getHttpResponse();
			try {
				response.sendRedirect("login.html");
			} catch (IOException e) {
				throw new UrlNotFoundException();
			}
		}
		
		Map result = new HashMap();
		int pageSize=10;
		String keyword = request.getParameter("keyword") == null ? "" : request.getParameter("keyword");
		String page = request.getParameter("page") == null ? "1" : request.getParameter("page");
		String service_id = request.getParameter("service_id") == null ? "1" : request.getParameter("service_id");
		
		result.put("store_id", member.getStore_id());
		result.put("keyword", keyword);
		result.put("service_id",service_id);
		
		Page serviceGoodsList = new Page();
		try {
			serviceGoodsList = this.serviceGoodsManager.serviceGoodsList(Integer.parseInt(page), pageSize, result);
		} catch (NumberFormatException e) {
			this.logger.error("获取卡密列表失败!");
			e.printStackTrace();
		}
		
		Long totalCount = serviceGoodsList.getTotalCount();
		
		result.put("page", page);
		result.put("pageSize", pageSize);
		result.put("totalCount", totalCount);
		result.put("serviceGoodsList", serviceGoodsList);
		
		Map serviceGoods = new HashMap();
		try {
			serviceGoods = this.serviceGoodsManager.getServiceGoods(result);
		} catch (Exception e) {
			this.logger.error("获取卡密信息失败！");
			e.printStackTrace();
		}
		
		result.put("serviceGoods", serviceGoods);
		
		return result;
	}

}
