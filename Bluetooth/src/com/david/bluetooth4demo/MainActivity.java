package com.david.bluetooth4demo;


//import com.example.xiaomi.R.layout;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
	
	public class MainActivity extends Activity implements OnGestureListener {
	
		private Button but = null;
		
		private GestureDetector gestureScanner;
		@Override
	
	protected void onCreate(Bundle savedInstanceState) {
	// TODO Auto-generated method stub
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_main);
	gestureScanner = new GestureDetector(this);
	but = (Button)findViewById(R.id.serial);
	but.setOnClickListener(new ButtonListener1());
		}
		   private class ButtonListener1 implements OnClickListener, android.view.View.OnClickListener{
				@Override
				public void onClick(View arg0) {
					// TODO Auto-generated method stub
					Intent intent2 = new Intent();
		    		intent2.setClass(MainActivity.this, Version.class);
		    		startActivity(intent2);
				}

				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					// TODO Auto-generated method stub
					
				}
		    }
		@Override
		public boolean onDown(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (e1.getX() != e2.getX()) {
				startActivity(new Intent(this, Activity3.class)); 
				//overridePendingTransition(R.anim.hold, R.anim.close); 
			}
			else
			{
				return false;
				
			}
			return true;
		}
		public boolean onTouchEvent(MotionEvent event) {
			return gestureScanner.onTouchEvent(event);
		}
		@Override
		public void onLongPress(MotionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public void onShowPress(MotionEvent arg0) {
			// TODO Auto-generated method stub
			
		}
		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			// TODO Auto-generated method stub
			return false;
		}

		
}
