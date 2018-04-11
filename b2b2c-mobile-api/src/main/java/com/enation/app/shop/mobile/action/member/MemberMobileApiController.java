/**
 * 版权：Copyright (C) 2015  易族智汇（北京）科技有限公司.
 * 本系统是商用软件,未经授权擅自复制或传播本程序的部分或全部将是非法的.
 * 描述：会员api
 * 修改人：Sylow  
 * 修改时间：
 * 修改内容：
 */
package com.enation.app.shop.mobile.action.member;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.enation.app.base.core.model.Member;
import com.enation.app.base.core.service.IMemberManager;
import com.enation.eop.sdk.context.UserConext;
import com.enation.eop.sdk.utils.StaticResourcesUtil;
import com.enation.eop.sdk.utils.ValidCodeServlet;
import com.enation.framework.action.JsonResult;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.util.EncryptionUtil1;
import com.enation.framework.util.HttpUtil;
import com.enation.framework.util.JsonResultUtil;
import com.enation.framework.util.StringUtil;

/**
 * 会员api
 * @author Sylow
 * @version v1.0 , 2015-08-24
 * @since v1.0
 */
@Controller	
@RequestMapping("/api/mobile/member")
public class MemberMobileApiController{

	protected final Logger logger = Logger.getLogger(getClass());
	@Autowired
	private IMemberManager memberManager;

	/**
	 * 更改密码
	 */


	/**
	 * 会员登录
	 *
	 * @return json字串
	 * result  为1表示登录成功，0表示失败 ，int型
	 * message 为提示信息 ，String型
	 */
	@ResponseBody
	@RequestMapping(value="/login",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult login(String username,String password,String validcode,Integer valid) {
		try {
			if(valid < 1 && this.validcode(validcode, "memberlogin") < 1){
				return JsonResultUtil.getErrorJson("验证码错误");
			}
			if (memberManager.login(username, password) != 1) {
				return JsonResultUtil.getErrorJson("账号密码错误");
			}

			String cookieValue = EncryptionUtil1.authcode(
					"{username:\"" + username + "\",password:\"" + StringUtil.md5(password) + "\"}",
					"ENCODE", "", 0);
			HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(), "JavaShopUser", cookieValue, 60 * 24 * 14);

			Member member =UserConext.getCurrentMember();

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("username", member.getUname());
			map.put("face", StaticResourcesUtil.convertToUrl(member.getFace()));
			map.put("level", member.getLvname());
			return JsonResultUtil.getObjectJson(map);
		} catch(RuntimeException e) {
			this.logger.error("登录出错", e);
			return JsonResultUtil.getErrorJson("登录出错[" + e.getMessage() + "]");
		}
	}
	/**
	 * 会员登录
	 *
	 * @return json字串
	 * result  为1表示登录成功，0表示失败 ，int型
	 * message 为提示信息 ，String型
	 */
	@ResponseBody
	@RequestMapping(value="/login-app",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult loginApp(String username,String password) {
		try { 
			if (memberManager.login(username, password) != 1) {
				return JsonResultUtil.getErrorJson("账号密码错误");
			}

			String cookieValue = EncryptionUtil1.authcode(
					"{username:\"" + username + "\",password:\"" + StringUtil.md5(password) + "\"}",
					"ENCODE", "", 0);
			HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(), "JavaShopUser", cookieValue, 60 * 24 * 14);

			Member member =UserConext.getCurrentMember();

			HashMap<String, String> map = new HashMap<String, String>();
			map.put("username", member.getUname());
			map.put("face", StaticResourcesUtil.convertToUrl(member.getFace()));
			map.put("level", member.getLvname());

			return JsonResultUtil.getObjectJson(map); 
		} catch(RuntimeException e) {
			this.logger.error("登录出错", e);
			return JsonResultUtil.getErrorJson("登录出错[" + e.getMessage() + "]");
		}
	}

