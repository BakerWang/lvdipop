package com.enation.app.base.core.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.enation.framework.util.StringUtil;

/**
 * 
 * @ClassName: SensitiveUtil
 * @Description: 敏感词工具
 * @author: liuyulei
 * @date: 2016年9月28日 下午4:35:22
 * @since:v61
 */
public class SensitiveUtil {

	@SuppressWarnings("rawtypes")
	private static Map sensitiveWordMap ;
	public static int minMatchTYpe = 1; // 最小匹配规则
	public static int maxMatchType = 2; // 最大匹配规则

	/**
	 * 
	 * @Title: isContaintSensitiveWord
	 * @Description: TODO 判断文字是否包含敏感字符
	 * @param txt
	 *            文字
	 * @param matchType
	 *            匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
	 * @return
	 * @return: boolean 若包含返回true，否则返回false
	 */
	@SuppressWarnings({ "static-access", "rawtypes" })
	public boolean isContaintSensitiveWord(String txt, int matchType, Map sensitiveWordMap) {
		boolean flag = false;
		for (int i = 0; i < txt.length(); i++) {
			int matchFlag = this.CheckSensitiveWord(txt, i, matchType, sensitiveWordMap); // 判断是否包含敏感字符
			if (matchFlag > 0) { // 大于0存在，返回true
				flag = true;
			}
		}
		return flag;
	}

	/**
	 * 
	 * @Title: getSensitiveWord
	 * @Description: TODO 获取文字中的敏感词
	 * @param txt
	 *            文字
	 * @param matchType
	 *            匹配规则&nbsp;1：最小匹配规则，2：最大匹配规则
	 * @return
	 * @return: Set<String>
	 */
	public static Set<String> getSensitiveWord(String txt, int matchType, Map sensitiveWordMap) {
		Set<String> sensitiveWordList = new HashSet<String>();

		for (int i = 0; i < txt.length(); i++) {
			int length = CheckSensitiveWord(txt, i, matchType, sensitiveWordMap); // 判断是否包含敏感字符
			if (length > 0) { // 存在,加入list中
				sensitiveWordList.add(txt.substring(i, i + length));
				i = i + length - 1; // 减1的原因，是因为for会自增
			}
		}

		return sensitiveWordList;
	}

	/**
	 * 
	 * @Title: replaceSensitiveWord
	 * @Description: TODO 替换敏感字字符
	 * @param txt
	 * @param matchType
	 * @param replaceChar
	 *            替换字符，默认*
	 * @return
	 * @return: String
	 */
	public static String replaceSensitiveWord(String txt, Integer matchType, String replaceChar,Map sensitiveWordMap) {
		String resultTxt = txt;
		if (matchType == null) {// 如果为空，则设置最大匹配规则 即默认为最大匹配规则
			matchType = 2;
		}
		Set<String> set = getSensitiveWord(txt, matchType,sensitiveWordMap); // 获取所有的敏感词
		Iterator<String> iterator = set.iterator();
		String word = null;
		String replaceString = null;
		while (iterator.hasNext()) {
			word = iterator.next();
			replaceString = getReplaceChars(StringUtil.isEmpty(replaceChar) ? "*" : replaceChar, word.length());
			resultTxt = resultTxt.replaceAll(word, replaceString);
		}

		return resultTxt;
	}

	/**
	 * 
	 * @Title: getReplaceChars
	 * @Description: TODO 获取替换字符串
	 * @param replaceChar
	 * @param length
	 * @return
	 * @return: String
	 */
	private static String getReplaceChars(String replaceChar, int length) {
		String resultReplace = replaceChar;
		for (int i = 1; i < length; i++) {
			resultReplace += replaceChar;
		}

		return resultReplace;
	}

	/**
	 * 
	 * @Title: CheckSensitiveWord
	 * @Description: TODO 检查文字中是否包含敏感字符，检查规则如下：<br>
	 * @param txt
	 * @param beginIndex
	 * @param matchType
	 * @return
	 * @return: 如果存在，则返回敏感词字符的长度，不存在返回0
	 */
	@SuppressWarnings({ "rawtypes" })
	public static int CheckSensitiveWord(String txt, int beginIndex, int matchType, Map sensitiveWordMap) {
		boolean flag = false; // 敏感词结束标识位：用于敏感词只有1位的情况
		int matchFlag = 0; // 匹配标识数默认为0
		char word = 0;
		sensitiveWordMap = new SensitiveWordInit().initKeyWord();
		Map nowMap = sensitiveWordMap;
		for (int i = beginIndex; i < txt.length(); i++) {
			word = txt.charAt(i);
			nowMap = (Map) nowMap.get(word); // 获取指定key
			if (nowMap != null) { // 存在，则判断是否为最后一个
				matchFlag++; // 找到相应key，匹配标识+1
				if ("1".equals(nowMap.get("isEnd"))) { // 如果为最后一个匹配规则,结束循环，返回匹配标识数
					flag = true; // 结束标志位为true
					if (SensitiveUtil.minMatchTYpe == matchType) { // 最小规则，直接返回,最大规则还需继续查找
						break;
					}
				}
			} else { // 不存在，直接返回
				break;
			}
		}
		if (matchFlag < 2 || !flag) { // 长度必须大于等于1，为词
			matchFlag = 0;
		}
		return matchFlag;
	}
}
