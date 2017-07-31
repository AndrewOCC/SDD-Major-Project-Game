package com.aocc.majorproject;

import com.aocc.framework.Graphics;
import com.aocc.framework.PersonalMethods;
import com.aocc.framework.implementation.RotationHandler;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;

public class Player {
	
	final float BORDER_WIDTH = 3;
	
	// Gyro Values
	final float SENSITIVITY = 100;		// tilt sensitivity
	final int MAX_SPEED = 30;
	final int MAX_RADIUS = 50;
	final int MIN_RADIUS = 10;
	
	// max/min values for health/shield
	final int MAX_HEALTH = 5;
	final int MAX_SHIELD = 100;
	final int MAX_OVERHEAT = 75;
	
	private boolean firstOverheat = true;
	private int tiltMode = 2;

	private float xBias = 0;
	private float yBias = -0.3f;
	private int health = 5;
	private int combo = 0;
	private int shield = MAX_SHIELD;
	
	private float characterDiameter = health * 15f;
	private float shieldWidth = shield/4;
	
	private float defaultX = 600;
	private float defaultY = 320;
	
	private float centerX = 0;
	private float centerY = 0;
	
	private float velocityX = 0;
	private float velocityY = 0;
	
	private float facingAngle = -90;
	
	private RectF mainCharacter = new RectF(0,0,0,0);
	private int overheat = 0;
	
	
	public void update() {
		// Rotation limited within 90 degree range, then turn into a decimal between 0 and 1 for easier modification.
		// Sensitivity determines the amount of tilting required for maximum speed. Bias determines default position.
		velocityX = (PersonalMethods.limitInside(RotationHandler.getRotationX(),-90,90)/90 + xBias)*SENSITIVITY;
		velocityX = PersonalMethods.limitInside(velocityX, -MAX_SPEED, MAX_SPEED);
		velocityY = (PersonalMethods.limitInside(RotationHandler.getRotationY(),-90,90)/90 + yBias)*SENSITIVITY;
		velocityY = PersonalMethods.limitInside(velocityY, -MAX_SPEED, MAX_SPEED);
		
		// angle character is facing: set now so it always faces the tilt direction
		if (velocityX >0) {
			facingAngle = (float) Math.toDegrees(Math.atan((velocityY/velocityX)));
		} else {
			facingAngle = 180 + (float) Math.toDegrees(Math.atan((velocityY/velocityX)));
		}
		
		//handling collisions with sides
		if ((defaultX + velocityX ) < BORDER_WIDTH){
			velocityX = 0;
			defaultX = BORDER_WIDTH;
		}
		
		if ((defaultY + velocityY) < BORDER_WIDTH){
			velocityY = 0;
			defaultY = BORDER_WIDTH;
		}
		
		if ((defaultX + velocityX) > 1280 - characterDiameter - BORDER_WIDTH){
			velocityX = 0;
			defaultX = 1280 - characterDiameter - BORDER_WIDTH;
		}
		
		if ((defaultY + velocityY ) > 720 - characterDiameter - BORDER_WIDTH){
			velocityY = 0;
			defaultY = 720 - characterDiameter - BORDER_WIDTH;
		}
		
//		//logs
//		if (GameScreen.getUpdateCount()%20 == 0){
//			Log.d("MainCharacter", "RotationX: " + RotationHandler.getRotationX());
//			Log.d("MainCharacter", "RotationY: " + RotationHandler.getRotationY());
//			Log.d("MainCharacter", "RotationZ: " + RotationHandler.getRotationZ());
//		}
		
		if (health <= 0){
			GameScreen.setGameOverFlag(true);
		}
		
		//increases overheat when shields aren't full (i.e. when off the powerup)
		if (shield < MAX_SHIELD){
			overheat = overheat - 1;
		}
		
		shield --;
		
		
		shieldWidth = shield/2;
		
		// sets the final values for the update
		health = (int)PersonalMethods.limitInside((float)health, 0, MAX_HEALTH);
		shield = (int)PersonalMethods.limitInside((float)shield, 0, MAX_SHIELD);
		overheat = (int)PersonalMethods.limitInside((float)overheat, 0, MAX_OVERHEAT);
		characterDiameter = MAX_RADIUS * health/5 + MIN_RADIUS;
		
		if (overheat == MAX_OVERHEAT){
			combo = 0;
			
			// plays sound only once
			if (firstOverheat == true){
				Assets.burn.play(MainMenuScreen.tapVol);
				firstOverheat = false;
			}
		} else {
			// resets for next overheat sound
			firstOverheat = true;
		}
		
		defaultX = defaultX + velocityX;
		defaultY = defaultY + velocityY;
		
		mainCharacter.set((int)defaultX, (int)defaultY, (int)defaultX + characterDiameter,
				(int)defaultY + characterDiameter);
		
		//to get a 'lag' effect, this can be placed before the previous statement for a 1-frame delay
		centerX = mainCharacter.centerX();		
		centerY = mainCharacter.centerY();
		
	}

