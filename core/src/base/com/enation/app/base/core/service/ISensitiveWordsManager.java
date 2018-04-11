package com.enation.app.base.core.service;

import java.util.List;
import java.util.Map;

import com.enation.app.base.core.model.SensitiveWords;
import com.enation.framework.database.Page;

/**
 * 
 * @ClassName: ISensitiveWordsManager 
 * @Description: 敏感词管理接口
 * @author: liuyulei
 * @date: 2016年9月28日 上午11:33:36 
 * @since:v61
 */
public interface ISensitiveWordsManager {
	
	/**
	 * 
	 * @Title: add 
	 * @Description: TODO   敏感词添加
	 * @param sensitiveWords  敏感词实体   参数
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年9月28日 上午11:34:24
	 */
	public void add(SensitiveWords sensitiveWords);
	
	/**
	 * 
	 * @Title: edit 
	 * @Description: TODO  敏感词修改
	 * @param sensitiveWords 参数  敏感词实体
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年9月28日 上午11:35:13
	 */
	public void edit(SensitiveWords sensitiveWords);
	
	/**
	 * 
	 * @Title: delete 
	 * @Description: TODO  敏感词删除
	 * @param sensitiveWords_id   敏感词id
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年9月28日 上午11:36:09
	 */
	public void delete(Integer sensitiveWords_id);
	
	/**
	 * 
	 * @Title: delete 
	 * @Description: TODO  批量删除敏感词   
	 * @param sensitiveWords_id  敏感词id  数组  Integer类型
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年9月28日 下午2:22:39
	 */
	public void delete(Integer[] sensitiveWords_id);
	/**
	 * 
	 * @Title: searchSensitiveWordsByParams 
	 * @Description: TODO  搜索敏感词   分页
	 * @param pageNo
	 * @param pageSize
	 * @param params
	 * @return
	 * @return: Page
	 * @author： liuyulei
	 * @date：2016年9月28日 上午11:37:47
	 */
	public Page searchSensitiveWordsByParams(Integer pageNo,Integer pageSize,Map params);
	
	/**
	 * 
	 * @Title: getSensitiveWords 
	 * @Description: TODO   根据id查询敏感词数据
	 * @param sensitiveWords_id
	 * @return
	 * @return: SensitiveWords
	 * @author： liuyulei
	 * @date：2016年9月28日 上午11:38:49
	 */
	public SensitiveWords getSensitiveWords(Integer sensitiveWords_id);
	
	/**
	 * 
	 * @Title: selectListForParams 
	 * @Description: TODO  根据参数查询敏感词列表
	 * @param map
	 * @return
	 * @return: List
	 * @author： liuyulei
	 * @date：2016年9月28日 上午11:40:24
	 */
	public List selectListForParams(Map map);
	
	/**
	 * 
	 * @Title: getAll 
	 * @Description: TODO  获取所有敏感词
	 * @return
	 * @return: Map
	 * @author： liuyulei
	 * @date：2016年9月29日 上午11:20:39
	 */
	public List getAll();
	
	/**
	 * 
	 * @Title: getMaxId 
	 * @Description: TODO  获取id最大值
	 * @return
	 * @return: Integer
	 * @author： liuyulei
	 * @date：2016年9月30日 上午11:03:14
	 */
	public Integer getMaxId();
	
	
}
