package com.game;

import javax.swing.*;
import java.awt.*;

public class frame extends  JFrame{//窗口框架
    private client ct;
    public frame()throws Exception{
        this.setLayout(new BorderLayout());//格式
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//退出
        this.setAlwaysOnTop(true);//窗口浮在最前面
        ct = new client();
        this.setTitle(ct.name);//给客户端设置名字
        this.add(ct);
        ct.setFocusable(true);//获取焦点
        this.setSize(ct.getWidth(),ct.getHeight());
        this.setResizable(false);
        this.setVisible(true);

    }
}
