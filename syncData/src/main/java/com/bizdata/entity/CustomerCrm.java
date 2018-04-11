package com.bizdata.entity;

import java.util.Date;

/**
 * database oracle crm user data
 * @author furen
 *
 */
public class CustomerCrm {
	private String cmmemid;
	private String cmcustid;
	private Date cmmaintdate;
	private String cmname;
	private Date cmbirthday;
	private Character cmsex;
	private String cmmobile1;
	private String cmmobile2;
	private String cmemail;
	
	public String getCmmemid() {
		return cmmemid;
	}
	public void setCmmemid(String cmmemid) {
		this.cmmemid = cmmemid;
	}
	public String getCmcustid() {
		return cmcustid;
	}
	public void setCmcustid(String cmcustid) {
		this.cmcustid = cmcustid;
	}
	public Date getCmmaintdate() {
		return cmmaintdate;
	}
	public void setCmmaintdate(Date cmmaintdate) {
		this.cmmaintdate = cmmaintdate;
	}
	public String getCmname() {
		return cmname;
	}
	public void setCmname(String cmname) {
		this.cmname = cmname;
	}
	public Date getCmbirthday() {
		return cmbirthday;
	}
	public void setCmbirthday(Date cmbirthday) {
		this.cmbirthday = cmbirthday;
	}
	public Character getCmsex() {
		return cmsex;
	}
	public void setCmsex(Character cmsex) {
		this.cmsex = cmsex;
	}
	public String getCmmobile1() {
		return cmmobile1;
	}
	public void setCmmobile1(String cmmobile1) {
		this.cmmobile1 = cmmobile1;
	}
	public String getCmmobile2() {
		return cmmobile2;
	}
	public void setCmmobile2(String cmmobile2) {
		this.cmmobile2 = cmmobile2;
	}
	public String getCmemail() {
		return cmemail;
	}
	public void setCmemail(String cmemail) {
		this.cmemail = cmemail;
	}
	@Override
	public String toString() {
		return "CustomerCrm [cmmemid=" + cmmemid + ", cmcustid=" + cmcustid + ", cmmaintdate=" + cmmaintdate
				+ ", cmname=" + cmname + ", cmbirthday=" + cmbirthday + ", cmsex=" + cmsex + ", cmmobile1=" + cmmobile1
				+ ", cmmobile2=" + cmmobile2 + ", cmemail=" + cmemail + "]";
	}
	
	
}
