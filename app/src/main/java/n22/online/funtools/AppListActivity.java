package n22.online.funtools;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.SDCardUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import n22.online.funtools.adapter.AppLiatAdapter;
import n22.online.funtools.bean.AppBean;
import n22.online.funtools.utils.DialogHelp;

import static com.blankj.utilcode.util.Utils.getContext;

public class AppListActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private ProgressDialog pd;

    public static void startPage(Activity activity) {
        activity.startActivity(new Intent(activity, AppListActivity.class));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);
        initView();
        loadData();
    }

    private void loadData(){
        pd = new ProgressDialog(this);
        pd.setMessage("加载中...");
        pd.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<AppBean> appList = getAllApk();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (pd != null && pd.isShowing()) {
                            pd.dismiss();
                        }
                        initAdapter(appList);
                    }
                });
            }
        }).start();
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        //添加Android自带的分割线
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    @SuppressWarnings("unchecked")
    private void initAdapter(final List<AppBean> appList) {
        BaseQuickAdapter appListAdapter = new AppLiatAdapter(R.layout.app_item_layout, appList);
        appListAdapter.openLoadAnimation();
        appListAdapter.setOnItemLongClickListener(new BaseQuickAdapter.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(BaseQuickAdapter adapter, View view, int position) {
                show(appList.get(position));
                return true;
            }
        });
        mRecyclerView.setAdapter(appListAdapter);
    }

    public void show(final AppBean appBean) {
        DialogHelp.getConfirmDialog(AppListActivity.this, "确认导出" + appBean.getAppName() + "?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String newPath = SDCardUtils.getSDCardPath() + "exapp/" + appBean.getAppName() + ".apk";
                try {
                    boolean fileFromIS = FileUtils.writeFileFromIS(FileUtils.getFileByPath(newPath), new FileInputStream(appBean.getApkPath()), false);
                    if(fileFromIS){
                        loadData();
                    }
                    ToastUtils.showLong(fileFromIS ? "导出成功" : "导出失败");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }).show();
    }

    public static List<AppBean> getAllApk() {
        List<AppBean> appBeanList = new ArrayList<>();
        AppBean bean = null;
        PackageManager packageManager = getContext().getPackageManager();
        List<PackageInfo> list = packageManager.getInstalledPackages(0);
        for (PackageInfo p : list) {
            bean = new AppBean();
            bean.setAppIcon(p.applicationInfo.loadIcon(packageManager));
            bean.setAppName(packageManager.getApplicationLabel(p.applicationInfo).toString());
            bean.setAppPackageName(p.applicationInfo.packageName);
            bean.setApkPath(p.applicationInfo.sourceDir);
            File file = new File(p.applicationInfo.sourceDir);
            bean.setAppSize((int) file.length());
            int flags = p.applicationInfo.flags;
            //判断是否是属于系统的apk
            if ((flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                bean.setSystem(true);
            } else {
                bean.setSd(true);
            }
            appBeanList.add(bean);
        }
        return appBeanList;
    }

}
