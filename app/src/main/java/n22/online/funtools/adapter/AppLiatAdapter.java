package n22.online.funtools.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.text.DecimalFormat;
import java.util.List;

import n22.online.funtools.R;
import n22.online.funtools.bean.AppBean;

/**
 * Created by zhanxiaolin-n22 on 2018/10/30.
 */

public class AppLiatAdapter extends BaseQuickAdapter<AppBean, BaseViewHolder> {
    public AppLiatAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, AppBean item) {
        helper.setImageDrawable(R.id.icon_app, item.getAppIcon());
        helper.setText(R.id.tv_app_name, item.getAppName());
        helper.setText(R.id.tv_app_package, formetFileSize(item.getAppSize()));
        helper.setText(R.id.tv_app_sys, item.isSystem() ? "系统" : "第三方");

    }

    /**
     * 转换文件大小
     *
     * @param fileS
     * @return
     */
    private static String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.00");
        String fileSizeString = "";
        String wrongSize = "0B";
        if (fileS == 0) {
            return wrongSize;
        }
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "GB";
        }
        return fileSizeString;
    }
}