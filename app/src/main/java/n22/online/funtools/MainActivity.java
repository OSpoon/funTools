package n22.online.funtools;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.didikee.donate.AlipayDonate;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.Gson;
import com.uuzuche.lib_zxing.activity.CaptureActivity;
import com.uuzuche.lib_zxing.activity.CodeUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cn.com.epsoft.keyboard.PayKeyboardFragment;
import cn.com.epsoft.keyboard.widget.PayKeyboardView;
import n22.online.funtools.bean.PingNetEntity;
import n22.online.funtools.bean.VersionBean;
import n22.online.funtools.utils.DialogHelp;
import n22.online.funtools.utils.ImageUtil;
import n22.online.funtools.utils.PingNet;
import n22.online.funtools.utils.ZipUtil;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks, PayKeyboardView.OnKeyboardListener {

    /* 检测更新地址 */
    public String base_url = "http://mitst.sunlife-everbright.com:8010/com.ifp.ipartner/moUpgrade";
    public String down_url = "http://mitst.sunlife-everbright.com:8010/com.ifp.ipartner/proposalShare/index.html#/down/info/2";
    /* 下载文件名 资源文件名 解压文件名 */
    public String base_name = "gsb_mobile_sit_data";
    /* 下载文件名 APK文件名 解压文件名 */
    public String base_app_name = "gsb_sit_mobile";
    /* 加载本地资源位置,不做修改 */
    public String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + "/www/index.html";
    public String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name;
    /* 加载版本配置文件*/
    public String configPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + "/config.xml";
    /* 解压本地资源位置,不做修改 */
    public String downFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + ".zip";
    /* SD卡根路径,不做修改 */
    public String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

    RadioButton rb_sit;
    RadioButton rb_uat;
    RadioButton rb_sc;
    AppCompatTextView tv_hint;
    AppCompatTextView tv_hint_1;
    ProgressDialog dialog;
    Handler handler;

    PayKeyboardFragment dialogFrag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        rb_sit = (RadioButton) findViewById(R.id.rb_sit);
        rb_uat = (RadioButton) findViewById(R.id.rb_uat);
        rb_sc = (RadioButton) findViewById(R.id.rb_sc);
        tv_hint_1 = (AppCompatTextView) findViewById(R.id.tv_hint_1);
//        tv_hint_1.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/STXINWEI.TTF"));
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
        Utils.init(this);
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
        checkPermission();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 0x300:
                        if (msg.obj != null) {
                            PingNetEntity pingNetEntity = (PingNetEntity) msg.obj;
                            tv_hint.setText(pingNetEntity.getResultBuffer().toString());
                            tv_hint.append("\n" + pingNetEntity.getIp() + " time=" + pingNetEntity.getPingTime() + " " + pingNetEntity.isResult());
                        } else {
                            tv_hint.setText("请稍等...\n");
                        }
                        break;
                }
            }
        };
        checkHelperVersion();
    }

    private static final int INIT_PERM = 0x200;

    private static final int REQUEST_CODE = 0x400;
    private static final int REQUEST_IMAGE = 0x500;

    @AfterPermissionGranted(INIT_PERM)
    private void checkPermission() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
        if (EasyPermissions.hasPermissions(this, perms)) {

        } else {
            EasyPermissions.requestPermissions(this, "请求获取文件读写权限",
                    INIT_PERM, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        ToastUtils.showShort("权限申请通过");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        ToastUtils.showShort("申请权限被拒");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.this.finish();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    private void initDelWwwTask(int index) {
        if (index == 0) {
            DialogHelp.getConfirmDialog(this, "资源文件清空后需要重新打开光速保进行下载,请确认继续?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    delAllWww();
                }
            }).show();
        } else if (index == 1) {
            DialogHelp.getConfirmDialog(this, "资源文件初始化后需要重新打开光速保进行下载,请确认继续?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    delWww();
                }
            }).show();
        }
    }

    private void init_www() {
        tv_hint.setText("");
        if (rb_sit.isChecked()) {
            base_name = "gsb_mobile_sit_data";
            base_app_name = "gsb_sit_mobile";
            base_url = "http://mitst.sunlife-everbright.com:8010/com.ifp.ipartner/moUpgrade";
            down_url = "http://mitst.sunlife-everbright.com:8010/com.ifp.ipartner/proposalShare/index.html#/down/info/2";
        } else if (rb_uat.isChecked()) {
            base_name = "gsb_mobile_beta_data";
            base_app_name = "gsb_beta_mobile";
            base_url = "http://mitphone.sunlife-everbright.com:8010/com.ifp.ipartner/moUpgrade";
            down_url = "http://mitphone.sunlife-everbright.com:8010/com.ifp.ipartner/proposalShare/index.html#/down/info/2";
        } else if (rb_sc.isChecked()) {
            base_name = "gsb_mobile_sc_data";
            base_app_name = "gsb_sc_mobile";
            base_url = "http://mit.sunlife-everbright.com:8010/com.ifp.ipartner/moUpgrade";
            down_url = "http://mit.sunlife-everbright.com:8010/com.ifp.ipartner/proposalShare/index.html#/down/info/2";
        }
        basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name;
        rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + "/www/index.html";
        configPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + "/config.xml";
        downFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + ".zip";
    }

    private void delWww() {
        init_www();
        boolean next = false;
        try {
            if (new File(configPath).exists()) {
                next = true;
            } else {
                tv_hint.setText("资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!");
                Toast.makeText(MainActivity.this, "资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!", Toast.LENGTH_SHORT).show();
                next = false;
            }
        } catch (Exception e) {
            next = false;
            tv_hint.setText("资源文件初始化失败,可能已初始化/丢失,请登录光速保进行下载!" + e.getMessage());
            Toast.makeText(MainActivity.this, "资源文件初始化失败,可能已初始化/丢失,请登录光速保进行下载!" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (next) {
            try {
                boolean isdel = FileUtils.deleteFile(configPath);
                if (isdel) {
                    tv_hint.setText("资源文件初始化成功");
                    Toast.makeText(MainActivity.this, "资源文件初始化成功", Toast.LENGTH_SHORT).show();
                } else {
                    tv_hint.setText("资源文件初始化失败");
                    Toast.makeText(MainActivity.this, "资源文件初始化失败", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                tv_hint.setText("资源文件初始化失败" + e.getMessage());
                Toast.makeText(MainActivity.this, "资源文件初始化失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void delAllWww() {
        init_www();
        boolean next = false;
        try {
            if (new File(basePath).exists()) {
                next = true;
            } else {
                tv_hint.setText("资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!");
                Toast.makeText(MainActivity.this, "资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!", Toast.LENGTH_SHORT).show();
                next = false;
            }
        } catch (Exception e) {
            next = false;
            tv_hint.setText("资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!" + e.getMessage());
            Toast.makeText(MainActivity.this, "资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (next) {
            try {
                boolean isdel = FileUtils.deleteFilesInDir(basePath);
                if (isdel) {
                    tv_hint.setText("资源文件清空成功");
                    Toast.makeText(MainActivity.this, "资源文件清空成功", Toast.LENGTH_SHORT).show();
                } else {
                    tv_hint.setText("资源文件清空失败");
                    Toast.makeText(MainActivity.this, "资源文件清空失败", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                tv_hint.setText("资源文件清空失败" + e.getMessage());
                Toast.makeText(MainActivity.this, "资源文件清空失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void install() {
        init_www();
        Utils.init(this);
        boolean next = false;
        try {
            if (new File(Environment.getExternalStorageDirectory().getAbsolutePath(), base_app_name + ".apk").exists()) {
                next = true;
            } else {
                tv_hint.setText("安装文件不存在,请登录光速保进行下载!");
                Toast.makeText(MainActivity.this, "安装文件不存在,请登录光速保进行下载!", Toast.LENGTH_SHORT).show();
                next = false;
            }
        } catch (Exception e) {
            next = false;
            tv_hint.setText("安装文件不存在,请登录光速保进行下载!" + e.getMessage());
            Toast.makeText(MainActivity.this, "安装文件不存在,请登录光速保进行下载!" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (next) {
            AppUtils.installApp(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), base_app_name + ".apk"), this.getApplicationInfo().processName + ".provider");
        }
    }

    private void checkVersion() {
        init_www();
        dialog = new ProgressDialog(this);
        dialog.setMessage("检测中...");
        dialog.show();
        OkHttpUtils.get().url(base_url).build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        dialog.dismiss();
                        tv_hint.setText(e.getMessage());
                    }

                    @Override
                    public void onResponse(String json, int i) {
                        dialog.dismiss();
                        tv_hint.setText(json);
                    }
                });
    }

    private void checkHelperVersion() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("请稍等...");
        dialog.show();
        OkHttpUtils.get().url("http://api.fir.im/apps/latest/5ba2f209959d69290b2969b8?api_token=0ac334a22e69c2345a5cec8e1674fff9").build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        dialog.dismiss();
                    }

                    @Override
                    public void onResponse(String json, int i) {
                        dialog.dismiss();
                        final VersionBean version = new Gson().fromJson(json, VersionBean.class);
                        try {
                            PackageInfo packageInfo = MainActivity.this.getApplicationContext()
                                    .getPackageManager()
                                    .getPackageInfo(MainActivity.this.getPackageName(), 0);
                            if (!packageInfo.versionName.equals(version.getVersionShort())) {
                                DialogHelp.getConfirmDialog(MainActivity.this, version.getChangelog().replaceAll("\n", "<br>"), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Uri uri = Uri.parse(version.getInstall_url());
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        startActivity(intent);
                                    }
                                }).show();
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void getWwwVersion() {
        init_www();
        double currentVersion = 0.9d;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse("file:///" + sdPath + base_name + "/config.xml");
            Element rootElement = doc.getDocumentElement();
            Node productVersionNode = rootElement.getElementsByTagName("productVersion").item(0);
            currentVersion = Double.valueOf(productVersionNode.getTextContent().trim());
        } catch (Exception e) {
            currentVersion = 0.9d;
        }
        tv_hint.setText("资源文件版本号 : " + String.valueOf(currentVersion));
    }

    private void unZipFile() {
        init_www();
        DialogHelp.getConfirmDialog(this, "成功解压后原目录会发生覆盖,请确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("解压中...");
                dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (ZipUtil.unZip(downFilePath, sdPath)) {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    ToastUtils.showShort("解压成功");
                                    tv_hint.setText("资源文件解压成功");
                                }
                            });
                        } else {
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                    ToastUtils.showShort("解压失败");
                                    tv_hint.setText("资源文件解压失败");
                                }
                            });
                        }
                    }
                }).start();
            }
        }).show();
    }

    private void create_nomedia() {
        init_www();
        boolean isok = FileUtils.createOrExistsFile(basePath + "/.nomedia");
        tv_hint.setText(isok ? "创建隐藏完成" : "创建隐藏失败");
        ToastUtils.showShort(isok ? "创建隐藏完成" : "创建隐藏失败");
    }

    private void getFiles() {
        init_www();
        buffer = new StringBuffer("");
        count = 0;
        count2 = 0;
        count3 = 0;
        File file = new File(basePath);
        getFileDir(file.listFiles());
        tv_hint.setText("文件夹总数 : " + count2 + "\n"
                + "文件总数 : " + count + "\n"
                + ".DS_Store文件总数 : " + count3 + "\n"
                + buffer.toString());
    }

    StringBuffer buffer;
    int count = 0;
    int count2 = 0;
    int count3 = 0;

    private void getFileDir(File[] subFile) {
        for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
            // 判断是否为文件夹
            if (!subFile[iFileLength].isDirectory()) {
                String filename = subFile[iFileLength].getName();
                String parentfilename = subFile[iFileLength].getParentFile().getAbsolutePath();
                if (!".DS_Store".equals(filename)) {
                    if (iFileLength == 0) {
                        if (parentfilename.contains("/0/")) {
                            buffer.append(parentfilename.split("/0/")[1] + "\n      " + filename + "\n");
                        } else {
                            buffer.append(parentfilename + "\n    " + filename + "\n");
                        }
                    } else {
                        buffer.append("    " + filename + "\n");
                    }
                    count++;
                } else {
                    count3++;
                }
            } else {
                count2++;
                getFileDir(subFile[iFileLength].listFiles());
            }
        }
    }

    private void cleanCache() {
        init_www();
        ToastUtils.showShort("待开放");
    }

    /**
     * 复制文件到SD卡
     *
     * @param context
     * @param fileName 复制的文件名
     * @return
     */
    public static String copyAssetsFile(Context context, String fileName) {
        try {
            InputStream mInputStream = context.getAssets().open(fileName);
            File plug_install_dir = context.getExternalFilesDir("plug_");
            if (!plug_install_dir.exists()) {
                if (!plug_install_dir.mkdirs()) {
                    return null;
                }
            }
            FileOutputStream mFileOutputStream = new FileOutputStream(plug_install_dir + "/" + fileName);
            byte[] mbyte = new byte[1024];
            int i = 0;
            while ((i = mInputStream.read(mbyte)) > 0) {
                mFileOutputStream.write(mbyte, 0, i);
            }
            mInputStream.close();
            mFileOutputStream.close();
            return plug_install_dir.getPath() + "/" + fileName;
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("TAG", fileName + "not exists" + "or write err");
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 加载插件中
     */
    private void loadPluginsZipFile(final String path) {
        init_www();
        DialogHelp.getConfirmDialog(this, "提示修复成功后,请重新登录光速保", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialog = new ProgressDialog(MainActivity.this);
                dialog.setMessage("修复中...");
                dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(300);
                            if (ZipUtil.unZip(path, MainActivity.this.sdPath)) {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        ToastUtils.showShort("修复完成,请重新登录");
                                        tv_hint.setText("修复完成,请重新登录");
                                    }
                                });
                            } else {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialog.dismiss();
                                        ToastUtils.showShort("修复失败,请联系管理员");
                                        tv_hint.setText("修复失败,请联系管理员");
                                    }
                                });
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        }).show();
    }

    /**
     * 支付宝支付
     *
     * @param payCode 收款码后面的字符串；例如：收款二维码里面的字符串为 https://qr.alipay.com/stx00187oxldjvyo3ofaw60 ，则
     *                payCode = stx00187oxldjvyo3ofaw60
     *                注：不区分大小写
     */
    private void donateAlipay(String payCode) {
        boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(this);
        if (hasInstalledAlipayClient) {
            AlipayDonate.startAlipayClient(this, payCode);
        } else {
            ToastUtils.showShort("安装支付宝后可进行打赏哦~");
        }
    }

    private void ping() {
        final String[] arr = {"www.baidu.com", "mitst.sunlife-everbright.com", "mitphone.sunlife-everbright.com", "mit.sunlife-everbright.com"};
        DialogHelp.getSelectDialog(MainActivity.this, "请选择PING地址", arr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, final int i) {
                Message msg = Message.obtain();
                msg.obj = null;
                msg.what = 0x300;
                handler.sendMessage(msg);
                //网络操作应在子线程中操作，避免阻塞UI线程，导致ANR
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PingNetEntity pingNetEntity = new PingNetEntity(arr[i], 5, 5, new StringBuffer());
                        pingNetEntity = PingNet.ping(pingNetEntity);
                        PingNetEntity finalPingNetEntity = pingNetEntity;
                        Message msg = Message.obtain();
                        msg.obj = finalPingNetEntity;
                        msg.what = 0x300;
                        handler.sendMessage(msg);
                    }
                }).start();
            }
        }).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.munes, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_clear_www:
                initDelWwwTask(0);
                break;
            case R.id.menu_init_www:
                initDelWwwTask(1);
                break;
            case R.id.menu_fast_install:
                DialogHelp.getConfirmDialog(MainActivity.this, "如本地已下载所选环境的安装包会调用安装界面,安装后会替换手机已有的对应环境的应用,请确认继续?", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        install();
                    }
                }).show();
                break;
            case R.id.menu_check_version:
                checkVersion();
                break;
            case R.id.menu_get_www_version:
                getWwwVersion();
                break;
            case R.id.menu_un_zip_file:
                unZipFile();
                break;
            case R.id.menu_create_nomedia:
                create_nomedia();
                break;
            case R.id.menu_get_files:
                getFiles();
                break;
            case R.id.menu_reward:
                donateAlipay("FKX03352RMPYBVZMGAFN5F");
                break;
            case R.id.menu_consultation:
                WebViewActivity.startWebPage(this);
                break;
            case R.id.menu_ping:
                ping();
                break;
            case R.id.menu_get_version_address:
                init_www();
                tv_hint.append(Html.fromHtml("<a href=\"" + down_url + "\">" + down_url + "</a><br>点击跳转浏览器下载"));
                tv_hint.setAutoLinkMask(Linkify.ALL);
                tv_hint.setMovementMethod(LinkMovementMethod.getInstance());
                break;
            case R.id.menu_gonggao:
                WebViewActivity.startWebPage(this, "http://n22.online/doc/readme.txt");
                break;
