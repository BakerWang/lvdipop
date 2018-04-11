package com.enation.app.b2b2c.core.member.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.b2b2c.core.member.service.IStoreMemberAddressManager;
import com.enation.framework.database.IDaoSupport;

/**
 * 多店会员地址管理类
 * @author Kanon 2016-3-2；6.0版本改造
 *
 */
@Service("storeMemberAddressManager")
public class StoreMemberAddressManager implements IStoreMemberAddressManager {
	
	@Autowired
	private IDaoSupport daoSupport;
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.member.service.IStoreMemberAddressManager#updateMemberAddress(java.lang.Integer, java.lang.Integer)
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void updateMemberAddress(Integer memberid,Integer addr_id) {
		this.daoSupport.execute("update es_member_address set def_addr=0 where member_id=?", memberid);
		this.daoSupport.execute("update es_member_address set def_addr=1 where addr_id=?", addr_id);
	}

	@Override
	public Integer getRegionid(Integer member_id) {
		String sql = "select region_id from es_member_address where member_id=? and def_addr=1";
		List<Map> result=this.daoSupport.queryForList(sql, member_id);
		if(result.size()==0){
			return 0;
		}else{
			return (Integer)result.get(0).get("region_id");
		}
	}
	

}
