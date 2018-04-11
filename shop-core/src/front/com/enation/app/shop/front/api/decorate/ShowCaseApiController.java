package com.enation.app.shop.front.api.decorate;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.shop.core.decorate.model.ShowCase;
import com.enation.app.shop.core.decorate.service.IShowCaseManager;
import com.enation.app.shop.core.goods.model.Goods;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;
/**
 * 
 * 橱窗前台api
 * @author    jianghongyan
 * @version   1.0.0,2016年6月20日
 * @since     v6.1
 */
@Controller
@RequestMapping("/api/core/showcase")
public class ShowCaseApiController {
	
	@Autowired
	private IShowCaseManager showCaseManager;
	/**
	 * 换一批功能
	 * @param id 橱窗id
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="refresh")
	public JsonResult refresh(Integer id){
		ShowCase showCase=this.showCaseManager.getShowCaseById(id);
		String goods_ids=showCase.getContent();
		if(!StringUtil.isEmpty(goods_ids)){
			List<Goods> list=this.showCaseManager.getSelectGoods(goods_ids);
			Collections.shuffle(list);
			return JsonResultUtil.getObjectJson(list);
		}
		return JsonResultUtil.getErrorJson("查询失败");
	}
}
