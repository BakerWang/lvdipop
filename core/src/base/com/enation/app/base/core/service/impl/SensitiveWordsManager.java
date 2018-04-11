package com.enation.app.base.core.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.base.core.model.SensitiveWords;
import com.enation.app.base.core.service.ISensitiveWordsManager;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.util.StringUtil;

/**
 * 
 * @ClassName: SensitiveWordsManager
 * @Description: 敏感词管理
 * @author: liuyulei
 * @date: 2016年9月28日 上午11:41:29
 * @since:v61
 */
@Service("sensitiveWordsDbManager")
public class SensitiveWordsManager implements ISensitiveWordsManager {

	@Autowired
	private IDaoSupport daoSupport;

	/* (non Javadoc) 
	 * @Title: add
	 * @Description: TODO
	 * @param sensitiveWords 
	 * @see com.enation.app.javashop.customized.core.service.sensitivewords.ISensitiveWordsManager#add(com.enation.app.javashop.customized.core.model.sensitivewords.SensitiveWords) 
	 * @author： liuyulei
	 * @date：2016年9月28日 下午2:44:08
	 */
	@Override
	public void add(SensitiveWords sensitiveWords) {
		this.daoSupport.insert("es_sensitivewords", sensitiveWords);

	}

	/* (non Javadoc) 
	 * @Title: edit
	 * @Description: TODO
	 * @param sensitiveWords 
	 * @see com.enation.app.javashop.customized.core.service.sensitivewords.ISensitiveWordsManager#edit(com.enation.app.javashop.customized.core.model.sensitivewords.SensitiveWords) 
	 * @author： liuyulei
	 * @date：2016年9月28日 下午2:44:04
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void edit(SensitiveWords sensitiveWords) {
		this.daoSupport.update("es_sensitivewords", sensitiveWords,
				" sensitivewords_id = " + sensitiveWords.getSensitivewords_id());

	}

	/* (non Javadoc) 
	 * @Title: delete
	 * @Description: TODO
	 * @param sensitiveWords_id 
	 * @see com.enation.app.javashop.customized.core.service.sensitivewords.ISensitiveWordsManager#delete(java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年9月28日 下午2:44:00
	 */
	@Override
	public void delete(Integer sensitiveWords_id) {
		SensitiveWords sensitiveWords = this.getSensitiveWords(sensitiveWords_id);
		if (sensitiveWords != null) {
			this.daoSupport.execute("delete from es_sensitivewords where sensitivewords_id = ?",
					sensitiveWords_id);
		}
	}

	/* (non Javadoc) 
	 * @Title: searchSensitiveWordsByParams
	 * @Description: TODO
	 * @param pageNo
	 * @param pageSize
	 * @param params
	 * @return 
	 * @see com.enation.app.javashop.customized.core.service.sensitivewords.ISensitiveWordsManager#searchSensitiveWordsByParams(java.lang.Integer, java.lang.Integer, java.util.Map) 
	 * @author： liuyulei
	 * @date：2016年9月28日 下午2:43:56
	 */
	@Override
	public Page searchSensitiveWordsByParams(Integer pageNo, Integer pageSize, Map params) {
		StringBuffer sql = new StringBuffer(this.createTempleteSQL(""));
		String keyword = params.get("keyword").toString();
		if (!StringUtil.isEmpty(keyword)) {
			sql.append(" and s.sensitivewords_name like '%" + keyword + "%'");
		}
		Page webPage = this.daoSupport.queryForPage(sql.toString(), pageNo, pageSize);
		return webPage;
	}

	/* (non Javadoc) 
	 * @Title: getSensitiveWords
	 * @Description: TODO
	 * @param sensitiveWords_id
	 * @return 
	 * @see com.enation.app.javashop.customized.core.service.sensitivewords.ISensitiveWordsManager#getSensitiveWords(java.lang.Integer) 
	 * @author： liuyulei
	 * @date：2016年9月28日 下午2:43:49
	 */
	@Override
	public SensitiveWords getSensitiveWords(Integer sensitiveWords_id) {
		StringBuffer sql = new StringBuffer(this.createTempleteSQL(""));
		sql.append(" and sensitivewords_id = ?");
		SensitiveWords sensitiveWords = (SensitiveWords) this.daoSupport.queryForObject(sql.toString(),
				SensitiveWords.class, sensitiveWords_id);
		return sensitiveWords;
	}

	/* (non Javadoc) 
	 * @Title: selectListForParams
	 * @Description: TODO
	 * @param map
	 * @return 
	 * @see com.enation.app.javashop.customized.core.service.sensitivewords.ISensitiveWordsManager#selectListForParams(java.util.Map) 
	 * @author： liuyulei
	 * @date：2016年9月28日 下午2:44:17
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public List<SensitiveWords> selectListForParams(Map map) {
		StringBuffer sql = new StringBuffer(this.createTempleteSQL(""));
		List<SensitiveWords> sensitiveWordsList = this.daoSupport.queryForList(sql.toString(), SensitiveWords.class);
		return sensitiveWordsList;
	}


	/* (non Javadoc) 
	 * @Title: delete
	 * @Description: TODO
	 * @param sensitiveWords_id 
	 * @see com.enation.app.javashop.customized.core.service.sensitivewords.ISensitiveWordsManager#delete(java.lang.Integer[]) 
	 * @author： liuyulei
	 * @date：2016年9月28日 下午2:44:28
	 */
	@Override
	public void delete(Integer[] sensitiveWords_id) {
		if (sensitiveWords_id == null || sensitiveWords_id.equals("")) {
			return;
		}
		String id_str = StringUtil.arrayToString(sensitiveWords_id, ",");
		String sql = "delete from es_sensitivewords where sensitivewords_id in (" + id_str + ")";
		this.daoSupport.execute(sql);
	}
	
	/* (non Javadoc) 
	 * @Title: getAll
	 * @Description: TODO
	 * @return 
	 * @see com.enation.app.javashop.customized.core.service.ISensitiveWordsManager#getAll() 
	 * @author： liuyulei
	 * @date：2016年9月29日 上午11:21:21   
	 */
	@Override
	public List getAll() {
		List<Map> listMap = this.daoSupport.queryForList("select * from es_sensitivewords where disabled = 0");
		return listMap;
	}
	
	/**
	 * 
	 * @Title: createTempleteSQL 
	 * @Description: TODO 
	 * @param leftSql  联表字符串   如 ：   left join tableA  a  on a.id=s.sensitivewords_id
	 * @return
	 * @return: String sql语句
	 * @author： liuyulei
	 * @date：2016年9月28日 下午2:44:45
	 */
	private String createTempleteSQL(String leftSql) {
		StringBuffer sql = new StringBuffer();
		sql.append("select * from es_sensitivewords s ");
		if (!StringUtil.isEmpty(leftSql)) {
			sql.append(leftSql);
		}
		sql.append(" where 1=1  and disabled = 0");
		return sql.toString();
	}

	/** 
	 * @Title: getMaxId 
	 * @Description: TODO
	 * @return
	 * @return: Integer
	 * @author： liuyulei
	 * @date：2016年9月30日 上午11:05:37
	 */
	@Override
	public Integer getMaxId() {
		Integer id = this.daoSupport.queryForInt("select MAX(s.sensitivewords_id) from es_sensitivewords s");
		return id;
	}

	
}
