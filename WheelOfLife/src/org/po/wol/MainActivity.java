package org.po.wol;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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

		paint.setColor(Color.BLUE);
		paint.setStrokeWidth(10);
		canvas.drawLine(10, 10, 510, 510, paint);
		paint.setColor(Color.RED);
		canvas.drawLine(10, 510, 510, 10, paint);
		canvas.drawLine(20, 0, 20, 80, paint);
		paint.setColor(Color.GREEN);
		canvas.drawRect(new Rect(0, 0, 10, 10), paint);

		wgl = new WolGlRenderer(2, 2, new Bitmap[]{bi});
		glSurfaceView.setRenderer(wgl);

		set_dots();

//		final Bitmap puu = BitmapFactory.decodeResource(getResources(), R.drawable.puu);

		new Thread(){@Override
		public void run() {
			try {sleep(1000);} catch (Exception e) {}
			int size = 3072;
			Bitmap worldBm = dots_screen_view.getWorldAsBitmap(3072);

			Bitmap nw = Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888);
			Canvas nw_c = new Canvas(nw);
			nw_c.drawBitmap(worldBm, new Rect(0, 0, size / 2, size / 2), new Rect(0, 0, size / 2, size / 2), null);

			Bitmap ne = Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888);
			Canvas ne_c = new Canvas(ne);
			ne_c.drawBitmap(worldBm, new Rect(size / 2, 0, size, size / 2), new Rect(0, 0, size / 2, size / 2), null);

			Bitmap sw = Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888);
			Canvas sw_c = new Canvas(sw);
			sw_c.drawBitmap(worldBm, new Rect(0, size / 2, size / 2, size), new Rect(0, 0, size / 2, size / 2), null);

			Bitmap se = Bitmap.createBitmap(size / 2, size / 2, Bitmap.Config.ARGB_8888);
			Canvas se_c = new Canvas(se);
			se_c.drawBitmap(worldBm, new Rect(size / 2, size / 2, size, size), new Rect(0, 0, size / 2, size / 2), null);

			wgl.setBitmap( new Bitmap[]{nw, ne, sw, se} );
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