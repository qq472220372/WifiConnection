
package com.wifi.entity;

import android.media.Image;

public class ImgEntity {
    private static final String TAG = ImgEntity.class.getSimpleName();

    private String name;

    private String date;

    private Image img;

    private int layoutID;

    public Image getImg() {
		return img;
	}

	public void setImg(Image img) {
		this.img = img;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getLayoutID() {
        return layoutID;
    }

    public void setLayoutID(int layoutID) {
        this.layoutID = layoutID;
    }

    public ImgEntity() {
    }

    public ImgEntity(String name, String date, Image img, int layoutID) {
        super();
        this.name = name;
        this.date = date;
        this.img = img;
        this.layoutID = layoutID;
    }

}
