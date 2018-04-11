package com.enation.app.javashop.solr.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.chenlb.mmseg4j.analysis.MaxWordAnalyzer;
import com.enation.framework.util.StringUtil;

/**
 * 
 *
 * @author Sylow
 * @version v1.0,2016年1月25日
 * @since v5.2
 */
public class SimpleDemo {

	//private static String dirPath = "/Users/Sylow/Desktop/luence/doc";
	private static String indexPath = "/Users/Sylow/Desktop/luence/index";
	
    public static void main(String[] args) throws IOException {
        
        createIndex("苹果笔记本-MacBook Pro" , false);
        //createIndex("亨氏 (Heinz) 苹果汁 1段 (辅食添加初期-36个月适用)" , false);
        
    	try {
    		String keyword = "苹";
    		
			search(keyword);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void search(String keyword) throws IOException, ParseException {
    	File indexDir = new File(indexPath);
		FSDirectory directory = FSDirectory.open(indexDir.toPath());
		IndexReader reader = DirectoryReader.open(directory);
		IndexSearcher searcher = new IndexSearcher(reader);
		
		//HttpServletRequest request = ThreadContextHolder.getHttpRequest();
		//String keyword = "苹";

		Analyzer analyzer = getAnalyzer();

		BooleanQuery query = new BooleanQuery();
		// 关键字检索
		if (!StringUtil.isEmpty(keyword)) {
			QueryParser parser = new QueryParser("name", analyzer);
			Query queryname =  parser.parse( keyword );
			query.add(queryname,BooleanClause.Occur.MUST);
		}
		
		TopDocs results = searcher.search(query, 100);
		
		ScoreDoc[] hits = results.scoreDocs;
		//int totalCount = results.totalHits;
		
		//List<Document> list = new ArrayList<Document>();
		
		for (ScoreDoc hit : hits) {
			Document document = searcher.doc(hit.doc);
			//list.add(document);
			System.out.println(document.get("name"));
		}
		
		
    }

    /**
     * 创建索引
     * @param dirPath 需要索引的文件目录
     * @param indexPath 索引存放目录
     * @param createOrAppend
     * @throws IOException
     */
    public static void createIndex(String name, boolean createOrAppend) throws IOException {
        
        Directory directory = FSDirectory.open(Paths.get(indexPath, new String[0]));
        Analyzer analyzer = getAnalyzer();
        IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
        if(createOrAppend){
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        }else{
            indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        }
        IndexWriter indexWriter = new IndexWriter(directory,indexWriterConfig);
        indexDocs(indexWriter, name);
        indexWriter.close();
        //System.out.println(System.currentTimeMillis()-startTime);
    }

    /**
     * 根据文件路径对文件内容进行索引
     * @param indexWriter
     * @param path
     * @throws IOException
     */
    public static void indexDocs(IndexWriter indexWriter, String name) throws IOException {
        Document document = new Document();
        Field nameField = new TextField("name", name, Field.Store.YES);
        document.add(nameField);
       
        if(indexWriter.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE){
            indexWriter.addDocument(document);
        }else{
            indexWriter.updateDocument(new Term("name", name), document);
        }
        indexWriter.commit();
    }
    
    private static Analyzer getAnalyzer() {
		//return new StandardAnalyzer();
		//return new SimpleAnalyzer();
		//return new MMSegAnalyzer();
		return new MaxWordAnalyzer();
	}

}
