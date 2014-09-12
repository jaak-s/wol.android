package org.po.wol.state;

public class State {
	/** Camera angle */
	public float c_a = 280;
	/** Camera radius, this radius is at vieport bottom */
	public float c_r = 1400;

	// public float p_a = 170;
	// public float p_r = 119;

	public float p_a = 107;
	public float p_r = 240;

	/** Character's radial velocity */
	public float p_rv = 0;
	/** Character's linear velocity */
	public float p_lv = 0;

	public Season season = Season.SPRING;

	@Override
	public String toString() {
		return "State [c_a=" + c_a + ", c_r=" + c_r + ", p_a=" + p_a + ", p_r="
				+ p_r + ", p_rv=" + p_rv + ", p_lv=" + p_lv + "]";
	}

	public enum Season {
		WINTER, // 0-90
		SPRING, // 90-180
		SUMMER, // 180-270
		AUTUMN; // 270-360
	}
}