package org.po.wol;

import java.util.ArrayList;
import java.util.List;

import org.po.wheeloflife.R;
import org.po.wol.logic.Logic;
import org.po.wol.state.InputState;
import org.po.wol.state.State.Season;
import org.po.wol.world.Line;
import org.po.wol.world.World;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;

public class WolThread extends Thread {
	private int mCanvasWidth;
	private int mCanvasHeight;
	private SurfaceHolder holder;
	private boolean running = false;
	private final int refresh_rate = 20; // How often we update the screen, in
											// ms

	private World world;
	private org.po.wol.state.State state;

	private static final long SEASON_TEXT_DURATION = 2800;
	private static final float SEASON_TEXT_FADE_START = 2000f;
	private static final int SEASON_TEXT_FONT_SIZE = 50;
	// private static final Font SEASON_TEXT_FONT = new Font("SansSerif",
	// Font.BOLD, 50);

	private int width = 0;
	private int height = 0;
	private int w_2 = 0;

	private float blokeSize = 5;

	//private long delta;
	private long seasonTextRemaining;

	private Season previousSeason = null;
	private InputState inputState;
	private Logic logic;
	private Resources res;

	private List<FixedBitmap> bitmaps;

	private static final int[] backgrounds = { Color.parseColor("#d3f8ff"),
			Color.parseColor("#a8ff7d"), Color.parseColor("#ead957"),
			Color.parseColor("#cb6463"), };

	private static final int[] blokeColors = { Color.parseColor("#235fd9"),
			Color.parseColor("#55a23a"), Color.parseColor("#818e4f"),
			Color.parseColor("#833124"), };

	public WolThread(SurfaceHolder holder, World world,
			org.po.wol.state.State state, InputState inputState, Logic logic, Resources res) {
		this.holder = holder;
		this.world = world;
		this.state = state;
		this.inputState = inputState;
		this.logic = logic;
		this.res = res;
	}

