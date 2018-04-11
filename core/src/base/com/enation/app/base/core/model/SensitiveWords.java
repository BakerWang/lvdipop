package com.enation.app.base.core.model;

import java.io.Serializable;

import com.enation.framework.database.PrimaryKeyField;
/**
 * 
 * @ClassName: SensitiveWords 
 * @Description: 敏感词实体
 * @author: liuyulei
 * @date: 2016年9月28日 上午11:26:30 
 * @since:v61
 */
@SuppressWarnings("serial")
public class SensitiveWords implements Serializable {

	private Integer sensitivewords_id;   // 敏感词id
	
	private String  sensitivewords_name; //敏感词
	
	private Integer sensitivewords_status;//敏感词状态   备用字段   暂未启用
	 
	private Integer disabled;      //删除： 回收站：1	 正常 ：0    备用字段 暂未启用

	@PrimaryKeyField
	public Integer getSensitivewords_id() {
		return sensitivewords_id;
	}

	public void setSensitivewords_id(Integer sensitivewords_id) {
		this.sensitivewords_id = sensitivewords_id;
	}

	public String getSensitivewords_name() {
		return sensitivewords_name;
	}

	public void setSensitivewords_name(String sensitivewords_name) {
		this.sensitivewords_name = sensitivewords_name;
	}

	public Integer getSensitivewords_status() {
		return sensitivewords_status;
	}

	public void setSensitivewords_status(Integer sensitivewords_status) {
		this.sensitivewords_status = sensitivewords_status;
	}

	public Integer getDisabled() {
		return disabled;
	}

	public void setDisabled(Integer disabled) {
		this.disabled = disabled;
	}
	
	
	
}
