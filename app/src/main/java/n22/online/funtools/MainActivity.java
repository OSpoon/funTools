package n22.online.funtools;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import n22.online.funtools.adapter.MenuLiatAdapter;
import n22.online.funtools.bean.EventMessage;
import n22.online.funtools.bean.MenuBean;
import n22.online.funtools.utils.DialogHelp;
import n22.online.funtools.utils.EventBusUtils;
import n22.online.funtools.utils.FormatJson;
import n22.online.funtools.utils.FunTools;
import n22.online.funtools.utils.RuntimeRationale;

public class MainActivity extends AppCompatActivity implements BaseQuickAdapter.OnItemClickListener {

    public static final String TAG = "MainActivity";
    private int REQUEST_CODE_SCAN = 111;

    RadioButton rb_uat;
    RadioButton rb_sc;

    RadioButton rb_new;

    AppCompatTextView tv_hint;
    AppCompatTextView tv_hint_1;

    private RecyclerView mRecyclerView;
    private DrawerLayout mDrawerLayout;

    private List<MenuBean> menuList;

    private static final int REQUEST_CODE_SETTING = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBusUtils.register(this);
        setContentView(R.layout.activity_main);
        initData();
        initView();

        requestPermission(Permission.Group.STORAGE);

        FunTools.checkHelperVersion(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBusUtils.unregister(this);
    }

