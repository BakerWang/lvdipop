package com.enation.app.service.core.servicegoods.model;

import java.io.Serializable;

import com.enation.framework.database.NotDbField;
import com.enation.framework.database.PrimaryKeyField;

/**
 * 
 * @ClassName: ServiceGoods 
 * @Description: 服务商品 属性
 * @author: liuyulei
 * @date: 2016年9月14日 下午4:54:26 
 * @since:v61
 */
public class ServiceGoods implements Serializable {

	private Integer service_id;

	private Integer item_id; // 订单项id

	private Integer valid_term; // 有效时间 单位：天

	private String code; // 服务验证码

	private Integer status = 0; //是否使用         默认为 0 可以使用          1：正常使用后，       2：超过有效期           3:失效   （申请退货、退款成功后）

	private Long create_time; // 购买日期

	private Long enable_time; // 有效日期

	private String useDate;// 使用日期 非数据库字段
	
	private Integer store_id;
	
	private Integer look_status;   //查看状态，0默认值  未查看       1  已查看
	

	@PrimaryKeyField
	public Integer getService_id() {
		return service_id;
	}

	public void setService_id(Integer service_id) {
		this.service_id = service_id;
	}

	public Integer getItem_id() {
		return item_id;
	}

	public void setItem_id(Integer item_id) {
		this.item_id = item_id;
	}

	public Integer getValid_term() {
		return valid_term;
	}

	public void setValid_term(Integer valid_term) {
		this.valid_term = valid_term;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@NotDbField
	public String getUseDate() {
		return useDate;
	}

	public void setUseDate(String useDate) {
		this.useDate = useDate;
	}

	public Long getCreate_time() {
		return create_time;
	}

	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}

	public Long getEnable_time() {
		return enable_time;
	}

	public void setEnable_time(Long enable_time) {
		this.enable_time = enable_time;
	}

	public Integer getLook_status() {
		return look_status;
	}

	public void setLook_status(Integer look_status) {
		this.look_status = look_status;
	}

	public Integer getStore_id() {
		return store_id;
	}

	public void setStore_id(Integer store_id) {
		this.store_id = store_id;
	}

	
}
