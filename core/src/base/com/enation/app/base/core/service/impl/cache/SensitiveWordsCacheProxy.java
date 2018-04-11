package com.enation.app.base.core.service.impl.cache;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.base.core.model.SensitiveWords;
import com.enation.app.base.core.service.ISensitiveWordsManager;
import com.enation.framework.cache.AbstractCacheProxy;
import com.enation.framework.cache.CacheFactory;
import com.enation.framework.cache.ICache;
import com.enation.framework.database.Page;

/**
 * 
 * @ClassName: SensitiveWordsCacheProxy
 * @Description: 敏感词缓存
 * @author: liuyulei
 * @date: 2016年9月30日 上午10:27:36
 * @since:v61
 */

@Service("sensitiveWordsManager")
public class SensitiveWordsCacheProxy extends AbstractCacheProxy<List<SensitiveWords>>
		implements ISensitiveWordsManager {
	public static final String SENSITIVEWORDS_LIST_CACHE_KEY = "sensitiveWordsList";
	public static final String SENSITIVEWORDS_MAP_CACHE_KEY = "sensitiveWordsMap";

	@Autowired
	private ISensitiveWordsManager sensitiveWordsManager;

	@Autowired
	public SensitiveWordsCacheProxy(ISensitiveWordsManager sensitiveWordsDbManager) {
		this.sensitiveWordsManager = sensitiveWordsDbManager;
	}

	/* (non Javadoc) 
	 * @Title: add
	 * @Description: TODO
	 * @param sensitiveWords 
	 * @see com.enation.app.javashop.customized.core.service.ISensitiveWordsManager#add(com.enation.app.javashop.customized.core.model.SensitiveWords) 
	 * @author： liuyulei
	 * @date：2016年9月30日 上午10:38:33
	 */
	@Override
	public void add(SensitiveWords sensitiveWords) {
		this.sensitiveWordsManager.add(sensitiveWords);
		this.cleanCache();
	}

	/* (non Javadoc) 
	 * @Title: edit
	 * @Description: TODO
	 * @param sensitiveWords 
	 * @see com.enation.app.javashop.customized.core.service.ISensitiveWordsManager#edit(com.enation.app.javashop.customized.core.model.SensitiveWords) 
	 * @author： liuyulei
	 * @date：2016年9月30日 上午10:38:39
	 */
	@Override
	public void edit(SensitiveWords sensitiveWords) {
		this.sensitiveWordsManager.edit(sensitiveWords);
		this.cleanCache();
	}

	/* (non Javadoc) 
	 * @Title: delete
	 * @Description: TODO
	 * @param sensitiveWords_id 
	 * @see com.enation.app.javashop.customized.core.service.ISensitiveWordsManager#delete(java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年9月30日 上午10:38:44
	 */
	@Override
	public void delete(Integer sensitiveWords_id) {
		this.sensitiveWordsManager.delete(sensitiveWords_id);
		this.cleanCache();
	}

	/* (non Javadoc) 
	 * @Title: delete
	 * @Description: TODO
	 * @param sensitiveWords_id 
	 * @see com.enation.app.javashop.customized.core.service.ISensitiveWordsManager#delete(java.lang.Integer[]) 
	 * @author： liuyulei
	 * @date：2016年9月30日 上午10:38:49
	 */
	@Override
	public void delete(Integer[] sensitiveWords_id) {
		this.sensitiveWordsManager.delete(sensitiveWords_id);
		this.cleanCache();
	}

	/* (non Javadoc) 
	 * @Title: searchSensitiveWordsByParams
	 * @Description: TODO
	 * @param pageNo
	 * @param pageSize
	 * @param params
	 * @return 
	 * @see com.enation.app.javashop.customized.core.service.ISensitiveWordsManager#searchSensitiveWordsByParams(java.lang.Integer, java.lang.Integer, java.util.Map) 
	 * @author： liuyulei
	 * @date：2016年9月30日 上午10:38:54
	 */
	@Override
	public Page searchSensitiveWordsByParams(Integer pageNo, Integer pageSize, Map params) {
		return this.sensitiveWordsManager.searchSensitiveWordsByParams(pageNo, pageSize, params);
	}

	/* (non Javadoc) 
	 * @Title: getSensitiveWords
	 * @Description: TODO
	 * @param sensitiveWords_id
	 * @return 
	 * @see com.enation.app.javashop.customized.core.service.ISensitiveWordsManager#getSensitiveWords(java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年9月30日 上午10:38:59
	 */
	@Override
	public SensitiveWords getSensitiveWords(Integer sensitiveWords_id) {
		return this.sensitiveWordsManager.getSensitiveWords(sensitiveWords_id);
	}

	/* (non Javadoc) 
	 * @Title: selectListForParams
	 * @Description: TODO
	 * @param map
	 * @return 
	 * @see com.enation.app.javashop.customized.core.service.ISensitiveWordsManager#selectListForParams(java.util.Map) 
	 * @author： liuyulei
	 * @date：2016年9月30日 上午10:39:10
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List selectListForParams(Map map) {
		ICache cache=CacheFactory.getCache(SENSITIVEWORDS_LIST_CACHE_KEY);
		List<SensitiveWords> sensitiveWordsList  =  (List<SensitiveWords>) cache.get( SENSITIVEWORDS_LIST_CACHE_KEY);
		
		if(sensitiveWordsList== null ){
			sensitiveWordsList = this.sensitiveWordsManager.selectListForParams(map);
			cache.put( SENSITIVEWORDS_LIST_CACHE_KEY,sensitiveWordsList);
			if(this.logger.isDebugEnabled()){
				this.logger.debug("load sitemenu from database");
			}
		}else{
			if(this.logger.isDebugEnabled()){
				this.logger.debug("load sitemenu from cache");
			}
		}
		return sensitiveWordsList;
	}

	/* (non Javadoc) 
	 * @Title: getAll
	 * @Description: TODO
	 * @return 
	 * @see com.enation.app.javashop.customized.core.service.ISensitiveWordsManager#getAll() 
	 * @author： liuyulei
	 * @date：2016年9月30日 上午10:39:14
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List getAll() {
		ICache cache=CacheFactory.getCache(SENSITIVEWORDS_MAP_CACHE_KEY);
		List list  =   (List) cache.get( SENSITIVEWORDS_MAP_CACHE_KEY);
		
		if(list== null ){
			list = this.sensitiveWordsManager.getAll();
			cache.put( SENSITIVEWORDS_MAP_CACHE_KEY,list);
			if(this.logger.isDebugEnabled()){
				this.logger.debug("load sitemenu from database");
			}
		}else{
			if(this.logger.isDebugEnabled()){
				this.logger.debug("load sitemenu from cache");
			}
		}
		return list;
	}
	
	/* (non Javadoc) 
	 * @Title: getMaxId
	 * @Description: TODO
	 * @return 
	 * @see com.enation.app.javashop.customized.core.service.ISensitiveWordsManager#getMaxId() 
	 * @author： liuyulei
	 * @date：2016年9月30日 上午11:06:24
	 */
	@Override
	public Integer getMaxId() {
		return this.sensitiveWordsManager.getMaxId();
	}
	
	/** 
	 * @Title: cleanCache 
	 * @Description: TODO  清楚缓存
	 * @return: void
	 * @author： liuyulei
	 * @date：2016年9月30日 上午10:39:20
	 */
	@SuppressWarnings("rawtypes")
	private void cleanCache() {
		ICache cache = CacheFactory.getCache(SENSITIVEWORDS_LIST_CACHE_KEY);
		if (cache != null) {
			cache.remove(SENSITIVEWORDS_LIST_CACHE_KEY);
		}
		cache = CacheFactory.getCache(SENSITIVEWORDS_MAP_CACHE_KEY);
		if (cache != null) {
			cache.remove(SENSITIVEWORDS_MAP_CACHE_KEY);
		}
	}
}
