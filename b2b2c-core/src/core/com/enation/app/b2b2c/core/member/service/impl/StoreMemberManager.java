package com.enation.app.b2b2c.core.member.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.b2b2c.core.member.model.StoreMember;
import com.enation.app.b2b2c.core.member.service.IStoreMemberManager;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.IDaoSupport;

/**
 * 多店会员管理类
 * @author Kanon 2016-3-2；6.0版本改造
 *
 */
@Service("storeMemberManager")
public class StoreMemberManager  implements IStoreMemberManager{
	
	@Autowired
	private IDaoSupport daoSupport;
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.member.IStoreMemberManager#edit(com.enation.app.b2b2c.core.model.member.StoreMember)
	 */
	public void edit(StoreMember member) {
		this.daoSupport.update("es_member", member, "member_id=" + member.getMember_id());
		ThreadContextHolder.getSession().setAttribute(this.CURRENT_STORE_MEMBER_KEY, member);
	}
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.member.IStoreMemberManager#getMember(java.lang.Integer)
	 */
	@Override
	public StoreMember getMember(Integer member_id) {
		String sql="select * from es_member where member_id=? and disabled!=1";
		return (StoreMember) this.daoSupport.queryForObject(sql, StoreMember.class, member_id);
	}
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.member.IB2b2cMemberManager#getStoreMember()
	 */
	@Override
	public StoreMember getStoreMember() {
		StoreMember member=(StoreMember) ThreadContextHolder.getSession().getAttribute(this.CURRENT_STORE_MEMBER_KEY);
		return member;
	}
	/*
	 * (non-Javadoc)
	 * @see com.enation.app.b2b2c.core.service.member.IStoreMemberManager#getMember(java.lang.String)
	 */
	@Override
	public StoreMember getMember(String member_name) {
		String sql="select * from es_member where uname=? and disabled!=1";
		return (StoreMember) this.daoSupport.queryForObject(sql, StoreMember.class,member_name );
	}
	
}

