package com.ampsoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Calendar;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Color;
import android.opengl.GLES20;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * 
 * @author jhyry
 *
 */
public class ChroBar {
	
	//For drawing the bars on the screen
	private static final int HOURS_IN_DAY = 24;
	private static final int MINUTES_IN_HOUR = 60;
	private static final int SECONDS_IN_MINUTE = 60;
	private static final int MILLIS_IN_SECOND = 1000;
	
	//Bar pixel margin, shared between all bars
	private static float barMargin = 5;
	
	//Sequence of when to draw each vertex
	private static final byte[] vertexSequence = {0, 3, 2, 0, 2, 1};
	
	//Constant arrays of width or height adjustment indexes.
	private static final int[] heightAdjustIndices = {1, 4};
	private static final int[] widthAdjustIndices = {0, 3, 6, 9};

	//OpenGL shader code
	private final String vertexShaderCode =
		    "attribute vec4 vPosition;" +
		    "void main() {" +
		    "  gl_Position = vPosition;" +
		    "}";

	private final String fragmentShaderCode =
		    "precision mediump float;" +
		    "uniform vec4 vColor;" +
		    "void main() {" +
		    "  gl_FragColor = vColor;" +
		    "}";
	
	//Specifies the color of this bar
	private int barColor;
	private int mProgram;
	private int mPositionHandle;
	private boolean usingMillis = false;
	
	//Type of data this represents
	private ChroType barType;
	
	//OpenGL Surface and drawing buffers
	private GL10 surface = null;
	private ByteBuffer rawVertexBuffer, drawDirection;
	private FloatBuffer verticesBuffer;
	
	//Screen size for the current device is
	//found using these objects
	private DisplayMetrics screen = new DisplayMetrics();
	private int mColorHandle;
	private static WindowManager wm;
	
	/**
	 * 
	 * @param t
	 * @param value
	 * @param color
	 */
	public ChroBar(ChroType t, Integer color, Context activityContext) {
		
		barType = t;
		
		int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
	    int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

	    mProgram = GLES20.glCreateProgram();             // create empty OpenGL ES Program
	    GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
	    GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
	    GLES20.glLinkProgram(mProgram);
	    
		if(color != null)
			barColor = color;
		else {
			switch(barType.getType()) {
			
			case 0:
				barColor = Color.BLACK;
				break;
			case 1:
				barColor = Color.CYAN;
				break;
			case 2:
				barColor = Color.GREEN;
				break;
			case 3:
				barColor = Color.MAGENTA;
				break;
				
			default:
				System.err.print("Invalid type!");
			}
		}
		
		System.out.println("CHROBARS-AMPSOFT<" + getTimeString() + ">: Bar color set to " + barColor);
		
		wm = (WindowManager) activityContext.getSystemService(Context.WINDOW_SERVICE);
		
		allocateVerticesBuffer();
		
		//Convert the raw buffer to the correct type
		verticesBuffer = rawVertexBuffer.asFloatBuffer();
		allocateSequenceBuffer();
		
		//According to the time, set the bar height
		adjustBarHeight(barType.getType());
	}
	
	/**
	 * 
	 * @param type
	 * @param shaderCode
	 * @return
	 */
	public int loadShader(int type, String shaderCode) {

	    // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
	    // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
	    int shader = GLES20.glCreateShader(type);

	    // add the source code to the shader and compile it
	    GLES20.glShaderSource(shader, shaderCode);
	    GLES20.glCompileShader(shader);

	    return shader;
	}

	/**
	 * 
	 */
	private void allocateVerticesBuffer() {

		rawVertexBuffer =
				ByteBuffer.allocateDirect(barType.getTypeVertices().length * 4);
		rawVertexBuffer.order(ByteOrder.nativeOrder());
	}

	/**
	 * 
	 * @param screenMetrics
	 */
	private void setBarWidth(DisplayMetrics screenMetrics) {
		
		//Perform bar width calculations
		float[] verts = barType.getTypeVertices();
		int numberOfBars = usingMillis  ? 4 : 3;
		float barWidth = ((float)numberOfBars/(float)screen.widthPixels)/(float)screen.widthPixels;
		barWidth -= (barMargin/screen.widthPixels)*2;
		
		//Set the width of this bar
		verts[0] = verts[9] = ((barMargin/screen.widthPixels) +
				(barWidth * (float)barType.getType())) +
				((barMargin/screen.widthPixels) * barType.getType());
		verts[3] = verts[6] = verts[0] + barWidth;
		
		//Commit width settings
		barType.setTypeVertices(verts, widthAdjustIndices);
	}

	/**
	 * 
	 */
	private void allocateSequenceBuffer() {
		
		drawDirection = ByteBuffer.allocateDirect(vertexSequence.length);
		drawDirection.put(vertexSequence);
		drawDirection.position(0);
	}

