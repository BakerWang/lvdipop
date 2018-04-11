package com.enation.app.b2b2c.core.store.model;

import com.enation.framework.database.NotDbField;
import com.enation.framework.database.PrimaryKeyField;

/**
 * 
 * @ClassName: StoreClassify
 * @Description: 店铺分类实体
 * @author: liuyulei
 * @date: 2016年10月10日 下午6:03:01
 * @since:v61
 */
public class StoreClassify {

	private Integer sclassify_id; // 店铺分类id
	private Integer province_id; // 省份id
	private String province_name; // 省份名称
	private Integer city_id; // 城市id
	private String city_name; // 城市名称
	
	private String classify_name; // 分类名称
	private Integer classify_sort; // 排序
	private Double longitude; // 经度
	private Double latitude; // 纬度
	private Integer pid;	//父id
	private Integer grade;	//等级 1=省级 2=市级  3=区级  4=街道

	private Double distance;  // 距离   非数据库字段  单位:米
	
	@NotDbField
	public Double getDistance() {
		return distance;
	}

	public void setDistance(Double distance) {
		this.distance = distance;
	}

	@PrimaryKeyField
	public Integer getSclassify_id() {
		return sclassify_id;
	}

	public void setSclassify_id(Integer sclassify_id) {
		this.sclassify_id = sclassify_id;
	}

	public Integer getProvince_id() {
		return province_id;
	}

	public void setProvince_id(Integer province_id) {
		this.province_id = province_id;
	}

	public Integer getCity_id() {
		return city_id;
	}

	public void setCity_id(Integer city_id) {
		this.city_id = city_id;
	}

	public String getClassify_name() {
		return classify_name;
	}

	public void setClassify_name(String classify_name) {
		this.classify_name = classify_name;
	}

	public Integer getClassify_sort() {
		return classify_sort;
	}

	public void setClassify_sort(Integer classify_sort) {
		this.classify_sort = classify_sort;
	}

	public String getProvince_name() {
		return province_name;
	}

	public void setProvince_name(String province_name) {
		this.province_name = province_name;
	}

	public String getCity_name() {
		return city_name;
	}

	public void setCity_name(String city_name) {
		this.city_name = city_name;
	}

	public Double getLongitude() {
		return longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getLatitude() {
		return latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Integer getPid() {
		return pid;
	}

	public void setPid(Integer pid) {
		this.pid = pid;
	}

	public Integer getGrade() {
		return grade;
	}

	public void setGrade(Integer grade) {
		this.grade = grade;
	}

}
