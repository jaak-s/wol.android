package org.po.wol;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class DrawingView extends View {

	private WolThread surfaceThread;

	public DrawingView(Context context) {
		super(context);
	}

	public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public DrawingView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		surfaceThread.setSurfaceSize(getWidth(), getHeight());
		surfaceThread.draw(canvas);

		try { Thread.sleep(50); } catch (Exception e) {}
		invalidate();
	}

	public void setDrawer(WolThread surfaceThread) {
		this.surfaceThread = surfaceThread;
	}

}
