package org.po.wol;

import org.po.wheeloflife.R;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;

public class MainActivity extends Activity {
	WolSurface dots_screen_view;

	GLSurfaceView glSurfaceView;

	WolGlRenderer wgl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		glSurfaceView = new GLSurfaceView(this);

		Bitmap bi = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bi);

		Paint paint = new Paint();
		paint.setColor(Color.YELLOW);
		canvas.drawRect(0, 0, 512, 512, paint);

		wgl = new WolGlRenderer(bi);
		glSurfaceView.setRenderer(wgl);

		set_dots();

		final Bitmap puu = BitmapFactory.decodeResource(getResources(), R.drawable.puu);
		new Thread(){@Override
		public void run() {
			try {sleep(4000);} catch (Exception e) {}
			wgl.setBitmap(puu);
		};}.start();
	}

	@Override
	public void onPause() {
		super.onPause();
		glSurfaceView.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		glSurfaceView.onResume();
	}

	void set_dots() {
		dots_screen_view = new WolSurface(this);
//		setContentView(dots_screen_view);
		dots_screen_view.setOnTouchListener(dots_screen_view);
		setContentView(glSurfaceView);
	}
}