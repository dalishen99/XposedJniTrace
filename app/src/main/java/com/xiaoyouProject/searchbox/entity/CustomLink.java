package com.xiaoyouProject.searchbox.entity;

import static com.xiaoyouProject.searchbox.adapter.SearchHistoryAdapter.ITEM_LINK;

/**
 * 自定义链接,包括标题和链接
 * @author 小游
 * @date 2021/02/23
 */
public class CustomLink<T> {

    public CustomLink(){ }

    public CustomLink(String tittle, T data){
        this.title = tittle;
        this.data = data;
        this.itemType = ITEM_LINK;
    }

    public CustomLink(String tittle, T data,int itemType){
        this.title = tittle;
        this.data = data;
        this.itemType = itemType;
    }

    private String title;
    private T data;
    private int itemType;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }
}
