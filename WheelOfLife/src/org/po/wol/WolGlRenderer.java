package org.po.wol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.graphics.Bitmap;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.util.Log;

public class WolGlRenderer implements Renderer {

	private FloatBuffer[] vertexBuffer;
	private FloatBuffer textureBuffer;

	private static final float[] texture = {
			// Mapping coordinates for the vertices
			0.0f, 1.0f,		// top left		(V2)
			0.0f, 0.0f,		// bottom left	(V1)
			1.0f, 1.0f,		// top right	(V4)
			1.0f, 0.0f,		// bottom right	(V3)
	};

	/*
	 * V2 V4
	 * V1 V3
	 */
	private static final float[] vertices_reference = {
			0.0f, 0.0f, // V1 - b bottom right
			0.0f, 1.0f, // V2 - b top right
			1.0f, 0.0f, // V3 - b bottom left
			1.0f, 1.0f, // V4 - b top left
	};

	private int[] textures;

	private Bitmap[] bitmaps;

	public WolGlRenderer(int gridW, int gridH, Bitmap[] bitmaps) {
		Log.i("wgl", "init");
		this.bitmaps = bitmaps;

		vertexBuffer = new FloatBuffer[gridW * gridH];
		textures = new int[vertexBuffer.length];

		float[] vertices = new float[vertices_reference.length];

		int gi = 0;
		for (int gy = gridH / 2 - 1 ; gy >= -gridH / 2 ; gy--) {
			for (int gx = -gridW / 2 ; gx < gridW / 2 ; gx++) {
				fillWithGridVertices(vertices, gx, gy);

				// a float has 4 bytes so we allocate for each coordinate 4 bytes
				ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
				byteBuffer.order(ByteOrder.nativeOrder());

				// allocates the memory from the byte buffer
				vertexBuffer[gi] = byteBuffer.asFloatBuffer();

				// fill the vertexBuffer with the vertices
				vertexBuffer[gi].put(vertices);

				// set the cursor position to the beginning of the buffer
				vertexBuffer[gi].position(0);

				gi++;
			}
		}


		ByteBuffer byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuffer.asFloatBuffer();
		textureBuffer.put(texture);
		textureBuffer.position(0);
	}

	private void fillWithGridVertices(float[] vertices, int gx, int gy) {
		for (int i = 0 ; i < vertices_reference.length ; i++) {
			vertices[i] = vertices_reference[i] + ((i % 2) == 0 ? gx : gy);
		}
	}

	public void setBitmap(Bitmap[] bitmaps) {
		this.bitmaps = bitmaps;
	}

	public void loadGlTexture(GL10 gl) {
		Log.i("wgl", "load");
		gl.glGenTextures(textures.length, textures, 0);
		for (int ti = 0 ; ti < textures.length ; ti++) {
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[ti]);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
			gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);

			int bitmapsIndex = Math.min(ti, bitmaps.length - 1);
			GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmaps[bitmapsIndex], 0);
			if (bitmapsIndex < bitmaps.length - 1 || textures.length - 1 <= ti) {
				bitmaps[bitmapsIndex].recycle();
			}
		}

	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		gl.glEnable(GL10.GL_TEXTURE_2D);			//Enable Texture Mapping ( NEW )
		gl.glShadeModel(GL10.GL_SMOOTH); 			//Enable Smooth Shading
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); 	//Black Background
		gl.glClearDepthf(1.0f); 					//Depth Buffer Setup
		gl.glEnable(GL10.GL_DEPTH_TEST); 			//Enables Depth Testing
		gl.glDepthFunc(GL10.GL_LEQUAL); 			//The Type Of Depth Testing To Do

		//Really Nice Perspective Calculations
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		Log.i("wgl", "changed w=" + width + ", h=" + height);
		if(height == 0) { 						//Prevent A Divide By Zero By
			height = 1; 						//Making Height Equal One
		}

		gl.glViewport(0, 0, width, height); 	//Reset The Current Viewport
		gl.glMatrixMode(GL10.GL_PROJECTION); 	//Select The Projection Matrix
		gl.glLoadIdentity(); 					//Reset The Projection Matrix

		//Calculate The Aspect Ratio Of The Window
		GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW); 	//Select The Modelview Matrix
		gl.glLoadIdentity(); 					//Reset The Modelview Matrix
	}

	float c_x = 0.0f;
	float x_direction = 1.0f;
	int count = 2;
	private static final float C_X_CHANGE = 0.08f;
	private static final float MAX_X_ABS = 200f;

	@Override
	public void onDrawFrame(GL10 gl) {
		if (bitmaps != null) {

			loadGlTexture(gl);

			bitmaps = null;
		}
		// clear Screen and Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Reset the Modelview Matrix
		gl.glLoadIdentity();

		// Drawing
		c_x += x_direction * C_X_CHANGE * (0.2f + MAX_X_ABS - Math.abs(c_x));
		if (Math.abs(c_x) >= MAX_X_ABS) {
			x_direction = -1f * Math.signum(c_x);
			count++;
//			if (count > vertices.length / 2) {
//				count = 2;
//			}
//			count = 8;
			Log.i("count", "count=" + count);
		}
		// -10 ... 10 -> -0.3 ... -5
		final float MIN_DEPTH = -0.4f;
		final float MAX_DEPTH = -5.0f;
		float depth = (c_x + MAX_X_ABS) / (MAX_X_ABS * 2) * (MAX_DEPTH - MIN_DEPTH) + MIN_DEPTH;
		gl.glTranslatef(0.0f, -0.3f, depth);		// move 2 units INTO the screen
												// is the same as moving the camera 5 units away
		gl.glRotatef(c_x, 0.0f, 0.0f, 1.0f);

		for (int gi = 0 ; gi < textures.length ; gi++) {
			gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[gi]);

			gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			gl.glFrontFace(GL10.GL_CW);

			gl.glVertexPointer(2, GL10.GL_FLOAT, 0, vertexBuffer[gi]);
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);

			gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, 4);

			gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}

	}

}
