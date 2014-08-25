package org.po.wol;

import java.util.ArrayList;

import org.po.wol.logic.Logic;
import org.po.wol.state.InputState;
import org.po.wol.state.State.Season;
import org.po.wol.world.Line;
import org.po.wol.world.World;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.SurfaceHolder;

public class WolThread extends Thread {
	private int mCanvasWidth;
	private int mCanvasHeight;
	private ArrayList<dot> Dots = new ArrayList<dot>(); // Dynamic array with
														// dots
	private SurfaceHolder holder;
	private boolean running = false;
	private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final int refresh_rate = 10; // How often we update the screen, in
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
	private int textWidth;

	private Season previousSeason = null;
	private InputState inputState;
	private Logic logic;

	private static final int[] backgrounds = { Color.parseColor("#d3f8ff"),
			Color.parseColor("#a8ff7d"), Color.parseColor("#ead957"),
			Color.parseColor("#cb6463"), };

	private static final int[] blokeColors = { Color.parseColor("#235fd9"),
			Color.parseColor("#55a23a"), Color.parseColor("#818e4f"),
			Color.parseColor("#833124"), };

	public WolThread(SurfaceHolder holder, World world,
			org.po.wol.state.State state, InputState inputState, Logic logic) {
		this.holder = holder;
		this.world = world;
		this.state = state;
		this.inputState = inputState;
		this.logic = logic;
	}

	public synchronized void addDot() {
		int x, y, radius;
		float[] color = new float[3]; // HSV (0..360,0..1,0..1)

		// ADD ONE MORE DOT TO THE SCREEN
		x = 100 + (int) (Math.random() * (mCanvasWidth - 200));
		y = 100 + (int) (Math.random() * (mCanvasHeight - 200));
		radius = 1 + (int) (Math.random() * 99);
		color[0] = (float) (Math.random() * 360);
		color[1] = 1;
		color[2] = 1;
		dot mdot = new dot(x, y, radius, Color.HSVToColor(128, color));
		if (Dots.size() >= 6) Dots.remove(0);
		Dots.add(mdot);

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
		paintDots(canvas);
		paintWorld(canvas);
		paintBloke(canvas);
		paintOverlay(canvas);
	}

	// The actual drawing in the Canvas (not the update to the screen).
	private void paintDots(Canvas canvas) {
		dot temp_dot;
		paint.setStyle(Style.FILL_AND_STROKE);
		for (int i = 0; i < Dots.size(); i++) {
			temp_dot = Dots.get(i);
			paint.setColor(temp_dot.get_color());
			canvas.drawCircle((float) temp_dot.get_x(),
					(float) temp_dot.get_y(), (float) temp_dot.get_radius(),
					paint);
		}
	}

	// /////////// sverik's code

	private void paintBackground(Canvas canvas) {
		canvas.drawColor(Color.LTGRAY);

		final float r = 1500;
		float ox = -(r - w_2);
		float oy = -(r + state.c_r - height);
		float angle = worldToScreenAngle(0);
		for (int c : backgrounds) {
			Paint paint = new Paint();
			paint.setColor(c);
			paint.setStrokeWidth(0.1f);
			paint.setStyle(Paint.Style.FILL);

			canvas.drawArc(new RectF(ox, oy, ox+(r * 2), oy+(r * 2)), angle, 90.1f,
					true, paint);
			angle += 90;
		}
	}

	private void paintWorld(Canvas canvas) {
		for (Line line : world.getLines()) {
			paintLineWithArc(canvas, line, Color.BLACK);
		}
	}

	private void paintLineWithArc(Canvas canvas, Line line, int color) {
		float thickness = Math.abs(line.r1 - line.r0);

		Paint paint = new Paint();
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

		canvas.drawArc(oval, startAngle, arc, false, paint);
	}

	private void paintBloke(Canvas canvas) {
		PointF s = polarToScreen(state.p_a, state.p_r - 3);

		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(blokeColors[state.season.ordinal()]);

		RectF oval = new RectF(s.x - blokeSize, s.y - blokeSize, s.x + blokeSize*2, s.y + blokeSize*2);
		canvas.drawOval(oval, paint);
	}
	
	private void paintOverlay(Canvas canvas) {
		Paint paint = new Paint();
		
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
		double radianAngle = -a * Math.PI / 180d;
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

	private float worldToScreenAngle(float worldAngle) {
		return worldAngle - state.c_a - 90;
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

	private class dot {
		private int x, y, radius, color;

		dot(int x, int y, int radius, int color) {
			this.x = x;
			this.y = y;
			this.radius = radius;
			this.color = color;
		}

		public int get_x() {
			return this.x;
		}

		public int get_y() {
			return this.y;
		}

		public int get_radius() {
			return this.radius;
		}

		public int get_color() {
			return this.color;
		}
	}

}