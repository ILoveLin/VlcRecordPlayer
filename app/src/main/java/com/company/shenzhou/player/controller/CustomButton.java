package com.company.shenzhou.player.controller;

import android.graphics.drawable.Drawable;

/**
 * author : LoveLin
 * github : https://github.com/ILoveLin/VlcRecordPlayer.git
 * time   : 2025/12/3
 * desc   : 自定义按钮数据类，用于在控制器右侧栏添加自定义功能按钮
 */
public class CustomButton {

    private int id;
    private int iconResId;
    private Drawable iconDrawable;
    private String tag;
    private int order = 100; // 排序值，数字越小越靠上，默认100
    private OnCustomButtonClickListener clickListener;

    public CustomButton(int id, int iconResId) {
        this.id = id;
        this.iconResId = iconResId;
    }

    public CustomButton(int id, Drawable iconDrawable) {
        this.id = id;
        this.iconDrawable = iconDrawable;
    }

    public int getOrder() {
        return order;
    }

    /**
     * 设置排序值，数字越小越靠上
     * 内置按钮默认顺序：录像=10，截图=20
     * 自定义按钮默认=100
     */
    public CustomButton setOrder(int order) {
        this.order = order;
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public Drawable getIconDrawable() {
        return iconDrawable;
    }

    public void setIconDrawable(Drawable iconDrawable) {
        this.iconDrawable = iconDrawable;
    }

    public String getTag() {
        return tag;
    }

    public CustomButton setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public OnCustomButtonClickListener getClickListener() {
        return clickListener;
    }

    public CustomButton setClickListener(OnCustomButtonClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    /**
     * 自定义按钮点击监听器
     */
    public interface OnCustomButtonClickListener {
        void onClick(CustomButton button);
    }
}
