/**
 * 版权：Copyright (C) 2015  易族智汇（北京）科技有限公司.
 * 本系统是商用软件,未经授权擅自复制或传播本程序的部分或全部将是非法的.
 * 描述：广告api  
 * 修改人：Sylow  
 * 修改时间：2015-08-22
 * 修改内容：增加获得广告列表api
 */
package com.enation.app.shop.mobile.action.goods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.base.core.model.AdColumn;
import com.enation.app.base.core.model.Adv;
import com.enation.app.base.core.service.IAdColumnManager;
import com.enation.app.base.core.service.IAdvManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.JsonResultUtil;

/**
 * 广告位api
 * 提供广告相关api
 * @author hoser
 * @version v1.1
 * @since v1.1
 */
@SuppressWarnings("serial")
@Controller
@Scope("prototype")
@RequestMapping("/api/mobile/adv")
public class AdvMobileApiController  {
	protected final Logger logger = Logger.getLogger(AdvMobileApiController.class);
	@Autowired
	private IAdvManager advManager;
	@Autowired
	private IAdColumnManager adColumnManager;
	
//	private long advid;

	/**
	 * @param acid
	 *            广告位id
	 * @return Map广告信息数据，其中key结构为 adDetails:广告位详细信息 {@link AdColumn}
	 *         advList:广告列表 {@link Adv}
	 */
	@SuppressWarnings("unchecked")
	@ResponseBody
	@RequestMapping(value="/adv-list.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult advList() {
		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String cname = request.getParameter("cname");
		cname = cname == null ? "appbanner" : cname;
		Map<String, Object> data = new HashMap<String,Object>();
		try {
			AdColumn adDetails = adColumnManager.getADcolumnDetail(cname);
			List<Adv> advList = null;
			if (adDetails != null) {
				advList = advManager.listAdv(Long.valueOf(adDetails.getAcid()));
			}
			advList = advList == null ? new ArrayList<Adv>() : advList;
			data.put("adDetails", adDetails);// 广告位详细信息
			data.put("advList", advList);// 广告列表
			return JsonResultUtil.getObjectJson(data);
		} catch (RuntimeException e) {
			if (this.logger.isDebugEnabled()) {
				this.logger.error(e.getStackTrace());
			}
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}

	/**
	 * 根据某个广告id获取广告信息
	 * 
	 * @param advid
	 * 
	 * @return result:1调用成功 0调用失败 data: Adv对象json
	 * 
	 */
	
	@ResponseBody
	@RequestMapping(value="/get-one-adv.do",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult getOneAdv(String advid) {

		try {

			Adv adv = advManager.getAdvDetail(Long.parseLong(advid));
			return JsonResultUtil.getObjectJson(adv);

		} catch (Exception e) {
			this.logger.error("获取某个广告出错", e);
			return JsonResultUtil.getErrorJson(e.getMessage());
		}
	}
	
	


}
