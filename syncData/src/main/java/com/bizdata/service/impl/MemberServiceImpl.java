package com.bizdata.service.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.stereotype.Service;
import com.bizdata.entity.MemberPop;
import com.bizdata.service.MemberService;

@Service
public class MemberServiceImpl implements MemberService {
	final static String insertsql = "insert into es_member (uname,email,name,nickname,lastupdate,cmmemid,cmcustid,mobile,lv_id) values (?,?,?,?,?,?,?,?,1)";
	final static String updatesql = "update es_member set uname = ?,email = ?,name = ?,nickname = ?,lastupdate = ?,mobile = ? where cmmemid = ?";

	@Autowired
	@Qualifier("sourceJdbcTemplate")
	JdbcTemplate jdbcTemplate;

	@Override
	public Date queryLastUpdateTime() throws SQLException {
		Date date = jdbcTemplate.queryForObject("select max(lastupdate) from es_member", Date.class);
		return date;
	}

	@Override
	public void insertMembers(List<MemberPop> members) throws SQLException {
		jdbcTemplate.batchUpdate(insertsql, new MemberBatchPreparedStatementSetter(members));
	}

	@Override
	public int insertMember(MemberPop member) throws SQLException {
		return jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(insertsql, PreparedStatement.RETURN_GENERATED_KEYS);
				ps.setObject(1, member.getUname());
				ps.setObject(2, member.getEmail());
				ps.setObject(3, member.getName());
				ps.setObject(4, member.getNickname());
				ps.setObject(5, member.getLastupdate());
				ps.setObject(6, member.getCmmemid());
				ps.setObject(7, member.getCmcustid());
				ps.setObject(8, member.getMobile());
				return ps;
			}
		});
	}

	@Override
	public int updateMember(MemberPop member) throws SQLException {
		return jdbcTemplate.update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement ps = connection.prepareStatement(updatesql, PreparedStatement.RETURN_GENERATED_KEYS);
				ps.setObject(1, member.getUname());
				ps.setObject(2, member.getEmail());
				ps.setObject(3, member.getName());
				ps.setObject(4, member.getNickname());
				ps.setObject(5, member.getLastupdate());
				ps.setObject(6, member.getMobile());
				ps.setObject(7, member.getCmmemid());
				return ps;
			}
		});
	}

	@Override
	public boolean isExit(String cmmemid) throws SQLException {
		boolean flag = false;
		int result = jdbcTemplate.queryForObject("select count(1) from es_member where cmmemid = " + cmmemid,
				Integer.class);
		if (result > 0) {
			flag = true;
		}
		return flag;
	}

	@Override
	public List<String> queryAllCmmemid() {
		List<String> list = jdbcTemplate.queryForList("SELECT cmmemid FROM es_member", String.class);
		return list;
	}
}
