package n22.online.funtools.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.didikee.donate.AlipayDonate;
import android.net.Uri;
import android.os.Environment;
import android.os.Message;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.Toast;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.Utils;
import com.google.gson.Gson;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.File;
import java.text.DecimalFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import cn.addapp.pickers.picker.NumberPicker;
import n22.online.funtools.MainActivity;
import n22.online.funtools.bean.EventMessage;
import n22.online.funtools.bean.PingNetEntity;
import n22.online.funtools.bean.VersionBean;
import okhttp3.Call;

/**
 * Created by zhanxiaolin-n22 on 2019/3/8.
 */

public class FunTools {

    /* 检测更新地址 */
    public static String base_url = "http://mitst.sunlife-everbright.com:8010/com.ifp.ipartner/moUpgrade";
    public static String down_url = "http://mitst.sunlife-everbright.com:8010/com.ifp.ipartner/proposalShare/index.html#/down/info/2";
    /* 下载文件名 资源文件名 解压文件名 */
    public static String base_name = "gsb_mobile_sit_data";
    /* 下载文件名 APK文件名 解压文件名 */
    public static String base_app_name = "gsb_sit_mobile";
    /* 加载本地资源位置,不做修改 */
    public static String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + "/www/index.html";
    public static String basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name;
    /* 加载版本配置文件*/
    public static String configPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + "/config.xml";
    /* 解压本地资源位置,不做修改 */
    public static String downFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + ".zip";
    /* SD卡根路径,不做修改 */
    public static String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";

