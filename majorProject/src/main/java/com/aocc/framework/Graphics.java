package com.aocc.framework;

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

import android.graphics.Paint;
import android.graphics.RectF;

public interface Graphics {
	public static enum ImageFormat {
		ARGB8888, ARGB4444, RGB565
	}

	public Image newImage(String fileName, ImageFormat format);

	public void clearScreen(int color);

	public void drawLine(int x, int y, int x2, int y2, int color);

	public void drawRect(int x, int y, int width, int height, int color);

	public void drawImage(Image image, int x, int y, int srcX, int srcY,
			int srcWidth, int srcHeight);

	public void drawImage(Image Image, int x, int y);

	void drawString(String text, int x, int y, int color, Paint paint);

	public int getWidth();

	public int getHeight();

	public void drawARGB(int i, int j, int k, int l);


// The following two methods were created as part of the Major Project:
	void drawCircle(float x, float y, float radius, int color);

	void drawArc(RectF oval, float startAngle, float sweepAngle,
			boolean useCenter, int color);

    //Intelligently draws a button of appropriate width given an input string/location
    void drawButton(int x, int y, int height, int color, String text);

	

}
