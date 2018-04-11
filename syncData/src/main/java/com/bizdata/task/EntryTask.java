package com.bizdata.task;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.bizdata.entity.CustomerCrm;
import com.bizdata.entity.MemberPop;
import com.bizdata.service.CustomerCrmService;
import com.bizdata.service.MemberService;

@Component
public class EntryTask {
	Logger logger = LoggerFactory.getLogger(EntryTask.class);
	@Autowired
	CustomerCrmService customerCrmService;
	
	@Autowired
	MemberService memberService;
	
    static int executeCount = 1;
	
	
	/**
	 * 初始化数据
	 */
	public void initData() {
//		List<String> listCmmemid  = customerCrmService.queryAllCmmemid();
//		System.out.println("要同步的数据" + listCmmemid.size());
//		List<String> listMem = memberService.queryAllCmmemid();
//		System.out.println("存在的数据" + listMem.size());
//		listCmmemid.removeAll(listMem);
//		System.out.println("还未同步的数据" + listCmmemid.size());
		
		
		
//		System.out.println(listCmmemid);
		
//		logger.info("sync Data init begin ....");
//		Date date = getMemberLastUpdateTime();
//		if (date == null) {
//			List<CustomerCrm> customers = new ArrayList<CustomerCrm>();
//			List<MemberPop> members = new ArrayList<MemberPop>();
//			customers = customerCrmService.queryList();
//			members = convertDestData(customers);
//			try {
//				memberService.insertMembers(members);
//			} catch (SQLException e) {
//				logger.error(e.getMessage());
//				System.out.println(e.getMessage());
//			}
//		}
//		logger.info("sync Data init end ....");
		
		doTask();
	}
	
//	@Scheduled(fixedRate = 1000 * 60 * 20)
	public void doTask() {
		logger.info(String.format("execute [%s] times sync Data scheduler begin ....",executeCount));
		Date date = getMemberLastUpdateTime();
		List<CustomerCrm> customers = new ArrayList<CustomerCrm>();
		List<MemberPop> members = new ArrayList<MemberPop>();
		if (date != null) {
			customers = loadSourceDataByCmmaintdate(date);
			members = convertDestData(customers);
			int result = 0;
			try {
				for (MemberPop member : members) {
					if(!memberService.isExit(member.getCmmemid())){
						result = memberService.insertMember(member);
						if (result > -1) {
							continue;
						}
					}else{
						memberService.updateMember(member);
					}
				}
			} catch (SQLException e) {
				logger.error(e.getMessage());
				System.out.println(e.getMessage());
			}
		}
		logger.info(String.format("execute [%s] times sync Data scheduler end ....",executeCount++));
	}
    
   /**
    * get  member last updatetime
    * @return
    */
    public Date getMemberLastUpdateTime(){
    	Date date = null;
    	try {
			date = memberService.queryLastUpdateTime();
		} catch (SQLException e) {
			logger.error(e.getMessage());
			System.out.println(e.getMessage());
		}
    	return date;
    }
    
   
    public List<CustomerCrm> loadSourceDataAll(){
    	List<CustomerCrm> customers = new ArrayList<CustomerCrm>();
    	customers = customerCrmService.queryList();
    	return customers;
    }
    
    
    
    public List<CustomerCrm> loadSourceDataByCmmaintdate(Date date){
    	List<CustomerCrm> customers = new ArrayList<CustomerCrm>();
    	customers = customerCrmService.queryListByCmmaintdate(date);
    	return customers;
    }
    
    public List<MemberPop> convertDestData(List<CustomerCrm> customers){
    	List<MemberPop> members = new ArrayList<MemberPop>();
    	for (CustomerCrm customer : customers) {
			MemberPop member = new MemberPop();
			
			
			member.setUname(Convert8859P1ToGBK(customer.getCmname()));
			member.setEmail(customer.getCmemail());
			member.setName(Convert8859P1ToGBK(customer.getCmname()));
			member.setNickname(Convert8859P1ToGBK(customer.getCmname()));
			member.setMobile(customer.getCmmobile1());
			member.setLastupdate(customer.getCmmaintdate());
			member.setCmmemid(customer.getCmmemid());
			member.setCmcustid(customer.getCmcustid());
			
			
			members.add(member);
		}
    	return members;
    }
    
  //oracle数据库中文编码是iso-8859-1，需要转成utf-8才能存储到mysql
  	public static String Convert8859P1ToGBK(String s)
      {
  		if(StringUtils.isEmpty(s)){
  			return null;
  		}
  		String result =null;
  		try {
  			byte[] buf = s.getBytes("ISO-8859-1");
  			result = new String(buf,"GBK");
  		} catch (Exception e) {
  			e.printStackTrace();
  		}
  		return result;
      }
	
}
