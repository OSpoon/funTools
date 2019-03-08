package n22.online.funtools.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.text.DecimalFormat;
import java.util.List;

import n22.online.funtools.R;
import n22.online.funtools.bean.AppBean;
import n22.online.funtools.bean.MenuBean;

/**
 * Created by zhanxiaolin-n22 on 2018/10/30.
 */

public class MenuLiatAdapter extends BaseQuickAdapter<MenuBean, BaseViewHolder> {
    public MenuLiatAdapter(int layoutResId, List data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, MenuBean item) {
        helper.setText(R.id.tv_menu_name, item.getMenuId()+"、"+item.getMenuName());
    }
}