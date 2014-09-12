package org.po.wol;

import android.graphics.Bitmap;

public class FixedBitmap {
	private final float a;
	private final float r;
	private final float rot;
	private final float width;
	private final float height;

	private final Bitmap bitmap;

	public FixedBitmap(float a, float r, float rot, Bitmap bitmap, float width, float height) {
		this.a = a;
		this.r = r;
		this.rot = rot;
		this.bitmap = bitmap;
		this.width = width;
		this.height = height;
	}

	/**
	 * @return apparent width of the bitmap in the world
	 */
	public float getWidth() {
		return width;
	}

	/**
	 * @return apparent height of the bitmap in the world
	 */
	public float getHeight() {
		return height;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	/**
	 * @return angular position of the middle of the bitmap in regard to world angle 0
	 */
	public float getA() {
		return a;
	}

	/**
	 * @return distance of the bottom of the bitmap from world center
	 */
	public float getR() {
		return r;
	}

	/**
	 * @return rotation of the bitmap in regard to (a,r)
	 */
	public float getRot() {
		return rot;
	}
}
