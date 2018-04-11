package com.bizdata.service;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import com.bizdata.entity.MemberPop;

public interface MemberService {
	
	 Date queryLastUpdateTime() throws SQLException;
	 
	 void insertMembers(List<MemberPop> members) throws SQLException;
	 
	 int insertMember(MemberPop member) throws SQLException;
	 
	 int updateMember(MemberPop member) throws SQLException;
	 
	 boolean isExit(String cmmemid) throws SQLException;
	 
	 List<String> queryAllCmmemid();
	 
	 

}
