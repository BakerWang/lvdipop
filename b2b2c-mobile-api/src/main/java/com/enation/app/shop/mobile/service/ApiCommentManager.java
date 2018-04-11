package com.enation.app.shop.mobile.service;

import org.springframework.stereotype.Service;

import com.enation.app.shop.core.member.model.MemberComment;
import com.enation.eop.sdk.database.BaseSupport;

/**
 * Created by Dawei on 4/11/15.
 */
@Service
public class ApiCommentManager extends BaseSupport<MemberComment> {

    /**
     * 获取商品评论数
     * @param goods_id
     * @return
     */
    public int getCommentsCount(int goods_id) {
        return this.baseDaoSupport.queryForInt("SELECT COUNT(0) FROM member_comment WHERE goods_id=? AND status=1 AND type=1", goods_id);
    }

    /**
     * 获取某个评分以上的评论数
     * @param goods_id
     * @param grade
     * @return
     */
    public int getCommentsCount(int goods_id, int grade){
        return this.baseDaoSupport.queryForInt("SELECT COUNT(0) FROM member_comment WHERE goods_id=? AND status=1 AND type=1 AND grade >= ?", goods_id, grade);
    }


}
