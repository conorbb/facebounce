package com.conorb.test;





import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.SlidingDrawer;

public class FBView extends SurfaceView implements SurfaceHolder.Callback{
	
	private FBThread thread;
	private Context mContext;
	
	class FBThread extends Thread   {
		
		// Reference to stuff that we need to use to animate
		private SurfaceHolder mSurfaceHolder;
		
		private Handler  mHandler;
		private boolean isMoving;
		//Position
		//private float xPos, yPos;
		private float [] coOrdPairs;  //xyxyxyxy...
		private float [] velocityPairs; //dXdYdXdY....
		private int [] ballColor;
		private final int NUM_BALLS = 13 ;
		
		// Velocity
		//private float mDY, mDX;
		private Random myRandom;
		private long mLastTime;
		private boolean mRun;
		private Paint mPaint;
		private Rect mCanvasArea;
		
		private boolean eventToConsume;
		private float eventX, eventY;
		private int mTouchedCount;
		
		public FBThread(SurfaceHolder surfaceHolder, Context context, Handler handler){
			Log.v("FB Thread","Constructor called!");
			mSurfaceHolder = surfaceHolder;
			
			
			mContext = context;
			mHandler = handler;
			isMoving = false;
			
			//xPos = 150f;
			//yPos = 150f;
			
			
			myRandom = new Random();
			
			//create single ball
			//mDY = myRandom.nextInt(30+1);
			//mDX = myRandom.nextInt(30+1);
			
			//create arrays of balls and their speed
			coOrdPairs = new float [NUM_BALLS*2];
			velocityPairs = new float [NUM_BALLS*2];
			// create array of colors
			ballColor = new int[NUM_BALLS];
			mTouchedCount= NUM_BALLS ;
			
			for(int i = 0;i< NUM_BALLS *2; i+=2){
				
				coOrdPairs[i] = (float)myRandom.nextInt(250 -50) + 25;		//x
				coOrdPairs[i+1] = (float)myRandom.nextInt(450 -50) + 25;	//y
				
				velocityPairs[i] = (float)myRandom.nextInt(20)-10;		//dx
				velocityPairs[i+1] = (float)myRandom.nextInt(20)-10;	//dy
				
			}
			
			for(int i = 0;i< NUM_BALLS; i++){
				ballColor [i] = Color.rgb(myRandom.nextInt(255), myRandom.nextInt(255), myRandom.nextInt(255));
			}
			
			mPaint = new Paint();
			//mRun = true;
			doStart();
			
			
		}
		
		public void doStart(){
			//TODO find out why this is important.
			
		
			
			synchronized (mSurfaceHolder){
				Log.v("FB Thread","Do start called!");
				//m
				mLastTime = System.currentTimeMillis() + 100;
				
			}
		}
		// Thread main loop runs here
		@Override
        public void run() {
        	Log.v("FB Thread","Thread going!");
        	
        	
        	Canvas canTemp = mSurfaceHolder.lockCanvas();
			mCanvasArea = canTemp.getClipBounds();
			mSurfaceHolder.unlockCanvasAndPost(canTemp);
        	
            while (mRun) {
            	Canvas c = null;
            	
            	
                
                try {
                    c = mSurfaceHolder.lockCanvas();
                    
                    synchronized (mSurfaceHolder) {
                    	//TODO  Draw method
                    	doDraw(c);
                    	//TODO  Physics method
                    	
                    	doMovement();
                    }
                }
                catch(Exception e){
                	e.printStackTrace();
                }
                finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
                
            }
            
        }
        
        private void doDraw(Canvas can){
        	
        	
        	//Clear the canvas.
            can.drawColor(Color.BLACK);
        	
            //Draw the one ball in the new position
            //drawBall(xPos,yPos,can);
        	
            //Draw the balls in the array
			for(int i = 0;i< NUM_BALLS *2; i+=2){
				
				drawBall(coOrdPairs[i], coOrdPairs[i+1], can,ballColor [i/2] );
	
			}
			mPaint.setColor(Color.WHITE);
			mPaint.setTextSize(20);
        	can.drawText("Remaining Balls: " + mTouchedCount, 15f, (float)mCanvasArea.bottom, mPaint);
        	
        	
        }
        
