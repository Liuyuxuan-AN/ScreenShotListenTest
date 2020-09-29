package com.example.screenshotlistentest;

public class Item {
    private String s1,s2;
    private int imageId;

    public Item(String s1,String s2,int imageId){
        this.s1 = s1;
        this.s2 = s2;
        this.imageId = imageId;
    }

    public String getS1() {
        return s1;
    }

    public String getS2() {
        return s2;
    }

    public int getImageId() {
        return imageId;
    }
}
