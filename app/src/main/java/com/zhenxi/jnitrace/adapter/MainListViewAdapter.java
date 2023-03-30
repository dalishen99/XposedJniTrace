package com.zhenxi.jnitrace.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.zhenxi.jnitrace.BuildConfig;
import com.zhenxi.jnitrace.R;
import com.zhenxi.jnitrace.bean.AppBean;

import com.zhenxi.jnitrace.utils.CLog;
import com.zhenxi.jnitrace.utils.Constants;
import com.zhenxi.jnitrace.utils.FileUtils;
import com.zhenxi.jnitrace.utils.RootUtils;
import com.zhenxi.jnitrace.utils.SpUtil;
import com.zhenxi.jnitrace.utils.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;


import static com.zhenxi.jnitrace.config.ConfigKey.CONFIG_JSON;
import static com.zhenxi.jnitrace.config.ConfigKey.IS_SERIALIZATION;
import static com.zhenxi.jnitrace.config.ConfigKey.MOUDLE_SO_PATH;
import static com.zhenxi.jnitrace.config.ConfigKey.PACKAGE_NAME;
import static com.zhenxi.jnitrace.config.ConfigKey.SAVE_TIME;


import androidx.appcompat.app.AlertDialog;

import org.json.JSONObject;


/**
 * Created by lyh on 2019/2/14.
 */
public class MainListViewAdapter extends BaseAdapter {


    private ArrayList<AppBean> data;

    private final Context mContext;
    private final CheckBox isSerialization;


    public MainListViewAdapter(Context context,
                               ArrayList<AppBean> data,
                               CheckBox info) {
        this.mContext = context;
        this.data = data;
        this.isSerialization = info;

    }


    public void setData(ArrayList<AppBean> data) {

        this.data = data;

        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return data == null ? 0 : data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = View.inflate(mContext, R.layout.activity_list_item, null);
        }
        ViewHolder holder = ViewHolder.getHolder(convertView);
        AppBean appBean = data.get(position);

        holder.iv_appIcon.setImageBitmap(Constants.drawable2Bitmap(appBean.appIcon));
        holder.tv_appName.setText(appBean.appName);
        holder.tv_packageName.setText(appBean.packageName);
        holder.All.setOnClickListener(v ->
                saveConfig(appBean)
        );
        return convertView;
    }

    private  ArrayList<String>  mSoFiltersList = null;


    /**
     * 弹出对话框收集用户需要采集的So调用信息
     */
    private void showDialogForList(Context context){
        View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_input, null);
        EditText input = (EditText) view.findViewById(R.id.ed_input);
        new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton("确定", (dialog, which) -> {
                    String inputStr = input.getText().toString();
                    if(inputStr.length() == 0){
                        ToastUtils.showToast(context,"输入错误,未找到需要需要监听的SO信息");
                        return;
                    }
                    if(inputStr.equals("ALL")) {
                        //集合个数为0并且不等于NULL,则认为监听ALL
                        mSoFiltersList = new ArrayList<>();
                    }else {
                        String[] split = inputStr.split("\\|");
                        CLog.e("input str msg -> "+Arrays.toString(split));
                        if(mSoFiltersList == null) {
                            mSoFiltersList = new ArrayList<>();
                        }
                        mSoFiltersList.addAll(Arrays.asList(split));
                    }
                    // 点击了确认按钮
                    dialog.dismiss();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    // 点击了取消按钮
                    dialog.dismiss();
                })
                .create()
                .show();
    }

    /**
     * 保存配置信息
     */
    public void saveConfig(AppBean bean) {
        if(!isSerialization.isChecked()){
            //没有选中内存漫游,则执行正常逻辑
            showDialogForList(mContext);
        }
        //init config json
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(PACKAGE_NAME, bean.packageName);
            try {
                PackageInfo packageInfo =
                        mContext.getPackageManager().getPackageInfo(BuildConfig.APPLICATION_ID, 0);
                jsonObject.put(MOUDLE_SO_PATH, packageInfo.applicationInfo.publicSourceDir);
            } catch (Throwable ignored) {
                jsonObject.put(MOUDLE_SO_PATH, null);
            }
            //保存时间,增加时效性
            jsonObject.put(SAVE_TIME, System.currentTimeMillis() + "");
            String isMemSerialization = isSerialization.isChecked() + "";
            //CLog.e("isMemSerialization "+isMemSerialization);
            jsonObject.put(IS_SERIALIZATION, isMemSerialization);
            if(!isSerialization.isChecked()){
                if(isAllListening){

                }
            }
        } catch (Throwable e) {
            CLog.e("save config to json error "+e);
        }

        CLog.e("save config file info -> "+jsonObject);

        SpUtil.putString(mContext, CONFIG_JSON, jsonObject.toString());
        saveConfig(bean.packageName, jsonObject);
        ToastUtils.showToast(mContext,
                "保存成功文件路径为\n" +
                        "data/data/" + bean.packageName);
    }

    /**
     * 很多加壳app
     * shared = new XSharedPreferences(BuildConfig.APPLICATION_ID, "config"); 导致失效
     * 通过root强行将数据保存一份到目标apk 私有目录
     */
    private void saveConfig(String packageName, JSONObject jsonObject) {
        try {
            File filesDir = mContext.getFilesDir();
            FileUtils.makeSureDirExist(filesDir);
            File config = new File(filesDir.getPath() + "/" + BuildConfig.project_name + "Config");
            CLog.e("temp config file path " + config.getPath());
            File temp = new File("/data/data/" + packageName);

            if (config.exists()) {
                boolean delete = config.delete();
                if (!delete) {
                    CLog.e("delete org config file error");
                }
            }
            boolean newFile = config.createNewFile();
            if (!newFile) {
                CLog.e("create config file error " + config.getPath());
            }
            FileUtils.saveString(config, jsonObject.toString());
            RootUtils.execShell("mv " + config.getPath() + " " + temp);
            CLog.e("config mv success !");
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


    private static class ViewHolder {
        TextView tv_appName, tv_packageName;
        LinearLayout All;
        ImageView iv_appIcon;

        ViewHolder(View convertView) {
            All = convertView.findViewById(R.id.ll_all);
            tv_packageName = convertView.findViewById(R.id.tv_packName);
            tv_appName = convertView.findViewById(R.id.tv_appName);
            iv_appIcon = convertView.findViewById(R.id.iv_appIcon);
        }

        static ViewHolder getHolder(View convertView) {
            ViewHolder holder = (ViewHolder) convertView.getTag();
            if (holder == null) {
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            return holder;
        }
    }
}
