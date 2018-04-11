package com.enation.app.b2b2c.core.store.service;

import java.util.List;
import java.util.Map;

import com.enation.app.b2b2c.core.store.model.StoreClassify;
import com.enation.framework.database.Page;

/**
 * 
 * @ClassName: IStoreClassifyManager 
 * @Description: 店铺分类管理接口
 * @author: liuyulei
 * @date: 2016年10月10日 下午6:08:06 
 * @since:v61
 */
public interface IStoreClassifyManager {

	
	/**
	 * 
	 * @param parentId
	 * @return
	 */
	public List listChildrenAsyn(Integer parentId);
	
	/**
	 * 
	 * @Title: add 
	 * @Description: TODO  添加店铺分类
	 * @param storeClassify  店铺分类实体
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年10月10日 下午6:09:35
	 */
	public void add(StoreClassify storeClassify);
	
	/**
	 * 
	 * @Title: edit 
	 * @Description: TODO  修改店铺分类
	 * @param storeClassify  店铺分类实体
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年10月10日 下午6:10:25
	 */
	public void edit(StoreClassify storeClassify);
	
	/**
	 * 
	 * @Title: getStoreClassify 
	 * @Description: TODO 根据id获取店铺分类详细信息
	 * @param sclassify_id    店铺分类id
	 * @return  店铺分类实体
	 * @return: StoreClassify
	 * @author： liuyulei
	 * @date：2016年10月10日 下午6:11:46
	 */
	public StoreClassify getStoreClassify(Integer sclassify_id);
	
	/**
	 * 
	 * @Title: getStoreClassifyList 
	 * @Description: TODO  根据参数  获取店铺分类   list
	 * @param params       参数
	 * @return  店铺分类   list
	 * @return: List
	 * @author： liuyulei
	 * @date：2016年10月10日 下午6:13:40
	 */
	public List getStoreClassifyList(Map params);
	
	/**
	 * 
	 * @Title: searchStoreClassify 
	 * @Description: TODO   获取分页店铺分类数据
	 * @param pageNo   
	 * @param pageSize  
	 * @param params  搜索参数
	 * @return
	 * @return: Page
	 * @author： liuyulei
	 * @date：2016年10月10日 下午6:16:15
	 */
	public Page searchStoreClassify(Integer pageNo,Integer pageSize,Map params); 
	
	
	/**
	 * 
	 * @param classifyId
	 */
	public void delete(Integer classifyId);
	
	
	public List getAll();
	
	public List getByCityId(Integer cityId);
	
}
