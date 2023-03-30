package com.xiaoyouProject.searchbox.custom;

/**
 * 回调事件
 * @author 小游
 * @date 2021/02/23
 */
public interface IOnSearchClickListener<T> {

    /**
     *  点击搜索按钮时触发
     * @param keyword 搜索的关键词
     */
    void onSearchClick(String keyword);

    /**
     *  点击链接时触发
     * @param data 链接携带的数据
     */
    void onLinkClick(T data);

    /**
     *  搜索框内容改变时触发数据
     * @param keyword 搜索的关键词
     */
    void onTextChange(String keyword);

}
