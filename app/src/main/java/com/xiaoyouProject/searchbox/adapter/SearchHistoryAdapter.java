package com.xiaoyouProject.searchbox.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.xiaoyouProject.searchbox.custom.IOnItemClickListener;
import com.xiaoyouProject.searchbox.entity.CustomLink;
import com.zhenxi.jnitrace.R;

import java.util.List;


/**
 * 自定义内容展示列表
 *
 * @author 小游
 * @date 2021/02/23
 */
public class SearchHistoryAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * 自己定义两个不同类型的布局
     */
    public static final int ITEM_HISTORY = 1;
    public static final int ITEM_LINK = 2;

    private final Context context;
    private final List<CustomLink<T>> items;

    public SearchHistoryAdapter(Context context, List<CustomLink<T>> items) {
        this.context = context;
        this.items = items;
    }

    // 根据类型不同我们来进行重写
    @Override
    public int getItemViewType(int position) {
        // 直接返回当前的类型
        return items.get(position).getItemType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 判断不同的类型来显示不同的控件
        RecyclerView.ViewHolder holder = null;
        if (viewType == ITEM_HISTORY) {
            holder = new HistoryHolder(LayoutInflater.from(context).
                    inflate(R.layout.item_search_history, parent, false));
        } else {
            holder = new LinkHolder(LayoutInflater.from(context).
                    inflate(R.layout.item_search_link, parent, false));
        }
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        // 不同的类型对应不同的点击事件
        if (getItemViewType(position) == ITEM_HISTORY) {
            HistoryHolder historyHolder = (HistoryHolder) holder;
            historyHolder.historyInfo.setText(items.get(position).getTitle());
            historyHolder.historyInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iOnItemClickListener.onItemClick(items.get(position).getTitle());
                }
            });
            historyHolder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iOnItemClickListener.onItemDeleteClick(items.get(position));
                }
            });
        } else {
            LinkHolder linkHolder = (LinkHolder) holder;
            linkHolder.linkInfo.setText(items.get(position).getTitle());
            linkHolder.linkInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iOnItemClickListener.onLinkClick(items.get(position).getData());
                }
            });
            linkHolder.linkInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    iOnItemClickListener.onLinkClick(items.get(position).getData());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * 历史记录的 viewHolder
     */
    static class HistoryHolder extends RecyclerView.ViewHolder {
        TextView historyInfo;
        ImageView delete;

        public HistoryHolder(View view) {
            super(view);
            historyInfo = view.findViewById(R.id.tv_item_search_history);
            delete = view.findViewById(R.id.iv_item_search_delete);
        }
    }

    /**
     * 链接的ViewHolder
     */
    static class LinkHolder extends RecyclerView.ViewHolder {
        TextView linkInfo;

        public LinkHolder(View view) {
            super(view);
            linkInfo = view.findViewById(R.id.tv_item_search_link);
        }
    }


    private IOnItemClickListener<T> iOnItemClickListener;

    public void setOnItemClickListener(IOnItemClickListener<T> iOnItemClickListener) {
        this.iOnItemClickListener = iOnItemClickListener;
    }

}
