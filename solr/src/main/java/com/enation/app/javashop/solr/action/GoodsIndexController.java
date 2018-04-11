
package com.enation.app.javashop.solr.action;


import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.base.core.model.cluster.SolrSetting;
import com.enation.app.base.core.service.ProgressContainer;
import com.enation.app.javashop.solr.service.IGoodsIndexManager;
import com.enation.app.javashop.solr.service.impl.GoodsIndexManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.jms.EopJmsMessage;
import com.enation.framework.jms.EopProducer;
import com.enation.framework.util.JsonResultUtil;


/**
 * 
 * 商品索引生成action
 * @author zh
 * @version v1.0
 * @since v6.1
 * 2016年10月13日 上午11:13:21
 */
@Controller
@RequestMapping("/shop/admin/goods-index")
public class GoodsIndexController{
	protected final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private EopProducer eopProducer;

	@Autowired
	private IGoodsIndexManager goodsIndexManager;

	/**
	 * 转向生成页面
	 */

	@RequestMapping(value="input")
	public String execute(){
		HttpServletRequest request=ThreadContextHolder.getHttpRequest();
		request.setAttribute("ctx", request.getContextPath());
		return "/shop/admin/cluster/create_index";
	}


	/**
	 * 生成索引
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="create")
	public JsonResult create(){
		try {
			if (ProgressContainer.getProgress(GoodsIndexManager.PRGRESSID)!=null ){
				return JsonResultUtil.getErrorJson("有索引任务正在进行中，需等待本次任务完成后才能再次生成。");
			} else{
				EopJmsMessage jmsMessage = new EopJmsMessage();
				jmsMessage.setProcessorBeanId("goodsIndexManager");
				eopProducer.send(jmsMessage);
				return JsonResultUtil.getSuccessJson("索引任务下达成功");
			}
		} catch (Exception e) {
			this.logger.error("生成出错", e);
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("生成出错");
		}

	}






}