	/**
	 * 
	 * @return
	 */
	private String getTimeString() {
		
		return (Calendar.YEAR + "-" + Calendar.MONTH + "-" +
				Calendar.DAY_OF_MONTH + "{" + Calendar.HOUR +
				":" + Calendar.MINUTE + ":" + Calendar.SECOND +
				"." + Calendar.MILLISECOND + "}");
	}

	/**
	 * 
	 * @param type
	 */
	private void adjustBarHeight(int type) {
		
		wm.getDefaultDisplay().getMetrics(screen);
		
		setBarWidth(screen);
		
		float[] verts = barType.getTypeVertices();
		
		switch(type) {
		
		case 0:
			verts[4] = verts[1] = ((float)Calendar.HOUR_OF_DAY/(float)HOURS_IN_DAY);
			break;
		case 1:
			verts[4] = verts[1] = ((float)Calendar.MINUTE/(float)MINUTES_IN_HOUR);
			break;
		case 2:
			verts[4] = verts[1] = ((float)Calendar.SECOND/(float)SECONDS_IN_MINUTE);
			break;
		case 3:
			verts[4] = verts[1] = ((float)Calendar.MILLISECOND/(float)MILLIS_IN_SECOND);
			break;
		
		default:
			System.err.print("Invalid type!");
		}
		
		barType.setTypeVertices(verts, heightAdjustIndices);
		verticesBuffer.clear();
		verticesBuffer.put(barType.getTypeVertices());
		verticesBuffer.position(0);
	}

	/**
	 * 
	 * @param drawSurface
	 */
	public void draw(GL10 drawSurface) {
		
		float[] color = {   (float)Color.red(barColor)/256.0f,
							(float)Color.green(barColor)/256.0f,
							(float)Color.blue(barColor)/256.0f,
							(float)Color.alpha(barColor)/256.0f  };
		
		// Add program to OpenGL ES environment
	    GLES20.glUseProgram(mProgram);

	    // get handle to vertex shader's vPosition member
	    mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
	    
	    // Enable a handle to the triangle vertices
	    GLES20.glEnableVertexAttribArray(mPositionHandle);
	    
	    // Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, 3,
	                                 GLES20.GL_FLOAT, false,
	                                 0, verticesBuffer);

	    // get handle to fragment shader's vColor member
	    mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

	    // Set color for drawing the triangle
	    GLES20.glUniform4fv(mColorHandle, 1, color, 0);

	    // Draw the triangle
	    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 4);

	    // Disable vertex array
	    GLES20.glDisableVertexAttribArray(mPositionHandle);
	    
	    if(surface == null)
			surface = drawSurface;
		
        /*drawSurface.glColor4f((float)Color.red(barColor)/256.0f,
        					  (float)Color.green(barColor)/256.0f,
        					  (float)Color.blue(barColor)/256.0f,
        					  (float)Color.alpha(barColor)/256.0f);
		
		adjustBarHeight(barType.getType());

		//Set up face culling
	    drawSurface.glFrontFace(GL10.GL_CCW);
	    drawSurface.glEnable(GL10.GL_CULL_FACE);
	    drawSurface.glCullFace(GL10.GL_BACK);
		
	    //Enable the OpenGL vertex array buffer space
		drawSurface.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		drawSurface.glVertexPointer(barType.getDimensions(),
									GL10.GL_FLOAT, 0, verticesBuffer);
		//Draw the bar
		drawSurface.glDrawElements(GL10.GL_TRIANGLES, (int) drawDirection.capacity()/3,
									GL10.GL_UNSIGNED_BYTE, drawDirection);
		//Clear the buffer space
		drawSurface.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		
		// Disable face culling.
		drawSurface.glDisable(GL10.GL_CULL_FACE);*/
	}
	
	/**
	 * Changes the color of this bar using ARGB parameters.
	 * <br><br>
	 * If the surface has not yet been drawn, the color will 
	 * be changed on the next redraw.
	 * 
	 * @param alpha
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void changeChroBarColor(int alpha, int red, int green, int blue) {
		
		barColor = Color.argb(alpha, red, green, blue);
		
		if(surface != null)
			draw(surface);
	}
	
	/**
	 * Changes the color of this bar using a formatted string.
	 * <br><br>
	 * If the surface has not yet been drawn, the color will 
	 * be changed on the next redraw.
	 * 
	 * @param colorstring format of #RRGGBB or #AARRGGBB
	 * 
	 * @see android.graphics.Color#parseColor(String)
	 */
	public void changeChroBarColor(String colorstring) {
		
		barColor = Color.parseColor(colorstring);

		if(surface != null)
			draw(surface);
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		
		return "ChroBar Object " + this.hashCode() + "\nType:\n" + barType
				+ "\nColor: " + barColor;
	}
}