    public static void initDelWwwTask(final Activity activity, int index, final int v1, final int v2) {
        if (index == 0) {
            DialogHelp.getConfirmDialog(activity, "资源文件清空后需要重新打开光速保进行下载,请确认继续?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    delAllWww(activity,v1,v2);
                }
            }).show();
        } else if (index == 1) {
            DialogHelp.getConfirmDialog(activity, "资源文件初始化后需要重新打开光速保进行下载,请确认继续?", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    delWww(activity,v1,v2);
                }
            }).show();
        }
    }

    public static void install(final Activity activity, final int v1, final int v2) {
        DialogHelp.getConfirmDialog(activity, "如本地已下载所选环境的安装包会调用安装界面,安装后会替换手机已有的对应环境的应用,请确认继续?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                init_www(v1,v2);
                boolean next = false;
                try {
                    if (new File(Environment.getExternalStorageDirectory().getAbsolutePath(), base_app_name + ".apk").exists()) {
                        next = true;
                    } else {
                        EventBusUtils.post(new EventMessage(100,"安装文件不存在,请登录光速保进行下载!"));
                        Toast.makeText(activity, "安装文件不存在,请登录光速保进行下载!", Toast.LENGTH_SHORT).show();
                        next = false;
                    }
                } catch (Exception e) {
                    next = false;
                    EventBusUtils.post(new EventMessage(100,"安装文件不存在,请登录光速保进行下载!"));
                    Toast.makeText(activity, "安装文件不存在,请登录光速保进行下载!" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                if (next) {
                    AppUtils.installApp(new File(Environment.getExternalStorageDirectory().getAbsolutePath(), base_app_name + ".apk"), activity.getApplicationInfo().processName + ".provider");
                }
            }
        }).show();
    }

    public static void checkVersion(Activity activity,int v1,int v2) {
        init_www(v1,v2);
        final ProgressDialog dialog = new ProgressDialog(activity);
        dialog.setMessage("检测中...");
        dialog.show();
        OkHttpUtils.get().url(base_url).build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int i) {
                        dialog.dismiss();
                        EventBusUtils.post(new EventMessage(100,e.getMessage()));
                    }

                    @Override
                    public void onResponse(String json, int i) {
                        dialog.dismiss();
                        EventBusUtils.post(new EventMessage(100,json));
                    }
                });
    }

    public static void getWwwVersion(Activity activity,int v1,int v2) {
        init_www(v1,v2);
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
        EventBusUtils.post(new EventMessage(100,"资源文件版本号 : " + String.valueOf(currentVersion)));
    }

    public static void unZipFile(final Activity activity, int v1, int v2) {
        init_www(v1,v2);
        DialogHelp.getConfirmDialog(activity, "成功解压后原目录会发生覆盖,请确认", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                final ProgressDialog dialog = new ProgressDialog(activity);
                dialog.setMessage("解压中...");
                dialog.show();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (ZipUtil.unZip(downFilePath, sdPath)) {
                            dialog.dismiss();
                            EventBusUtils.post(new EventMessage(100,"资源文件解压成功"));
                        } else {
                            dialog.dismiss();
                            EventBusUtils.post(new EventMessage(100,"资源文件解压失败"));
                        }
                    }
                }).start();
            }
        }).show();
    }

    public static void create_nomedia(Activity activity, int v1, int v2) {
        init_www(v1,v2);
        boolean isok = FileUtils.createOrExistsFile(basePath + "/.nomedia");
        EventBusUtils.post(new EventMessage(100,isok ? "创建隐藏完成" : "创建隐藏失败"));
        ToastUtils.showShort(isok ? "创建隐藏完成" : "创建隐藏失败");
    }

    public static void getDownUrl(Activity activity, int v1, int v2){
        init_www(v1,v2);
        EventBusUtils.post(new EventMessage(200,"<a href=\"" + down_url + "\">" + down_url + "</a><br>点击跳转浏览器下载"));
    }

    public static void modifyVersion(Activity activity, int v1, int v2) {
        init_www(v1,v2);
        boolean isHave = FileUtils.isFileExists(configPath);
        if (isHave) {
            String versionStr = FileUtils.readFile2String(configPath, "UTF-8");
            String str1 = "<productVersion>";
            String str2 = "</productVersion>";
            double versionDouble = Double.valueOf(versionStr.split(str1)[1].split(str2)[0]);
            onNumberPicker(activity,versionDouble);
        } else {
            ToastUtils.showShort("资源文件版本文件不存在");
        }
    }

    public static void ping(Activity activity) {
        final String[] arr = {"www.baidu.com", "mitst.sunlife-everbright.com", "mitphone.sunlife-everbright.com", "mit.sunlife-everbright.com"};
        DialogHelp.getSelectDialog(activity, "请选择PING地址", arr, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, final int i) {
                //网络操作应在子线程中操作，避免阻塞UI线程，导致ANR
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        PingNetEntity pingNetEntity = new PingNetEntity(arr[i], 5, 5, new StringBuffer());
                        pingNetEntity = PingNet.ping(pingNetEntity);
                        PingNetEntity finalPingNetEntity = pingNetEntity;
                        EventBusUtils.post(new EventMessage(100,"\n" + pingNetEntity.getIp() + " time=" + pingNetEntity.getPingTime() + " " + pingNetEntity.isResult()));
                    }
                }).start();
            }
        }).show();
    }

    private static void onNumberPicker(final Activity activity, final double number) {
        NumberPicker picker = new NumberPicker(activity);
        picker.setCanLoop(false);
        picker.setLineVisible(false);
        picker.setWheelModeEnable(true);
        picker.setOffset(1);//偏移量
        picker.setRange(1.0, 99.9, 0.1);//数字范围
        picker.setSelectedItem(number);
        picker.setLabel(" version");
        picker.setOnNumberPickListener(new NumberPicker.OnNumberPickListener() {
            @Override
            public void onNumberPicked(int index, Number item) {
                DecimalFormat df = new DecimalFormat("#.0");
                final String version = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n" +
                        "<config>\n" +
                        "\t <!-- 产品及资源文件版本 -->\n" +
                        "\t<productVersion>" + df.format(item.doubleValue()) + "</productVersion>\n" +
                        "</config>\n";
                DialogHelp.getConfirmDialog(activity, "当前资源文件版本: " + number + ", 修改后版本号: " + df.format(item.doubleValue()) + ", 请确认修改!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        boolean writeFileFromString = FileUtils.writeFileFromString(configPath, version, false);
                        if (writeFileFromString) {
                            String versionStr = FileUtils.readFile2String(configPath, "UTF-8");
                            String str1 = "<productVersion>";
                            String str2 = "</productVersion>";
                            double versionDouble = Double.valueOf(versionStr.split(str1)[1].split(str2)[0]);
                            ToastUtils.showShort("资源文件最新版本 : " + versionDouble);
                        }
                    }
                }).show();
            }
        });
        picker.show();
    }

    public static void checkHelperVersion(final Activity activity) {
        final ProgressDialog dialog = new ProgressDialog(activity);
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
                            PackageInfo packageInfo = activity.getApplicationContext()
                                    .getPackageManager()
                                    .getPackageInfo(activity.getPackageName(), 0);
                            if (!packageInfo.versionName.equals(version.getVersionShort())) {
                                DialogHelp.getConfirmDialog(activity, version.getChangelog().replaceAll("\n", "<br>"), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Uri uri = Uri.parse(version.getInstall_url());
                                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                        activity.startActivity(intent);
                                    }
                                }).show();
                            }
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 支付宝支付
     *
     * @param payCode 收款码后面的字符串；例如：收款二维码里面的字符串为 https://qr.alipay.com/stx00187oxldjvyo3ofaw60 ，则
     *                payCode = stx00187oxldjvyo3ofaw60
     *                注：不区分大小写
     */
    public static void donateAlipay(Activity activity,String payCode) {
        boolean hasInstalledAlipayClient = AlipayDonate.hasInstalledAlipayClient(activity);
        if (hasInstalledAlipayClient) {
            AlipayDonate.startAlipayClient(activity, payCode);
        } else {
            ToastUtils.showShort("安装支付宝后可进行打赏哦~");
        }
    }

    static StringBuffer buffer;
    static int count = 0;
    static int count2 = 0;
    static int count3 = 0;

    public static void getFiles(Activity activity, int v1, int v2) {
        init_www(v1,v2);
        buffer = new StringBuffer("");
        count = 0;
        count2 = 0;
        count3 = 0;
        File file = new File(basePath);
        getFileDir(file.listFiles());
        EventBusUtils.post(new EventMessage(100,"文件夹总数 : " + count2 + "\n"
                + "文件总数 : " + count + "\n"
                + ".DS_Store文件总数 : " + count3 + "\n"
                + buffer.toString()));
    }

    private static void getFileDir(File[] subFile) {
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

    private static void delWww(Activity activity,int v1,int v2) {
        init_www(v1,v2);
        boolean next = false;
        try {
            if (new File(configPath).exists()) {
                next = true;
            } else {
                EventBusUtils.post(new EventMessage(100,"资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!"));
                Toast.makeText(activity, "资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!", Toast.LENGTH_SHORT).show();
                next = false;
            }
        } catch (Exception e) {
            next = false;
            EventBusUtils.post(new EventMessage(100,"资源文件初始化失败,可能已初始化/丢失,请登录光速保进行下载!"));
            Toast.makeText(activity, "资源文件初始化失败,可能已初始化/丢失,请登录光速保进行下载!" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (next) {
            try {
                boolean isdel = FileUtils.deleteFile(configPath);
                if (isdel) {
                    EventBusUtils.post(new EventMessage(100,"资源文件初始化成功"));
                    Toast.makeText(activity, "资源文件初始化成功", Toast.LENGTH_SHORT).show();
                } else {
                    EventBusUtils.post(new EventMessage(100,"资源文件初始化失败"));
                    Toast.makeText(activity, "资源文件初始化失败", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                EventBusUtils.post(new EventMessage(100,"资源文件初始化失败"));
                Toast.makeText(activity, "资源文件初始化失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void delAllWww(Activity activity,int v1,int v2) {
        init_www(v1,v2);
        boolean next = false;
        try {
            if (new File(basePath).exists()) {
                next = true;
            } else {
                EventBusUtils.post(new EventMessage(100,"资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!"));
                Toast.makeText(activity, "资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!", Toast.LENGTH_SHORT).show();
                next = false;
            }
        } catch (Exception e) {
            next = false;
            EventBusUtils.post(new EventMessage(100,"资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!"));
            Toast.makeText(activity, "资源文件清空失败,可能已清空/丢失,请登录光速保进行下载!" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        if (next) {
            try {
                boolean isdel = FileUtils.deleteFilesInDir(basePath);
                if (isdel) {
                    EventBusUtils.post(new EventMessage(100,"资源文件清空成功"));
                    Toast.makeText(activity, "资源文件清空成功", Toast.LENGTH_SHORT).show();
                } else {
                    EventBusUtils.post(new EventMessage(100,"资源文件初始化失败"));
                    Toast.makeText(activity, "资源文件清空失败", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                EventBusUtils.post(new EventMessage(100,"资源文件初始化失败"));
                Toast.makeText(activity, "资源文件清空失败" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private static void init_www(int v1,int v2) {
        if (v1 == 1) {
            base_name = "gsb_mobile_sit_data";
            base_app_name = "gsb_sit_mobile";
            base_url = "http://mitst.sunlife-everbright.com:8010/com.ifp.ipartner/moUpgrade";
            down_url = "http://mitst.sunlife-everbright.com:8010/com.ifp.ipartner/proposalShare/index.html#/down/info/2";
        } else if (v1 == 2) {
            base_name = "gsb_mobile_beta_data";
            base_app_name = "gsb_beta_mobile";
            base_url = "https://mitphone.sunlife-everbright.com:8010/com.ifp.ipartner/moUpgrade";
            down_url = "https://mitphone.sunlife-everbright.com:8010/com.ifp.ipartner/proposalShare/index.html#/down/info/2";
        } else if (v1 == 3) {
            base_name = "gsb_mobile_ysc_data";
            base_app_name = "gsb_ysc_mobile";
            base_url = "http://180.213.5.47:8010/com.ifp.ipartner/moUpgrade";
            down_url = "http://180.213.5.47:8010/com.ifp.ipartner/proposalShare/index.html#/down/info/2";
        } else if (v1 == 4) {
            base_name = "gsb_mobile_sc_data";
            base_app_name = "gsb_sc_mobile";
            base_url = "http://mit.sunlife-everbright.com:8010/com.ifp.ipartner/moUpgrade";
            down_url = "http://mit.sunlife-everbright.com:8010/com.ifp.ipartner/proposalShare/index.html#/down/info/2";
        }
        if(v2 == 1){
            basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name;
            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + "/www/index.html";
            configPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + "/config.xml";
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/";
        }else if(v2 == 2){
            basePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.gsb_src/" + base_name;
            rootPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.gsb_src/" + base_name + "/www/index.html";
            configPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.gsb_src/" + base_name + "/config.xml";
            sdPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.gsb_src/";
        }
        downFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + base_name + ".zip";
    }
}
