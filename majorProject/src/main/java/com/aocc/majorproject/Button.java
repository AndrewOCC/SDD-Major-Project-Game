package com.aocc.majorproject;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import com.aocc.framework.Graphics;
import com.aocc.framework.Input;
import com.aocc.majorproject.ui.UiBounds;
import com.aocc.majorproject.ui.UiText;

public class Button {

	private int posX, posY, width, height;

	private RectF enemyRectF = new RectF(0,0,0,0);


    // Constants


    // Other Variables
	private int type;

    private enum ButtonState {active, pressed, inactive};
    ButtonState state;

    String text;



	public Button(int posX, int posY, int type, int state, String text){
		this.type = type;

        if(state == 0){
            this.state = ButtonState.active;
        } else if (state == 1) {
            this.state = ButtonState.pressed;
        } else {
            this.state = ButtonState.inactive;
        }

		if (type == 1){
            // Main Menu buttons (Play/Pause/How to Play)
            width = 440;
            height = 200;

		} else if (type == 2) {
            // Volume Adjust Buttons



        } else if (type == 3) {
            // Tilt Adjust Buttons

            width = height = 64; //button radius = 64


        } else if (type == 4) {
            // 'Menu' button
            width = 200;
            height = 100;

        }

        this.text = text;
		this.posX = posX;
		this.posY = posY;
	}

	public void update() {

        if (type == 1){

        } else if (type == 2){

        } else if (type == 3){

        } else if (type == 4){

        }
    }

	public void paint(Graphics g, Paint paint, Player player){

        if (type == 1){

        } else if (type == 2){

        } else if (type == 3){

            if ("Flat".equals(text)){
                if(player.getTiltMode() == 1){
                    g.drawImage(Assets.tilt_control_flat_2, this.posX, this.posY);
                } else {
                    g.drawImage(Assets.tilt_control_flat, this.posX, this.posY);
                }

                UiText.drawLeftOfCenter(g, paint, text, this.posX + width + 12,
                        this.posY + width / 2, Color.BLACK);

            } else if ("Tilted".equals(text)){
                if( player.getTiltMode() == 2){
                    g.drawImage(Assets.tilt_control_tilted_2, this.posX, this.posY);
                } else {
                    g.drawImage(Assets.tilt_control_tilted, this.posX, this.posY);
                }

                UiText.drawLeftOfCenter(g, paint, text, this.posX + width + 12,
                        this.posY + width / 2, Color.BLACK);

            } else if ("Custom".equals(text)){
                // custom shouldn't depress, as it is not a simple toggle. Gains border when used instead
                if( player.getTiltMode() == 3){
                    g.drawCircle(this.posX + width/2, this.posY + width/2, + width/2 + 5, Color.RED);
                }

                g.drawImage(Assets.tilt_control_custom, this.posX, this.posY);

                UiText.drawLeftOfCenter(g, paint, text, this.posX + width + 12,
                        this.posY + width / 2, Color.BLACK);
            }

        } else if (type == 4){
            UiBounds bounds = new UiBounds(posX, posY, width, height);
            g.drawRect(bounds.x, bounds.y, bounds.width, bounds.height, Color.DKGRAY);
            UiText.drawInBounds(g, paint, text, bounds, UiText.HAlign.CENTER, Color.WHITE);
        }
	}

    public boolean touchInBounds(Input.TouchEvent event) {	//handles rectangular collision

        if (event.x > posX && event.x < posX + width - 1
                && event.y > posY && event.y < posY + height - 1)
            return true;
        else
            return false;
    }

    public void onTap(){
        Assets.tap.play(MainMenuScreen.tapVol);
    }

    public int getPosX() {
        return posX;
    }

    public void setPosX(int posX) {
        this.posX = posX;
    }

    public int getPosY() {
        return posY;
    }

    public void setPosY(int posY) {
        this.posY = posY;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public RectF getEnemyRectF() {
        return enemyRectF;
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

    public ButtonState getState() {
        return state;
    }

    public void setState(ButtonState state) {
        this.state = state;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
