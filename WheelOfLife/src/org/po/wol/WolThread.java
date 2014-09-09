package org.po.wol;

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
import android.graphics.PointF;
import android.graphics.RectF;
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

	private Bitmap testBgImage;

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

		testBgImage = BitmapFactory.decodeResource(res, R.drawable.puu);

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

	private void draw(Canvas canvas) {
		paintBackground(canvas);
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

		final float t_a = 123;
		final float t_sa = 5;
		final float t_r = 1285;
		final int t_w = 447;
		final int t_h = 450;
		final int t_w_2 = t_w / 2;
		PointF t_s = polarToScreen(t_a, t_r);
		Matrix tr = new Matrix();
		tr.preRotate((state.c_a - t_a + t_sa), t_s.x, t_s.y);
		t_s.x -= t_w_2;
		t_s.y -= t_h;
		tr.preTranslate(t_s.x, t_s.y);
		tr.preScale((float)t_w / testBgImage.getWidth(), (float)t_h / testBgImage.getHeight());
		canvas.drawBitmap(testBgImage, tr, null);
	}

	private void paintWorld(Canvas canvas) {
		for (Line line : world.getLines()) {
			paintLineWithArc(canvas, line, Color.BLACK);
		}
	}

	private void paintLineWithArc(Canvas canvas, Line line, int color) {
		float thickness = Math.abs(line.r1 - line.r0);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStrokeWidth(thickness);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeCap(Paint.Cap.BUTT);
		paint.setStrokeJoin(Paint.Join.BEVEL);
		paint.setColor(color);

		float r = (line.r0 + line.r1) / 2;

		float ox = -(r - w_2);
		float oy = -(r + state.c_r - height);

		float arc = line.a1 - line.a0;
		if (arc < 0) {
			arc += 360;
		}

		float startAngle = worldToScreenAngle(line.a0);
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
			seasonTextRemaining -= refresh_rate;
			paint.setTextSize(SEASON_TEXT_FONT_SIZE);
			int c = Color.argb((int)Math.min(255, (float)255*seasonTextRemaining / SEASON_TEXT_FADE_START), 0, 0, 0);
			paint.setColor(c);
			paint.setTextAlign(Align.CENTER);

			canvas.drawText(state.season.toString(), width/2, 100, paint);
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
		return - worldAngle + state.c_a + 90;
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

}