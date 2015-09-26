package com.monicaung.redgreen12;

import java.util.ArrayList;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

//import com.physicaloid.lib.Physicaloid;



import com.physicaloid.lib.Physicaloid;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity implements CvCameraViewListener2 {

	private Physicaloid physicaloid;
	private TextView textStatus;
	private TextView textStatus2;
	private TextView TextView1;
	private TextView TextView2;
	
	protected static final String TAG = "MainActivity";
	private CameraBridgeViewBase openCvCameraView;
	private Mat rgba;
	private int intensityThreshold = 250;
	private int baseG = 200;
	private int baseR = 220;
	private double phi = 5.67;
	private int RPC=0;
	private int GPC=0;
	private boolean goingState=false;
	private boolean oldGoingState =false;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
      //Set up the views
        setContentView(R.layout.main_activity);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
		openCvCameraView.setMaxFrameSize(200, 200);
	    openCvCameraView.setVisibility(SurfaceView.VISIBLE);
	    openCvCameraView.setCvCameraViewListener(this);
	     
        textStatus = (TextView) findViewById(R.id.textStatus);
        textStatus2 = (TextView) findViewById(R.id.textStatus2);
    	TextView1 = (TextView) findViewById(R.id.TextView01);
    	TextView2 = (TextView) findViewById(R.id.TextView02);
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
        
        //Set up physicaloid
        physicaloid = new Physicaloid(this);
//      physicaloid.upload(Boards.ARDUINO_UNO, "/storage/emulated/0/Download/Blink.hex");
       
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
		rgba = inputFrame.rgba();
////		Mat histogram = getHistogram(rgba);
////		double[] data = rgba.get(0,0);
		double[] blackPix = {0,0,0,0};


		GPC = 0;
		RPC = 0;
		
		int maxHeight = (int) (0.9999*rgba.size().height -1);
		int maxWidth = (int) (0.9999*rgba.size().width -1);
		for(int i=0; i<maxHeight; i++){
			for(int j=0; j<maxWidth; j++){
				double[] data = rgba.get(i,j);
				if(Math.sqrt(Math.pow(data[0], 2)+ Math.pow(data[2], 2)) < phi* (data[1]-baseG) || Math.sqrt(Math.pow(data[1], 2)+ Math.pow(data[2], 2)) < phi* (data[0]-baseR)){
					if(data[1] > data[0]){
						GPC++;
					}
					else if(data[0] > data[1]){
						RPC++;
					}
				}

			}
		}
		
		
		if(RPC >= GPC || GPC<50){
			goingState = false;
		}
		
		else if(GPC > RPC){
			goingState= true;
		}	
		if(goingState != oldGoingState){
			if(goingState = true)
				sendToArduino("y");
			else sendToArduino("n");
			oldGoingState = goingState;
		}
		
		

		
		final String text = rgba.size().width + " ___" + rgba.size().height;
		final String RPCount = "R = " + Integer.toString(RPC);
		final String GPCount = "G = " + Integer.toString(GPC);
		runOnUiThread(new Runnable() {
			@Override
            public void run() {
				textStatus2.setText(text);
				TextView1.setText(RPCount);
				TextView2.setText(GPCount);
            }
		});
		return rgba;
	}
	
//	private Mat getHistogram(Mat mat) {
//		ArrayList<Mat> matList = new ArrayList<Mat>();
//		matList.add(mat);
//		int channelArray[] = {0,1,2};
////		int channelArray[] = {0};
//		MatOfInt channels = new MatOfInt(channelArray);
//		Mat hist = new Mat();
//		MatOfInt histSize = new MatOfInt(64,64,64);
////		MatOfInt histSize = new MatOfInt(256);
//		MatOfFloat ranges = new MatOfFloat(0.0f, 255.0f, 0.0f, 255.0f, 0.0f, 255.0f);
////		MatOfFloat ranges = new MatOfFloat(0,0f, 255.0f);
//		Imgproc.calcHist(matList, channels, new Mat(), hist, histSize, ranges);
//		return hist;
//	}
	
//	private void sendToArduino(String message) {
//		if(physicaloid.open()) {
//		    byte[] buf = message.getBytes();
//		    physicaloid.write(buf, buf.length);
//		    physicaloid.close();
//		    if (message.equals("y")) {
//		    	textStatus.setText("Move!");
//		    } else if (message.equals("n")) {
//		    	textStatus.setText("Stop!");
//		    }
//		} else {
//			textStatus.setText("Failed to send! :(");
//		}
//	}
	
	private void sendToArduino(String message) {
		if(physicaloid.open()) {
		    byte[] buf = message.getBytes();
		    physicaloid.write(buf, buf.length);
		    physicaloid.close();
		    if (message.equals("y")) {
		    	runOnUiThread(new Runnable() {
					@Override
		            public void run() {
						textStatus.setText(oldGoingState + "_+_" + goingState);
		            }
				});
		    } else if (message.equals("n")) {
		    	runOnUiThread(new Runnable() {
					@Override
		            public void run() {
						textStatus.setText(oldGoingState + "_+_" + goingState);
						//textStatus.setText("Stop!");
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
