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

public class FBView extends SurfaceView implements SurfaceHolder.Callback{
	
	private FBThread thread;
	private Context mContext;
	
	class FBThread extends Thread   {
		
		// Reference to stuff that we need to use to animate
		private SurfaceHolder mSurfaceHolder;
		
		private Handler  mHandler;
		private boolean isMoving;
		//Position
		private float xPos, yPos;
		// Velocity
		private float mDY, mDX;
		private long mLastTime;
		private boolean mRun;
		private Paint mPaint;
		private Rect mCanvasArea;
		//long whileCount =0;
		
		public FBThread(SurfaceHolder surfaceHolder, Context context, Handler handler){
			Log.v("FB Thread","Constructor called!");
			mSurfaceHolder = surfaceHolder;
			mContext = context;
			mHandler = handler;
			isMoving = false;
			xPos = 150f;
			yPos = 150f;
			
			Random r = new Random();
			
			mDY = r.nextInt(30+1);
			mDX = r.nextInt(30+1);
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
        	
        	
        	//mSurfaceHolder.lockCanvas().getClipBounds();
        	
            while (mRun) {
            	Canvas c = null;
            	
            	
                
                try {
                    c = mSurfaceHolder.lockCanvas();
                    mCanvasArea = c.getClipBounds();
                    synchronized (mSurfaceHolder) {
                    	//TODO  Draw method
                    	doDraw(c);
                    	//TODO  Physics method
                    	
                    	doMovement();
                    }
                } finally {
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
            can.drawColor(Color.WHITE);
        	
            //Draw the ball in the new position
            mPaint.setColor(Color.RED);
        	can.drawCircle(xPos, yPos, 25, mPaint);
        	mPaint.setColor(Color.WHITE);
        	can.drawCircle(xPos, yPos, 15, mPaint);
        	mPaint.setColor(Color.RED);
        	can.drawCircle(xPos, yPos, 5, mPaint);
        	
        	
        }
        
        private void doMovement(){
        	
        	//get current system time
        	long timeNow = System.currentTimeMillis();
        	
        	// If the current time is less than the target time do nothing.
        	// Target time is initialized at start of program as currentSystemTimeMillis()+100
        	//
        	if (timeNow > mLastTime) {
        		
        		//Do the move!
        		yPos+= mDY;
        		xPos+= mDX;
        		
        		//Hit a corner, switch direction!
        		if(yPos> mCanvasArea.bottom || yPos < mCanvasArea.top) mDY *= -1;
        		
        		
        		if(xPos> mCanvasArea.right || xPos < mCanvasArea.left) mDX *= -1;
        		
        		
        		
        		
        		mLastTime+=50;
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
    
    
	

}
