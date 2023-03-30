package com.xiaoyouProject.searchbox;

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.xiaoyouProject.searchbox.adapter.SearchHistoryAdapter;
import com.xiaoyouProject.searchbox.custom.CircularRevealAnim;
import com.xiaoyouProject.searchbox.custom.IOnItemClickListener;
import com.xiaoyouProject.searchbox.custom.IOnSearchClickListener;
import com.xiaoyouProject.searchbox.db.SearchHistoryDB;
import com.xiaoyouProject.searchbox.entity.CustomLink;
import com.xiaoyouProject.searchbox.utils.KeyBoardUtils;
import com.zhenxi.jnitrace.R;

import java.util.ArrayList;
import java.util.List;

/**
 *  搜索fragment
 * @author 小游
 * @date 2021/02/23
 */
public class SearchFragment<T> extends DialogFragment implements DialogInterface.OnKeyListener, ViewTreeObserver.OnPreDrawListener, CircularRevealAnim.AnimListener, IOnItemClickListener<T>, View.OnClickListener {

    public static final String TAG = "SearchFragment";
    private EditText etSearchKeyword;
    private ImageView ivSearchSearch;
    private View searchUnderline;

    private View view;
    //动画
    private CircularRevealAnim mCircularRevealAnim;
    /**历史搜索记录*/
    private List<CustomLink<T>> allItems = new ArrayList<>();
    /**当前链接内容*/
    private final List<CustomLink<T>> items = new ArrayList<>();

    /**适配器*/
    private SearchHistoryAdapter<T> searchHistoryAdapter;
    /**数据库*/
    public SearchHistoryDB<T> historyDb;

