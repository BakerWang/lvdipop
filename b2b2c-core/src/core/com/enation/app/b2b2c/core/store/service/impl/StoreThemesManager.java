package com.enation.app.b2b2c.core.store.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.app.b2b2c.core.store.model.StoreThemes;
import com.enation.app.b2b2c.core.store.service.IStoreThemesManager;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
/**
 * 店铺模板管理类
 * @author Kanon
 *
 */
@Service("storeThemesManager")
public class StoreThemesManager implements IStoreThemesManager {
	
	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private IStoreMemberManager storeMemberManager;
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.distribution.core.service.store.IStoreThemesManager#list(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	public Page list(Integer pageNo,Integer pageSize) {
		String sql="select * from es_store_themes";
		return daoSupport.queryForPage(sql, pageNo, pageSize);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.distribution.core.service.store.IStoreThemesManager#add(java.lang.String, java.lang.String)
	 */
	@Override
	public void add(StoreThemes storeThemes) {
		String sql="select count(id) from es_store_themes";
		if(daoSupport.queryForInt(sql)==0){
			storeThemes.setIs_default(1);
		}else{
			storeThemes.setIs_default(0);
		}
		this.daoSupport.insert("es_store_themes", storeThemes);
	}

	/*
	 * (non-Javadoc)
	 * @see com.enation.app.distribution.core.service.store.IStoreThemesManager#edit(java.lang.String, java.lang.String)
	 */
	@Override
	public void edit(StoreThemes storeThemes) {

		this.daoSupport.update("es_store_themes", storeThemes, "id="+storeThemes.getId());
	}
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.distribution.core.service.store.IStoreThemesManager#delete(java.lang.String)
	 */
	@Override
	public void delete(Integer id) {
		this.daoSupport.execute("delete from es_store_themes where id=?", id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.distribution.core.service.store.IStoreThemesManager#getStorethThemes(java.lang.Integer)
	 */
	@Override
	public StoreThemes getStorethThemes(Integer id) {
		return (StoreThemes) this.daoSupport.queryForObject("select * from es_store_themes where id=?", StoreThemes.class, id);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.distribution.core.service.store.IStoreThemesManager#getStrorePath(java.lang.Integer)
	 */
	@Override
	public String getStrorePath(Integer store_id) {
		String storePath=this.daoSupport.queryForString("select themes_path from es_store where store_id="+store_id);
		if(storePath!=null){
			return storePath;
		}else{
			return this.getDefaultStoreThemes().getPath();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.distribution.core.service.store.IStoreThemesManager#changeStoreThemes(java.lang.Integer)
	 */
	@Override
	public void changeStoreThemes(Integer themes_id) {
		StoreMember member=storeMemberManager.getStoreMember();
		StoreThemes storeThemes=this.getStorethThemes(themes_id);
		this.daoSupport.execute("update es_store set themes_id=?,themes_path=? where member_id=?", themes_id,storeThemes.getPath(),member.getMember_id());
	}

	@Override
	public StoreThemes getDefaultStoreThemes() {
		return (StoreThemes) this.daoSupport.queryForObject("select * from es_store_themes where is_default=1", StoreThemes.class);
	}

	
	

}
