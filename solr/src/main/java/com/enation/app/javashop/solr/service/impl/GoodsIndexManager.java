package com.enation.app.javashop.solr.service.impl;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PackedTokenAttributeImpl;
import org.apache.lucene.util.NumericUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrQuery.ORDER;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.chenlb.mmseg4j.analysis.MaxWordAnalyzer;
import com.chenlb.mmseg4j.analysis.TokenUtils;
import com.enation.app.base.core.model.TaskProgress;
import com.enation.app.base.core.model.cluster.SolrSetting;
import com.enation.app.base.core.service.ProgressContainer;
import com.enation.app.javashop.solr.GoodsIndexPluginBundle;
import com.enation.app.javashop.solr.model.GoodsWords;
import com.enation.app.javashop.solr.service.IGoodsIndexManager;
import com.enation.app.javashop.solr.service.ISearchSelectorCreator;
import com.enation.app.javashop.solr.service.PinYinUtil;
import com.enation.app.javashop.solr.util.HexUtil;
import com.enation.app.shop.core.goods.model.Attribute;
import com.enation.app.shop.core.goods.model.Cat;
import com.enation.app.shop.core.goods.model.support.GoodsTypeDTO;
import com.enation.app.shop.core.goods.service.AttributeType;
import com.enation.app.shop.core.goods.service.IGoodsCatManager;
import com.enation.app.shop.core.goods.service.IGoodsTypeManager;
import com.enation.app.shop.core.goods.service.Separator;
import com.enation.app.shop.core.goods.utils.ParamsUtils;
import com.enation.app.shop.core.goods.utils.SortContainer;
import com.enation.eop.SystemSetting;
import com.enation.eop.processor.core.UrlNotFoundException;
import com.enation.eop.sdk.context.EopSetting;
import com.enation.framework.context.spring.SpringContextHolder;
import com.enation.framework.context.webcontext.ThreadContextHolder;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.database.Page;
import com.enation.framework.jms.EopProducer;
import com.enation.framework.jms.IJmsProcessor;
import com.enation.framework.util.StringUtil;
import com.hp.hpl.sparta.ParseException;

/**
 * 商品索引管理实现类
 * 
 * @author kingapex 2015-4-16
 * @version v1.1,2016-01-21 by Sylow
 * @since v5.2
 */
@Component
public class GoodsIndexManager implements IGoodsIndexManager, IJmsProcessor {
	
	private static HttpSolrServer solrServer = null;
	private static String solrUrl = "";
	
//	private static HttpSolrServer solrServer = new HttpSolrServer(
//			"http://localhost:8983/solr/gettingstarted/");
	@Autowired
	private IDaoSupport daoSupport;
	@Autowired
	private EopProducer eopProducer;
	public final static String PRGRESSID = "lucene_create"; // 进度id
	@Autowired
	private IGoodsTypeManager goodsTypeManager;
	@Autowired
	private IGoodsCatManager goodsCatManager;
	private String[] selectorCreators = { "catSelectorCreator",
			"propSelectorCreator", "brandSelectorCreator",
			"sortSelectorCreator", "priceSelectorCreator" };
	protected final Logger logger = Logger.getLogger(getClass());
	@Autowired
	private GoodsIndexPluginBundle goodsIndexPluginBundle;
	
	public GoodsIndexManager() {}

