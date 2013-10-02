package com.cam.dualcam;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.Menu;
import android.view.View.DragShadowBuilder;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Button;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.view.OrientationEventListener;

import com.cam.dualcam.R.string;
import com.cam.dualcam.utility.*;
import com.cam.dualcam.bitmap.*;
import com.cam.dualcam.color.ColorPickerDialog;
import com.cam.dualcam.color.ColorPickerDialog.OnColorChangedListener;
import com.cam.dualcam.view.CameraPreview;

@SuppressLint("NewApi")
public class DualCamActivity extends Activity implements OnClickListener, OnColorChangedListener {
	
	//Defined variables
	//Jap Messages
	private String errorMessage			= "ç”³ã�—è¨³ã�‚ã‚Šã�¾ã�›ã‚“ã�Œã€�ä½•ã�‹ã�Œã‚«ãƒ¡ãƒ©ã�§é–“é�•ã�£ã�¦ã�„ã�Ÿã€‚";
	private String retakeMessage		= "å†™çœŸã‚’æ’®ã‚Šã�ªã�Šã�—ã�¾ã�™ã�‹ï¼Ÿ";
	
	public static String TAG 			= "DualCamActivity";
	private String fileName				= null;
	private String cameraSide 			= null;
	private String orientationScreen	= null;
	
	private Bitmap tempPic				= null;
	private Bitmap frontPic 			= null;
	private Bitmap backPic  			= null;
	public BitmapFactory.Options options= null;
	
	private boolean isBackTaken 		= false;
	private boolean isFrontTaken		= false;
	private boolean isSaved				= false;
	private boolean isSavable			= false;
	private boolean isSet				= false;
	
	public Integer screenHeight;
	public Integer screenWidth;
	
	public static int result = 0;
	public static int degrees = 0;
	public static int orientationOfPhone = 0;
	public static int FontSize = 1;  //aid
	public static String TextToShow; //aid
	public static int FontColor;  //aid
	private static final String COLOR_PREFERENCE_KEY = "color";  //aid
	//Utility
	public PackageCheck packageCheck;
	public static MediaUtility mediaUtility;
	public static CameraUtility cameraUtility;
	public Intent sharingIntent;
	
	//Camera Settings
	public Parameters param;
	public Camera mCamera;
	
	//Widgets
	
	//Previews
	public ImageView backPreview
					,frontPreview
					,previewImage;
	

	public RelativeLayout toSaveLayout;  //aid
	public LinearLayout pictureLayout;
	public FrameLayout  mainPreview, 
						createTextFrameLayout;  //aid
    public CameraPreview cameraPreview;
    
    
    //Buttons
    public ImageView captureButton
    				,saveButton
    				,retryButton
    				,shareButton;

    private int offset_x = 0;
    private int offset_y = 0;
    
    
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dualcam);

		
		mediaUtility = new MediaUtility(getApplicationContext());
		packageCheck = new PackageCheck(getApplicationContext());

		captureButton= (ImageView) findViewById(R.id.smileyButton);
		saveButton   = (ImageView) findViewById(R.id.saveButton);
		retryButton  = (ImageView) findViewById(R.id.retryButton);
		shareButton  = (ImageView) findViewById(R.id.shareButton);
		backPreview  = (ImageView) findViewById(R.id.cumPreviewBack);
		frontPreview = (ImageView) findViewById(R.id.cumPreviewFront);
		previewImage = (ImageView) findViewById(R.id.previewImage);
		
		mainPreview = (FrameLayout) findViewById(R.id.cumshot);
		pictureLayout = (LinearLayout) findViewById(R.id.picLayout);
		createTextFrameLayout = (FrameLayout) findViewById(R.id.createTextFrame);
		toSaveLayout = (RelativeLayout)findViewById(R.id.createTextLayout);
		captureButton.setOnClickListener(this);
		saveButton.setOnClickListener(this);
		retryButton.setOnClickListener(this);
		shareButton.setOnClickListener(this);
		backPreview.setOnClickListener(this);
		frontPreview.setOnClickListener(this);
		
		
		backPreview.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				if(mCamera != null)
				mCamera.autoFocus(new AutoFocusCallback(){
					@Override
					public void onAutoFocus(boolean arg0, Camera arg1) {
						
						takeAShot();
					}
				});
				
				return true;
			}
		});
		
		frontPreview.setOnLongClickListener(new OnLongClickListener(){
			@Override
			public boolean onLongClick(View arg0) {
				if(mCamera != null)
				mCamera.autoFocus(new AutoFocusCallback(){
					@Override
					public void onAutoFocus(boolean arg0, Camera arg1) {
						takeAShot();
					}
				});
				return true;
			}
		});
		
		
		
		try{
			orientationOfPhone = this.getResources().getConfiguration().orientation;
			screenHeight = new PhoneChecker(this).screenHeight;
			screenWidth = new PhoneChecker(this).screenWidth;
			
			if(orientationOfPhone == Configuration.ORIENTATION_PORTRAIT){
				orientationScreen = "PORTRAIT";
			}
			else if(orientationOfPhone == Configuration.ORIENTATION_LANDSCAPE){
				orientationScreen = "LANDSCAPE";
			}
			else{
				orientationScreen = "UNKNOWN";
			}
			
			if (savedInstanceState != null) {
				
				
				isBackTaken 		= savedInstanceState.getBoolean("isBackTaken", isBackTaken);
				isFrontTaken 		= savedInstanceState.getBoolean("isFrontTaken", isFrontTaken);
				isSavable			= savedInstanceState.getBoolean("isSavable", isSavable);
				isSaved				= savedInstanceState.getBoolean("isSaved", isSaved);
				cameraSide			= savedInstanceState.getString("cameraSide");
				
				if(isBackTaken){
					getPressedPreview("BACK").setVisibility(ImageView.VISIBLE);
					getPressedPreview("BACK").setBackgroundDrawable(null);
					getPressedPreview("BACK").setImageBitmap(null);
					backPic = savedInstanceState.getParcelable("backPic");
					settoBackground(getPressedPreview("BACK"), backPic);
				}

				
				if(isFrontTaken){
					getPressedPreview("FRONT").setVisibility(ImageView.VISIBLE);
					getPressedPreview("FRONT").setBackgroundDrawable(null);
					getPressedPreview("FRONT").setImageBitmap(null);
					frontPic = savedInstanceState.getParcelable("frontPic");
					settoBackground(getPressedPreview("FRONT"), frontPic);
				}
				
				if(isBackTaken && !isFrontTaken){
					setSide("FRONT");
				}
				else if(!isBackTaken && isFrontTaken){
					setSide("BACK");
				}
				else if(!isBackTaken && !isFrontTaken){
					setSide("BACK");
				}
				
				setButtons();
				
			}
			else
				setSide("BACK");
		}catch(Exception e){
			
		}

