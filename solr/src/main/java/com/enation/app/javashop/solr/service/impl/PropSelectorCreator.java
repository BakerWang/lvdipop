/**
 * 
 */
package com.enation.app.javashop.solr.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;


import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.springframework.stereotype.Component;

import com.enation.app.javashop.solr.service.ISearchSelectorCreator;
import com.enation.app.shop.core.goods.plugin.search.SearchSelector;
import com.enation.app.shop.core.goods.service.Separator;
import com.enation.app.shop.core.goods.utils.ParamsUtils;
import com.enation.app.shop.core.goods.utils.PropUrlUtils;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.StringUtil;

/**
 * 属性选择器生成
 * @author zh
 * @version v1.0
 * @since v6.1
 * 2016年10月13日 下午12:10:34
 */
@Component
public class PropSelectorCreator implements ISearchSelectorCreator {

	/* (non-Javadoc)
	 * @see com.enation.app.shop.component.goodsindex.service.ISearchSelectorCreator#createAndPut(java.util.Map, java.util.List)
	 */
	public void createAndPut(Map<String, Object> map, List<FacetField> results) {
		String[] prop_ar = ParamsUtils.getProps();
		
		HttpServletRequest request  = ThreadContextHolder.getHttpRequest();
		String servlet_path = request.getServletPath();
	    Map<String,List<SearchSelector>> propSelectorMap = new HashMap<String, List<SearchSelector>>();

		 for (FacetField tmp : results) {  
		    	String dim =tmp.getName();//维度
		    	
		    	//对分类维度的特殊处理 
		    	if( !dim.equals("cat_id")  &&  !dim.equals("brand_id")  ) {//对于属性维度的处理
			    	 
			    	
			    	String selected_prop_value = this.checkSelected(dim, prop_ar);
			    	if(StringUtil.isEmpty(selected_prop_value )){ //未选择的属性维度
			    	
				    	List<SearchSelector> selectorList  = new ArrayList();
				    	
				    	List<Count> countList =  tmp.getValues();
				    	for (Count count : countList) {
				    		SearchSelector selector = new SearchSelector();
				    		selector.setName(count.getName());
				    		
				    		String url =servlet_path +"?"+PropUrlUtils.createPropUrl(dim, count.getName());
				    		selector.setUrl(url);
				    		selectorList.add(selector);
						}
				    
				    	propSelectorMap.put(dim, selectorList); 
			    	}
			    	
		    	}
	        }
		 
		 map.put("prop", propSelectorMap); //属性维度
		 
		 //已经选的属性维度
		    List<SearchSelector> selectedProp  = new ArrayList();
		    selectedProp= this.getPropDimSelected();
		    
		    map.put("selected_prop", selectedProp);//已经选择的属性
	}

	
	
	/**
	 * 获取已选择的属性维度
	 * @return
	 */
	public  List<SearchSelector> getPropDimSelected(){
		
		List<SearchSelector> selectorList  = new ArrayList();
		HttpServletRequest request  = ThreadContextHolder.getHttpRequest();
		String servlet_path = request.getServletPath();
		String prop  = request.getParameter("prop");
		String[]  prop_ar = ParamsUtils.getProps();
		for (String p : prop_ar) {
			 String[] onprop_ar=p.split(Separator.separator_prop_vlaue);
			 SearchSelector selector = new SearchSelector();
			 	String name = onprop_ar[0];
			 	String value= onprop_ar[1];
	    		selector.setName(name);
	    		selector.setValue(value);
	    		String url= servlet_path +"?"+this.createPropUrlWithoutSome(name, value);
	    		selector.setUrl(url);
	    		selectorList.add(selector);
		}
	 return selectorList;
		
	}
	
	
	
	/**
	 * 检测某个维度是否已经选择
	 * @param dim
	 * @param props
	 * @return 如果没有选择返回null，如果选择了返回属性值
	 */
	private String  checkSelected(String dim,String[] props){
		for (int i = 0; i < props.length; i++) {
			String p = props[i];
			String[] oneprop_ar=p.split(Separator.separator_prop_vlaue);
			if(oneprop_ar[0].equals(dim)){
				return oneprop_ar[1];
			}
		}
		
		return null;
	}
	
	


	
	
	/**
	 * 排除某个属性的方式生成属性字串<br>
	 * search.html?cat=1&prop=p1_1@p2_2 传入p2和2则返回search.html?cat=1&prop=p1_1<br>
	 * 用于生成已经选择的selector的url
	 * @param name
	 * @param value
	 * @return
	 */
	private String createPropUrlWithoutSome(String name,String value){
		Map<String,String> params= ParamsUtils.getReqParams();
		String prop  = params.get("prop");
		if(!StringUtil.isEmpty(prop)){
			prop = prop.replaceAll("("+Separator.separator_prop+"?)"+name+Separator.separator_prop_vlaue+value+"("+Separator.separator_prop+"?)", "");
		}else{
			prop="";
		}
		params.put("prop", prop);
		return ParamsUtils.paramsToUrlString(params);
	}
	
	
}
