package com.bizdata.service.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.util.CollectionUtils;

import com.bizdata.entity.MemberPop;

public class MemberBatchPreparedStatementSetter implements BatchPreparedStatementSetter {
	private List<MemberPop> members;

	public MemberBatchPreparedStatementSetter(List<MemberPop> members) {
		this.members = members;
	}

	@Override
	public int getBatchSize() {
		if (CollectionUtils.isEmpty(members)) {
			return 0;
		}
		return members.size();
	}

	@Override
	public void setValues(PreparedStatement ps, int i) throws SQLException {
		if (!CollectionUtils.isEmpty(members)) {
			MemberPop member = members.get(i);
			ps.setObject(1, member.getUname());
			ps.setObject(2, member.getEmail());
			ps.setObject(3, member.getName());
			ps.setObject(4, member.getNickname());
			ps.setObject(5, member.getLastupdate());
			ps.setObject(6, member.getCmmemid());
			ps.setObject(7, member.getCmcustid());
			ps.setObject(8, member.getMobile());
		}
	}

}
