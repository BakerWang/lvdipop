package com.enation.app.base.core.action;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.enation.app.base.core.model.SensitiveWords;
import com.enation.app.base.core.service.ISensitiveWordsManager;
import com.enation.framework.action.GridController;
import com.enation.framework.action.GridJsonResult;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.JsonResultUtil;

/**
 * 
 * @ClassName: SensitiveWordsController
 * @Description: 敏感词action
 * @author: liuyulei
 * @date: 2016年9月28日 下午12:19:59
 * @since:v61
 */
@Controller
@Scope("prototype")
@RequestMapping("/b2b2c/admin/sensitivewords")
public class SensitiveWordsController extends GridController {

	@Autowired
	private ISensitiveWordsManager sensitiveWordsManager;

	/**
	 * 
	 * @Title: getSensitiveWordsHtml
	 * @Description: TODO 跳转到敏感词管理页面
	 * @return
	 * @return: ModelAndView @author： liuyulei
	 * @date：2016年9月28日 下午12:30:58
	 */
	@RequestMapping(value = "/get-sensitivewords-manager-html")
	public ModelAndView getSensitiveWordsHtml() {
		ModelAndView view = this.getGridModelAndView();
		view.setViewName("/b2b2c/admin/sensitivewords/sensitivewords-list");
		return view;
	}

	/**
	 * 
	 * @Title: getSensitiveWordsList
	 * @Description: TODO 获取敏感词信息列表
	 * @return
	 * @return: JsonResult @author： liuyulei
	 * @date：2016年9月28日 下午1:23:20
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@ResponseBody
	@RequestMapping(value = "/list-json")
	public GridJsonResult getSensitiveWordsList(String keyword) {
		Map params = new HashMap();
		params.put("keyword", keyword == null ? "" : keyword);
		this.webpage = this.sensitiveWordsManager.searchSensitiveWordsByParams(this.getPage(), this.getPageSize(),
				params);
		return JsonResultUtil.getGridJson(webpage);
	}

	/**
	 * 
	 * @Title: add
	 * @Description: TODO 跳转至敏感词添加页面
	 * @return
	 * @return: String @author： liuyulei
	 * @date：2016年9月28日 下午1:40:49
	 */
	@RequestMapping(value = "/add")
	public String add() {
		return "/b2b2c/admin/sensitivewords/sensitivewords-add";
	}

	/**
	 * 
	 * @Title: save
	 * @Description: TODO 保存敏感词添加
	 * @param sensitiveWords
	 * @return
	 * @return: JsonResult @author： liuyulei
	 * @date：2016年9月28日 下午2:02:25
	 */
	@ResponseBody
	@RequestMapping(value = "/save")
	public JsonResult save(SensitiveWords sensitiveWords, String sensitivewords_name) {
		try {
			sensitiveWords.setSensitivewords_status(0);    //设置状态，默认启用  备用字段  暂未启用
			sensitiveWords.setDisabled(0);                 //设置是否可用，默认可用    备用字段    暂未启用
			sensitiveWords.setSensitivewords_name(sensitivewords_name);
			this.sensitiveWordsManager.add(sensitiveWords);
			return JsonResultUtil.getSuccessJson("敏感词添加成功");
		} catch (Exception e) {
			this.logger.error("敏感词添加异常", e);
			return JsonResultUtil.getErrorJson("敏感词添加失败");
		}
	}

	/**
	 * 
	 * @Title: edit
	 * @Description: TODO跳转至敏感词修改页面
	 * @param brandId
	 * @return
	 * @return: ModelAndView @author： liuyulei
	 * @date：2016年9月28日 下午2:02:55
	 */
	@RequestMapping(value = "/edit")
	public ModelAndView edit(Integer sensitiveWords_id) {
		ModelAndView view = new ModelAndView();
		SensitiveWords sensitiveWords = this.sensitiveWordsManager.getSensitiveWords(sensitiveWords_id);
		view.addObject("sensitiveWords", sensitiveWords);
		view.setViewName("/b2b2c/admin/sensitivewords/sensitivewords-edit");
		return view;
	}

	/**
	 * 
	 * @Title: saveEdit
	 * @Description: TODO 敏感词修改
	 * @param sensitiveWords
	 * @return
	 * @return: JsonResult @author： liuyulei
	 * @date：2016年9月28日 下午2:13:30
	 */
	@ResponseBody
	@RequestMapping(value = "/save-edit")
	public JsonResult saveEdit(SensitiveWords sensitiveWords) {
		try {
			this.sensitiveWordsManager.edit(sensitiveWords);
			return JsonResultUtil.getSuccessJson("敏感词修改成功");
		} catch (Exception e) {
			this.logger.error("敏感词修改异常", e);
			return JsonResultUtil.getErrorJson("敏感词修改添加失败");
		}
	}

	/**
	 * 
	 * @Title: delete
	 * @Description: TODO 删除敏感词
	 * @param brand_id
	 * @return
	 * @return: JsonResult @author： liuyulei
	 * @date：2016年9月28日 下午2:20:46
	 */
	@ResponseBody
	@RequestMapping(value = "/delete")
	public JsonResult delete(Integer[] sensitivewords_id) {
		try {
			this.sensitiveWordsManager.delete(sensitivewords_id);
			return JsonResultUtil.getSuccessJson("删除成功");
		} catch (RuntimeException e) {
			this.logger.error("删除失败", e);
			return JsonResultUtil.getErrorJson("删除失败:" + e.getMessage());
		}
	}

}
