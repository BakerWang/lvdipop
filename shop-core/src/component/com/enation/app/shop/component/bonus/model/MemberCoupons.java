package com.enation.app.shop.component.bonus.model;

/**
 * 会员优惠券实体类
 * @author DMRain
 * @date 2016-9-22
 * @since v61
 * @version 1.0
 */
public class MemberCoupons {
	/** 会员优惠券id */
	private Integer mcoup_id;
	
	/** 店铺优惠券id(父id) */
	private Integer coupons_id;
	
	/** 优惠券所属店铺id */
	private Integer store_id;
	
	/** 优惠券名称 */
	private String mcoup_name;
	
	/** 优惠券识别码 */
	private String mcoup_code;
	
	/** 优惠券金额 */
	private Double mcoup_money;
	
	/** 优惠券折扣 */
	private Integer mcoup_discount;
	
	/** 优惠券类型 0：现金券，1：折扣券, 2:现金券 */
	private Integer mcoup_type;
	
	/** 最小订单金额 */
	private Double min_order_money;
	
	/** 发放日期 */
	private Long mcoup_create_date;
	
	/** 优惠券生效日期 */
	private Long mcoup_start_date;
	
	/** 优惠券失效日期 */
	private Long mcoup_end_date;
	
	/** 优惠券适用地区省份id */
	private Integer mcoup_province_id;
	
	/** 优惠券适用地区省份名称 */
	private String mcoup_province_name;
	
	/** 是否已领取 0：未领取，1：已领取 */
	private Integer is_received;
	
	/** 领取日期 */
	private Long received_date;
	
	/** 是否已使用 0：未使用，1：已使用 */
	private Integer is_used;
	
	/** 使用日期 */
	private Long used_date;
	
	/** 订单id */
	private Integer order_id;
	
	/** 订单号 */
	private String order_sn;
	
	/** 优惠券所属会员id */
	private Integer member_id;
	
	/** 优惠券所属会员名称 */
	private String member_name;
	
	/** 优惠券所属会员手机号 */
	private String member_mobile;
	
	/** 状态 0：正常，1：无效*/
	private Integer status;
	
	/** 是否已删除 0：未删除，1：已删除 */
	private Integer is_deleted;

	/** 优惠券获取途径 0：店铺领取，1：活动奖励，2：店铺发放 */
	private Integer get_type;
	
	/** 优惠券描述 */
	private String coupons_describe;
	
	private Integer is_check;
	
	public Integer getMcoup_id() {
		return mcoup_id;
	}

	public void setMcoup_id(Integer mcoup_id) {
		this.mcoup_id = mcoup_id;
	}

	public Integer getCoupons_id() {
		return coupons_id;
	}

	public void setCoupons_id(Integer coupons_id) {
		this.coupons_id = coupons_id;
	}

	public Integer getStore_id() {
		return store_id;
	}

	public void setStore_id(Integer store_id) {
		this.store_id = store_id;
	}

	public String getMcoup_name() {
		return mcoup_name;
	}

	public void setMcoup_name(String mcoup_name) {
		this.mcoup_name = mcoup_name;
	}

	public String getMcoup_code() {
		return mcoup_code;
	}

	public void setMcoup_code(String mcoup_code) {
		this.mcoup_code = mcoup_code;
	}

	public Double getMcoup_money() {
		return mcoup_money;
	}

	public void setMcoup_money(Double mcoup_money) {
		this.mcoup_money = mcoup_money;
	}

	public Integer getMcoup_discount() {
		return mcoup_discount;
	}

	public void setMcoup_discount(Integer mcoup_discount) {
		this.mcoup_discount = mcoup_discount;
	}

	public Integer getMcoup_type() {
		return mcoup_type;
	}

	public void setMcoup_type(Integer mcoup_type) {
		this.mcoup_type = mcoup_type;
	}

	public Double getMin_order_money() {
		return min_order_money;
	}

	public void setMin_order_money(Double min_order_money) {
		this.min_order_money = min_order_money;
	}

	public Long getMcoup_create_date() {
		return mcoup_create_date;
	}

	public void setMcoup_create_date(Long mcoup_create_date) {
		this.mcoup_create_date = mcoup_create_date;
	}

	public Long getMcoup_start_date() {
		return mcoup_start_date;
	}

	public void setMcoup_start_date(Long mcoup_start_date) {
		this.mcoup_start_date = mcoup_start_date;
	}

	public Long getMcoup_end_date() {
		return mcoup_end_date;
	}

	public void setMcoup_end_date(Long mcoup_end_date) {
		this.mcoup_end_date = mcoup_end_date;
	}

	public Integer getMcoup_province_id() {
		return mcoup_province_id;
	}

	public void setMcoup_province_id(Integer mcoup_province_id) {
		this.mcoup_province_id = mcoup_province_id;
	}

	public String getMcoup_province_name() {
		return mcoup_province_name;
	}

	public void setMcoup_province_name(String mcoup_province_name) {
		this.mcoup_province_name = mcoup_province_name;
	}

	public Integer getIs_received() {
		return is_received;
	}

	public void setIs_received(Integer is_received) {
		this.is_received = is_received;
	}

	public Long getReceived_date() {
		return received_date;
	}

	public void setReceived_date(Long received_date) {
		this.received_date = received_date;
	}

	public Integer getIs_used() {
		return is_used;
	}

	public void setIs_used(Integer is_used) {
		this.is_used = is_used;
	}

	public Long getUsed_date() {
		return used_date;
	}

	public void setUsed_date(Long used_date) {
		this.used_date = used_date;
	}

	public Integer getOrder_id() {
		return order_id;
	}

	public void setOrder_id(Integer order_id) {
		this.order_id = order_id;
	}

	public String getOrder_sn() {
		return order_sn;
	}

	public void setOrder_sn(String order_sn) {
		this.order_sn = order_sn;
	}

	public Integer getMember_id() {
		return member_id;
	}

	public void setMember_id(Integer member_id) {
		this.member_id = member_id;
	}

	public String getMember_name() {
		return member_name;
	}

	public void setMember_name(String member_name) {
		this.member_name = member_name;
	}

	public String getMember_mobile() {
		return member_mobile;
	}

	public void setMember_mobile(String member_mobile) {
		this.member_mobile = member_mobile;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getIs_deleted() {
		return is_deleted;
	}

	public void setIs_deleted(Integer is_deleted) {
		this.is_deleted = is_deleted;
	}

	public Integer getGet_type() {
		return get_type;
	}

	public void setGet_type(Integer get_type) {
		this.get_type = get_type;
	}

	public String getCoupons_describe() {
		return coupons_describe;
	}

	public void setCoupons_describe(String coupons_describe) {
		this.coupons_describe = coupons_describe;
	}

	public Integer getIs_check() {
		return is_check;
	}

	public void setIs_check(Integer is_check) {
		this.is_check = is_check;
	}

}