	/**
	 * 是否已登录
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/is-login",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult isLogin(String username){
		Member member = UserConext.getCurrentMember();

		if (member == null) {
			return JsonResultUtil.getErrorJson("尚未登陆");
		}else {
			//this.showSuccessJson("已经登录");
			return JsonResultUtil.getObjectJson(member);
		}
	}

	/**
	 * 注销会员登录
	 *
	 * @return json字串
	 * result  为1表示注销成功，0表示失败 ，int型
	 * message 为提示信息 ，String型
	 */
	@ResponseBody
	@RequestMapping(value="/logout",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult logout() {
		try {
			this.memberManager.logout();
			//设置cookie有效时间为0 即删除
			HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(), "JavaShopUser", "", 0);
			return JsonResultUtil.getSuccessJson("注销成功");
		} catch (RuntimeException e) {
			this.logger.error("退出登录出错", e);
			return JsonResultUtil.getErrorJson("退出出错[" + e.getMessage() + "]");
		}
	}

	/**
	 * 修改会员密码
	 *
	 * @return json字串
	 * result  为1表示修改成功，0表示失败 ，int型
	 * message 为提示信息 ，String型
	 */
	@ResponseBody
	@RequestMapping(value="/change-password",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult changePassword(String password,String oldpassword,String re_passwd) {
		Member member = UserConext.getCurrentMember();

		if (member == null) {
			return JsonResultUtil.getErrorJson("尚未登陆");
		}

		oldpassword = oldpassword == null ? "" : StringUtil.md5(oldpassword);

		if (!oldpassword.equals(member.getPassword())) {
			return JsonResultUtil.getErrorJson("修改失败！原始密码不符");
		}

		if (!re_passwd.equals(password)) {
			return JsonResultUtil.getErrorJson("修改失败！两次输入的密码不一致");

		}
		try {
			memberManager.updatePassword(password);
			//注销
			this.memberManager.logout();
			//设置cookie有效时间为0 即删除
			HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(), "JavaShopUser", "", 0);
			return JsonResultUtil.getSuccessJson("修改密码成功");

		} catch (Exception e) {
			if (this.logger.isDebugEnabled()) {
				logger.error(e.getStackTrace());
			}
		}
		return JsonResultUtil.getErrorJson("修改密码失败");
	}

