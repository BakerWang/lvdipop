package com.enation.app.service.core.servicegoods.model;

import java.io.Serializable;

import com.enation.framework.database.NotDbField;
import com.enation.framework.database.PrimaryKeyField;

/**
 * 
 * @ClassName: BookingGoods
 * @Description: 预约商品实体
 * @author: liuyulei
 * @date: 2016年9月22日 下午4:42:09
 * @since:v61
 */
public class BookingGoods implements Serializable {

	private Integer booking_id;  // 预约信息id
	
	private Integer goods_id;// '商品id',
	
	private Integer store_id; // '店铺id',
	
	private Integer member_id;// '会员id',
	
	private Long create_time;// '创建时间',
	
	private String booking_name;// '预约姓名',
	
	private String booking_mobile;// '预约手机号',
	
	private Long booking_time;// '预约时间',
	
	private Integer sex ;    //性别   0 ：男   1：女
	
	private String sex_str;  //性别  字符串   男 女
	
	private String code;   //  预约验证码
	
	private Integer age; // 年龄
	
	private Integer booking_status;// 预约状态     0:申请预约，1：预约成功，2：申请取消预约，3：取消预约成功，4:取消预约失败
	
	
	@PrimaryKeyField
	public Integer getBooking_id() {
		return booking_id;
	}
	public void setBooking_id(Integer booking_id) {
		this.booking_id = booking_id;
	}
	public Integer getGoods_id() {
		return goods_id;
	}
	public void setGoods_id(Integer goods_id) {
		this.goods_id = goods_id;
	}
	public Integer getStore_id() {
		return store_id;
	}
	public void setStore_id(Integer store_id) {
		this.store_id = store_id;
	}
	public Integer getMember_id() {
		return member_id;
	}
	public void setMember_id(Integer member_id) {
		this.member_id = member_id;
	}
	public Long getCreate_time() {
		return create_time;
	}
	public void setCreate_time(Long create_time) {
		this.create_time = create_time;
	}
	public String getBooking_name() {
		return booking_name;
	}
	public void setBooking_name(String booking_name) {
		this.booking_name = booking_name;
	}
	public String getBooking_mobile() {
		return booking_mobile;
	}
	public void setBooking_mobile(String booking_mobile) {
		this.booking_mobile = booking_mobile;
	}
	@NotDbField
	public Long getBooking_time() {
		return booking_time;
	}
	public void setBooking_time(Long booking_time) {
		this.booking_time = booking_time;
	}
	public Integer getSex() {
		return sex;
	}
	public void setSex(Integer sex) {
		this.sex = sex;
	}
	@NotDbField
	public String getSex_str() {
		switch (this.getSex()) {
		case 0:
			this.setSex_str("男");
			break;
		case 1:
			this.setSex_str("女");
			break;
		}
		return sex_str;
	}
	public void setSex_str(String sex_str) {
		this.sex_str = sex_str;
	}
	@NotDbField
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public Integer getBooking_status() {
		return booking_status;
	}
	public void setBooking_status(Integer booking_status) {
		this.booking_status = booking_status;
	}
	
	
	
	

}
