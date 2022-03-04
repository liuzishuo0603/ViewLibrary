package com.lzs.rockerview;

/**
 * title：OnDirectionListener
 * author: zsliu
 * created by Administrator on 2022/2/15 16:37
 * description: 摇动杆方向
 */
public interface OnDirectionListener {
    void onLeft();//向左

    void onRight();//向右

    void onUp();//向上

    void onDown();//向下

    void onaUpLeft();//左上

    void onDownLeft();//左下

    void onUpRight();//右上

    void onDownRight();//右下
}
