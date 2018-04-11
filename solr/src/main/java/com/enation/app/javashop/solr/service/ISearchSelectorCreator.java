/**
 * 
 */
package com.enation.app.javashop.solr.service;

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.response.FacetField;

/**
 * 
 * 搜索选择器生成接口
 * @author kingapex
 *2015-4-23
 */
public interface ISearchSelectorCreator {
	
	
	/**
	 * 生成选择器并压入指定的map
	 * @param map
	 */
	public void createAndPut(Map<String,Object>  map,List<FacetField> results);
}
