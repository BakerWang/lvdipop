package com.enation.app.javashop.customized.core.service.impl;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.enation.app.javashop.customized.core.service.IMemberCouponsManager;
import com.enation.app.shop.component.bonus.model.MemberCoupons;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.jms.IJmsProcessor;
import com.enation.framework.util.DateUtil;
import com.enation.framework.util.StringUtil;


/**
 * 
 * @author Sylow
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
@Service("memberCouponsCreateProcessor")
public class MemberCouponsCreateProcessor implements IJmsProcessor {
	
	@Autowired
	private IDaoSupport daoSupport;

	@Autowired
	private IMemberCouponsManager memberCouponsManager;
	
	public static final String PRGRESSID = "member_coupons_";

	/*
	 * (non-Javadoc)
	 * @see com.enation.framework.jms.IJmsProcessor#process(java.lang.Object)
	 */
	@Override
	public void process(Object data) {
		Map<String, Object> map = (Map<String, Object>) data;
		MemberCoupons memberCoupons = (MemberCoupons) map.get("memberCoupons");
		
		
		Integer num = StringUtil.toInt(map.get("num").toString(), 0);
		this.sendMemberCoupons(memberCoupons, num);
		
	}

	
	private void sendMemberCoupons( MemberCoupons memberCoupons, int num) {
		while (num > 0) {
			
			//获取随机生成的优惠券识别码
			String code = this.createCode(memberCoupons.getCoupons_id().toString());
			
			//检查识别码是否重复
			int result = this.memberCouponsManager.checkCode(code);
			
			//如果生成的识别码不存在
			if (result == 0) {
				memberCoupons.setMcoup_code(code);
				this.memberCouponsManager.add(memberCoupons);
				num--;
			} else {
				//System.out.println("有相同的序列号,再生成一个序列号");
			}
			
		}
	}
	
	/**
	 * 随机生成一个优惠券识别码
	 * @param prefix 参数值
	 * @return
	 */
    private String createCode(String prefix){
		
		StringBuffer sb = new StringBuffer();
		sb.append(prefix);
		sb.append( DateUtil.toString(new Date(), "yyss"));
		sb.append( createRandom() );
		
		return sb.toString();
	}
    
    /**
	 * 生成一个随机数
	 * @return
	 */
	private String createRandom(){
		Random random  = new Random();
		StringBuffer pwd=new StringBuffer();
		for(int i = 0; i < 6; i++){
			pwd.append(random.nextInt(9));
			 
		}
		return pwd.toString();
	}
	
	
}
