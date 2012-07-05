package com.conorb.test;





import java.util.Random;



import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Canvas.VertexMode;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;

public class FBView extends SurfaceView implements SurfaceHolder.Callback{

	private FBThread thread;


	class FBThread extends Thread   {

		// Reference to stuff that we need to use to animate
		private SurfaceHolder mSurfaceHolder;


		//Shape Position information and appearance
		//
		private float [] shapeLocationPairs;  //xy xy xy...
		private float [] velocityPairs; //dXdY dXdY....
		private int [] shapeColor;
		private int [] shapeType;

		private static final int NUM_OBJECTS = 13 ;

		private Random myRandom;
		private long mLastTime;
		private boolean mRun;
		private Paint mPaint;
		private Rect mCanvasArea;
		private Bitmap mbgImg;
		private Bitmap cbImg;
		private boolean eventToConsume;
		private float eventX, eventY;
		private int mTouchedCount;
		private int mAlphaVal;
		private boolean mAlphaIncreasing;
		private long msAtstart;
		private long msAtEnd;

		private float mMusicVol;

		private MediaPlayer mMediaPlayer;

		private int gameState;



		//Different Shapes
		public static final int SHAPE_CIRCLE = 0;
		public static final int SHAPE_SQUARE = 1;
		/// TODO GET RID OF TRIANGLES

		public static final int SHAPE_BMP = 2;
		public static final int SHAPE_TRIANGLE = 3;
		public static final int SHAPE_PENTAGON = 4;

		public static final int GAME_STATE_RUNNING =0;
		public static final int GAME_STATE_PAUSED =1;
		public static final int GAME_STATE_COMPLETED =2;



		public FBThread(SurfaceHolder surfaceHolder, Context context, Handler handler){
			Log.v("FB Thread","Constructor called!");
			mSurfaceHolder = surfaceHolder;


			Resources res = context.getResources();
			mbgImg = BitmapFactory.decodeResource(res,R.drawable.bg);
			cbImg = BitmapFactory.decodeResource(res,R.drawable.cb);
			mMediaPlayer = MediaPlayer.create(context, R.raw.tt);

			mPaint = new Paint();
			doStart();


		}

