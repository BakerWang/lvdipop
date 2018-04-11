package com.enation.app.b2b2c.core.store.model;

/**
 * 店铺优惠券实体类
 * @author DMRain
 * @date 2016-9-22
 * @since v61
 * @version 1.0
 */
public class StoreCoupons {

	/** 优惠券id */
	private Integer coupons_id;
	
	/** 店铺id */
	private Integer store_id;
	
	/** 优惠券名称 */
	private String coupons_name;
	
	/** 优惠券库存 */
	private Integer coupons_stock;
	
	/** 优惠券金额 */
	private Double coupons_money;
	
	/** 优惠券折扣数 */
	private Integer coupons_discount;
	
	/** 优惠券类型 0：满减券，1：折扣券, 2:现金券 */
	private Integer coupons_type;
	
	/** 优惠券可以使用的最小订单金额 */
	private Double min_order_money;
	
	/** 优惠券创建日期 */
	private Long create_date;
	
	/** 优惠券生效日期 */
	private Long start_date;
	
	/** 优惠券失效日期 */
	private Long end_date;
	
	/** 优惠券已被领取的数量 */
	private Integer received_num;
	
	/** 优惠券已被使用的数量 */
	private Integer used_num;
	
	/** 优惠券限领数量 */
	private Integer limit_num;
	
	/** 优惠券限制使用的地区省份id */
	private Integer province_id;
	
	/** 优惠券限制使用的地区省份名称 */
	private String province_name;
	
	/** 优惠券发放状态 0:未发放，1：已发放 */
	private Integer send_status;

	/** 优惠券状态 0：正常，2：无效 */
	private Integer status;
	
	/** 优惠券是否删除 0：未删除，1：已删除 */
	private Integer is_deleted;
	
	/** 优惠券描述 */
	private String coupons_describe;
	
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

	public String getCoupons_name() {
		return coupons_name;
	}

	public void setCoupons_name(String coupons_name) {
		this.coupons_name = coupons_name;
	}

	public Integer getCoupons_stock() {
		return coupons_stock;
	}

	public void setCoupons_stock(Integer coupons_stock) {
		this.coupons_stock = coupons_stock;
	}

	public Double getCoupons_money() {
		return coupons_money;
	}

	public void setCoupons_money(Double coupons_money) {
		this.coupons_money = coupons_money;
	}

	public Integer getCoupons_discount() {
		return coupons_discount;
	}

	public void setCoupons_discount(Integer coupons_discount) {
		this.coupons_discount = coupons_discount;
	}

	public Integer getCoupons_type() {
		return coupons_type;
	}

	public void setCoupons_type(Integer coupons_type) {
		this.coupons_type = coupons_type;
	}

	public Double getMin_order_money() {
		return min_order_money;
	}

	public void setMin_order_money(Double min_order_money) {
		this.min_order_money = min_order_money;
	}

	public Long getCreate_date() {
		return create_date;
	}

	public void setCreate_date(Long create_date) {
		this.create_date = create_date;
	}

	public Long getStart_date() {
		return start_date;
	}

	public void setStart_date(Long start_date) {
		this.start_date = start_date;
	}

	public Long getEnd_date() {
		return end_date;
	}

	public void setEnd_date(Long end_date) {
		this.end_date = end_date;
	}

	public Integer getReceived_num() {
		return received_num;
	}

	public void setReceived_num(Integer received_num) {
		this.received_num = received_num;
	}

	public Integer getUsed_num() {
		return used_num;
	}

	public void setUsed_num(Integer used_num) {
		this.used_num = used_num;
	}

	public Integer getLimit_num() {
		return limit_num;
	}

	public void setLimit_num(Integer limit_num) {
		this.limit_num = limit_num;
	}

	public Integer getProvince_id() {
		return province_id;
	}

	public void setProvince_id(Integer province_id) {
		this.province_id = province_id;
	}

	public String getProvince_name() {
		return province_name;
	}
	
	public void setProvince_name(String province_name) {
		this.province_name = province_name;
	}
	
	public Integer getSend_status() {
		return send_status;
	}
	
	public void setSend_status(Integer send_status) {
		this.send_status = send_status;
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

	public String getCoupons_describe() {
		return coupons_describe;
	}

	public void setCoupons_describe(String coupons_describe) {
		this.coupons_describe = coupons_describe;
	}
}
