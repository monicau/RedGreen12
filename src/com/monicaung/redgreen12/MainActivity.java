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
	protected static final String TAG = "MainActivity";
	private CameraBridgeViewBase openCvCameraView;
	private Mat rgba;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
      //Set up the views
        setContentView(R.layout.main_activity);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		openCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
	    openCvCameraView.setVisibility(SurfaceView.VISIBLE);
	    openCvCameraView.setCvCameraViewListener(this);
	     
        textStatus = (TextView) findViewById(R.id.textStatus);
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
		return rgba;
	}
	
	private Mat getHistogram(ArrayList<Mat> matList) {
		int channelArray[] = {0,1,2};
		MatOfInt channels = new MatOfInt(channelArray);
		Mat hist = new Mat();
		MatOfInt histSize = new MatOfInt(256,256,256);
		MatOfFloat ranges = new MatOfFloat(0,0f, 255.0f, 0.0f, 255.0f, 0.0f, 255.0f);
		Imgproc.calcHist(matList, channels, new Mat(), hist, histSize, ranges);
		return hist;
	}
	
	private void sendToArduino(String message) {
		if(physicaloid.open()) {
		    byte[] buf = message.getBytes();
		    physicaloid.write(buf, buf.length);
		    physicaloid.close();
		    if (message.equals("y")) {
		    	textStatus.setText("Move!");
		    } else if (message.equals("n")) {
		    	textStatus.setText("Stop!");
		    }
		} else {
			textStatus.setText("Failed to send! :(");
		}
	}
}
