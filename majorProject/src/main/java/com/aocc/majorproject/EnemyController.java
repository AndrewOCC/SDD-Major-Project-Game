package com.aocc.majorproject;

import java.util.LinkedList;
import java.util.Random;

import android.graphics.Paint;
import com.aocc.framework.Graphics;

// This controller allows oen call in the GameScreen class to update or paint every 
// enemy through a LinkedList; it also allows the easy creation and deletion of enemies

public class EnemyController {
	private static final int MIN_SPAWN_TIME = 5;
	
	LinkedList<Enemy> e = new LinkedList<Enemy>();
	Enemy TempEnemy;
	Random r = new Random();
	
	static int nextEnemySpawn;

	
	public void generateNextEnemy(int speed){
		nextEnemySpawn = r.nextInt(40 - 2*speed) + MIN_SPAWN_TIME;
	}
	
	// for adding and removing enemies from the LinkedList
	public void addEnemy(int x, int y, int t){
		TempEnemy = new Enemy(x,y,t);
		e.add(TempEnemy);
		this.generateNextEnemy(GameScreen.getSpeed());
	}
	
	public void removeEnemy(int i){
		e.remove(i);
		Assets.zap.play(MainMenuScreen.tapVol);
	}
	
	public void removeAllEnemies(){
		e.clear();
	}
	
	public void update(){
		for (int i = 0; i < e.size(); i++){
			TempEnemy = e.get(i);
			TempEnemy.update();
			if (TempEnemy.getHealth() <= 0) {
				this.removeEnemy(i);
			}
		}
	}
	
	public void paint(Graphics g, Paint paint){
		for (int i = 0; i < e.size(); i++){
			TempEnemy = e.get(i);
			TempEnemy.paint(g, paint);
		}
	}
	
	public LinkedList<Enemy> getE() {
		return e;
	}

	public void setE(LinkedList<Enemy> e) {
		this.e = e;
	}
	
}
