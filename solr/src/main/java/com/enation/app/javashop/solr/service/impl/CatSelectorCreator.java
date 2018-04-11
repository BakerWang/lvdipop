/**
 * 
 */
package com.enation.app.javashop.solr.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.springframework.stereotype.Component;

import com.enation.app.javashop.solr.service.ISearchSelectorCreator;
import com.enation.app.shop.core.goods.model.Cat;
import com.enation.app.shop.core.goods.plugin.search.SearchSelector;
import com.enation.app.shop.core.goods.service.IGoodsCatManager;
import com.enation.app.shop.core.goods.utils.CatUrlUtils;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.StringUtil;

/**
 * 分类选择器生成
 * @author kingapex
 *2015-4-23
 */

@Component
public class CatSelectorCreator implements ISearchSelectorCreator {
	private IGoodsCatManager goodsCatManager;
	/* (non-Javadoc)
	 * @see com.enation.app.shop.component.goodsindex.service.ISearchSelectorCreator#createAndPut(java.util.Map)
	 */
	public void createAndPut(Map<String, Object> map,List<FacetField> results) {
		  map.put("cat", new ArrayList());
		List<Cat> allCatList  = this.goodsCatManager.listAllChildren(0);
		 for (FacetField tmp : results) {  
		    	String dim =tmp.getName();//维度
		    	
		    	//对分类维度的特殊处理 
		    	if(dim.equals("cat_id")){
		    		if(tmp.getValueCount() > 1){
		    			List<SearchSelector> catDim  = createCatSelector( tmp.getValues());
		    			map.put("cat", catDim);
		    		}
		    		break;
		    	}
		 }
		 
		 List<SearchSelector> selectedCat = CatUrlUtils.getCatDimSelected(allCatList);
		 map.put("selected_cat", selectedCat); //已经选择的分类

	}
	
	
	/**
	 * 生成分类选择器<br>
	 * lucene中索引的是catid，需要由缓存中取出catname，并生成正确的url
	 * @param lvs
	 * @return
	 */
	private List createCatSelector(List<Count> countList){
		HttpServletRequest request  = ThreadContextHolder.getHttpRequest();
		String servlet_path = request.getServletPath();
		List<SearchSelector> selectorList  = new ArrayList();
		//由缓存中加载所有的分类
		List<Cat> allCatList  =  this.goodsCatManager.listAllChildren(0);
		
		for (int i = 0; i < countList.size(); i++) {
			
    	
			Count count = countList.get(i);
			String catid= count.getName();
			String catname ="";
			Cat findcat  =CatUrlUtils.findCat(allCatList,StringUtil.toInt(catid,0));
			if(findcat!=null){
				catname = findcat.getName();
			}
			if(StringUtil.isEmpty(catname)){
	 
				continue;
			}
			SearchSelector selector = new SearchSelector();
			selector.setName(catname);
			String url =servlet_path +"?"+ CatUrlUtils.createCatUrl(findcat,false);
			selector.setUrl(url);
			selectorList.add(selector);
		}
		
		return selectorList;
	}
	
	
	
	
	


	public IGoodsCatManager getGoodsCatManager() {
		return goodsCatManager;
	}


	public void setGoodsCatManager(IGoodsCatManager goodsCatManager) {
		this.goodsCatManager = goodsCatManager;
	}
	

}
