/**
 * 
 */
package com.enation.app.javashop.solr.action;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.javashop.solr.model.GoodsWords;
import com.enation.app.javashop.solr.service.IGoodsIndexManager;
import com.enation.framework.action.JsonResult;
import com.enation.framework.util.JsonResultUtil;

/**
 * 
 * 商品分词 api action
 * @author zh
 * @version v1.0
 * @since v6.1
 * 2016年10月13日 上午11:16:26
 */
@Controller
@RequestMapping("/api/shop/goods-words")
public class GoodsWordsApiController {
	
	@Autowired
	private IGoodsIndexManager goodsIndexManager;
	
	@ResponseBody
	@RequestMapping(value="/list-words")
	public JsonResult execute(String keyword,  @RequestParam(value = "wordsList", required = false) List<GoodsWords> wordsList){
		try{
			wordsList = this.goodsIndexManager.getGoodsWords(keyword);
			return JsonResultUtil.getObjectJson(wordsList);
		}catch(Exception e){
			e.printStackTrace();
			return JsonResultUtil.getErrorJson("error");
		}
	} 
}
