package info.staticfree.android.robotfindskitten;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

public class Intro extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		startActivity(new Intent(this, robotfindskitten.class));
		return true;
	}
}
