package com.aocc.framework.implementation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//EXCEPT WHERE NOTED, THE FOLLOWING CODE IS SOURCED FROM THE KILOBOLT ANDROID FRAMEWORK

public class AndroidFastRenderView extends SurfaceView implements Runnable {
    AndroidGame game;
    Bitmap framebuffer;
    Thread renderThread = null;
    SurfaceHolder holder;
    volatile boolean running = false;
    
    public AndroidFastRenderView(AndroidGame game, Bitmap framebuffer) {
        super(game);
        this.game = game;
        this.framebuffer = framebuffer;
        this.holder = getHolder();

    }

    public void resume() { 
        running = true;
        renderThread = new Thread(this);
        renderThread.start();   

    }      
    
    public void run() {
        Rect dstRect = new Rect();
        long startTime = System.nanoTime();
        while(running) {  
            if(!holder.getSurface().isValid())
                continue;           
            

            float deltaTime = (System.nanoTime() - startTime) / 10000000.000f;
            startTime = System.nanoTime();
            
            if (deltaTime > 3.15){
            	deltaTime = (float) 3.15;
           }
     

            try {
                game.getCurrentScreen().update(deltaTime);
                game.getCurrentScreen().paint(deltaTime);
            } catch (RuntimeException e) {
                running = false;
                throw e;
            }
          
            
            
            Canvas canvas = holder.lockCanvas();
            if (canvas == null) {
                continue;
            }
            canvas.getClipBounds(dstRect);
            canvas.drawBitmap(framebuffer, null, dstRect, null);                           
            holder.unlockCanvasAndPost(canvas);
            
            
        }
    }

    public void pause() {                        
        running = false;
        if (renderThread == null) {
            return;
        }
        while(true) {
            try {
                renderThread.join();
                break;
            } catch (InterruptedException e) {
                // retry
            }
            
        }
    }     
    
  
}