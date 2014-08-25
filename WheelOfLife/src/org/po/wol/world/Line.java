package org.po.wol.world;

public class Line {
	public final float a0;
	public final float a1;
	public final float r0;
	public final float r1;

	public Line(float a0, float a1, float r0, float r1) {
		this.a0 = a0;
		this.a1 = a1;
		this.r0 = Math.min(r0, r1);
		this.r1 = Math.max(r0, r1);
	}
}