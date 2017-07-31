package com.aocc.framework;

import com.aocc.framework.Input.TouchEvent;

import android.graphics.Point;
import android.graphics.RectF;

// This is the one class created entirely by myself within the framework. It handles
// various different functions which I call repeatedly throughout the project, and
// is designed to be reusable.

public class PersonalMethods {

	public static float limitInside(float value, int lowerLimit, int upperLimit) {
		// Method to limit a value between two other values. Math.max returns the
		// largest of two values, and Math.min the smallest
		value = Math.max(lowerLimit, Math.min(value, upperLimit));
		return value;
	}
	
	public static Point limitOutside(Point value, int centerX, int centerY, int distance) {
		// Method to determine if a value (point2) is a certain distance from
		// another value (point1)
		
		if (value.x < centerX + distance && value.x > centerX - distance){
			if (value.y <= centerY){
				value.y = Math.min(value.y, centerY - distance);
			} else {
				value.y = Math.max(value.y, centerY + distance);
			}
		}
		if (value.y < centerY + distance && value.y > centerY - distance){
			if (value.x <= centerX){
				value.x = Math.min(value.x, centerX - distance);
			} else {
				value.x = Math.max(value.x, centerX + distance);
			}
		}
			
		return value;
	}
	

	public static boolean touchInBounds(TouchEvent event, int x, int y, 
			int width, int height) {	//handles rectangular collision
		
		if (event.x > x && event.x < x + width - 1 
				&& event.y > y && event.y < y + height - 1)
			return true;
        else
            return false;
    }
	
	//rectangle collision code
	public static boolean rectFInBounds(RectF rect1, int buffer, RectF rect2){
		if (rect1.left - buffer < rect2.right && rect1.right + buffer > rect2.left && 
			rect1.top - buffer < rect2.bottom && rect1.bottom + buffer > rect2.top){
			return true;
		} else
			return false;
	}
	
}