//		if (savedInstanceState != null) {
//			tempPic = savedInstanceState.getParcelable("bitmap");
//			settoBackground(getPressedPreview("BACK"), tempPic);
//		}
//		else
			//setSide("BACK");
	}
	
	
public void onClick(View view) {
		
		try{
			if(view.getId() == R.id.smileyButton){
				takeAShot();
			}
			
			else if(view.getId() == R.id.saveButton){
				
				//if(isSavable)
					try{
						toSaveLayout.buildDrawingCache();
						saveImage(toSaveLayout.getDrawingCache());
						toSaveLayout.destroyDrawingCache();
						//saveImage(); 
					}catch(Exception e){
						Toast.makeText(getApplicationContext(),errorMessage,Field.SHOWTIME).show();
					}
				//else
					//Toast.makeText(getApplicationContext(),"You don't want a pic of yourself?",Field.SHOWTIME).show();
				
			}
			
			else if(view.getId() == R.id.retryButton){
				linkSTART();
			}
			
			else if(view.getId() == R.id.shareButton){
				try{
					Log.i(TAG, "isSaved = "+isSaved);
					if(isSaved)
						shareFunction();
					
				}catch(Exception e){
					Log.i(TAG, "isSaved = "+isSaved);
					Log.i(TAG,"ERROR = "+e.getCause());
				}
				//else
					//Toast.makeText(getApplicationContext(),"ç”»åƒ�ã‚’ä¿�å­˜ã�—ã�¦ã��ã� ã�•ã�„",Field.SHOWTIME).show();
				
			}
			
			else if(view.getId() == R.id.retakeback){
				if(isSavable){
					setSide("BACK");
				}
			}
			
			else if(view.getId() == R.id.retakefront){
				if(isSavable){
					setSide("FRONT");
				}
			}
			
			else if(view.getId() == R.id.cumPreviewBack){
				
				if(isSavable){
					//setSide("BACK");
					retakeImage("BACK");
					//createAlert("","","");
					
				}
				else{
					if(cameraSide == "BACK")
						takeAShot();
				}
//				else{
//					mCamera.autoFocus(new AutoFocusCallback(){
//						@Override
//						public void onAutoFocus(boolean arg0, Camera arg1) {
//							//camera.takePicture(shutterCallback, rawCallback, jpegCallback);
//						}
//					});
//				}
			}
			
			else if(view.getId() == R.id.cumPreviewFront){
				if(isSavable){
					//setSide("FRONT");
					retakeImage("FRONT");
				}
				else{
					if(cameraSide == "FRONT")
						takeAShot();
				}
//				else{
//					mCamera.autoFocus(new AutoFocusCallback(){
//						@Override
//						public void onAutoFocus(boolean arg0, Camera arg1) {
//							//camera.takePicture(shutterCallback, rawCallback, jpegCallback);
//						}
//					});
//				}
//				
//				if(cameraSide == "BACK")
//				{
//					try{
//						//mCamera.takePicture(null, null, s3FixIloveS3);
//						mCamera.setErrorCallback(ec);
//						mCamera.takePicture(null, null, mPicture);
//						//Toast.makeText(getApplicationContext(),"Nice shot!",Field.SHOWTIME).show();
//						
//					}catch(Exception e){
//						//mCamera.takePicture(null, null, s3FixIloveS3);
//						Toast.makeText(getApplicationContext(),errorMessage,Field.SHOWTIME).show();
//						
//					}
//				}
			}
			
			else if(view.getId() == R.id.cumshot){
//				if(isSavable){
//					if(cameraSide == "BACK")
//						setSide("BACK");
//					else if(cameraSide == "FRONT")
//						setSide("FRONT");	
//				}
				
			}
		}
		catch(Exception e)
		{
			Log.i(TAG,"Error in here View = "+view.getId()+": Cause? I don't effing know -> "+e.getMessage());
			Toast.makeText(this,errorMessage,Field.SHOWTIME).show();
		}
	}


	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    Log.i(TAG, "Destroying onDestroy");
	    releaseCamera();
	    //relenquishTheSoul();
	    
	}

	@Override
	protected void onPause() {
	    super.onPause();
	    Log.i(TAG, "Destroying onPause");
	    releaseCamera();
	    //relenquishTheSoul();
	}
    
	@Override
	public void onSaveInstanceState(Bundle toSave) {
	  super.onSaveInstanceState(toSave);
	  
	  toSave.putBoolean("isBackTaken", isBackTaken);
	  toSave.putBoolean("isFrontTaken", isFrontTaken);
	  toSave.putBoolean("isSavable", isSavable);
	  toSave.putBoolean("isSaved", isSaved);
	  
	  if(frontPic != null)
		  toSave.putParcelable("frontPic", frontPic);
	  
	  if(backPic != null)
		  toSave.putParcelable("backPic", backPic);
	  
	  if(cameraSide != null)
		  toSave.putString("cameraSide", cameraSide);
	  
	}    
    
    
    //Custom Methods
	public void createAlert(String currentFunction,String thisside,String thismessage){
		final String title = currentFunction;
		final String side = thisside;
		final String message = thismessage;
		
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				DualCamActivity.this);
	
			// set title
			//alertDialogBuilder.setTitle(title);
	
			// set dialog message
			alertDialogBuilder
				.setMessage(message)
				.setCancelable(false)
				.setPositiveButton("ã�¯ã�„ ",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						if(title == "retake"){
							Log.i(TAG, "Initiating Retake :D");
							setSide(side);
						}
					}
				  })
				.setNegativeButton("ã�„ã�„ã�ˆ",new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,int id) {
						
					}
				});
	
			// create alert dialog
			AlertDialog alert = alertDialogBuilder.create();
	    	alert.show();
	    	
	}
    
    public ErrorCallback ec = new ErrorCallback(){

		@Override
		public void onError(int data, Camera camera) {
			Log.i(TAG,"ErrorCallback received");
		}
		
	};
    
	public PictureCallback getPic = new PictureCallback() {
			
		    @Override
		    public void onPictureTaken(byte[] data, Camera camera) {
		    	
		    	try {
		    		ImageView buttonView = getPressedPreview(cameraSide);
		    		Matrix matrix = new Matrix(); 
		    		int width = 0;
			    	int height = 0;
			    	int extraWidth = 0 ;
		           	int extraHeight = 0;
		           	int marginalWidth = 0;
		           	int marginalHeight = 0;
		    		
	//	         	if(tempPic != null){
	//	           		tempPic.recycle();
	//	           		tempPic = null;
	//	           	}
		    		
			    	
			    	
			    	
			    	Log.i(TAG,"Pic taken");
			            if(cameraSide == "BACK")
			            {
			            	Log.i(TAG,"Side = "+cameraSide);
			            	setRetake(cameraSide);
			            	matrix.postRotate(result); 
			            	
			            	
				    		options = new BitmapFactory.Options();
				 	  		options.inSampleSize = 1;
				 	  		options.inJustDecodeBounds = true;
				 	  		
				 	  		// Determine how much to scale down the image
				 	  	    //int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
				 	  	    
				 	  		Bitmap temp = BitmapFactory.decodeByteArray(data, 0,  data.length);
				 			int xW = temp.getWidth();
				 			int xH = temp.getHeight();
				 	  		// Determine how much to scale down the image
				 	  	    //int scaleFactor = Math.min(xW/screenHeight, xH/screenWidth);
				 			int scaleFactor = Math.max(xW/screenHeight, xH/screenWidth);
				 	  		// Calculate inSampleSize
				 		    //options.inSampleSize = bitmapResizer.calculateInSampleSize(options, shortWidth, shortHeight);
				 		    options.inSampleSize = scaleFactor;
				 		    // Decode bitmap with inSampleSize set
				 		    options.inJustDecodeBounds = false;
				 		    tempPic = BitmapFactory.decodeByteArray(data, 0,  data.length,options);
				 		    width = tempPic.getWidth();
				           	height = tempPic.getHeight();
				 		    
				           	Log.i(TAG,"Before SShot");
				           	Log.i(TAG,"*******************   TADAA!!   ***************************");
				           	Log.i(TAG,"xW = "+xW+": xH = "+xH);
				           	Log.i(TAG,"Width = "+tempPic.getWidth()+": Height = "+tempPic.getHeight());
				           	Log.i(TAG,"screenWidth = "+screenWidth+": screenHeight = "+screenHeight);
				           	Log.i(TAG,"scaleFactor = "+scaleFactor);
				           	
				           	Log.i(TAG,"*******************   TADAA!!   ***************************");
				           	if(width > screenHeight || height > screenWidth){
				           		if(width > 1280 || height > 1280){
				           			tempPic = Bitmap.createScaledBitmap(tempPic, Math.round(width/2), Math.round( height /2),true);
				           		}
				           		tempPic = Bitmap.createBitmap(tempPic, 0,0,tempPic.getWidth(), tempPic.getHeight(), matrix, true);
				           		width = tempPic.getWidth();
					           	height = tempPic.getHeight();
					           	extraWidth = width - screenWidth;
					           	extraHeight = height - screenHeight;
					           	marginalWidth = Math.round(extraWidth/2);
					           	marginalHeight = Math.round(extraHeight/2);
					           	if(marginalHeight < 0)
					           		marginalHeight = 0;
					           	if(marginalWidth < 0)
					           		marginalWidth = 0;
					           	if(extraWidth < 0)
					           		extraWidth = 0;
					           	if(extraHeight < 0)
					           		extraHeight = 0;
					           	
					           	Log.i(TAG,"Width = "+width+": Height = "+height);
					           	Log.i(TAG,"screenWidth = "+screenWidth+": screenHeight = "+screenHeight);
					           	Log.i(TAG,"marginalWidth = "+marginalWidth+": marginalHeight = "+marginalHeight);
					           	
					           	if(orientationScreen == "PORTRAIT"){
					           		tempPic = Bitmap.createBitmap(tempPic,marginalWidth,marginalHeight,width - extraWidth, height - marginalHeight);
					           		//tempPic = Bitmap.createBitmap(tempPic,0,0,width, height);
					           	}
					        }
				           	else{
				           		tempPic = Bitmap.createBitmap(tempPic, 0,0,tempPic.getWidth(), tempPic.getHeight(), matrix, true);
				           		width = tempPic.getWidth();
					           	height = tempPic.getHeight();
					           	Log.i(TAG,"Width = "+width+": Height = "+height);
					           	Log.i(TAG,"screenWidth = "+screenWidth+": screenHeight = "+screenHeight);
				           	}
				           	width = tempPic.getWidth();
				           	height = tempPic.getHeight();
				            //tempPic = Bitmap.createBitmap(tempPic, 0,0,width, Math.round(height/2));
				           	
				           	if(orientationScreen == "PORTRAIT"){
				           		//Portrait
				           		tempPic = Bitmap.createBitmap(tempPic, 0,0,width,height - Math.round(height/3));
				           		
				           	}
				           	else if(orientationScreen == "LANDSCAPE"){
				           		//Landscape
				           		tempPic = Bitmap.createBitmap(tempPic, 0,0,width - Math.round(width/3),height);
				           		
				           	}
				           	
				 		    settoBackground(buttonView,tempPic);
				 		    backPic = tempPic;
			            	mCamera.stopPreview();
			            	releaseCamera();
			            	previewImage.setVisibility(ImageView.GONE);
			            	isBackTaken = true;
					        
					        if(!isFrontTaken )
					        {
					        	setSide("FRONT");
					        	//setUntake("BACK");
					        }
			            	
			            	
			            }
			            else
			            {
			            	Log.i(TAG,"Side = "+cameraSide);
			            	setRetake(cameraSide);
			            	matrix.postRotate(result); 
				 		    matrix.preScale(-1, 1);
				           	
			            	options = new BitmapFactory.Options();
				 	  		options.inSampleSize = 1;
				 	  		options.inJustDecodeBounds = true;
				 	  		
				 	  		// Determine how much to scale down the image
				 	  	    //int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
				 	  	  
				 	  		Bitmap temp = BitmapFactory.decodeByteArray(data, 0,  data.length);
				 			int xW = temp.getWidth();
				 			int xH = temp.getHeight();
				 	  		// Determine how much to scale down the image
				 	  	    int scaleFactor = Math.min(xW/screenHeight, xH/screenWidth);
				 	  		// Calculate inSampleSize
				 		    //options.inSampleSize = bitmapResizer.calculateInSampleSize(options, shortWidth, shortHeight);
				 		    options.inSampleSize = scaleFactor;
				 		    // Decode bitmap with inSampleSize set
				 		    options.inJustDecodeBounds = false;
					    		
				 		    tempPic = BitmapFactory.decodeByteArray(data, 0,  data.length,options);
				 		    width = tempPic.getWidth();
				           	height = tempPic.getHeight();
				 		    
				           	Log.i(TAG,"Before SShot");
				           	Log.i(TAG,"Width = "+tempPic.getWidth()+": Height = "+tempPic.getHeight());
				           	
				           	if(width > screenHeight || height > screenWidth){
				           		if(width > 1280 || height > 1280){
				           			tempPic = Bitmap.createScaledBitmap(tempPic, Math.round(width/2), Math.round( height /2),true);
				           		}
				           		tempPic = Bitmap.createBitmap(tempPic, 0,0,tempPic.getWidth(), tempPic.getHeight(), matrix, true);
				           		width = tempPic.getWidth();
					           	height = tempPic.getHeight();
					           	extraWidth = width - screenWidth;
					           	extraHeight = height - screenHeight;
					           	marginalWidth = Math.round(extraWidth/2);
					           	marginalHeight = Math.round(extraHeight/2);
					           	if(marginalHeight < 0)
					           		marginalHeight = 0;
					           	if(marginalWidth < 0)
					           		marginalWidth = 0;
					           	if(extraWidth < 0)
					           		extraWidth = 0;
					           	if(extraHeight < 0)
					           		extraHeight = 0;
					           	
					           	Log.i(TAG,"Width = "+width+": Height = "+height);
					           	Log.i(TAG,"screenWidth = "+screenWidth+": screenHeight = "+screenHeight);
					           	Log.i(TAG,"marginalWidth = "+marginalWidth+": marginalHeight = "+marginalHeight);
					           	Log.i(TAG,"Resizing~ ching ching!");
					           	
					           	if(orientationScreen == "PORTRAIT"){
					           		tempPic = Bitmap.createBitmap(tempPic,marginalWidth,marginalHeight,width - extraWidth, height - marginalHeight);
					           	}
					        }
				           	else{
				           		tempPic = Bitmap.createBitmap(tempPic, 0,0,tempPic.getWidth(), tempPic.getHeight(), matrix, true);
				           		width = tempPic.getWidth();
					           	height = tempPic.getHeight();
					           	Log.i(TAG,"Width = "+width+": Height = "+height);
					           	Log.i(TAG,"screenWidth = "+screenWidth+": screenHeight = "+screenHeight);
					           	Log.i(TAG,"Unresized booya!");
				           	}
				           	width = tempPic.getWidth();
				           	height = tempPic.getHeight();
				           	boolean b = tempPic.isMutable();
				           	Log.i(TAG, "The reason = "+b);
				           	Log.i(TAG,"Flag 1");
				            //tempPic = Bitmap.createBitmap(tempPic, 0,Math.round(height/2),width, height/2);
				           	
				        	if(orientationScreen == "PORTRAIT"){
				           		//Portrait
				        		tempPic = Bitmap.createBitmap(tempPic, 0,Math.round(height/3),width, height - Math.round(height/3));
				        	}
				        	else if(orientationScreen == "LANDSCAPE"){
				        		//Landscape
				        		tempPic = Bitmap.createBitmap(tempPic, Math.round(width/3),0,width  - Math.round(width/3), height);
				        		
				        	}
				        	
				           	Log.i(TAG,"Flag 2");
				            settoBackground(buttonView,tempPic);
				            frontPic = tempPic;
				           	Log.i(TAG,"Flag 3");
				 		    mCamera.stopPreview();
			 	            releaseCamera();
			 	            isFrontTaken = true;
	
	
			            }
			            //FileOutputStream fos = new FileOutputStream(pictureFile);
			            //fos.write(data);
			            //fos.close();
			            if(isBackTaken && isFrontTaken){
				    		isSavable = true;
				    		saveButton.setImageResource(R.drawable.save1);
				    		
				    		AlertDialog.Builder alertDialogBuilderCreateText = new AlertDialog.Builder(DualCamActivity.this);
				    	
				    			// set title
				    			alertDialogBuilderCreateText.setTitle("Create Text");
				    	
				    			// set dialog message
				    			alertDialogBuilderCreateText
				    				.setMessage("Are you want to create a personalized message?")
				    				.setCancelable(false)
				    				.setPositiveButton("Yes",new DialogInterface.OnClickListener() {
				    					public void onClick(DialogInterface dialog,int id) {
				    						customAlertdialog();
				    					}
				    				  })
				    				.setNegativeButton("No",new DialogInterface.OnClickListener() {
				    					public void onClick(DialogInterface dialog,int id) {
				    						//no
				    					}
				    				});
				    	
				    			// create alert dialog
				    			AlertDialog alert = alertDialogBuilderCreateText.create();
				    	    	alert.show();
				    	}
			            
			        }catch (Exception e) {
			        	Log.i(TAG,"not isSaved");
			        	Log.e(TAG,"Error accessing file: " + e.getMessage());
			        	Toast.makeText(getApplicationContext(),errorMessage,Field.SHOWTIME).show();
			        	//linkSTART();
			        	
			        }
		    }
	};
	
	public ImageView getPressedPreview(String cameraSide){
		ImageView buttonView = null;
		
		if(cameraSide == "BACK")
			buttonView = backPreview;
        if(cameraSide == "FRONT")
        	buttonView = frontPreview;
        
		return buttonView;
	}
	
	public void linkSTART(){
		finish();
		Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage( getBaseContext().getPackageName() );
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(i);
	}
	
	public void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }
	
	public void relenquishTheSoul(){
		releaseCamera();              // release the camera immediately on pause event
        options = null;
        if(tempPic != null)
        	tempPic.recycle();
        finish();
        System.exit(0);
	}
	
	 public void retakeImage(String thisside){
		  cameraSide = thisside;
		  String message = retakeMessage;
		  String title = "retake";
		  createAlert(title,cameraSide,message);
	  }
	 
	 public void saveImage(Bitmap bmp){
			try {
					
				   Log.d(TAG,"the filename = "+mediaUtility.getOutputMediaFile(Field.MEDIA_TYPE_IMAGE).toString());
				   fileName = mediaUtility.getOutputMediaFile(Field.MEDIA_TYPE_IMAGE).toString();
				   Log.d(TAG,"The utility = "+mediaUtility.getOutputMediaFile(Field.MEDIA_TYPE_IMAGE).toString());
			       FileOutputStream out = new FileOutputStream(mediaUtility.getOutputMediaFile(Field.MEDIA_TYPE_IMAGE));
			       Log.d(TAG,"Before saving");
			       bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
			       Log.d(TAG,"After saving");
			       mediaUtility.updateMedia(TAG,"file://" +mediaUtility.getOutputMediaFile(Field.MEDIA_TYPE_IMAGE).toString());
			       Log.d(TAG,"file://" +mediaUtility.getOutputMediaFile(Field.MEDIA_TYPE_IMAGE).toString());
			       out.flush();
			       out.close();
			       Log.d(TAG,"Saved to "+mediaUtility.getOutputMediaFile(Field.MEDIA_TYPE_IMAGE).toString());
			       Toast.makeText(getApplicationContext(),"å†™çœŸã�®ä¿�å­˜ã�Œå®Œäº†ã�—ã�¾ã�—ã�Ÿã€‚",Field.SHOWTIME).show();
			       isSaved = true;
			       shareButton.setImageResource(R.drawable.share1);
			  
			} catch (Exception e) {
			       e.printStackTrace();
			       Log.d(TAG,"Saving failed cause = "+ e.getCause() );
			       Toast.makeText(getApplicationContext(),errorMessage,Field.SHOWTIME).show();
					
			}
		}

	public void seePreview(String cameraSide){
		try{

        	previewImage.setVisibility(ImageView.GONE);
			mainPreview.setVisibility(FrameLayout.VISIBLE);
			createTextFrameLayout.setVisibility(FrameLayout.VISIBLE);//aid
			isSavable = false;	
			saveButton.setImageResource(R.drawable.save2);
			releaseCamera();
			setUntake(cameraSide);
			ImageView buttonView = getPressedPreview(cameraSide);
			cameraUtility = new CameraUtility(getApplicationContext());
			Log.i(TAG, "1");
			
			//Normal
			//mCamera = cameraUtility.getCameraInstance(cameraSide,screenHeight,screenWidth,orientationScreen);
			mCamera = cameraUtility.getCameraInstance(cameraSide,screenHeight/2,screenWidth,orientationScreen);
			Log.i(TAG, "a");
			setCameraDisplayOrientation(this,cameraUtility.findCamera(cameraSide),mCamera);
			Log.i(TAG, "b");
			cameraPreview = new CameraPreview(getApplicationContext(), mCamera);
			mainPreview.removeAllViews();
			//createTextFrameLayout.removeAllViews(); //aid
			
			RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)mainPreview.getLayoutParams();
			if(orientationScreen == "PORTRAIT"){
				//Log.i(TAG,"PASOK DITO!!!");
				if(cameraSide == "BACK"){
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 0);
					
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(screenWidth,(screenHeight*3)/4);
					cameraPreview.setLayoutParams(lp);
				}
				else{
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
					
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(screenWidth,(screenHeight*3)/4);
					lp.setMargins(0,screenHeight/4, 0, 0);
					cameraPreview.setLayoutParams(lp);
					//backPreview.setVisibility(ImageView.GONE);
				}

				//mainPreview.setLayoutParams(lp);
			}
			else if(orientationScreen == "LANDSCAPE"){
				if(cameraSide == "BACK"){
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 0);
					
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((screenWidth*3)/4,screenHeight);
					cameraPreview.setLayoutParams(lp);
				}
				else{
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 0);
					layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
					
					FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams((screenWidth*3)/4,screenHeight);
					lp.setMargins(screenWidth/4,0, 0, 0);
					cameraPreview.setLayoutParams(lp);
				}
			}
			
			mainPreview.setLayoutParams(layoutParams);    
			mainPreview.addView(cameraPreview);
			Log.i(TAG, "2");
	
			buttonView.setBackgroundDrawable(null);
			buttonView.setImageBitmap(null);
				
		}catch(Exception e){
			Log.e(TAG,"Di ko na alam to wtf ftw");
			Log.e(TAG,"e = "+e.getCause());
			
			//Toast.makeText(getApplicationContext(),"OOPS!! Error = "+e.getMessage(),Field.SHOWTIME).show();
			Toast.makeText(getApplicationContext(),errorMessage,Field.SHOWTIME).show();
        	//linkSTART();
		}
	}
	
	public void setButtons(){
		
		
		if(isSaved)
			shareButton.setImageResource(R.drawable.share1);
		else
			shareButton.setImageResource(R.drawable.share2);
		
		if(isSavable)
			saveButton.setImageResource(R.drawable.save1);
		else
			saveButton.setImageResource(R.drawable.save2);
		
	}
	
	public static void setCameraDisplayOrientation(Activity activity,
	         int cameraId, android.hardware.Camera camera) {
		 Parameters params;
		 int width = 0;
		 int height = 0;
	     android.hardware.Camera.CameraInfo info =
	             new android.hardware.Camera.CameraInfo();
	     android.hardware.Camera.getCameraInfo(cameraId, info);
	     int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
	     
	     switch (rotation) {
	         case Surface.ROTATION_0: degrees = 0; break;
	         case Surface.ROTATION_90: degrees = 90; break;
	         case Surface.ROTATION_180: degrees = 180; break;
	         case Surface.ROTATION_270: degrees = 270; break;
	     }

	    
	     Log.i(TAG,"Degrees = "+degrees);
	     if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
	         result = (info.orientation + degrees) % 360;
	         result = (360 - result) % 360;  // compensate the mirror
	     } else {  // back-facing
	         result = (info.orientation - degrees + 360) % 360;
	     }
	     
	     width = cameraUtility.getCamWidth();
    	 height = cameraUtility.getCamHeight();

	     Log.i(TAG,"width "+width);
	     Log.i(TAG,"RESULT = "+result);
	     camera.setDisplayOrientation(result);
	 }
	

	  public void setRetake(String cameraSide){
		  
		  
		  if(cameraSide == "BACK"){
			  isBackTaken = true;
		  }
		  else if(cameraSide == "FRONT"){
			  isFrontTaken = true;
		  }
	  }
	
	
	public void setSide(String thisside){
		  cameraSide = thisside;
		  seePreview(cameraSide);
	 }
	
	public void settoBackground(View view, Bitmap bitmap){
		BitmapDrawable bd = new BitmapDrawable(bitmap);
		int sdk = android.os.Build.VERSION.SDK_INT;
        if(sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
    	   view.setBackgroundDrawable(bd);
        } else {
    	   view.setBackground(bd);
        }
	}
	
	public void setUntake(String cameraSide){
		  if(cameraSide == "BACK"){
			  isBackTaken = false;
		  }
		  else if(cameraSide == "FRONT"){
			  isFrontTaken = false;
		  }
	  }
	
	public void shareFunction(){
		Uri uri = Uri.parse("file://"+fileName);

		String shareBody = "Here is the share content body";
		sharingIntent = new Intent(Intent.ACTION_SEND);
		sharingIntent.setType("image/png");
		sharingIntent.putExtra(Intent.EXTRA_STREAM,uri);
		startActivity(Intent.createChooser(sharingIntent, "Share via"));
	}
  
	public void takeAShot(){
		if(mCamera != null){
			try{
				mCamera.setErrorCallback(ec);
				mCamera.takePicture(null, null,getPic);
			}catch(Exception e){
				Log.i(TAG, "Error at capture button : e = "+e.getCause());
				Toast.makeText(getApplicationContext(),errorMessage,Field.SHOWTIME).show();
			}
			
		}
	}
	
	public void customAlertdialog(){
		
		final AlertDialog.Builder alert = new AlertDialog.Builder(this); 

	    LinearLayout linear=new LinearLayout(this); 

	    linear.setOrientation(1); 
	    
	    final EditText addedText = new EditText(this);
	    addedText.setHint("Type text here...");
	    
	    final TextView textFontSize = new TextView(this); 
	    textFontSize.setText("Font Size = " + FontSize ); 
	    textFontSize.setPadding(10, 10, 10, 10); 
        
	    SeekBar seek=new SeekBar(this); 
	    
	    seek.setMax(50);
	    seek.setProgress(FontSize);
	    
	    Button bt = new Button(this);
	    bt.setText("Select a Font Color");
	    bt.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

	    bt.setOnClickListener(new OnClickListener(){
	    	   public void onClick(View v) {
	    		    int color = PreferenceManager.getDefaultSharedPreferences(
	    	                DualCamActivity.this).getInt(COLOR_PREFERENCE_KEY,
	    	                Color.WHITE);
	    	        new ColorPickerDialog(DualCamActivity.this, DualCamActivity.this,
	    	                color).show();
	    	   }
	    	});
	    
	    linear.addView(addedText); 
	    linear.addView(seek); 
	    linear.addView(textFontSize); 
	    linear.addView(bt); 
	    
	    alert.setView(linear); 
	     
	    seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
	        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
	        	textFontSize.setText("Font Size = " + FontSize ); 
	        	FontSize = progress;
	        }

			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
			}

			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
			}
	    });

	    alert.setPositiveButton("Ok",new DialogInterface.OnClickListener() 
	    { 
	        public void onClick(DialogInterface dialog,int id)  
	        {
	        	TextToShow = addedText.getText().toString();
	        	createAText();
	        }
	    }); 

	    alert.setNegativeButton("Cancel",new DialogInterface.OnClickListener()  
	    { 
	        public void onClick(DialogInterface dialog,int id)  
	        { 
	           // Toast.makeText(getApplicationContext(), "Cancel Pressed",Toast.LENGTH_LONG).show(); 
	        	/*Intent intent = new Intent(DualCamActivity.this,  GetColorClass.class); 
	        	DualCamActivity.this.startActivity(intent);
	        	DualCamActivity.this.finish();  */
	            
	            return; 
	        } 
	    }); 
	    alert.show(); 	
		
	}
	
	
	public void createAText(){
		
		// this is the method to create text on the picture
		RelativeLayout rlv = (RelativeLayout)findViewById(R.id.buttonLayout);
		
		final RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams)createTextFrameLayout.getLayoutParams();
		
		layoutParams.setMargins(10, 10, 10, 10);
		layoutParams.addRule(RelativeLayout.ABOVE);  
		final TextView tv2 = new TextView(getApplicationContext()); 
		//tv2.setLayoutParams(layoutParams);       
		tv2.setGravity(Gravity.TOP);   
		tv2.setText(TextToShow);     
		tv2.setTextSize(FontSize);
		tv2.setTextColor(FontColor);     
		
		tv2.setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		    	switch(event.getActionMasked())
                {
                        case MotionEvent.ACTION_DOWN:
                                offset_x = (int)event.getX();
                                offset_y = (int)event.getY();
                                //selected_item = v;
                                break;
                        default:
                                break;
                }
                  
                return false;
		    }
		});
		
		
		createTextFrameLayout.setOnTouchListener(new OnTouchListener() {
		    @Override
		    public boolean onTouch(View v, MotionEvent event) {
		        switch(event.getActionMasked())
                {
                	case MotionEvent.ACTION_MOVE:
                		int x = (int)event.getX() - offset_x;
                		int y = (int)event.getY() - offset_y;

                		int w = getWindowManager().getDefaultDisplay().getWidth() - 100;
                		int h = getWindowManager().getDefaultDisplay().getHeight() - 100;
                			
                		if(x > w)
                		x = w;
                		if(y > h)
                		y = h;
                        
                	FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                                 new FrameLayout.MarginLayoutParams(
                                		 FrameLayout.LayoutParams.WRAP_CONTENT,
                                		 FrameLayout.LayoutParams.WRAP_CONTENT));

                		lp.setMargins(x, y, 0, 0);

                		tv2.setLayoutParams(lp);
                                break;
                        default:
                                break;
                }
                return true;
		    }
		});
		
		createTextFrameLayout.addView(tv2, layoutParams); 
		createTextFrameLayout.bringToFront();
		Log.i(TAG, ":D = "+tv2.isShown());
		rlv.bringToFront();  
	}

	//Change color of the font
	@Override
	public void colorChanged(int color) {
		PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(
		        COLOR_PREFERENCE_KEY, color).commit();
    	FontColor = color;
	}
	
}