	/**
	 * 会员注册
	 */
	@ResponseBody
	@RequestMapping(value="/register",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult register(String username,String password,String oldpassword,String validcode,int valid) {
		try {
			if(valid < 1 && this.validcode(validcode, "memberreg") < 1){
				return JsonResultUtil.getErrorJson("验证码错误");
			}
			Member member = new Member();
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String registerip = request.getRemoteAddr();

			if (StringUtil.isEmpty(username)) {
				return JsonResultUtil.getErrorJson("用户名不能为空！");

			}
			if (username.length() < 4 || username.length() > 20) {
				return JsonResultUtil.getErrorJson("用户名的长度为4-20个字符！");

			}
			if (username.contains("@")) {
				return JsonResultUtil.getErrorJson("用户名中不能包含@等特殊字符！");
			}
			if (StringUtil.isEmpty(password)) {
				return JsonResultUtil.getErrorJson("密码不能为空！");
			}
			if (memberManager.checkname(username) > 0) {
				return JsonResultUtil.getErrorJson("此用户名已经存在，请您选择另外的用户名!");
			}

			member.setMobile("");
			member.setUname(username);
			member.setPassword(password);
			member.setEmail("");
			member.setRegisterip(registerip);

			if (memberManager.register(member) != 1) {
				return JsonResultUtil.getErrorJson("用户名[" + member.getUname() + "]已存在!");
			}

			this.memberManager.login(username, password);
			String cookieValue = EncryptionUtil1.authcode("{username:\""
					+ username + "\",password:\"" + StringUtil.md5(password)
					+ "\"}", "ENCODE", "", 0);
			HttpUtil.addCookie(ThreadContextHolder.getHttpResponse(),
					"JavaShopUser", cookieValue, 60 * 24 * 14);
			return JsonResultUtil.getSuccessJson("注册成功");
		} catch(RuntimeException e) {
			this.logger.error("注册出错", e);
			return JsonResultUtil.getErrorJson("注册出错[" + e.getMessage() + "]");

		}
	}

	/**
	 * 获取用户信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/info",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult info() {
		Member member = UserConext.getCurrentMember();
		if (member == null) {
			return JsonResultUtil.getErrorJson("尚未登陆");

		}
		Map map = new HashMap<String, String>();
		map.put("name", member.getName());
		map.put("sex", member.getSex());
		map.put("birthday", member.getBirthday());
		map.put("province", member.getProvince());
		map.put("province_id", member.getProvince_id());
		map.put("city", member.getCity());
		map.put("city_id", member.getCity_id());
		map.put("region", member.getRegion());
		map.put("region_id", member.getRegion_id());
		map.put("address", member.getAddress());
		map.put("zip", member.getZip());
		map.put("mobile", member.getMobile());
		map.put("tel", member.getTel());
		map.put("point", member.getPoint());
		map.put("mp", member.getMp());

		return JsonResultUtil.getObjectJson(map);
	}

	/**
	 * 修改用户信息
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value="/save",produces= MediaType.APPLICATION_JSON_VALUE)
	public JsonResult save(Member member) {
		Member member_before = UserConext.getCurrentMember();
		if (member_before == null) {
			return JsonResultUtil.getErrorJson("尚未登陆");
		}
		if (StringUtil.isEmpty(member.getName())) {
			return JsonResultUtil.getErrorJson("真实姓名不能为空");
		}
		if (member.getBirthday() == 0) {
			member.setBirthday(19700101L);
		}

		if (StringUtil.isEmpty(member.getMobile())
				&& StringUtil.isEmpty(member.getTel())) {
			return JsonResultUtil.getErrorJson("手机或固定电话必填一项");

		}

		if (!StringUtil.isEmpty(member.getMobile())
				&& !isMobile(member.getMobile())) {
			return JsonResultUtil.getErrorJson("手机格式不正确");
		}

		member_before.setName(member.getName());
		member_before.setSex(member.getSex());
		member_before.setBirthday(member.getBirthday());
		member_before.setProvince(member.getProvince());
		member_before.setProvince_id(member.getProvince_id());
		member_before.setCity(member.getCity());
		member_before.setCity_id(member.getCity_id());
		member_before.setRegion(member.getRegion());
		member_before.setRegion_id(member.getRegion_id());
		member_before.setAddress(member.getAddress());
		member_before.setMobile(member.getMobile());
		member_before.setZip(member.getZip());
		member_before.setTel(member.getTel());

		try {
			this.memberManager.edit(member_before);
			return JsonResultUtil.getSuccessJson("保存资料成功");
		} catch (Exception e) {
			return JsonResultUtil.getErrorJson("保存资料失败");
		}
	}

	/**
	 * 验证手机号码
	 * 
	 * @param mobile
	 * @return
	 */
	private boolean isMobile(String mobile) {
		boolean flag = false;
		try {
			Pattern p = Pattern
					.compile("^((13[0-9])|(15[^4,\\D])|(17[0-9])|(18[0,5-9]))\\d{8}$");
			Matcher m = p.matcher(mobile);
			flag = m.matches();
		} catch (Exception e) {
			this.logger.error("验证手机号码错误", e);
			flag = false;
		}
		return flag;
	}

	/**
	 * 校验验证码
	 * 
	 * @param validcode
	 * @param name (1、memberlogin:会员登录  2、memberreg:会员注册)
	 * @return 1成功 0失败
	 */
	private int validcode(String validcode,String name) {
		if (validcode == null) {
			return 0;
		}

		String code = (String) ThreadContextHolder.getSession().getAttribute(ValidCodeServlet.SESSION_VALID_CODE + name);
		if (code == null) {
			return 0;
		} else {
			if (!code.equalsIgnoreCase(validcode)) {
				return 0;
			}
		}
		return 1;
	}



}
