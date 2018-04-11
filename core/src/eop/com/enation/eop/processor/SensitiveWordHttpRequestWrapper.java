package com.enation.eop.processor;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.enation.app.base.core.util.SensitiveUtil;
/**
 * 
 * @Description: 敏感词过滤包装器
 * @author: liuyulei
 * @date: 2016年10月17日 下午2:10:39 
 * @since:v61
 */
public class SensitiveWordHttpRequestWrapper extends  HttpServletRequestWrapper{

	public SensitiveWordHttpRequestWrapper(HttpServletRequest request) {
		super(request);
	}
	
	
	public String getParameter(String name) {
		String value = super.getParameter(name);
		value = this.replaceSensitiveWord(value);
		return value;
	}

	public Map getParameterMap() {
		Map map = super.getParameterMap();
		Iterator keiter = map.keySet().iterator();
		while (keiter.hasNext()) {
			String name = keiter.next().toString();
			Object value = map.get(name);
			if (value instanceof String) {
				value = this.replaceSensitiveWord(value == null ? "" : value.toString());
			}
			if (value instanceof String[]) {
				String[] values = (String[]) value;
				this.replaceSensitiveWord(values);
			}
		}
		return map;
	}
	
	public String[] getParameterValues(String arg0) {
		String[] values = super.getParameterValues(arg0);
		this.replaceSensitiveWord(values);
		return values;
	}
	
	
	//过滤敏感词
	private String replaceSensitiveWord(String value){
		if(value == null ){
			return null;
		}
		return SensitiveUtil.replaceSensitiveWord( value, 2, "", null);
	}
	//过滤敏感词
	private void replaceSensitiveWord(String [] values){
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				values[i] = SensitiveUtil.replaceSensitiveWord(values[i], 2, "", null);
			}
		}
	}

}