	public void paint(Graphics g, Paint paint){
		paint.setStyle(Paint.Style.FILL);
		g.drawCircle(centerX, centerY, characterDiameter/2 + BORDER_WIDTH, Color.WHITE);
        g.drawCircle(centerX, centerY, characterDiameter/2 + shieldWidth, Color.argb(150, 0, 0, 255));
        g.drawArc(mainCharacter, facingAngle, 120, true, Color.BLUE);
		g.drawArc(mainCharacter, facingAngle + 120, 120, true, Color.RED);
		g.drawArc(mainCharacter, facingAngle + 240, 120, true, Color.GREEN);
        g.drawCircle(centerX, centerY, (int)characterDiameter/10, Color.WHITE);
        if (overheat >= MAX_OVERHEAT){
        	g.drawCircle(centerX, centerY, (int)characterDiameter/2 + BORDER_WIDTH + shieldWidth + 3, Color.argb(250, 255, 127, 39));
        	g.drawString("Overheating!", 100, 50, Color.MAGENTA, paint);
        } else {
        	g.drawCircle(centerX, centerY, (int)characterDiameter/2 + BORDER_WIDTH + shieldWidth + 3, Color.argb(150*overheat/MAX_OVERHEAT, 255, 127, 39));
        }
        
        // UI
        g.drawString("x" + combo, (int)mainCharacter.left, (int)mainCharacter.top, Color.WHITE, paint);
        
	}
	

	public int getCombo() {
		return combo;
	}

	public void setCombo(int combo) {
		this.combo = combo;
	}

	public int getOverheat() {
		return overheat;
	}

	public void setOverheat(int overheat) {
		this.overheat = overheat;
	}
	
	public float getCharacterDiameter() {
		return characterDiameter;
	}

	public void setCharacterDiameter(float characterDiameter) {
		this.characterDiameter = characterDiameter;
	}

	public float getShieldWidth() {
		return shieldWidth;
	}

	public void setShieldWidth(float shieldWidth) {
		this.shieldWidth = shieldWidth;
	}

	public int getShield() {
		return shield;
	}

	public void setShield(int shield) {
		this.shield = shield;
	}

	public float getShieldRadius() {
		return shieldWidth;
	}

	public void setShieldRadius(float shieldRadius) {
		this.shieldWidth = shieldRadius;
	}

	public float getDefaultX() {
		return defaultX;
	}


	public float getDefaultY() {
		return defaultY;
	}


	public float getVelocityX() {
		return velocityX;
	}


	public float getVelocityY() {
		return velocityY;
	}


	public void setDefaultX(int defaultX) {
		this.defaultX = defaultX;
	}


	public void setDefaultY(int defaultY) {
		this.defaultY = defaultY;
	}


	public void setVelocityX(float velocityX) {
		this.velocityX = velocityX;
	}


	public void setVelocityY(float velocityY) {
		this.velocityY = velocityY;
	}


	public float getCharacterRadius() {
		return characterDiameter;
	}

	public void setCharacterRadius(int characterRadius) {
		this.characterDiameter = characterRadius;
	}


	public void setDefaultX(float defaultX) {
		this.defaultX = defaultX;
	}

	public void setDefaultY(float defaultY) {
		this.defaultY = defaultY;
	}


	public RectF getMainCharacter() {
		return mainCharacter;
	}


	public void setMainCharacter(RectF mainCharacter) {
		this.mainCharacter = mainCharacter;
	}


	public float getFacingAngle() {
		return facingAngle;
	}


	public void setFacingAngle(float facingAngle) {
		this.facingAngle = facingAngle;
	}


	public float getCenterX() {
		return centerX;
	}


	public float getCenterY() {
		return centerY;
	}


	public void setCenterX(float centerX) {
		this.centerX = centerX;
	}


	public void setCenterY(float centerY) {
		this.centerY = centerY;
	}


	public float getBORDER_WIDTH() {
		return BORDER_WIDTH;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public void setCharacterRadius(float characterRadius) {
		this.characterDiameter = characterRadius;
	}	
	
	public float getxBias() {
		return xBias;
	}

	public void setxBias(float xBias) {
		this.xBias = xBias;
	}
	
	public float getyBias() {
		return yBias;
	}

	public void setyBias(float yBias) {
		this.yBias = yBias;
	}

	public int getTiltMode() {
		return tiltMode;
	}

	public void setTiltMode(int tiltMode) {
		this.tiltMode = tiltMode;
	}
	
}