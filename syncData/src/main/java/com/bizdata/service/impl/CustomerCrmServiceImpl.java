package com.bizdata.service.impl;

import java.text.SimpleDateFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.bizdata.entity.CustomerCrm;
import com.bizdata.service.CustomerCrmService;

@Service
public class CustomerCrmServiceImpl implements CustomerCrmService {
	final static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Autowired
	@Qualifier("destJdbcTemplate")
	JdbcTemplate jdbcTemplate;

	@Override
	public List<CustomerCrm> queryList() {
		return jdbcTemplate.query(
				"SELECT cmmemid,cmcustid,cmmaintdate,cmname,cmbirthday,cmsex,cmmobile1,cmmobile2 FROM CUSTMEMBER",
				new Object[] {}, new BeanPropertyRowMapper<CustomerCrm>(CustomerCrm.class));
	}

	@Override
	public List<CustomerCrm> queryListByCmmaintdate(java.util.Date date) {
		String dateString = dateFormat.format(date);
		return jdbcTemplate
				.query("SELECT cmmemid,cmcustid,cmmaintdate,cmname,cmbirthday,cmsex,cmmobile1,cmmobile2 FROM CUSTMEMBER WHERE cmmaintdate > " + 
						"to_date('"+dateString +"','yyyy-MM-dd hh24:mi:ss')", new Object[] {}, new BeanPropertyRowMapper<CustomerCrm>(CustomerCrm.class));
	}

	@Override
	public List<String> queryAllCmmemid() {
		List<String> list = jdbcTemplate.queryForList("SELECT cmmemid FROM CUSTMEMBER", String.class);
		return list;
	}
	
	
	
	
	

}
