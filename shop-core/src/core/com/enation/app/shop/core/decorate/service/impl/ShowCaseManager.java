package com.enation.app.shop.core.decorate.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.enation.app.shop.core.decorate.DecoratePluginsBundle;
import com.enation.app.shop.core.decorate.model.ShowCase;
import com.enation.app.shop.core.decorate.service.IShowCaseManager;
import com.enation.app.shop.core.goods.model.Goods;
import com.enation.framework.database.IDaoSupport;
import com.enation.framework.util.StringUtil;

/**
 * 橱窗管理实现类
 * @author    jianghongyan
 * @version   1.0.0,2016年6月20日
 * @since     v6.1
 */
@Service
public class ShowCaseManager implements IShowCaseManager{

	@Autowired
	private IDaoSupport daoSupport;
	
	@Autowired
	private DecoratePluginsBundle decoratePluginsBundle;
	
	@Override
	public List listAll() {
		String sql="select * from es_showcase order by sort";
		List list=this.daoSupport.queryForList(sql, ShowCase.class);
		return list;
	}

	@Override
	public void saveShowCase(ShowCase showcase) throws RuntimeException {
		try {
			this.daoSupport.insert("es_showcase",showcase);
			Integer showcase_id=this.daoSupport.getLastId("es_showcase");
			this.decoratePluginsBundle.onSave(DecorateTypeEnum.SHOWCASE.toString(),showcase_id);
		} catch (Exception e) {
			throw new RuntimeException("保存失败");
		}
	}

	@Override
	public void saveSort(Integer[] showcase_ids,Integer[] showcase_sorts) throws RuntimeException {
		//拼接sql语句
				try {
					StringBuffer sqlsb=new StringBuffer();
					sqlsb.append("update es_showcase set sort= case id ");
					for(int i=0;i<showcase_ids.length;i++){
						sqlsb.append(" when "+showcase_ids[i]);
						sqlsb.append(" then "+showcase_sorts[i]);
					}
					sqlsb.append(" end ");
					sqlsb.append(" where id in ");
					sqlsb.append("(");
					for(int i=0;i<showcase_ids.length;i++){
						sqlsb.append(showcase_ids[i]+",");
					}
					sqlsb.deleteCharAt(sqlsb.lastIndexOf(","));
					sqlsb.append(")");
					String sql=sqlsb.toString();
					this.daoSupport.execute(sql);
					this.decoratePluginsBundle.onSave(DecorateTypeEnum.SHOWCASE.toString(),null);
				} catch (Exception e) {
					throw new RuntimeException();
				}
	}

	@Override
	public void saveDisplay(Integer id, Integer is_display)
			throws RuntimeException {
		try {
			Map<String,Object> fields=new HashMap<String,Object>();
			fields.put("is_display", is_display);
			this.daoSupport.update("es_showcase", fields, "id="+id);
			this.decoratePluginsBundle.onSave(DecorateTypeEnum.SHOWCASE.toString(),id);
		} catch (Exception e) {
			throw new RuntimeException("保存排序失败");
		}
	}

	@Override
	public ShowCase getShowCaseById(Integer id) {
		String sql="select * from es_showcase where id=?";
		ShowCase showCase=(ShowCase) this.daoSupport.queryForObject(sql, ShowCase.class, id);
		return showCase;
	}

	@Override
	public void saveEdit(ShowCase showCase) throws RuntimeException {
		try {
			Map<String,Object> fields=new HashMap<String,Object>();
			fields.put("title", showCase.getTitle());
			fields.put("flag",  showCase.getFlag());
			fields.put("content", showCase.getContent());
			fields.put("is_display", showCase.getIs_display());
			fields.put("sort", showCase.getSort());
			this.daoSupport.update("es_showcase", fields, "id="+showCase.getId());
			this.decoratePluginsBundle.onSave(DecorateTypeEnum.SHOWCASE.toString(),showCase.getId());
		} catch (Exception e) {
			throw new RuntimeException("修改失败");
		}
	}

	@Override
	public void delete(Integer id) throws RuntimeException {
		try {
			String sql ="delete from es_showcase where id=?";
			this.daoSupport.execute(sql, id);
			this.decoratePluginsBundle.onSave(DecorateTypeEnum.SHOWCASE.toString(),id);
		} catch (Exception e) {
			throw new RuntimeException("修改失败");
		}
	}

	@Override
	public List<Goods> getSelectGoods(String content) {
		if(!StringUtil.isEmpty(content)){
			String sql="select * from es_goods where goods_id in ("+content+") order by instr('"+content+"',goods_id)";
			return this.daoSupport.queryForList(sql);
		}else{
			return new ArrayList<Goods>();
		}
	}

	@Override
	public List getShowCaseByFlag(String flag) {
		// TODO Auto-generated method stub
		String sql="select * from es_showcase where flag=? and is_display=0 order by sort";
		return this.daoSupport.queryForList(sql, flag);
	}
}