    private void initData() {
        menuList = new ArrayList<>();
        menuList.add(new MenuBean("1", "清除资源文件"));
        menuList.add(new MenuBean("2", "初始化资源版本"));
        menuList.add(new MenuBean("3", "快速安装"));
        menuList.add(new MenuBean("4", "获取服务器版本"));
        menuList.add(new MenuBean("5", "获取本地资源版本"));
        menuList.add(new MenuBean("6", "解压资源文件"));
        menuList.add(new MenuBean("7", "隐藏资源图片"));
        menuList.add(new MenuBean("8", "资源数量统计"));
        menuList.add(new MenuBean("9", "获取下载地址"));
        menuList.add(new MenuBean("10", "快速修改资源版本"));
        menuList.add(new MenuBean("11", "公告"));
        menuList.add(new MenuBean("12", "帮助"));
        menuList.add(new MenuBean("13", "PING"));
        menuList.add(new MenuBean("14", "扫一扫"));
        menuList.add(new MenuBean("15", "应用列表"));
        menuList.add(new MenuBean("16", "打赏"));
    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.rv_menu_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));
        BaseQuickAdapter appListAdapter = new MenuLiatAdapter(R.layout.menu_item_layout, menuList);
        appListAdapter.openLoadAnimation();
        appListAdapter.setOnItemClickListener(this);
        mRecyclerView.setAdapter(appListAdapter);

        rb_uat = (RadioButton) findViewById(R.id.rb_uat);
        rb_sc = (RadioButton) findViewById(R.id.rb_sc);

        rb_new = (RadioButton) findViewById(R.id.rb_new);

        tv_hint_1 = (AppCompatTextView) findViewById(R.id.tv_hint_1);
        tv_hint = (AppCompatTextView) findViewById(R.id.tv_hint);
        tv_hint.setMovementMethod(ScrollingMovementMethod.getInstance());
        tv_hint.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (!TextUtils.isEmpty(tv_hint.getText())) {
                    ClipboardManager cmb = (ClipboardManager) MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
                    cmb.setText(tv_hint.getText().toString().trim()); //将内容放入粘贴管理器,在别的地方长按选择"粘贴"即可
                    ToastUtils.showShort("复制文本成功");
                }
                return true;
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //下面的代码主要通过actionbardrawertoggle将toolbar与drawablelayout关联起来
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 扫描二维码/条码回传
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                tv_hint.setText(Html.fromHtml("<a href=\"" + content + "\">" + content + "</a><br>点击跳转浏览器查看/下载"));
                tv_hint.setAutoLinkMask(Linkify.ALL);
                tv_hint.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }
    }

    /**
     * 接收到分发的事件
     *
     * @param event 事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveEvent(EventMessage event) {
        if(event.getCode() == 201){
            tv_hint.setText(Html.fromHtml(event.getData().toString()));
        }else{
            tv_hint.setText(FormatJson.format(event.getData().toString()));
        }
        if (event.getCode() == 200) {
            tv_hint.setAutoLinkMask(Linkify.ALL);
            tv_hint.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        mDrawerLayout.closeDrawer(GravityCompat.START);
        int v1 = 0, v2 = 0;
        if (rb_uat.isChecked()) {
            v1 = 2;
        }else if (rb_sc.isChecked()) {
            v1 = 4;
        }
        if (rb_new.isChecked()) {
            v2 = 2;
        }
        switch (menuList.get(position).getMenuId()) {
            case "1":
                FunTools.initDelWwwTask(this, 0, v1, v2);
                break;
            case "2":
                FunTools.initDelWwwTask(this, 1, v1, v2);
                break;
            case "3":
                FunTools.install(this, v1, v2);
                break;
            case "4":
                FunTools.checkVersion(this, v1, v2);
                break;
            case "5":
                FunTools.getWwwVersion(this, v1, v2);
                break;
            case "6":
                FunTools.unZipFile(this, v1, v2);
                break;
            case "7":
                FunTools.create_nomedia(this, v1, v2);
                break;
            case "8":
                FunTools.getFiles(this, v1, v2);
                break;
            case "9":
                FunTools.getDownUrl(this, v1, v2);
                break;
            case "10":
                FunTools.modifyVersion(this, v1, v2);
                break;
            case "11":
                WebViewActivity.startWebPage(this, "http://n22.online/doc/readme.txt");
                break;
            case "12":
                String data = "<h5>各个菜单项简要说明:</h5>" +
                        "<p>1.清除资源文件:清除SD卡存放的资源文件,需光速保重新下载恢复;光速保打开后出现丢失文件可尝试此操作</p>" +
                        "<p>2.初始化资源版本:清除SD卡存放的资源文件版本号,需光速保重新下载恢复;光速保打开后出现丢失文件可尝试此操作</p>" +
                        "<p>3.快速安装:将光速保已下载的安装包快速打开进行安装,会覆盖已安装版本;光速保下载新版本无法正常升级尝试此操作</p>" +
                        "<p>4.获取服务器版本:将获取服务器最新的各项的版本信息;</p>" +
                        "<p>5.获取本地资源版本:将获取SD卡存储的资源版本信息</p>" +
                        "<p>6.解压资源文件:解压存放在SD卡指定目录的压缩包,可进行快速解压</p>" +
                        "<p>7.隐藏资源图片:资源图片会被相册等应用扫描,业务员可能造成误删除,可尝试此操作屏蔽相册扫描</p>" +
                        "<p>8.资源数量统计:统计本地资源文件数据</p>" +
                        "<p>9.获取下载地址:获取光速保下载链接,点击后跳转浏览器下载</p>" +
                        "<p>如有问题请选咨询菜单</p>" +
                        "<p>如查看群公告历史信息选择公告菜单</p>";
                DialogHelp.getMessageDialog(MainActivity.this, data, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
                break;
            case "13":
                FunTools.ping(this);
                break;
            case "14":
                AndPermission.with(this).runtime()
                        .permission(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
                        .rationale(new RuntimeRationale())
                        .onGranted(new Action<List<String>>() {
                            @Override
                            public void onAction(List<String> permissions) {
                                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                                ZxingConfig config = new ZxingConfig();
                                config.setDecodeBarCode(false);//是否扫描条形码 默认为true
                                intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);
                                startActivityForResult(intent, REQUEST_CODE_SCAN);
                            }
                        })
                        .onDenied(new Action<List<String>>() {
                            @Override
                            public void onAction(@NonNull List<String> permissions) {
                                toast(R.string.failure);
                                if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions)) {
                                    showSettingDialog(MainActivity.this, permissions);
                                }
                            }
                        })
                        .start();
                break;
            case "15":
                AppListActivity.startPage(this);
                break;
            case "16":
                FunTools.donateAlipay(this, "FKX03352RMPYBVZMGAFN5F");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Request permissions.
     */
    private void requestPermission(String... permissions) {
        AndPermission.with(this)
                .runtime()
                .permission(permissions)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        toast(R.string.successfully);
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        toast(R.string.failure);
                        if (AndPermission.hasAlwaysDeniedPermission(MainActivity.this, permissions)) {
                            showSettingDialog(MainActivity.this, permissions);
                        }
                    }
                })
                .start();
    }

    protected void toast(@StringRes int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Display setting dialog.
     */
    public void showSettingDialog(Context context, final List<String> permissions) {
        List<String> permissionNames = Permission.transformText(context, permissions);
        String message = context.getString(R.string.message_permission_always_failed,
                TextUtils.join("\n", permissionNames));

        new AlertDialog.Builder(context).setCancelable(false)
                .setTitle(R.string.title_dialog)
                .setMessage(message)
                .setPositiveButton(R.string.setting, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setPermission();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    /**
     * Set permissions.
     */
    private void setPermission() {
        AndPermission.with(this).runtime().setting().start(REQUEST_CODE_SETTING);
    }
}
