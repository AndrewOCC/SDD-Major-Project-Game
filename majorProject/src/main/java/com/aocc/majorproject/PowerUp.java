package com.aocc.majorproject;

import java.util.Random;

import com.aocc.framework.Graphics;
import com.aocc.framework.PersonalMethods;
import com.aocc.framework.Sound;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class PowerUp {
	private int fullTime = 200;
	final int MIN_TIME = 20;
	private int type, radius, posX, posY, timeLeft;
	private boolean usable;
	private boolean firstContact = true;
	
	RectF powerUpRectF;
	Random r = new Random();
	Player player = GameScreen.getPlayer();
	
	public PowerUp(int t){
		type = t;
		powerUpRectF = new RectF();
		
		usable = true;
		radius = 50;
		
		posX = r.nextInt(1280);
		posY = r.nextInt(720);
		powerUpRectF.set(posX - radius, posY - radius, posX + radius, posY + radius);
		
		timeLeft = fullTime;
		
	}
	
	public void update(){
		// when player contacts the powerup zone
		if (PersonalMethods.rectFInBounds(player.getMainCharacter(), 0, powerUpRectF) && usable == true){
			
			//plays sound on first contact
			if (firstContact == true){
				Assets.powerup.play(MainMenuScreen.tapVol);
				firstContact = false;
			}
			
			// sets shields to max and reduces time left while player is on pad
			player.setShield(100);
			timeLeft = timeLeft - 1;
			player.setOverheat(player.getOverheat() + 2);
			
			if (timeLeft <= 0){
				usable = false;
				//lowers time plate is active each time it respawns
				if (fullTime > MIN_TIME){
					fullTime = fullTime - 2;
				}
			}
			
		} else {
			//resets first contact upon leaving plate
			firstContact = true;
		}
		
		//Log.d("MajorProjectGame", "Usable: " + usable + "timeLeft: " + timeLeft + " Angle: " + (360*(timeLeft/MAX_TIME)) );
		
		if(usable == false){
			//counts upwards as a delay for the generation fo a new pad
			timeLeft = timeLeft + 2;
			if (timeLeft > fullTime){
				posX = r.nextInt(1180) + 50;
				posY = r.nextInt(620) + 50;
				powerUpRectF.set(posX - radius, posY - radius, posX + radius, posY + radius);
				timeLeft = fullTime;
				usable = true;
			}
		}
		
	}

	public void paint(Graphics g, Paint paint) {
		if (usable == true){
			g.drawCircle(posX, posY, radius, Color.RED);
			g.drawCircle(posX, posY, radius*timeLeft/fullTime, Color.BLUE);
		}
	}
	
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public RectF getPowerUpRectF() {
		return powerUpRectF;
	}

	public void setPowerUpRectF(RectF powerUpRectF) {
		this.powerUpRectF = powerUpRectF;
	}
	
	public boolean isUsable() {
		return usable;
	}

	public void setUsable(boolean usable) {
		this.usable = usable;
	}
	
}