//            case R.id.menu_test_date:
//                if (dialogFrag == null) {
//                    dialogFrag = new PayKeyboardFragment();
//                    dialogFrag.setTitle("请输入提取数据密码");
//                    dialogFrag.setOnKeyboardListener(MainActivity.this);
//                }
//                dialogFrag.show(getFragmentManager(), "payKeyboardDialog");
//                break;
            case R.id.menu_helper:
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
            case R.id.menu_zxing_1:
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
            case R.id.menu_zxing_2:
                intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onComplete(String one, String two, String three, String four, String five, String six) {
        if ("7".equals(one) && "9".equals(two) && "5".equals(three) && "1".equals(four) && "3".equals(five) && "5".equals(six)) {
            Toast.makeText(getBaseContext(), "验证通过", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(getBaseContext(), "验证失败", Toast.LENGTH_LONG).show();
        }
        if (dialogFrag != null) {
            dialogFrag.dismiss();
        }
    }

    @Override
    public void onBack() {
        if (dialogFrag != null) {
            dialogFrag.dismiss();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /**
         * 处理二维码扫描结果
         */
        if (requestCode == REQUEST_CODE) {
            //处理扫描结果（在界面上显示）
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle == null) {
                    return;
                }
                if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_SUCCESS) {
                    String result = bundle.getString(CodeUtils.RESULT_STRING);
                    tv_hint.setText(Html.fromHtml("<a href=\"" + result + "\">" + result + "</a><br>点击跳转浏览器查看/下载"));
                    tv_hint.setAutoLinkMask(Linkify.ALL);
                    tv_hint.setMovementMethod(LinkMovementMethod.getInstance());
                } else if (bundle.getInt(CodeUtils.RESULT_TYPE) == CodeUtils.RESULT_FAILED) {
                    tv_hint.setText("解析二维码失败,请重新识别");
                }
            }
        } else if (requestCode == REQUEST_IMAGE) {
            if (data != null) {
                Uri uri = data.getData();
                try {
                    CodeUtils.analyzeBitmap(ImageUtil.getImageAbsolutePath(this, uri), new CodeUtils.AnalyzeCallback() {
                        @Override
                        public void onAnalyzeSuccess(Bitmap mBitmap, String result) {
                            tv_hint.setText(Html.fromHtml("<a href=\"" + result + "\">" + result + "</a><br>点击跳转浏览器查看/下载"));
                            tv_hint.setAutoLinkMask(Linkify.ALL);
                            tv_hint.setMovementMethod(LinkMovementMethod.getInstance());
                        }

                        @Override
                        public void onAnalyzeFailed() {
                            tv_hint.setText("解析二维码失败,请重新识别");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
