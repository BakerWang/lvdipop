package com.enation.app.cms.core.service;

import java.util.List;

import com.enation.app.cms.core.model.DataModel;

/**
 * 数据模型管理
 * @author kingapex
 * 2010-7-2下午02:25:37
 */
public interface IDataModelManager {
	/**
	 * 添加一个数据模型
	 * @param dataModel
	 */
	public void add(DataModel dataModel);
	
	
	/**
	 * 修改一个数据模型
	 * @param dataModel
	 */
	public void edit(DataModel dataModel);
	
	
	/**
	 * 删除一个数据模型
	 * @param modelid
	 */
	public void delete(Integer modelid);
	
	/**
	 * 读取所有数据模型列表
	 * @return
	 */
	public List<DataModel>  list();
	
	
	/**
	 * 获取一个数据模型
	 * @param modelid
	 * @return
	 */
	public DataModel get(Integer modelid);
	
	/**
	 * 检查数据模型是否已经被使用
	 * @param modelid
	 * @return 0:未被使用
	 */
	public int checkIfModelInUse(Integer modelid);
	
	/**
	 * 检查数据模型是否已经被使用
	 * add by DMRain 2016-4-21
	 * @param name 数据模型名称
	 * @param modelid 模型ID
	 * @return
	 */
	public int checkIfModelInUse(String name, Integer modelid);
	
	
	/**
	 * 根据模型英文名获得数据模型
	 * @param englishName 英文名
	 * @return
	 */
	public DataModel get(String englishName);
	
	
}
