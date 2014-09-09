package org.po.wol;

import org.po.wol.logic.Logic;
import org.po.wol.state.InputState;
import org.po.wol.state.State;
import org.po.wol.world.World;

import android.content.Context;
import android.graphics.PointF;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

// We extend SurfaceView. Internally (private) SurfaceView creates an object SurfaceHolder
// effectively defining the methods of the SurfaceHolder interface. Notice that it does
// not create a new class or anything, it just defines it right there. When we extend
// the SurfaceView with the SurfaceHolder.Callback interface, we need to add in that extension
// the methods of that interface.

public class WolSurface extends SurfaceView implements SurfaceHolder.Callback,
		OnTouchListener {
	private SurfaceHolder holder; // This is no instantiation. Just saying that
									// the holder
									// will be of a class implementing
									// SurfaceHolder
	private WolThread surfaceThread;// The thread that displays the dots

	private SparseArray<PointF> mActivePointers;

	// WheelOfLife stuff
	private State state;
	private World world;
	private Logic logic;
	private InputState inputState;

	public WolSurface(Context context) {
		super(context);
		mActivePointers = new SparseArray<PointF>();
		holder = getHolder(); // Holder is now the internal/private
								// mSurfaceHolder inherit
								// from the SurfaceView class, which is from an
								// anonymous
								// class implementing SurfaceHolder interface.
		holder.addCallback(this);
		state = new State();
		world = new World(getResources());
		world.load();
		inputState = new InputState();
		logic = new Logic(state, inputState, world);
	}

	private boolean isLeft(float x, float width) {
		return x < width / 2 - 10;
	}

	private boolean isRight(float x, float width) {
		return x > width / 2 + 10;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// get pointer index from the event object
		int pointerIndex = event.getActionIndex();

		// get pointer ID
		int pointerId = event.getPointerId(pointerIndex);

		// get masked (not specific to a pointer) action
		int maskedAction = event.getActionMasked();

		switch (maskedAction) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN: {
			// We have a new pointer. Lets add it to the list of pointers

			PointF f = new PointF();
			f.x = event.getX(pointerIndex);
			f.y = event.getY(pointerIndex);
			mActivePointers.put(pointerId, f);

			// Getting the X-Coordinate of the touched position
			float width = surfaceThread.getmCanvasWidth();
			if (isLeft(f.x, width)) {
				if (inputState.right) {
					inputState.up = true;
				} else {
					inputState.left = true;
				}
			} else if (isRight(f.x, width)) {
				// for fun and debugging:
				if (inputState.left) {
					inputState.up = true;
				} else {
					inputState.right = true;
				}
			}

			break;
		}

		case MotionEvent.ACTION_MOVE: { // a pointer was moved
			for (int size = event.getPointerCount(), i = 0; i < size; i++) {
				PointF point = mActivePointers.get(event.getPointerId(i));
				if (point != null) {
					point.x = event.getX(i);
					point.y = event.getY(i);
				}
			}
			break;
		}

		case MotionEvent.ACTION_UP:
	    case MotionEvent.ACTION_POINTER_UP:
	    case MotionEvent.ACTION_CANCEL: {
	    	PointF f = mActivePointers.get(pointerId);
	    	mActivePointers.remove(pointerId);
			inputState.up = false;
			float width = surfaceThread.getmCanvasWidth();

			if (isLeft(f.x, width)) {
				inputState.left = false;
			} else if (isRight(f.x, width)) {
				inputState.right = false;
			}
			break;
		}
		}
		return true;
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	}

	@Override
	// This is always called at least once, after surfaceCreated
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		if (surfaceThread == null) {
			surfaceThread = new WolThread(holder, world, state, inputState,
					logic, getResources());
			surfaceThread.setRunning(true);
			surfaceThread.setSurfaceSize(width, height);
			surfaceThread.start();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		boolean retry = true;
		surfaceThread.setRunning(false);
		while (retry) {
			try {
				surfaceThread.join();
				retry = false;
			} catch (InterruptedException e) {
			}
		}
	}

	public Thread getThread() {
		return surfaceThread;
	}
}