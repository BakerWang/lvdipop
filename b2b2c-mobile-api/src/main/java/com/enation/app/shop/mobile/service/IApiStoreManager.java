package com.enation.app.shop.mobile.service;

import java.util.Map;

import com.enation.framework.database.Page;

/**
 * 
 * @Description: 店铺api
 * @author: liuyulei
 * @date: 2016年10月24日 上午11:56:03 
 * @since:v61
 */
public interface IApiStoreManager {

	/**
	 * 
	 * @Description: 获取店铺列表
	 * @param other  【Map】     参数列表  
	 * @param disabled  
	 * @param pageNo
	 * @param pageSize
	 * @return   返回店铺列表分页
	 * @return: Page
	 * @author： liuyulei
	 * @date：2016年10月24日 上午11:58:34
	 */
	@SuppressWarnings("rawtypes")
	public Page storeList(Map other, Integer disabled, int pageNo, int pageSize) ;
	
	
	/**
	 * 
	 * @Description: 根据店铺id   获取店铺详情
	 * @param storeId  【Integer】   店铺id
	 * @return
	 * @return: Map
	 * @author： liuyulei
	 * @date：2016年10月24日 上午11:59:16
	 */
	@SuppressWarnings("rawtypes")
	public Map getStore(Integer storeId);
}
