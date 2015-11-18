package com.socks.jiandan.model;

import android.support.v4.app.Fragment;

/**
 * 侧边栏选项
 */
public class MenuItem {

    /**
     * 菜单选项对应的FragmentType类型
     */
    public enum FragmentType {
        FreshNews, BoringPicture, Sister, Joke, Video
    }

    /**
     * 菜单名称
     */
    private String title;
    /**
     * 菜单图标
     */
    private int resourceId;
    /**
     * 菜单对应的页面类型
     */
    private FragmentType type;
    /**
     * 菜单绑定的Fragment类
     */
    private Class<? extends Fragment> fragment;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getResourceId() {
        return resourceId;
    }

    public Class<? extends Fragment> getFragment() {
        return fragment;
    }

    public void setFragment(Class<? extends Fragment> fragment) {
        this.fragment = fragment;
    }


    public FragmentType getType() {
        return type;
    }

    public void setType(FragmentType type) {
        this.type = type;
    }

    public MenuItem() {
    }

    public MenuItem(String title, int resourceId, Class<? extends Fragment> fragment) {
        this.resourceId = resourceId;
        this.title = title;
        this.fragment = fragment;
    }

    public MenuItem(String title, int resourceId, FragmentType type, Class<? extends Fragment> fragment) {
        this.title = title;
        this.resourceId = resourceId;
        this.type = type;
        this.fragment = fragment;
    }
}