package com.example.screenshotlistentest;

/**
 * author : 刘雨轩
 * e-mail : 1262610086@qq.com
 * date   : 2020/10/14
 * desc   :用于提示笔记信息发生改变的工具类
 */
public class NoteChangeMessage {

    public final String message;

    public static NoteChangeMessage getInstance(String message){
        return new NoteChangeMessage(message);
    }

    private NoteChangeMessage(String message){
        this.message = message;
    }
}
