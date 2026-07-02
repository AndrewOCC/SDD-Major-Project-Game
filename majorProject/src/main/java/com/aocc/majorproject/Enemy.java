package com.aocc.majorproject;

import com.aocc.framework.Graphics;
import com.aocc.framework.GameConstants;
import com.aocc.framework.PersonalMethods;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Enemy {
	private final Player player;
	private final GameSession session;
	
	private float posX, posY;
	
	private float accelX, velocityX = 0;
	private float accelY, velocityY = 0;
	
	private float radius = 0;	
	private int topspeed = 2;
	private int health = 1;
	
	private RectF enemyRectF = new RectF(0,0,0,0);
	
	private int type;
		
	public Enemy(float X, float Y, int t, GameSession session){
		this.session = session;
		this.player = session.getPlayer();
		setType(t);
		if (t == 1){
			topspeed = 3;
			radius = 10;
		} else if (t == 2){
			topspeed = 2;
			radius = 20;
		}
		posX = X;
		posY = Y;
	}

	public void update(float deltaSeconds){
		float step = GameConstants.secondsToSteps(deltaSeconds);
		
		// calculating velocities
		if (posX < player.getCenterX()){
			accelX = 0.1f;
		}
		
		if (posY < player.getCenterY()){
			accelY = 0.1f;
		}
		
		if (posX > player.getCenterX()){
			accelX = -0.1f;
		}
		
		if (posY > player.getCenterY()){
			accelY = -0.1f;
		}
		
		// applies acceleration and velocity
		velocityX = velocityX + accelX * step;
		velocityY = velocityY + accelY * step;
		
		//handling collisions with sides
		if ((posX - radius + velocityX * step) < 0){
			velocityX = 0;
			posX = radius;
		}
		
		if ((posY - radius + velocityY * step) < 0){
			velocityY = 0;
			posY = radius;
		}
		
		if ((posX + radius + velocityX * step) > GameConstants.WORLD_WIDTH){
			velocityX = 0;
			posX = GameConstants.WORLD_WIDTH - radius;
		}
		
		if ((posY + radius + velocityY * step ) > GameConstants.WORLD_HEIGHT){
			velocityY = 0;
			posY = GameConstants.WORLD_HEIGHT - radius;
		}
		
		// collision with the player
		if (PersonalMethods.rectFInBounds(player.getMainCharacter(), (int)player.getShieldRadius(), enemyRectF) && health > 0){
			health --;
			
			if (player.getShield() > 0){
				session.addScore(10 * player.getCombo());
				player.setCombo(player.getCombo() + 1);
			} else {
				player.setHealth(player.getHealth() - 1);
				player.setCombo(0);
			}
			
		}
	
		// limits velocity		
		velocityX = PersonalMethods.limitInside(velocityX, -topspeed, topspeed);
		velocityY = PersonalMethods.limitInside(velocityY, -topspeed, topspeed);
		
		// applies velocity
		posX = posX + velocityX * step;
		posY = posY + velocityY * step;
		
		enemyRectF.set(posX - radius, posY - radius, posX + radius,
				posY + radius);
		
	}
	
	public void increaseTopSpeed() {
		if (topspeed < 20) {
			topspeed++;
		}
	}
	
	public void paint(Graphics g, Paint paint){
		g.drawCircle(posX, posY, radius, Color.WHITE);
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public float getAccelX() {
		return accelX;
	}

	public float getVelocityX() {
		return velocityX;
	}

	public float getAccelY() {
		return accelY;
	}

	public float getVelocityY() {
		return velocityY;
	}

	public RectF getEnemyRectF() {
		return enemyRectF;
	}

	public void setAccelX(float accelX) {
		this.accelX = accelX;
	}

	public void setVelocityX(float velocityX) {
		this.velocityX = velocityX;
	}

	public void setAccelY(float accelY) {
		this.accelY = accelY;
	}

	public void setVelocityY(float velocityY) {
		this.velocityY = velocityY;
	}

	public void setEnemyRectF(RectF enemyRectF) {
		this.enemyRectF = enemyRectF;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}