	@Override
	public void run() {
		long previousTime, currentTime;
		previousTime = System.currentTimeMillis();
		Canvas canvas = null;

		long previous;
		long start = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		long delta = 0;

		bitmaps = new ArrayList<FixedBitmap>();
		bitmaps.add(new FixedBitmap(123, 1285, 5, BitmapFactory.decodeResource(res, R.drawable.puu), 447, 450));

//		bitmaps.add(paintWorldToImage());

		while (running) {
			// Look if time has past
			currentTime = System.currentTimeMillis();
			while ((currentTime - previousTime) < refresh_rate) {
				currentTime = System.currentTimeMillis();
			}
			previousTime = currentTime;
			previous = start;
			start = System.currentTimeMillis();
//			delta = start - previous;
			delta = refresh_rate;

			// GAME
			if (inputState.reloading) {
				world.load();
			}

			logic.step(delta);
			logic.step(delta);

			// PAINT
			try {
				canvas = holder.lockCanvas();
				synchronized (holder) {
					draw(canvas);
				}
			} finally {
				if (canvas != null) {
					holder.unlockCanvasAndPost(canvas);
				}
			}
			// WAIT
			try {
				Thread.sleep(refresh_rate - 2); // Wait some time till I need to
												// display again
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private FixedBitmap paintWorldToImage() {
		int size = 512;
		Bitmap bm = getWorldAsBitmap( size );

		return new FixedBitmap(0, size / 2, 0, bm, size, size);
	}

	public Bitmap getWorldAsBitmap(int size) {
		Bitmap bm = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bm);
		Paint paint = new Paint();
		paint.setColor(Color.CYAN);
		paint.setStyle(Style.FILL);
		canvas.drawRect(0, 0, size, size, paint);

		paintWorld(canvas, size, size, 0f, size / 2f, world.getLines());

		return bm;

	}

	public void draw(Canvas canvas) {
		paintBackground(canvas);
		paintBitmaps(canvas);
		paintWorld(canvas);
		paintBloke(canvas);
		paintOverlay(canvas);
	}

	// /////////// sverik's code

	private void paintBackground(Canvas canvas) {
		canvas.drawColor(Color.LTGRAY);

		final float r = 1500;
		float ox = -(r - w_2);
		float oy = -(r + state.c_r - height);
		float angle = worldToScreenAngle(0);
		for (int c : backgrounds) {
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setColor(c);
			paint.setStrokeWidth(0.1f);
			paint.setStyle(Paint.Style.FILL);

			canvas.drawArc(new RectF(ox, oy, ox+(r * 2), oy+(r * 2)), angle, -90.1f,
					true, paint);
			angle -= 90;
		}

	}

	private void paintBitmaps(Canvas canvas) {
		for (FixedBitmap fb : bitmaps) {
			long start = System.currentTimeMillis();
			PointF t_s = polarToScreen(fb.getA(), fb.getR());
			Matrix tr = new Matrix();
			tr.preRotate((state.c_a - fb.getA() + fb.getRot()), t_s.x, t_s.y);
			t_s.x -= fb.getWidth() / 2f;
			t_s.y -= fb.getHeight();
			tr.preTranslate(t_s.x, t_s.y);
			tr.preScale(fb.getWidth() / fb.getBitmap().getWidth(), fb.getHeight() / fb.getBitmap().getHeight());
			canvas.drawBitmap(fb.getBitmap(), tr, null);
			long end = System.currentTimeMillis();
			Log.i("time", "" + (end - start) + " ms");
			Log.i("time", "hw:" + canvas.isHardwareAccelerated());
		}
	}

	private void paintWorld(Canvas canvas) {
		paintWorld(canvas, width, height, state.c_a, state.c_r, world.getLines());
	}

	private static void paintWorld(Canvas canvas, int width, int height, float c_a, float c_r, Iterable<Line> lines) {
		for (Line line : lines) {
			paintLineWithArc(canvas, width, height, c_a, c_r, line);
		}
	}

	private static void paintLineWithArc(Canvas canvas, int width, int height, float c_a, float c_r, Line line) {
		float thickness = Math.abs(line.r1 - line.r0);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(thickness);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeCap(Paint.Cap.BUTT);
		paint.setStrokeJoin(Paint.Join.BEVEL);
		paint.setColor(Color.BLACK);

		float r = (line.r0 + line.r1) / 2;

		float ox = -(r - width / 2);
		float oy = -(r + c_r - height);

		float arc = line.a1 - line.a0;
		if (arc < 0) {
			arc += 360;
		}

		float startAngle = worldToScreenAngle(line.a0, c_a);
		RectF oval = new RectF(ox, oy, ox+(r * 2), oy+(r * 2));

		canvas.drawArc(oval, startAngle, -arc, false, paint);
	}

	private void paintBloke(Canvas canvas) {
		PointF s = polarToScreen(state.p_a, state.p_r - 3);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(blokeColors[state.season.ordinal()]);

		RectF oval = new RectF(s.x - blokeSize, s.y - blokeSize, s.x + blokeSize*2, s.y + blokeSize*2);
		canvas.drawOval(oval, paint);
	}

	private void paintOverlay(Canvas canvas) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		if (previousSeason != state.season) {
			seasonTextRemaining = SEASON_TEXT_DURATION;
		}
		previousSeason = state.season;

		if (seasonTextRemaining > 0) {
			paint.setTextSize(SEASON_TEXT_FONT_SIZE);
			int c = Color.argb((int)Math.min(255, (float)255*seasonTextRemaining / SEASON_TEXT_FADE_START), 0, 0, 0);
			paint.setColor(c);
			paint.setTextAlign(Align.CENTER);

			canvas.drawText(state.season.toString(), width/2, 100, paint);
			seasonTextRemaining -= refresh_rate;
		}
	}


	private static PointF polarToCart(float a, float r) {
		return polarToCart(a, r, null);
	}

	private static PointF polarToCart(float a, float r, PointF out) {
		if (out == null) {
			out = new PointF();
		}
		double radianAngle = a * Math.PI / 180d;
		out.x = (float) (r * Math.cos(radianAngle));
		out.y = (float) (r * Math.sin(radianAngle));

		return out;
	}

	private PointF polarToScreen(float a, float r) {
		return polarToScreen(worldToScreenAngle(a), r, null);
	}

	private PointF polarToScreen(float a, float r, PointF out) {
		if (out == null) {
			out = new PointF();
		}

		polarToCart(a, r, out);

		out.x += w_2;
		out.y += height - state.c_r;

		return out;
	}

	float worldToScreenAngle(float worldAngle) {
		return worldToScreenAngle(worldAngle, state.c_a);
	}

	static float worldToScreenAngle(float worldAngle, float c_a) {
		return - worldAngle + c_a + 90;
	}

	// /////////// end of sverik's code

	public void setRunning(boolean b) {
		running = b;
	}

	public int getmCanvasHeight() {
		return mCanvasHeight;
	}

	public int getmCanvasWidth() {
		return mCanvasWidth;
	}

	public void setSurfaceSize(int width, int height) {
		synchronized (holder) {
			mCanvasWidth = width;
			mCanvasHeight = height;
			this.width = mCanvasWidth;
			this.height = mCanvasHeight;
			this.w_2 = width / 2;

		}
	}

	public void setHolder(SurfaceHolder holder) {
		this.holder = holder;
	}

}