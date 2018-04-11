package com.enation.app.base.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.enation.app.base.core.service.ISensitiveWordsManager;
import com.enation.framework.context.spring.SpringContextHolder;
import com.enation.framework.util.FileUtil;

/**
 * 
 * @ClassName: SensitiveWordInit
 * @Description: 初始化敏感词库，将敏感词加入到HashMap中，构建DFA算法模型
 * @author: liuyulei
 * @date: 2016年9月28日 下午3:01:44
 * @since:v61
 */
public class SensitiveWordInit {
	private String ENCODING = "UTF-8"; // 字符编码

	@SuppressWarnings("rawtypes")
	public HashMap sensitiveWordMap;

	private ISensitiveWordsManager sensitiveWordsManager;
	public SensitiveWordInit() {
		super();
		sensitiveWordsManager = SpringContextHolder
				.getBean("sensitiveWordsManager");
		sensitiveWordMap = (HashMap) initKeyWord();
	}

	/**
	 * 
	 * @Title: initKeyWord
	 * @Description: TODO 初始化
	 * @return
	 * @return: Map
	 */
	@SuppressWarnings("rawtypes")
	public Map initKeyWord() {
		try {
			// 读取敏感词库
			Set<String> keyWordSet = getSensitiveWord();
			
			// 将敏感词库加入到HashMap中
			addSensitiveWordToHashMap(keyWordSet);
//			getSensitiveWordMap(keyWordSet);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return sensitiveWordMap;
	}

	/**
	 * 
	 * @Title: addSensitiveWordToHashMap
	 * @Description: TODO * 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：<br>
	 *               中 = { isEnd = 0 国 = {<br>
	 *               isEnd = 1 人 = {isEnd = 0 民 = {isEnd = 1} } 男 = { isEnd = 0
	 *               人 = { isEnd = 1 } } } } 五 = { isEnd = 0 星 = { isEnd = 0 红 =
	 *               { isEnd = 0 旗 = { isEnd = 1 } } } }
	 * 
	 * @param keyWordSet
	 *            敏感词库
	 * @return: void
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addSensitiveWordToHashMap(Set<String> keyWordSet) {
		sensitiveWordMap = new HashMap(keyWordSet.size()); // 初始化敏感词容器，减少扩容操作
		String key = null;
		Map nowMap = null;
		Map<String, String> newWorMap = null;
		// 迭代keyWordSet
		Iterator<String> iterator = keyWordSet.iterator();
		while (iterator.hasNext()) {
			key = iterator.next(); // 关键字
			nowMap = sensitiveWordMap;
			for (int i = 0; i < key.length(); i++) {
				char keyChar = key.charAt(i); // 转换成char型
				Object wordMap = nowMap.get(keyChar); // 获取

				if (wordMap != null) { // 如果存在该key，直接赋值
					nowMap = (Map) wordMap;
				} else { // 不存在则，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
					newWorMap = new HashMap<String, String>();
					newWorMap.put("isEnd", "0"); // 不是最后一个
					nowMap.put(keyChar, newWorMap);
					nowMap = newWorMap;
				}

				if (i == key.length() - 1) {
					nowMap.put("isEnd", "1"); // 最后一个
				}
			}
		}
	}

	/**
	 * 
	 * @Title: readSensitiveWordFile
	 * @Description: TODO 读取敏感词库中的内容，将内容添加到set集合中
	 * @return
	 * @throws Exception
	 * @return: Set<String>
	 */
	

	/**
	 * 
	 * @Title: getSensitiveWordInfo
	 * @Description: TODO 初始化敏感词库
	 * @param list
	 *            传入数据
	 * @return
	 * @return: Set<String> @author： liuyulei
	 * @date：2016年9月28日 下午3:26:56
	 */
	public static Set<String> getSensitiveWordInfo(Map map) {
		Set<String> set = null;
		for (Object obj : map.keySet()) {
			set.add(map.get(obj.toString()).toString());
		}
		return set;
	}
	
	/** 
	 * @Title: getSensitiveWord 
	 * @Description: TODO  读取敏感词库中的内容，将内容添加到set集合中
	 * @return
	 * @return: Set<String>
	 * @author： liuyulei
	 * @date：2016年10月8日 上午10:49:46
	 */
	public Set<String> getSensitiveWord(){
		Set<String> set = null;
		try {
			set = new HashSet<String>();
			List<Map> list = sensitiveWordsManager.getAll();
			for (Map map : list) {
				String str = (String) map.get("sensitivewords_name");
				set.add(str);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return set;
	}
	
	/**
	 * 
	 * @Title: getsensitiveWordMap 
	 * @Description: TODO* 读取敏感词库，将敏感词放入HashSet中，构建一个DFA算法模型：<br>
	 *               中 = { isEnd = 0 国 = {<br>
	 *               isEnd = 1 人 = {isEnd = 0 民 = {isEnd = 1} } 男 = { isEnd = 0
	 *               人 = { isEnd = 1 } } } } 五 = { isEnd = 0 星 = { isEnd = 0 红 =
	 *               { isEnd = 0 旗 = { isEnd = 1 } } } }
	 * @param keyWordSet
	 * @return
	 * @return: HashMap
	 * @author： liuyulei
	 * @date：2016年10月8日 上午10:52:02
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void getSensitiveWordMap(Set<String> keyWordSet){
		sensitiveWordMap = new HashMap(keyWordSet.size()); // 初始化敏感词容器，减少扩容操作
		String key = null;
		Map nowMap = null;
		Map<String, String> newWorMap = null;
		// 迭代keyWordSet
		Iterator<String> iterator = keyWordSet.iterator();
		while (iterator.hasNext()) {
			key = iterator.next(); // 关键字
			nowMap = sensitiveWordMap;
			for (int i = 0; i < key.length(); i++) {
				char keyChar = key.charAt(i); // 转换成char型
				Object wordMap = nowMap.get(keyChar); // 获取

				if (wordMap != null) { // 如果存在该key，直接赋值
					nowMap = (Map) wordMap;
				} else { // 不存在，则构建一个map，同时将isEnd设置为0，因为他不是最后一个
					newWorMap = new HashMap<String, String>();
					newWorMap.put("isEnd", "0"); // 不是最后一个
					nowMap.put(keyChar, newWorMap);
					nowMap = newWorMap;
				}
				if (i == key.length() - 1) {
					nowMap.put("isEnd", "1"); // 最后一个
				}
			}
		}
	}
	
	

}
