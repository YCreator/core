package com.frame.core.widget;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.frame.core.R;

/**
 * 异常提示页面展示控制
 * Created by yzd on 2016/8/12.
 */
public class ExtremelyPage {

    private ImageView ivTips;
    private TextView tvTips;
    private Button btnFunction;

    private View mView;
    private Context mContext;
    private FrameLayout frameLayout;

    private boolean shown;

    public ExtremelyPage(Context context) {
        this.mContext = context;
        mView = LayoutInflater.from(context).inflate(R.layout.extremely_page_layout, null);
        initView();
    }

    private void initView() {
        ivTips = (ImageView) mView.findViewById(R.id.iv_tips);
        tvTips = (TextView) mView.findViewById(R.id.tv_tips);
        btnFunction = (Button) mView.findViewById(R.id.btn_function);

        ivTips.setVisibility(View.INVISIBLE);
        tvTips.setVisibility(View.INVISIBLE);
        btnFunction.setVisibility(View.INVISIBLE);
    }

    public void bindParentViewById(@IdRes int layoutId) {
        View view = ((Activity)mContext).getWindow().getDecorView().findViewById(layoutId);
        ViewParent viewParent = view.getParent();
        if (viewParent instanceof FrameLayout) {
            frameLayout = (FrameLayout) viewParent;
        }
    }

    public void beanFragmentView(View view) {
        if (view instanceof  FrameLayout) {
            frameLayout = (FrameLayout) view;
        }
    }

    public void setImageViewTips(@DrawableRes int drawId) {
        ivTips.setImageResource(drawId);
        ivTips.setVisibility(View.VISIBLE);
    }

    public void setTextViewTips(String message) {
        tvTips.setText(message);
        tvTips.setVisibility(View.VISIBLE);
    }

    public void setButtonFunction(String name, View.OnClickListener onClickListener) {
        btnFunction.setText(name);
        btnFunction.setOnClickListener(onClickListener);
        btnFunction.setVisibility(View.VISIBLE);
    }

    public void show() {
        if (frameLayout != null) {
            frameLayout.addView(mView);
            shown = true;
        }
    }

    public void hide() {
        if (frameLayout != null) {
            frameLayout.removeView(mView);
            shown = false;
        }
    }

    public boolean isShown() {
        return shown;
    }
}
