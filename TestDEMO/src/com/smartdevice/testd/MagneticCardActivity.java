package com.smartdevice.testd;


import com.smartdevicesdk.stripcard.Stripcardhelper;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MagneticCardActivity extends Activity {
	protected static final String TAG = "MainActivity";

	Button btn_check, btn_readdata;
	EditText editText1;
	
	int cardLocation=0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_magneticcard);

		editText1 = (EditText) findViewById(R.id.editText1);
		
		btn_check = (Button) findViewById(R.id.btn_check);
		btn_check.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {				
				byte[] buffer=Stripcardhelper.ReadCard();
				byte[] track1=new byte[100];
				byte[] track2=new byte[140];
				byte[] track3=new byte[140];
				
				System.arraycopy(buffer, 0, track1, 0, track1.length);
				System.arraycopy(buffer, 100, track2, 0, track1.length); 
				System.arraycopy(buffer, 240, track3, 0, track1.length);
				
				String str="";
				str="Track1:"+new String(track1,0,track1.length);
				str+="\r\n"+"Track2:"+new String(track2,0,track2.length);
				str+="\r\n"+"Track3:"+new String(track3,0,track3.length);
				
				editText1.setText(str);
			}
		});
	}
}