	private static HttpSolrServer getSolrServer(){
		//if(SystemSetting.getClusterSetting() == null)
		//	return null;
		if(StringUtils.isEmpty(SolrSetting.getSolr()))
			return null;
		if(solrServer == null || !solrUrl.equals(SolrSetting.getSolr())){			
			solrServer = new HttpSolrServer(SolrSetting.getSolr());
		}
		return solrServer;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.enation.framework.jms.IJmsProcessor#process(java.lang.Object)
	 */
	@Transactional(propagation = Propagation.REQUIRED)
	public void process(Object data) {
		this.addAallIndex();
	}

	/**
	 * 将商品名称按中文名、英文全拼、首字母进行索引<br>
	 * 同时将名称的分词写入数据库，以便形成搜索建议
	 */
	public void addIndex(Map goods) {
		if(getSolrServer() == null)
			return;
		try {

			String goods_name = goods.get("name").toString();
			List<String> wordsList = toWordsList(goods_name);
			this.fillGoodsPinyin(goods);

			SolrInputDocument doc = createDocument(goods);

			this.wordsToDb(wordsList);// 分词入库
			
			getSolrServer().add(doc);
			getSolrServer().commit();		

		} catch (Exception e) {
			this.logger.error("商品索引错误", e);
			throw new RuntimeException("商品索引错误");
		}
	}

	/**
	 * 更新指定商品索引
	 */
	public void updateIndex(Map goods) {
		if(getSolrServer() == null)
			return;
		try {
			this.fillGoodsPinyin(goods);

			SolrInputDocument doc = this.createDocument(goods);
			
			getSolrServer().add(doc);
			getSolrServer().commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			this.logger.error("商品索引错误", e);
			throw new RuntimeException("商品索引错误");
		}

	}

	/**
	 * 删除指定商品的索引
	 */
	public void deleteIndex(Map goods) {
		if(getSolrServer() == null)
			return;
		try {
			String goods_name = goods.get("name").toString();
			List<String> wordsList = toWordsList(goods_name);	
			this.deleteWords(wordsList);
			
			getSolrServer().deleteById(goods.get("goods_id").toString());
			getSolrServer().commit();			
			
		} catch (Exception e) {
			this.logger.error("商品索引错误", e);
			throw new RuntimeException("商品索引错误");
		}
	}

	/**
	 * 重建全部索引
	 */
	public void addAallIndex() {
		if(getSolrServer() == null)
			return;
		try {

			//先删除全部索引
			getSolrServer().deleteByQuery("*:*");
			getSolrServer().commit();
			
			this.daoSupport.execute("delete from es_goods_words");

			// 商品总数
			int goods_count = this.getGoodsCount();
			int page_size = 100; // 100条查一次
			int page_count = 0;
			page_count = goods_count / page_size;
			page_count = goods_count % page_size > 0 ? page_count + 1
					: page_count;

			if (goods_count != 0) {
				// 生成任务进度
				TaskProgress taskProgress = new TaskProgress(goods_count);
				ProgressContainer.putProgress(PRGRESSID, taskProgress);
				System.out.println("put " + PRGRESSID);
				for (int i = 1; i <= page_count; i++) {
					
					Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
					
					List<Map> goodsList = this.daoSupport.queryForListPage(
							"select * from es_goods", i, page_size);
					for (Map goods : goodsList) {

						String goods_name = goods.get("name").toString();
						List<String> wordsList = toWordsList(goods_name);

						this.fillGoodsPinyin(goods);

						SolrInputDocument doc = createDocument(goods);
						docs.add(doc);

						this.wordsToDb(wordsList);// 分词入库

						ProgressContainer.getProgress(PRGRESSID).step(
								"正在为[" + goods.get("name") + "]生成索引");

					}
					getSolrServer().add(docs);
					getSolrServer().commit();

				}
			} else {
				TaskProgress taskProgress = new TaskProgress(1);
				ProgressContainer.putProgress(PRGRESSID, taskProgress);
				ProgressContainer.getProgress(PRGRESSID).step("没有数据");
			}
			ProgressContainer.getProgress(PRGRESSID).success();

		} catch (Exception e) {
			this.logger.error("商品索引错误", e);
			ProgressContainer.getProgress(PRGRESSID).fail(
					"生成索引出错:" + e.getMessage());
			throw new RuntimeException("商品索引错误");

		}

	}

	/**
	 * 获取分词结果
	 * 
	 * @param txt
	 * @return 分词list
	 */
	public static List<String> toWordsList(String txt) {

		List<String> list = new ArrayList<String>();
		TokenStream ts = null;
		try {
			ts = getAnalyzer().tokenStream("text", new StringReader(txt));
			ts.reset();

			while (true) {
				PackedTokenAttributeImpl t = new PackedTokenAttributeImpl();
				t = TokenUtils.nextToken(ts,  t);
				// 如果有数据就添加
				if (t != null) {
					list.add(t.toString());
				} else {
					break;
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (ts != null) {
				try {
					ts.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return list;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.shop.component.goodsindex.service.IGoodsIndexManager#
	 * getGoodsWords(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<GoodsWords> getGoodsWords(String keyword) {
		String sql = "select words,goods_num from es_goods_words where words like  '"
				+ keyword
				+ "%' or quanpin like '"
				+ keyword
				+ "%' or szm like '" + keyword + "%' order by goods_num desc";
		return (List) this.daoSupport
				.queryForPage(sql, 1, 15, GoodsWords.class).getResult();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.shop.component.goodsindex.service.IGoodsIndexManager#
	 * search(int, int)
	 */
	public Page search(int pageNo, int pageSize) {
		if(getSolrServer() == null)
			return new Page(0, 0, pageSize, new ArrayList());
		try {
			List list = new ArrayList();
			SolrQuery query = this.createQuery();

			this.setSort(query);
			
			query.setStart((pageNo-1)*pageSize);
			query.setRows(pageSize);
			
			QueryResponse queryResponse = getSolrServer().query(query);
			
			SolrDocumentList documentList = queryResponse.getResults();
			
			long totalCount = documentList.getNumFound();
			int start = pageSize * (pageNo - 1);
			// System.out.println("共检索出 "+totalCount+" 条记录");
			long end = Math.min(pageNo * pageSize, totalCount);
			for (SolrDocument document : documentList) {
				Map goods = new HashMap();
				for(String name : document.getFieldNames()){
					Object value =document.getFieldValue(name);
					if("store_id".equals(name)){
						 
//						Integer storeid = (Integer)value;
						System.out.println(name +"-->"+value);
						if(value instanceof String){
							System.out.println("string");
						}
						if(value instanceof Integer){
							System.out.println("int");
						}
						if(value instanceof ArrayList){
							System.out.println("array");
						}
						}
					if(value instanceof ArrayList){
						 ArrayList vlist =(ArrayList) value;
						 if(vlist!=null && !vlist.isEmpty()){
							 value =vlist.get(0);
						 }else{
							 value=null;
						 }
					}
					
					
					
					goods.put(name, value);
				}
				list.add(goods);
			}

			Page webPage = new Page(0, totalCount, pageSize, list);
			return webPage;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	/**
	 * 由reuquest的sort参数，设置query的排序
	 * 
	 * @return 如果是默认排序，则返回null;
	 */
	private void setSort(SolrQuery query) {

		HttpServletRequest request = ThreadContextHolder.getHttpRequest();

		String sortfield = request.getParameter("sort");
		
		if(StringUtils.isEmpty(sortfield)){
			return;
		}

		Map<String, String> sortMap = SortContainer.getSort(sortfield);

		String sortid = sortMap.get("id");

		if ("def".equals(sortid)) {
			return;
		}

		boolean desc = "desc".equals(sortMap.get("def_sort"));
		
		if(desc){
			query.setSort(sortMap.get("id") + "_sort", ORDER.desc);
		}else{
			query.setSort(sortMap.get("id") + "_sort", ORDER.asc);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.enation.app.shop.component.goodsindex.service.IGoodsIndexManager#
	 * createSelector()
	 */
	public Map<String, Object> createSelector() {
		if(getSolrServer() == null)
			return new HashMap<String, Object>();
		
		long start = System.currentTimeMillis();
		try {
			HttpServletRequest request = ThreadContextHolder.getHttpRequest();
			String servlet_path = request.getServletPath();

			SolrQuery query = this.createQuery();
			
			String[] prop_ar = ParamsUtils.getProps();
			for (String p : prop_ar) {
				String[] onprop_ar = p.split(Separator.separator_prop_vlaue);
				query.addFacetField("p_" + HexUtil.encode(onprop_ar[0]) +"_s");
			}

			Map<String, Object> map = new HashMap<String, Object>(); // 要返回的结果
			
			QueryResponse resp = getSolrServer().query(query);
			List<FacetField> factFields = resp.getFacetFields();
			if(factFields == null){
				factFields = new ArrayList<FacetField>();
			}

			// 处理选择器
			for (String creatorid : selectorCreators) {
				ISearchSelectorCreator creator = SpringContextHolder.getBean(creatorid);
				creator.createAndPut(map, factFields);
			}
			long end = System.currentTimeMillis();
			return map;

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	/**
	 * 根据搜索条件创建query
	 * 
	 * @return
	 * @throws ParseException
	 */
	private SolrQuery createQuery() throws ParseException {

		HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		String keyword = request.getParameter("keyword");		
		String cat = request.getParameter("cat");
		String brand = request.getParameter("brand");
		String price = request.getParameter("price");

		Analyzer analyzer = this.getAnalyzer();
		
		SolrQuery query = new SolrQuery();
		// 关键字检索
		if (!StringUtil.isEmpty(keyword)) {
//			QueryParser parser = new QueryParser("name", analyzer);
//			Query queryname = parser.parse(keyword);
//			query.add(queryname, BooleanClause.Occur.MUST);
			List<String> list = toWordsList(keyword);
			int i = 1;
			for (String str : list) {
				i++;
				// Query queryname = parser.parse( str );
				// query.add(queryname,BooleanClause.Occur.MUST);
			}
			query.setQuery("name:" + keyword);
		}

		// 品牌搜素
		if (!StringUtil.isEmpty(brand)) {
			query.addFilterQuery("brand_id:" + brand);
		}else{			
			query.addFacetField("brand_id");
		}

		// 分类检索
		if (!StringUtil.isEmpty(cat)) {

			String[] catar = cat.split(Separator.separator_prop_vlaue);
			String catid = catar[catar.length - 1];

			Cat goodscat = this.goodsCatManager.getById(StringUtil.toInt(catid,
					0));

			if (goodscat == null)
				throw new UrlNotFoundException();
			query.addFilterQuery("catpath:" + goodscat.getCat_path() + "*");// 只查出最后的分类（最小的子类）
		}else{
			query.addFacetField("cat_id");
		}

		// 属性检索
		String[] prop_ar = ParamsUtils.getProps();
		for (String p : prop_ar) {
			String[] onprop_ar = p.split(Separator.separator_prop_vlaue);
			String propName = "p_" + HexUtil.encode(onprop_ar[0]) + "_s";
			query.addFilterQuery(propName + ":" + onprop_ar[1]);
		}

		if (!StringUtil.isEmpty(price)) {

			String[] pricear = price.split(Separator.separator_prop_vlaue);
			int min = StringUtil.toInt(pricear[0], 0);
			int max = Integer.MAX_VALUE;

			if (pricear.length == 2) {
				max = StringUtil.toInt(pricear[1], Integer.MAX_VALUE);
			}
			query.addFilterQuery("price:[" + Double.valueOf(min) + " TO " + Double.valueOf(max)+ "]");
		}
		// 删除的商品不显示
		query.addFilterQuery("disabled:0");
		// 删除的商品不显示
		query.addFilterQuery("market_enable:1");
		return query;
	}

	private static Analyzer getAnalyzer() {
		return new MaxWordAnalyzer();
	}

	/**
	 * 填充商品
	 * 
	 * @param goods
	 */
	private void fillGoodsPinyin(Map goods) {
		String goods_name = goods.get("name").toString();
		List<String> wordsList = toWordsList(goods_name);
		String seg_name = StringUtil.listToString(wordsList, " "); // 将分词结果转为空格分格的一串字符

		String name_quanpin = PinYinUtil.getPingYin(seg_name); // 全拼
		String name_header_py = PinYinUtil.getPinYinHeadChar(seg_name);// 首字母

		goods.put("goods_name", goods_name);
		goods.put("name_quanpin", name_quanpin);
		goods.put("name_header_py", name_header_py);

	}

	/**
	 * 将list中的分词减一
	 * 
	 * @param wordsList
	 */
	private void deleteWords(List<String> wordsList) {
		for (String words : wordsList) {
			this.daoSupport
					.execute(
							"update es_goods_words set goods_num=goods_num-1 where words=?",
							words);
		}
	}

	/**
	 * 获取商品数量
	 * 
	 * @return
	 */
	private int getGoodsCount() {
		return this.daoSupport.queryForInt("select count(0) from es_goods");
	}

	/**
	 * 将一个商品创建为Document
	 * 
	 * @param goods
	 * @return
	 * @throws IOException
	 */
	private SolrInputDocument createDocument(Map goods) throws IOException {
		SolrInputDocument document = new SolrInputDocument();
		try {

			document.addField("id", NumberUtils.toInt(goods.get("goods_id").toString()));
			
			String goods_name = goods.get("goods_name").toString();
			System.out.println(goods_name);
			String name_quanpin = goods.get("name_quanpin").toString();
			String name_header_py = goods.get("name_header_py").toString();

			document.addField("name", goods_name);
			document.addField("name_quanpin", name_quanpin);
			document.addField("name_header_py",name_header_py);
			
			String thumb = goods.get("thumbnail") == null ? "" : goods.get(
					"thumbnail").toString();
			document.addField("thumbnail", thumb);

			// 价格要排序
			Double p = StringUtil.toDouble(goods.get("price").toString(), 0d);
			document.addField("price_sort",NumericUtils.doubleToSortableLong(p));
			document.addField("price", p);

			// 销量要排序
			Integer buy_count = (Integer) goods.get("buy_count");
			if (buy_count == null) {
				buy_count = 0;
			}
			document.addField("buynum_sort", buy_count);
			document.addField("buy_count", buy_count);

			// 销量 目前还未统一是用 buy_count 还是 buy_num 暂时共存 b2c用的是buy_count
			// b2b2c存在buy_count 和 buy_num 需要去除buy_num
			// 所以先共存一下 v53版本去除
			if (EopSetting.PRODUCT.equals("b2b2c")) {
				Integer buy_num = (Integer) goods.get("buy_num");
				document.addField("buy_num", buy_num);
				
				Integer store_id = (Integer) goods.get("store_id");
				document.addField("store_id", store_id);
				
				String store_name = goods.get("store_name").toString();
				document.addField("store_name", store_name);
				
			
				

				// 评论数量 目前也只有b2b2c有
				Integer comment_num = (Integer) goods.get("comment_num");
				document.addField("comment_num", comment_num);
			}

			// 评价要排序
			Integer grade = (Integer) goods.get("grade");
			if (grade == null) {
				grade = 0;
			}
			document.addField("grade_sort", grade);
			document.addField("grade", grade);

			document.addField("goods_id", goods.get("goods_id").toString());

			// 品牌维度
			document.addField("brand_id", NumberUtils.toInt(goods.get("brand_id").toString(), 0));

			// 分类维度
			Integer catid = NumberUtils.toInt(goods.get("cat_id").toString(), 0);
			document.addField("cat_id", catid);
			Cat cat = this.goodsCatManager.getById(catid);
			if (cat != null) {
				document.addField("catpath", cat.getCat_path());
			}

			/*---------------------------------*/
			/*
			 * 创建属性维度 /* -------------------------------
			 */

			Integer typeid = StringUtil.toInt(goods.get("type_id").toString(),
					0);
			GoodsTypeDTO goodsType = goodsTypeManager.get(typeid);

			List<Attribute> attrList = goodsType.getPropList();
			int i = 1;
			for (Attribute attribute : attrList) {
				String attrName = attribute.getName();

				String attrValue = "";

				// 如果是选择项-可搜索
				if (attribute.getType() == AttributeType.SELECT_SEARCH) {
					String[] s = attribute.getOptionAr();
					String pvalue = (String) goods.get("p" + i);
					Integer num = 0;
					if (!StringUtil.isEmpty(pvalue)) {
						num = Integer.parseInt(pvalue);
					}
					attrValue = s[num];

					// 或者是 输入项 可搜索
				} else if (attribute.getType() == AttributeType.INPUT_SEARCH) {
					attrValue = (String) goods.get("p" + i);

					// 其他项不参与搜索
				} else {
					continue;
				}

				// 如果没有数据也跳过
				if (StringUtil.isEmpty(attrName)
						|| StringUtil.isEmpty(attrValue)) {
					continue;
				}

				//使用动态字段*_s匹配
				document.addField("p_" + HexUtil.encode(attrName) + "_s", attrValue);
				i++;
			}



			// disabled 是否删除
			document.addField("disabled", goods.get("disabled").toString());
			
			// 是否上架
			document.addField("market_enable", goods.get("market_enable").toString());

			// 激发商品索引事件
			this.goodsIndexPluginBundle.onIndex(goods, document);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return document;

	}

	/**
	 * 将分词结果写入数据库
	 * 
	 * @param wordsList
	 */
	@SuppressWarnings("unchecked")
	private void wordsToDb(List<String> wordsList) {

		for (String words : wordsList) {
			int count = this.daoSupport
					.queryForInt(
							"select count(0)  from es_goods_words where words=?",
							words);

			// 已经存在此分词 +1
			if (count > 0) {
				this.daoSupport
						.execute(
								"update es_goods_words g set g.goods_num=g.goods_num+1 where g.words=? ",
								words);

			} else {
				Map data = new HashMap();
				data.put("words", words);
				String name_quanpin = PinYinUtil.getPingYin(words); // 全拼
				String name_header_py = PinYinUtil.getPinYinHeadChar(words);// 首字母
				data.put("quanpin", name_quanpin);
				data.put("szm", name_header_py);
				data.put("goods_num", 0);

				this.daoSupport.insert("es_goods_words", data);
			}

		}
	}
	
	

	public static void main(String[] args) throws UnsupportedEncodingException, DecoderException {
		String str = "";
		List<String> list = toWordsList(str);
		for (String s : list) {
			System.out.println(s);
		}
	}

	
	

}
