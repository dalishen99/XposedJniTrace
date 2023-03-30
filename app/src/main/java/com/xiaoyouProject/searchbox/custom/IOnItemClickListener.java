package com.xiaoyouProject.searchbox.custom;

import com.xiaoyouProject.searchbox.entity.CustomLink;

/**
 * adapter的回调函数
 * @author 小游
 * @date 2021/02/23
 */
public interface IOnItemClickListener<T> {
    /**
     *  点击历史链接
     * @param keyword 关键词
     */
    void onItemClick(String keyword);

    /**
     *  点击删除按钮
     * @param keyword 关键词
     */
    void onItemDeleteClick(CustomLink<T> keyword);

    /**
     *  点击 链接
     * @param keyword 关键词
     */
    void onLinkClick(T keyword);
}