        private void drawBall(float x, float y ,Canvas can,int color){
        	
            mPaint.setColor(color);
        	can.drawCircle(x, y, 25, mPaint);
        	mPaint.setColor(Color.WHITE);
        	can.drawCircle(x, y, 15, mPaint);
        	mPaint.setColor(color);
        	can.drawCircle(x, y, 5, mPaint);
        }
        
        private void doMovement(){
        	
        	//get current system time
        	long timeNow = System.currentTimeMillis();
        	
        	// If the current time is less than the target time do nothing.
        	// Target time is initialized at start of program as currentSystemTimeMillis()+100
        	//
        	if (timeNow > mLastTime) {
        		
        		//Do the move!
        		//yPos+= mDY;
        		//xPos+= mDX;
        		
        		//Hit a corner, switch direction!
        		//if(yPos  > mCanvasArea.bottom -25 || yPos  < mCanvasArea.top + 25) mDY *= -1;
        		
        		
        		//if(xPos > mCanvasArea.right -25|| xPos < mCanvasArea.left +25) mDX *= -1;
        		
        		
    			for(int i = 0;i< NUM_BALLS *2; i+=2){
    				
            		if(eventToConsume==true && mTouchedCount > 0){
            			//If the touch event is near any balls. Change their color to white.
            			if(Math.abs(coOrdPairs[i] - eventX) < 25 && Math.abs(coOrdPairs[i+1] - eventY) < 25){
            				ballColor[i/2] = Color.WHITE;
            				mTouchedCount--;
            			}
            			
            		}
    				
    				
    				//Move our balls
    				coOrdPairs[i] = coOrdPairs[i]  + velocityPairs[i];
    				coOrdPairs[i+1] = coOrdPairs[i+1]  + velocityPairs[i+1];
    				
    				// Check if our balls are touching a wall, if so change their direction;
    				if(coOrdPairs[i] > mCanvasArea.right -25|| coOrdPairs[i] < mCanvasArea.left +25) velocityPairs[i] *= -1;
    				
            		if(coOrdPairs[i+1]  > mCanvasArea.bottom -25 || coOrdPairs[i+1]  < mCanvasArea.top + 25) velocityPairs[i+1] *= -1;
            		

            		
    			}
        		
        		
        		
        		mLastTime+=50;
        		eventToConsume = false;
        	}
        	
        }
        
        private void doTouchEvent(float x, float y){
        	synchronized (mSurfaceHolder) {
		        	eventX = x;
		        	eventY = y;
		        	eventToConsume = true;
        	}
        	
        }
        
        public void setSurfaceSize(int width, int height) {
            //TODO Add this
        	// synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
               // mCanvasWidth = width;
                //mCanvasHeight = height;

                // don't forget to resize the background image
                //mBackgroundImage = Bitmap.createScaledBitmap(
                  //      mBackgroundImage, width, height, true);
            }
        }
        
        public void setRunning(boolean b) {
            mRun = b;
        }
		
		
		

		
	}// End of FBThread
	
    public FBThread getThread() {
        return thread;
    }
    
    public FBView(Context context, AttributeSet attrs){
    	super(context,attrs);
    	SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		setupThread(context,holder);
    }
    
    public FBView (Context context, AttributeSet attrs, int defStyle){
		super(context,attrs,defStyle);
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);
		setupThread(context,holder);
		
    }

	public FBView(Context context) {
		super(context);
		
		SurfaceHolder holder = getHolder();
		holder.addCallback(this);		
		setupThread(context,holder);

	}
	
	private void  setupThread(Context context, SurfaceHolder holder){
		
        //holder.addCallback(this);
		Log.v("FB View"," Setting up thread!");
        // create thread only; it's started in surfaceCreated()
		//TODO pass null handler for the Minute, find out what it does later!
        thread = new FBThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                //mStatusText.setVisibility(m.getData().getInt("viz"));
                //mStatusText.setText(m.getData().getString("text"));
            }
        });
        setFocusable(true);
        
        
	}

    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
    	
    	Log.v("FBView","About to start thread!");
    	thread.setRunning(true);
        thread.start();
        
        
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        
        thread.setRunning(false);
        Log.v("FBView", "Trying thread.join()");
        
        while (retry) {
            try {
            	
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
            	
            }
            
        }
        
        
        
    }
    
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
    	  Log.v("FBView", "In surface changed!");
        thread.setSurfaceSize(width, height);
    }

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		
		thread.doTouchEvent(event.getX(), event.getY());
		
		return super.onTouchEvent(event);
	}
    
    
    
    
	

}
