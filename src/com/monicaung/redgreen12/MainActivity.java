package com.monicaung.redgreen12;

import com.physicaloid.lib.Physicaloid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

	private Physicaloid physicaloid;
	TextView textStatus;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        
        //Set up the views
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
	@Override
    protected void onDestroy() {
        super.onDestroy();
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
