package org.po.wol.logic;

//import org.po.wheeloflife.state.InputState;
import org.po.wol.state.InputState;
import org.po.wol.state.State;
import org.po.wol.state.State.Season;
import org.po.wol.world.Line;
import org.po.wol.world.World;

import android.graphics.PointF;

public class Logic {
	private static class SeasonParams {
		public final float userAcceleration;
		public final float frictionDeceleration;
		public final float jumpAcceleration;
		public final float maxLinearVelocity;

		public SeasonParams(float userAcceleration, float frictionDeceleration,
				float jumpAcceleration, float maxLinearVelocity) {
			this.userAcceleration = userAcceleration;
			this.frictionDeceleration = frictionDeceleration;
			this.jumpAcceleration = jumpAcceleration;
			this.maxLinearVelocity = maxLinearVelocity;
		}
	}

	private static final SeasonParams[] params = {
			new SeasonParams(0.6f, 0.4f, -0.30f, 10f), // winter
			new SeasonParams(2.8f, 1.2f, -0.30f, 8f), // spring
			new SeasonParams(3f, 1f, -0.32f, 12f), // summer
			new SeasonParams(3f, 1f, -0.30f, 10f) // autumn
	};

	private static final float FALL_ACCELERATION = 0.03f;
	private static final float MAX_FALL_VELOCITY = 8f;
	private static final float BLOKE_FROM_CAMERA_BOTTOM = 120;
	private static final float TOUCHING_GROUND_THRESHOLD = 0.005f;
	private static final float AIR_CONTROL_MULTIPLIER = 0.3f;

	private State state;
	private World world;
	private InputState input;

	private PointF d = new PointF();
	private boolean jumping = false;
	private SeasonParams sParams;

	public Logic(State state, InputState inputState, World world) {
		this.state = state;
		this.world = world;
		this.input = inputState;
	}

	public void step(long delta) {
		updateSeason();

		input(delta);

		physics(delta);

		if (!validLocation(d)) {
			state.p_rv = 0;
		}

		state.p_a += d.x;
		state.p_r += d.y;

		if (state.p_a >= 360) {
			state.p_a -= 360;
		}
		if (state.p_a < 0) {
			state.p_a += 360;
		}
		d.x = 0;
		d.y = 0;

		updateCamera();

	}

	private void updateSeason() {
		int seasonIndex = (int) (state.p_a / 90);
		while (seasonIndex < 0) {
			seasonIndex += Season.values().length;
		}
		seasonIndex = seasonIndex % Season.values().length;
		state.season = Season.values()[seasonIndex];
		sParams = params[seasonIndex];
	}

	private void physics(long delta) {
		state.p_rv += FALL_ACCELERATION;
		if (state.p_rv > MAX_FALL_VELOCITY) {
			state.p_rv = MAX_FALL_VELOCITY;
		}
		d.y = delta * state.p_rv;

		if (!jumping) {
			state.p_lv -= Math.signum(state.p_lv)
					* sParams.frictionDeceleration;
		}
		d.x = delta * state.p_lv / (state.p_r + d.y);
	}

	private boolean validLocation(PointF d) {
		boolean collisionFree = true;
		float p_a = state.p_a + d.x;
		float p_r = state.p_r + d.y;
		float groundDistance = Float.MAX_VALUE;
		if (p_a >= 360) {
			p_a -= 360;
		}
		if (p_a < 0) {
			p_a += 360;
		}
		for (Line line : world.getLines()) {
			if (line.a0 > p_a) {
				continue;
			}
			if (line.a1 < p_a) {
				continue;
			}

			groundDistance = Math.min(groundDistance, Math.abs(line.r0 - p_r));

			if (line.r0 > p_r) {
				continue;
			}
			if (line.r1 < p_r) {
				continue;
			}

			// collision detected
			collisionFree = false;

			// how far can we go?
			if (d.x > 0 && line.a0 >= state.p_a && line.a0 - state.p_a < d.x) {
				d.x = line.a0 - state.p_a;
				p_a = state.p_a + d.x;
			} else if (d.x < 0 && line.a1 <= state.p_a
					&& line.a1 - state.p_a > d.x) {
				d.x = line.a1 - state.p_a;
				p_a = state.p_a + d.x;
			}

			if (d.y > 0 && line.r0 >= state.p_r && line.r0 - state.p_r < d.y) {
				d.y = line.r0 - state.p_r;
				p_r = state.p_r + d.y;
			} else if (d.y < 0 && line.r1 <= state.p_r
					&& line.r1 - state.p_r > d.y) {
				d.y = line.r1 - state.p_r;
				p_r = state.p_r + d.y;
			}
			groundDistance = Math.min(groundDistance, Math.abs(line.r0 - p_r));
		}
		if (groundDistance < TOUCHING_GROUND_THRESHOLD) {
			jumping = false;
		}
		return collisionFree;
	}

	private void updateCamera() {
		float d_a = (state.p_a - state.c_a);
		if (Math.abs(d_a) > 180) {
			d_a -= Math.signum(d_a) * 360;
		}
		state.c_a += d_a * 0.1;

		float d_r = (state.p_r + BLOKE_FROM_CAMERA_BOTTOM - state.c_r);
		state.c_r += d_r * 0.1;

		if (state.c_a >= 360) {
			state.c_a -= 360;
		}
		if (state.c_a < 0) {
			state.c_a += 360;
		}
	}

	private void input(long delta) {
		// debug
		// if (input.down) {
		// state.p_r -= 50;
		// }

		if (input.up && !jumping) {
			jumping = true;
			state.p_rv = sParams.jumpAcceleration;
		}

		float controlMultiplier = (jumping ? AIR_CONTROL_MULTIPLIER : 1f);
		if (input.right) {
			state.p_lv += sParams.userAcceleration * controlMultiplier;
			if (state.p_lv > sParams.maxLinearVelocity) {
				state.p_lv = sParams.maxLinearVelocity;
			}
		} else if (input.left) {
			state.p_lv -= sParams.userAcceleration * controlMultiplier;
			if (state.p_lv < -sParams.maxLinearVelocity) {
				state.p_lv = -sParams.maxLinearVelocity;
			}
		}

	}
}