package com.monicaung.redgreen12;

import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import com.physicaloid.lib.Physicaloid;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity implements CvCameraViewListener2 {

	private Physicaloid physicaloid;
	private TextView textStatus;
	private TextView textStatus2;
	private ImageView imagePope;
	protected static final String TAG = "MainActivity";
	private CameraBridgeViewBase openCvCameraView;
	private Mat mRgba;
	private double redThreshold = 80;
	private double greenThreshold = 5;
	private double darknessThreshold = 80;
	private double brightnessThreshold = 400;
	private int greenPixelCount=0;
	private int redPixelCount=0;
	double[] blackPix = {0,0,0,0};
	private boolean isDetecting = false;
	private boolean isMoving = false;
	private boolean isPopePresent = false;
	private int baseG=200;
	private int baseR=240;
	private double phi=5.67;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //Set up the views
        setContentView(R.layout.main_activity);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
	    openCvCameraView.setVisibility(SurfaceView.VISIBLE);
	    openCvCameraView.setCvCameraViewListener(this);
	    openCvCameraView.setMaxFrameSize(200,  200);
	    imagePope.findViewById(R.id.imagePope);
	    
        textStatus = (TextView) findViewById(R.id.textStatus);
        textStatus2 = (TextView) findViewById(R.id.textStatus2);
        Button buttonMove = (Button) findViewById(R.id.buttonMove);
        buttonMove.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendToArduino("y");
			}
		});
        Button buttonStop = (Button) findViewById(R.id.buttonStop);
        buttonStop.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				sendToArduino("n");
			}
		});
        
        final Button buttonDetecting = (Button) findViewById(R.id.buttonDetecting);
        buttonDetecting.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isDetecting) {
					//Turn on
					isDetecting = true;
					buttonDetecting.setText("TURN OFF");
					
				} else {
					//Turn off
					isDetecting = false;
					buttonDetecting.setText("TURN ON");
				}
			}
		});
        final Button buttonPope = (Button) findViewById(R.id.buttonPope);
        buttonPope.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!isPopePresent) {
					isPopePresent = true;
					imagePope.setVisibility(View.VISIBLE);
					runOnUiThread(new Runnable() {
						@Override
			            public void run() {
							buttonPope.setText("Unpopeify");
			            }
					});
				} else {
					isPopePresent = false;
					imagePope.setVisibility(View.INVISIBLE);
					runOnUiThread(new Runnable() {
						@Override
			            public void run() {
							buttonPope.setText("Popeify");
			            }
					});
				}
			}
		});
        
        //Set up physicaloid
        physicaloid = new Physicaloid(this);
       
	}
	
	//OpenCV library initialization
	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
	    @Override
	    public void onManagerConnected(int status) {
	        switch (status) {
	            case LoaderCallbackInterface.SUCCESS:
	            {
	                Log.i(TAG, "OpenCV loaded successfully");
	                openCvCameraView.enableView();
	            } break;
	            default:
	            {
	                super.onManagerConnected(status);
	            } break;
	        }
	    }
	};
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
    }
	
	@Override
	public void onResume()
	{
	    super.onResume();
	    OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
	}
	
	@Override
	 public void onPause()
	 {
	     super.onPause();
	     if (openCvCameraView != null)
	         openCvCameraView.disableView();
	 }
	@Override
	public void onCameraViewStarted(int width, int height) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onCameraViewStopped() {
		// TODO Auto-generated method stub
		
	}
	
	
	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		if (isDetecting) {
			mRgba = inputFrame.rgba();
			//Loop through each pixel and determine if it's red, green or other
			int maxHeight = (int) (0.9999*mRgba.size().height -1);
			int maxWidth = (int) (0.9999*mRgba.size().width -1);
			for(int i=0; i<maxHeight; i++){
				for(int j=0; j<maxWidth; j++){
					double[] data = mRgba.get(i,j);
					double red = data[0];
					double green = data[1];
					double blue = data[2];
					if(Math.sqrt(Math.pow(blue, 2) + Math.pow(red, 2)) < phi*(green-baseG) || Math.sqrt(Math.pow(blue, 2) + Math.pow(green, 2)) < phi*(red-baseR)){
						if (red > green) {
							redPixelCount++;
						} else{
							greenPixelCount++;
						}
					}
				}
			}
				
			double[] middlePixel = mRgba.get(mRgba.rows()/2, mRgba.cols()/2);
			final String text = "R: " + middlePixel[0] + ", G: " + middlePixel[1] + ", B:" + middlePixel[2] + " \n "
			+ " reds:" + redPixelCount + ", greens:" + greenPixelCount;
			
			runOnUiThread(new Runnable() {
				@Override
	            public void run() {
					textStatus2.setText(text);
	            }
			});
			
			if (greenPixelCount > redPixelCount && greenPixelCount > 10) {
				if (!isMoving) {
					isMoving = true;
					sendToArduino("y");
				}
			} else {
				if (isMoving) {
					isMoving = false;
					sendToArduino("n");
				}
			}
			redPixelCount=0;
			greenPixelCount=0;
		}
		return mRgba;
	}
		
	
	private void sendToArduino(String message) {
		if(physicaloid.open()) {
		    byte[] buf = message.getBytes();
		    physicaloid.write(buf, buf.length);
		    physicaloid.close();
		    if (message.equals("y") && isDetecting) {
		    	runOnUiThread(new Runnable() {
					@Override
		            public void run() {
						textStatus.setText("Move!");
		            }
				});
		    } else if (message.equals("n")) {
		    	runOnUiThread(new Runnable() {
					@Override
		            public void run() {
						textStatus.setText("Stop!");
		            }
				});
		    }
		} else {
			runOnUiThread(new Runnable() {
				@Override
	            public void run() {
					textStatus.setText("Failed to send! :(");
	            }
			});
		}
	}
}