    /**
     *  使用newInstance 自动创建数据
     * @param <T> 链接数据类型
     * @return fragment对象
     */
    public static <T> SearchFragment<T> newInstance() {
        return new SearchFragment<T>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.DialogStyle);
    }

    @Override
    public void onStart() {
        super.onStart();
        initDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.dialog_search, container, false);

        init();//实例化

        return view;
    }

    private void init() {
        ImageView ivSearchBack = view.findViewById(R.id.iv_search_back);
        etSearchKeyword = view.findViewById(R.id.et_search_keyword);
        ivSearchSearch = view.findViewById(R.id.iv_search_search);
        RecyclerView rvSearchHistory = view.findViewById(R.id.rv_search_history);
        searchUnderline = view.findViewById(R.id.search_underline2);
        TextView tvSearchClean = view.findViewById(R.id.tv_search_clean);
        View viewSearchOutside = view.findViewById(R.id.view_search_outside);

        //实例化动画效果
        mCircularRevealAnim = new CircularRevealAnim();
        //监听动画
        mCircularRevealAnim.setAnimListener(this);
        //键盘按键监听
        getDialog().setOnKeyListener(this);
        //键盘按键监听
        ivSearchSearch.getViewTreeObserver().addOnPreDrawListener(this);

        //实例化数据库
        historyDb = new SearchHistoryDB<T>(getContext(), SearchHistoryDB.DB_NAME, null, 1);

        allItems = historyDb.queryAllHistory();
        setAllHistorys();

        //初始化recyclerView
        rvSearchHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        searchHistoryAdapter = new SearchHistoryAdapter<>(getContext(), items);
        rvSearchHistory.setAdapter(searchHistoryAdapter);

        //设置删除单个记录的监听
        searchHistoryAdapter.setOnItemClickListener(this);
        //监听编辑框文字改变
        etSearchKeyword.addTextChangedListener(new TextWatcherImpl());
        //监听点击
        ivSearchBack.setOnClickListener(this);
        viewSearchOutside.setOnClickListener(this);
        ivSearchSearch.setOnClickListener(this);
        tvSearchClean.setOnClickListener(this);
    }

    /**
     * 显示Fragment，防止多次打开导致崩溃
     */
    public void showFragment(FragmentManager fragmentManager, String tag) {
        if (!this.isAdded()) {
            this.show(fragmentManager, tag);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.iv_search_back || view.getId() == R.id.view_search_outside) {
            hideAnim();
        } else if (view.getId() == R.id.iv_search_search) {
            search();
        } else if (view.getId() == R.id.tv_search_clean) {
            historyDb.deleteAllHistory();
            items.clear();
            searchUnderline.setVisibility(View.GONE);
            searchHistoryAdapter.notifyDataSetChanged();
        }
    }

    /**
     * 初始化SearchFragment
     */
    private void initDialog() {
        Window window = getDialog().getWindow();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = (int) (metrics.widthPixels * 0.98); //DialogSearch的宽
        window.setLayout(width, WindowManager.LayoutParams.MATCH_PARENT);
        window.setGravity(Gravity.TOP);
        window.setWindowAnimations(R.style.DialogEmptyAnimation);//取消过渡动画 , 使DialogSearch的出现更加平滑
    }

    /**
     * 监听键盘按键
     */
    @Override
    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
            hideAnim();
        } else if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            search();
        }
        return false;
    }

    /**
     * 监听搜索键绘制时
     */
    @Override
    public boolean onPreDraw() {
        ivSearchSearch.getViewTreeObserver().removeOnPreDrawListener(this);
        mCircularRevealAnim.show(ivSearchSearch, view);
        return true;
    }

    /**
     * 搜索框动画隐藏完毕时调用
     */
    @Override
    public void onHideAnimationEnd() {
        etSearchKeyword.setText("");
        dismiss();
    }

    /**
     * 搜索框动画显示完毕时调用
     */
    @Override
    public void onShowAnimationEnd() {
        if (isVisible()) {
            KeyBoardUtils.openKeyboard(getContext(), etSearchKeyword);
        }
    }

    /**
     * 监听编辑框文字改变
     */
    private class TextWatcherImpl implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }
        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

        @Override
        public void afterTextChanged(Editable editable) {
            String keyword = editable.toString();
            if (TextUtils.isEmpty(keyword.trim())) {
                setAllHistorys();
                searchHistoryAdapter.notifyDataSetChanged();
            } else {
                setKeyWordHistorys(editable.toString());
                // 触发监听事件
                iOnSearchClickListener.onTextChange(keyword);
            }
        }
    }

    /**
     * 点击单个搜索记录
     */
    @Override
    public void onItemClick(String keyword) {
        iOnSearchClickListener.onSearchClick(keyword);
        hideAnim();
    }

    /**
     *  点击删除历史记录时触发
     * @param keyword 内容
     */
    @Override
    public void onItemDeleteClick(CustomLink<T> keyword) {
        historyDb.deleteHistory(keyword.getTitle());
        items.remove(keyword);
        checkHistorySize();
        searchHistoryAdapter.notifyDataSetChanged();
    }

    /**
     *  点击链接时触发
     * @param keyword 关键词
     */
    @Override
    public void onLinkClick(T keyword) {
        String searchKey = etSearchKeyword.getText().toString();
        iOnSearchClickListener.onLinkClick(keyword);
        //historyDb.insertHistory(searchKey);
        hideAnim();
    }

    /**
     * 隐藏动画
     */
    private void hideAnim() {
        KeyBoardUtils.closeKeyboard(getContext(), etSearchKeyword);
        mCircularRevealAnim.hide(ivSearchSearch, view);
    }

    /**
     *  点击搜索按钮
     */
    private void search() {
        String searchKey = etSearchKeyword.getText().toString();
        if (TextUtils.isEmpty(searchKey.trim())) {
            Toast.makeText(getContext(), "请输入关键字", Toast.LENGTH_SHORT).show();
        } else {
            //接口回调
            iOnSearchClickListener.onSearchClick(searchKey);
            //插入到数据库
            //historyDb.insertHistory(searchKey);
            hideAnim();
        }
    }

    private void checkHistorySize() {
        if (items.size() < 1) {
            searchUnderline.setVisibility(View.GONE);
        } else {
            searchUnderline.setVisibility(View.VISIBLE);
        }
    }

    /**
     *  设置历史记录
     */
    private void setAllHistorys() {
        items.clear();
        items.addAll(allItems);
        checkHistorySize();
    }

    /**
     *  设置关键词
     * @param keyword 关键词
     */
    private void setKeyWordHistorys(String keyword) {
        items.clear();
        for (CustomLink<T> item : allItems) {
            if (item.getTitle().contains(keyword)) {
                items.add(item);
            }
        }
        searchHistoryAdapter.notifyDataSetChanged();
        checkHistorySize();
    }

    /**
     *  对外暴露链接设置
     * @param data 链接数据
     */
    public void setLinks(List<CustomLink<T>> data){
        // 清除历史记录并设置数据
        items.clear();
        items.addAll(data);
        searchHistoryAdapter.notifyDataSetChanged();
    }


    /**
     *  控件对外暴露的点击事件
     */
    private IOnSearchClickListener<T> iOnSearchClickListener;

    public void setOnSearchClickListener(IOnSearchClickListener<T> iOnSearchClickListener) {
        this.iOnSearchClickListener = iOnSearchClickListener;
    }

}
