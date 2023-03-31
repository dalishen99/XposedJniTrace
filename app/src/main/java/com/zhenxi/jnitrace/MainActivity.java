package com.zhenxi.jnitrace;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.snackbar.Snackbar;
import com.zhenxi.jnitrace.bean.AppBean;
import com.zhenxi.jnitrace.adapter.MainListViewAdapter;
import com.zhenxi.jnitrace.utils.PermissionUtils;
import com.zhenxi.jnitrace.utils.ToastUtils;
import com.zhenxi.jnitrace.view.Xiaomiquan;
import com.xiaoyouProject.searchbox.SearchFragment;
import com.xiaoyouProject.searchbox.custom.IOnSearchClickListener;
import com.xiaoyouProject.searchbox.entity.CustomLink;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ListView mLv_list;
    private ArrayList<AppBean> mAllPackageList = new ArrayList<>();
    private final ArrayList<AppBean> mCommonPackageList = new ArrayList<>();
    private Toolbar mToolbar;

    private final String[] permissionList = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INSTALL_PACKAGES,
            Manifest.permission.GET_PACKAGE_SIZE
    };


    private CheckBox mCb_checkbox;
    private CheckBox mCb_IsSerialization;
    private CheckBox misPassRoot;
    private ImageView search;
    private MainListViewAdapter mMainListViewAdapter;


    public static class configInfo{

        /**
         * 是否开启内存序列化
         */
        boolean isSerialization = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initView();
        PermissionUtils.initPermission(this, permissionList);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            if (granted) {

            } else {

            }
        }
    }


    private void initData() {
        mAllPackageList = getPackageList();
        //CLogUtils.e("发现 "+mAllPackageList.size()+" App");
    }

    private void initView() {
        mLv_list = findViewById(R.id.lv_list);

        mToolbar = findViewById(R.id.tb_toolbar);

        mCb_checkbox = findViewById(R.id.cb_checkbox);
        mCb_IsSerialization = findViewById(R.id.cb_invoke);

        //目前只支持9-11
        if(Build.VERSION.SDK_INT
                < Build.VERSION_CODES.P||Build.VERSION.SDK_INT>=31) {
            mCb_IsSerialization.setVisibility(View.GONE);
        }

        misPassRoot = findViewById(R.id.cb_isPassRoot);
        mCb_IsSerialization.setOnClickListener(v -> {
            if (mCb_IsSerialization.isChecked()) {
                showDialog();
            }
        });
        search = findViewById(R.id.iv_search);

        mMainListViewAdapter =
                new MainListViewAdapter(this,mCommonPackageList, mCb_IsSerialization);

        mCb_checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //需要显示 系统 app
                mMainListViewAdapter.setData(mAllPackageList);
            } else {
                mMainListViewAdapter.setData(mCommonPackageList);
            }
        });


        mToolbar.setTitle("");

        mToolbar.inflateMenu(R.menu.main_activity);

        mToolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.xiaomiquan) {
                xiaomiquan();
            } else if (item.getItemId() == R.id.kecheng) {
                Uri uri = Uri.parse("https://pan.baidu.com/s/17aDu5b0Qb0OR4qwBfxhFgw");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                ToastUtils.showToast(getBaseContext(), "解压密码 qqqq");
            } else if (item.getItemId() == R.id.info) {
                Uri uri = Uri.parse("https://pan.baidu.com/s/17aDu5b0Qb0OR4qwBfxhFgw");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
                ToastUtils.showToast(getBaseContext(), "解压密码 qqqq");
            }
            return false;
        });

        SearchFragment<AppBean> searchFragment = SearchFragment.newInstance();
        searchFragment.setOnSearchClickListener(new IOnSearchClickListener<AppBean>() {
            @Override
            public void onSearchClick(String key) {
                ArrayList<AppBean> dataList = null;
                if (mCb_checkbox.isChecked()) {
                    dataList = mAllPackageList;
                } else {
                    dataList = mCommonPackageList;
                }
                for (AppBean bean : dataList) {
                    if (bean.appName.equals(key)) {
                        mMainListViewAdapter.initConfig(bean);
                        //CLog.e("history " + bean.toString());
                        return;
                    }
                }
                ToastUtils.showToast(getBaseContext(), "没有找到 " + key + " 程序可能已经被卸载");
            }

            @Override
            public void onLinkClick(AppBean appBean) {
                mMainListViewAdapter.initConfig(appBean);
                searchFragment.historyDb.insertHistory(appBean.appName);
            }


            @Override
            public void onTextChange(String key) {
                List<CustomLink<AppBean>> data = new ArrayList<>();
                ArrayList<AppBean> dataList = null;
                if (mCb_checkbox.isChecked()) {
                    dataList = mAllPackageList;
                } else {
                    dataList = mCommonPackageList;
                }
                for (AppBean bean : dataList) {
                    if (bean.appName.contains(key)) {
                        data.add(new CustomLink<AppBean>(bean.appName, bean));
                    }
                }
                searchFragment.setLinks(data);
            }

        });
        search.setOnClickListener(v -> searchFragment.showFragment(getSupportFragmentManager(), SearchFragment.TAG));
        mLv_list.setAdapter(mMainListViewAdapter);
        mMainListViewAdapter.notifyDataSetChanged();

    }

    private void showDialog() {
        Snackbar make = Snackbar.make(mCb_IsSerialization,
                "内存漫游是将宿主Apk的全部Java对象实例用JSON字符串的方式进行保存和打印。\n" +
                        "主要用于方便快速分析目标ApkJava对象的结构顺序,每个类保存了哪些数据 。\n"+
                        "当开启以后,会在宿主Apk运行30秒以后进行内存序列化。此过程需要一些时间。\n" +
                        "保存文件路径在/data/data/包名下。\n"+
                        "请注意:\n" +
                        "开起了内存漫游以后其他Hook功能则不会生效!\n",
                Snackbar.LENGTH_LONG);
        make.setDuration(Snackbar.LENGTH_INDEFINITE);
        Snackbar.SnackbarLayout layout = (Snackbar.SnackbarLayout) make.getView();
        TextView textView =
                layout.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(100);
        make.setAction("关闭", view -> make.dismiss()).show();
    }

    private void xiaomiquan() {
        startActivity(new Intent(this, Xiaomiquan.class));
    }

    public ArrayList<AppBean> getPackageList() {
        PackageManager packageManager = getPackageManager();
        List<PackageInfo> packages = packageManager.getInstalledPackages(0);
        ArrayList<AppBean> appBeans = new ArrayList<>();

        for (PackageInfo packageInfo : packages) {
            AppBean appBean = new AppBean();
            // 判断系统/非系统应用
            // 非系统应用
            // 系统应用
            appBean.isSystemApp = (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
            appBean.appName = packageInfo.applicationInfo.loadLabel(getPackageManager()).toString();
            appBean.packageName = packageInfo.packageName;
            appBean.appIcon = packageInfo.applicationInfo.loadIcon(getPackageManager());

            appBeans.add(appBean);

            if (!appBean.isSystemApp) {
                mCommonPackageList.add(appBean);
            }

        }
        return appBeans;
    }


}