		public void doStart(){
			// Do initial setup of shapes
			msAtstart = System.currentTimeMillis();
			msAtEnd=0;
			mMusicVol=1f;


			synchronized (mSurfaceHolder){
				Log.v("FB Thread","Do start called!");

				myRandom = new Random();



				//create arrays of shapes and their speed
				shapeLocationPairs = new float [NUM_OBJECTS*2];
				velocityPairs = new float [NUM_OBJECTS*2];

				// create array of colors and shape types
				shapeColor = new int[NUM_OBJECTS];
				shapeType = new int[NUM_OBJECTS];
				mTouchedCount= NUM_OBJECTS ;


				// Initialize shapes with random positions and speed
				for(int i = 0;i< NUM_OBJECTS *2; i+=2){

					shapeLocationPairs[i] = (float)myRandom.nextInt(250 -50) + 25;		//x
					shapeLocationPairs[i+1] = (float)myRandom.nextInt(450 -50) + 25;	//y

					velocityPairs[i] = ((float)myRandom.nextInt(2000)-1000)/100;		//dx
					velocityPairs[i+1] = ((float)myRandom.nextInt(2000)-1000)/100;	//dy

				}

				// Randomize the shape and color of each object
				for(int i = 0;i< NUM_OBJECTS; i++){
					shapeColor [i] = Color.rgb(myRandom.nextInt(255), myRandom.nextInt(255), myRandom.nextInt(255));
					shapeType [i] = myRandom.nextInt(3);

				}
				mAlphaVal =0;
				mAlphaIncreasing = true;
				mLastTime = System.currentTimeMillis() + 50;
				gameState = GAME_STATE_RUNNING;
				mMediaPlayer.setVolume(mMusicVol, mMusicVol);
				mMediaPlayer.start();

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
						// Draw method
						doDraw(c);

						//  Physics method
						doMovement();

						//Check
						checkConditions();
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
			mMediaPlayer.release();

		}



		private void doDraw(Canvas can){


			//Clear the canvas by drawing the background.
			can.drawBitmap(mbgImg, 0, 0, null);


			can.drawARGB(mAlphaVal, 0, 0, 0);




			//Draw the shapes in the array
			for(int i = 0;i< NUM_OBJECTS *2; i+=2){

				if(shapeType[i/2]== SHAPE_CIRCLE){
					drawBall(shapeLocationPairs[i], shapeLocationPairs[i+1], can,shapeColor [i/2] );
				}
				else if(shapeType[i/2]== SHAPE_SQUARE){
					//call drawSquare
					drawSquare(shapeLocationPairs[i], shapeLocationPairs[i+1], can,shapeColor [i/2] );
				}
				else if(shapeType[i/2]== SHAPE_TRIANGLE){
					drawTriangle(shapeLocationPairs[i], shapeLocationPairs[i+1], can,shapeColor [i/2] );
				}
				else if(shapeType[i/2]== SHAPE_BMP){
					drawBMP(shapeLocationPairs[i], shapeLocationPairs[i+1], can,shapeColor [i/2] );
				}

			}

			if(gameState==GAME_STATE_RUNNING){
				mPaint.setColor(Color.WHITE);
				mPaint.setTextSize(20);
				can.drawText("Remaining Shapes: " + mTouchedCount, 15f, (float)mCanvasArea.bottom-5, mPaint);
			}
			else if (gameState==GAME_STATE_COMPLETED){
				can.drawARGB(100, 0, 0, 0);
				mPaint.setTextSize(50);
				can.drawText("You Won!", 15f, (float)mCanvasArea.bottom/2, mPaint);
				mPaint.setTextSize(30);
				can.drawText("Time Taken: " + ((msAtEnd - msAtstart)/1000) + " Seconds", 15f, (float)mCanvasArea.bottom/2+30, mPaint);

			}
			else if (gameState==GAME_STATE_PAUSED){
				can.drawARGB(100, 0, 0, 0);
				mPaint.setTextSize(50);
				mPaint.setColor(Color.WHITE);
				can.drawText("Paused", 15f, (float)mCanvasArea.bottom/2, mPaint);
				//mPaint.setTextSize(30);
				//can.drawText("Time Taken: " + ((msAtEnd - msAtstart)/1000) + " Seconds", 15f, (float)mCanvasArea.bottom/2+30, mPaint);

			}




		}

		private void drawBall(float x, float y ,Canvas can,int color){
			if(color!=Color.BLACK){
				mPaint.setColor(color);
				can.drawCircle(x, y, 25, mPaint);
				mPaint.setColor(Color.WHITE);
				can.drawCircle(x, y, 15, mPaint);
				mPaint.setColor(color);
				can.drawCircle(x, y, 5, mPaint);
			}
		}

		private void drawBMP(float x, float y ,Canvas can,int color){
			if(color!=Color.BLACK){
				can.drawBitmap(cbImg, null, new Rect((int)x-25,(int) y-25,(int) x+25,(int) y+25), mPaint);
			}
		}

		private void drawSquare(float x, float y ,Canvas can,int color){
			if(color!=Color.BLACK){
				mPaint.setColor(color);
				can.drawRect(x-25, y-25, x+25, y+25, mPaint);
			}
		}

		private void drawTriangle(float x, float y ,Canvas can,int color){
			if(color!=Color.BLACK){
				mPaint.setColor(color);
				//can.
				float [] triangleVerts = new float [] {x,y,x,y-25,x+25,y-25};

				int verticesColors[] = {
						color, color, color,
						0xFF000000, 0xFF000000, 0xFF000000
				};


				can.drawVertices(VertexMode.TRIANGLES, triangleVerts.length,triangleVerts, 0, null, 0, verticesColors, 0, null, 0, 0, mPaint);
			}
		}

		public void togglePause(){
			synchronized (mSurfaceHolder) {
				if(gameState==GAME_STATE_RUNNING){
					gameState=GAME_STATE_PAUSED;
				}
				else if(gameState==GAME_STATE_PAUSED){
					gameState=GAME_STATE_RUNNING;
				}
			}
		}


		private void doMovement(){



			//get current system time
			long timeNow = System.currentTimeMillis();

			// If the current time is less than the target time do nothing.
			// Target time is initialized at start of program as currentSystemTimeMillis()+100
			//
			if (timeNow > mLastTime) {
				if(gameState == GAME_STATE_RUNNING){
					resumeMusic();
					// Change the alpha value to play around with the background
					if(mAlphaIncreasing){
						if(mAlphaVal < 240){
							mAlphaVal += 10;
						}
						else
						{
							mAlphaVal += 10;
							mAlphaIncreasing=false;
						}

					}
					else{
						if(mAlphaVal > 10){
							mAlphaVal -= 10;
						}
						else
						{
							mAlphaVal -= 10;
							mAlphaIncreasing=true;
						}
					}



					for(int i = 0;i< NUM_OBJECTS *2; i+=2){

						// Handle touch event
						if(eventToConsume==true && mTouchedCount > 0){
							// If the touch event is near any shapes. Change their color to black.
							// Draw method does not draw black balls (makes them disappear )
							if(Math.abs(shapeLocationPairs[i] - eventX) < 25 && Math.abs(shapeLocationPairs[i+1] - eventY) < 25){
								if(shapeColor[i/2] != Color.BLACK){
									shapeColor[i/2] = Color.BLACK;
									mTouchedCount--;
								}
							}

						}


						//Move our shapes
						shapeLocationPairs[i] = shapeLocationPairs[i]  + velocityPairs[i];		    // Move X
						shapeLocationPairs[i+1] = shapeLocationPairs[i+1]  + velocityPairs[i+1];    //Move Y

						// Check if our shapes are touching a wall, if so change their direction;
						// TODO Predictive collisions. Current method will fail on high speed balls
						if(shapeLocationPairs[i] > mCanvasArea.right -25|| shapeLocationPairs[i] < mCanvasArea.left +25) velocityPairs[i] *= -1;

						if(shapeLocationPairs[i+1]  > mCanvasArea.bottom -25 || shapeLocationPairs[i+1]  < mCanvasArea.top + 25) velocityPairs[i+1] *= -1;



					}


				}
				else if (gameState == GAME_STATE_COMPLETED){

					fadeOutMusic();

				}
				else if (gameState == GAME_STATE_PAUSED){
					muteMusic();
				}

				mLastTime+=50;
				eventToConsume = false;

			}

		}

		private void checkConditions(){

			if(mTouchedCount<=0 && gameState == GAME_STATE_RUNNING){
				gameState = GAME_STATE_COMPLETED;
				msAtEnd = System.currentTimeMillis();

			}


		}

		private void fadeOutMusic(){

			if(mMusicVol > 0f){

				mMusicVol -= 0.05f;
				mMediaPlayer.setVolume(mMusicVol,mMusicVol);
			}
			else{
				if(mMediaPlayer.isPlaying()){
					mMediaPlayer.stop();
				}
			}
		}
		
		private void muteMusic(){
			if(mMediaPlayer.isPlaying()){
				mMediaPlayer.pause();
			}
		}
		private void resumeMusic(){
			if(!mMediaPlayer.isPlaying()){
				mMediaPlayer.start();
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

	public void togglePause(){
		thread.togglePause();
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

		thread.doTouchEvent(event.getX(), event.getY());

		return super.onTouchEvent(event);
	}








}
