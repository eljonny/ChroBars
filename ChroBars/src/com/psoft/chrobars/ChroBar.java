package com.psoft.chrobars;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Color;
import android.util.SparseArray;

import com.psoft.chrobars.activities.ChroBarsActivity;
import com.psoft.chrobars.data.ChroData;
import com.psoft.chrobars.opengl.BarsRenderer;
import com.psoft.chrobars.opengl.ChroSurface;
import com.psoft.chrobars.opengl.ChroTexture;
import com.psoft.chrobars.util.ChroPrint;
import com.psoft.chrobars.util.ChroUtilities;

/**
 * 
 * @author Jonathan Hyry
 */
public abstract class ChroBar implements IChroBar {

	/* Begin static fields */
	
	protected static ChroData barsData = null;
	protected static BarsRenderer renderer;
	protected static GL10 surface = null;
	//Used in determining bar height
	protected static Calendar currentTime;
	
	/* End static fields, Begin instance variables */

	//Whether this bar should be drawn
	protected boolean drawBar, drawNumber, wireframe;
	//The bar and edge color is stored as a packed color int
	protected int barColor, edgeColor;
	//Bar width instance variable, updated at every recalculation.
	//This is the actual width of the bar on screen and does not include margins.
	protected float barWidth;
	//Whether this bar has edge colors that should not be operated on.
	protected boolean[] noColorOp = new boolean[3];
	protected float[] barVertexColors, barVertices;
	protected float[] edgeVertexColors, normals;
	protected float[] textureVertices;
	//OpenGL Surface and drawing buffers
	protected ShortBuffer barDrawSequenceBuffer, edgeDrawSequenceBuffer;
	protected ShortBuffer textureDrawSequenceBuffer;
	protected FloatBuffer barVerticesBuffer, barsColorBuffer;
	protected FloatBuffer edgesColorBuffer, normalsBuffer;
	protected FloatBuffer textureVerticesBuffer, textureCoordinatesBuffer;
	//Type of data this represents
	protected ChroType barType;
	//The current number to draw for this bar.
	protected ChroTexture number;
	protected int textureId;
	//The numbers that are applicable to this bar type.
	protected SparseArray<ChroTexture> textures;
	
	/* End instance variables */

	/**
	 * 
	 * @param t
	 * @param color
	 * @param activityContext
	 */
	public ChroBar(ChroType t, ArrayList<ChroTexture> texs, Context activityContext) {
		
		setBarsDataInit();
		setRendererInit();
		setBarType(t);
		
		prepareTextures(texs);
		
		//Do the actual buffer allocation.
		barGLAllocate(ByteOrder.nativeOrder());
	}

	/**
	 * 
	 */
	private void setBarsDataInit() {
		//If the data object is null, make one. Otherwise do nothing.
		if(barsData == null)
			barsData = ChroData.getNewDataInstance();
	}

	/**
	 * 
	 */
	private void setRendererInit() {
		
		renderer = ChroSurface.getRenderer();
		
		//Set the data class object refs.
		synchronized(barsData) {
			if(barsData.getInt("barsCreated") < 1) {
				barsData.setObjectReference("renderer", ChroSurface.getRenderer());
			}
			barsData.modifyIntegerField("barsCreated", 1);
		}
	}

	/**
	 * @param texs
	 */
	private void prepareTextures(ArrayList<ChroTexture> texs) {
		
		//Add the current textures to this bar.
		putNumberTextures(texs);
		//Allocate space for the texture coordinate float array to be put into a FloatBuffer.
		textureCoordinatesBuffer = (FloatBuffer) ByteBuffer.allocateDirect(ChroData._textureCoordinates.length*ChroData._BYTES_IN_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer().put(ChroData._textureCoordinates).position(0);
		//Allocate space for the texture draw sequence float array to be put into a FloatBuffer.
		textureDrawSequenceBuffer = (ShortBuffer) ByteBuffer.allocateDirect(ChroData._texture_vertexDrawSequence.length*ChroData._BYTES_IN_SHORT).order(ByteOrder.nativeOrder()).asShortBuffer().put(ChroData._texture_vertexDrawSequence).position(0);
		
		ChroPrint.println("Initializing texture vertices...", System.out);
		initTextureVertices();
		
//		DEBUG
//		ChroPrint.println("Added " + textures.size() + " to " + barType + "'s texture cache.", System.out);
	}

	/**
	 * 
	 */
	private void initTextureVertices() {
		
		textureVertices = new float[ChroData._2D_VERTEX_COMPONENTS];
		
		//Allocate a FloatBuffer for the texture vertices.
		textureVerticesBuffer = (FloatBuffer) ByteBuffer.allocateDirect(textureVertices.length*ChroData._BYTES_IN_FLOAT).order(ByteOrder.nativeOrder()).asFloatBuffer().put(textureVertices).position(0);
		
		float _baseHeight = ChroData._baseHeight;
		
		//Place the texture plane on top of the bar.
		float[] texVerts = { 	 -0.5f, _baseHeight, -0.1f,    	// Lower Left  | 0
								  0.5f, _baseHeight, -0.1f,    	// Lower Right | 1
								 -0.5f, 1.0f, 		 -0.1f,    	// Upper Left  | 2
								  0.5f, 1.0f, 		 -0.1f  };	// Upper Right | 3
		
		for(int i = 0; i < ChroData._2D_VERTEX_COMPONENTS; i++)
			textureVertices[i] = texVerts[i];
	}

	/**
	 * 
	 * @param toDraw
	 */
	public final void setDrawBar(boolean toDraw) {
		drawBar = toDraw;
	}
	
	/**
	 * 
	 * @param drawNum
	 */
	public final void setDrawNumber(boolean drawNum) {
		drawNumber = drawNum;
	}
	
	/**
	 * 
	 * @return
	 */
	public final boolean isDrawn() {
		return drawBar;
	}
	
	/**
	 * 
	 * @return
	 */
	public final boolean isNumberDrawn() {
		return drawNumber;
	}
	
	/**
	 * 
	 */
	protected void calculateBarWidth() {

		//Gather required information
		float screenWidth = ChroBarsActivity.getDisplayMetrics().widthPixels;
//		DEBUG
//		ChroPrint.println("Screen width: " + screenWidth);
		float barTypeCode = (float)barType.getType();
		float barMargin = barsData.getFloat("barMarginBase");
		float edgeMargin = barsData.getFloat("edgeMarginBase");
		ChroBar[] visible = renderer.refreshVisibleBars();
		
		//Update the bar margin to current pixel width ratio of screen.
		barMargin /= screenWidth;
		barMargin *= 2f;
		barMargin *= renderer.getBarMarginScalar();
		//Update the edge margin to current screen pixels
		edgeMargin /= screenWidth; //Get the edge margin as a ratio to the entire screen width
		edgeMargin *= 2f; //Normalize the edge margin to cartesian coordinates.
		edgeMargin *= renderer.getEdgeMarginScalar(); //Scale the margin to the correct size.
		
		//Perform bar width calculations
		int numberOfBars = renderer.numberOfBarsToDraw();
//		DEBUG
//		ChroPrint.println("We are drawing " + numberOfBars + " bars.");
		float barWidth = (screenWidth/(float)numberOfBars)/screenWidth;
		barWidth *= 2f;
		barWidth -= ((edgeMargin*2f)/(float)numberOfBars);
		this.barWidth = barWidth -= ((barMargin*((float)numberOfBars-1f))/(float)numberOfBars);
		
		if(barType.is3D())
			barTypeCode -= 4f;
		barTypeCode -= (ChroData._MAX_BARS_TO_DRAW - numberOfBars);
		
		for(int i = barType.getType() + (barType.is3D() ? (-3) : 1);
							i < ChroData._MAX_BARS_TO_DRAW; i++) {
//			DEBUG
//			ChroPrint.println("Current bar check index: " + i);
			if(!visible[i].isDrawn())
				++barTypeCode;
		}
		
		if(barTypeCode < 0)
			barTypeCode = 0;
		
//		DEBUG
//		ChroPrint.println("Bar type code: " + barTypeCode + "\nBar type: " + barType);
					
		float leftX = ChroData._left_screen_edge + edgeMargin + (barWidth * barTypeCode) + (barMargin * barTypeCode);
		float rightX = leftX + barWidth;
		
		//Set the width of this bar object
		setBarWidth(leftX, rightX);
	}
	
	/**
	 * 
	 */
	protected void calculateBarHeight() {
		
		currentTime = Calendar.getInstance(TimeZone.getDefault(), Locale.US);
		
		//This seems to do the trick. Fun with magic numbers!
		float scalingFactor = 3.65f;
		float barTopHeight = ChroData._baseHeight + 0.01f;
		
		barTopHeight += (getRatio()*scalingFactor);
		
		setBarHeight(barTopHeight);
		
		//Reset the OpenGL vertices buffer with updated coordinates
		((FloatBuffer) barVerticesBuffer.clear()).put(barVertices).position(0);
		
		//If we're using dynamic lighting with a 3D bar, rebuild the vertex normals for the new bar height.
		if(barType.is3D() && renderer.usesDynamicLighting()) {
			try { initNormals(); }
			catch(Exception unknownEx) { ChroUtilities.printExDetails(unknownEx); }
			((FloatBuffer) normalsBuffer.clear()).put(normals).position(0);
		}
	}

	/**
	 * Returns the current time ratios for hours, minutes, seconds, and milliseconds.
	 * 
	 * This takes into account the selected motion precision.
	 * 
	 * @return
	 */
	protected float getRatio() {
		
		int t = barType.getType();
		
		float currentHour = (float)currentTime.get(renderer.usesTwelveHourTime() ? Calendar.HOUR : Calendar.HOUR_OF_DAY),
				currentMinute = (float)currentTime.get(Calendar.MINUTE),
				currentSecond = (float)currentTime.get(Calendar.SECOND),
				currentMillisecond = (float)currentTime.get(Calendar.MILLISECOND);
		
		float currentMSInDay = 0, currentMSInHour = 0, currentMSInMinute = 0;
		
		int precision = (int) renderer.getPrecision();
		
		float hoursInDay = renderer.usesTwelveHourTime() ? ChroData._HOURS_IN_DAY >> 1 : ChroData._HOURS_IN_DAY;
		float msInDay = renderer.usesTwelveHourTime() ? ChroData._msInDay >> 1 : ChroData._msInDay;
		
		if(precision > 0) {
			
			currentMSInMinute = (currentSecond*ChroData._MILLIS_IN_SECOND) + currentMillisecond;
			currentMSInHour = (currentMinute*ChroData._msInMinute) +
					 (currentSecond*ChroData._MILLIS_IN_SECOND) + currentMillisecond;
			currentMSInDay = (currentHour*ChroData._msInHour) +
					(currentMinute*ChroData._msInMinute) +
					(currentSecond*ChroData._MILLIS_IN_SECOND) + currentMillisecond;
		}
		
//		DEBUG
//		ChroPrint.println("Current time:\n" + currentHour + "/" + currentMSInDay + "\n" + currentMinute + "/" + currentMSInHour + "\n" + currentSecond + "/" + currentMSInMinute + "\n" + currentMillisecond);
		
		float precisionRatio = renderer.getPrecision()/ChroData._max_precision;
		
//		DEBUG
//		ChroPrint.println("Precision ratio: " + precisionRatio);
		
		switch(t > 3 ? t - 4 : t) {
		
		case 0:
			return ( currentHour + (precisionRatio * currentMSInDay) ) / ( hoursInDay + (precisionRatio * msInDay)	);
		case 1:
			return ( currentMinute + (precisionRatio * currentMSInHour) ) / ( (float)ChroData._MINUTES_IN_HOUR + (precisionRatio * ChroData._msInHour) );
		case 2:
			return ( currentSecond + (precisionRatio * currentMSInMinute) ) / ( (float)ChroData._SECONDS_IN_MINUTE + (precisionRatio * ChroData._msInMinute));
		case 3:
			return currentMillisecond / (float)ChroData._MILLIS_IN_SECOND;
			
		default:
			ChroPrint.println("Invalid type!", System.err);
			return 0;
		}
	}

	/**
	 * This method draws this ChroBar and its constituent parts,
	 *  including the number and the bar edges.
	 * 
	 * @param drawSurface
	 */
	public void draw(GL10 drawSurface) {

		surface = drawSurface;
		
		//If this bar should not be drawn, exit
		//The method
		if(drawBar) {
		
			setUpCulling_EnableStates(drawSurface);
			
			if(barType.is3D()) {
				setColorMaterials(drawSurface);
				setLightBuffers(drawSurface);
				setBarPointers(drawSurface);
			}
			
			if(!wireframe)
				drawBar(drawSurface);
			
			if(renderer.getBarEdgeSetting() != 0)
				drawBarEdges(drawSurface);
			
			if(drawNumber)
				drawTexture(drawSurface);
			
			disableStates(drawSurface);
			
			recalculateBarDimensions();
			calculateTextureDimensions();
			
//			DEBUG - Uncomment if you are having draw problems
//			ChroUtilities.glCheckError(drawSurface);
		}
	}

	/**
	 * @param drawSurface
	 */
	private void setUpCulling_EnableStates(GL10 drawSurface) {
		//Set up face culling
		//ChroPrint.println("Calling glFrontFace", System.out);
		drawSurface.glFrontFace(GL10.GL_CCW);
		//ChroPrint.println("Calling glEnable", System.out);
		drawSurface.glEnable(GL10.GL_CULL_FACE);
		//ChroPrint.println("Calling glCullFace", System.out);
		drawSurface.glCullFace(GL10.GL_BACK);
		
		//Enable the OpenGL vertex array buffer space
		//ChroPrint.println("Calling glEnableClientState for vertex array", System.out);
		drawSurface.glEnableClientState(GL10.GL_VERTEX_ARRAY);
	}

	/**
	 * @param drawSurface
	 */
	private void drawTexture(GL10 drawSurface) {
		
		//If something has gone awry, bail out.
		if(!shouldDrawTexture())
			return;

		//ChroPrint.println("Calling glDisableClientState for color array", System.out);
		drawSurface.glDisableClientState(GL10.GL_COLOR_ARRAY);
		
		drawSurface.glEnable(GL10.GL_BLEND);
		drawSurface.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		
		//For this drawing phase we need to enable textures.
		drawSurface.glEnable(GL10.GL_TEXTURE_2D);
		
//		DEBUG
//		ChroPrint.println("Trying to bind number " + this.number + " with texture ID " + this.textureId, System.out);
		drawSurface.glBindTexture(GL10.GL_TEXTURE_2D, this.textureId);
		drawSurface.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		drawSurface.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		drawSurface.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		drawSurface.glVertexPointer(ChroData._DIMENSIONS, GL10.GL_FLOAT,
									ChroData._VERTEX_STRIDE, textureVerticesBuffer);
		drawSurface.glTexCoordPointer(ChroData._DIMENSIONS - 1, GL10.GL_FLOAT,
									  ChroData._VERTEX_STRIDE, textureCoordinatesBuffer);
		
//		DEBUG
//		ChroPrint.println("Calling glDrawElements for textures", System.out);
		//Uses the same draw sequence as the 2D bars, to preserve polygon winding and conserve memory.
		//Draw the number texture
		drawSurface.glDrawElements(GL10.GL_TRIANGLES, ChroData._texture_vertexDrawSequence.length,
									GL10.GL_UNSIGNED_SHORT, textureDrawSequenceBuffer);
		//Alternate drawing method.
//		drawSurface.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0, textureVertices.length / 3);
		
		drawSurface.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		drawSurface.glDisable(GL10.GL_TEXTURE_2D);
		drawSurface.glDisable(GL10.GL_BLEND);

		//ChroPrint.println("Calling glEnableClientState for color array", System.out);
		drawSurface.glEnableClientState(GL10.GL_COLOR_ARRAY);
	}

	/**
	 * @param drawSurface
	 */
	private void setColorMaterials(GL10 drawSurface) {
		
		//ChroPrint.println("Calling glEnableClientState for color array", System.out);
		drawSurface.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		//Set general lighting buffers
		drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, renderer.getSpecularBuffer());
		drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_EMISSION, renderer.getEmissionLightBuffer());
		drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SHININESS, renderer.getShininessBuffer());
	}

	/**
	 * @param drawSurface
	 */
	private void setLightBuffers(GL10 drawSurface) {
		//Set the color material to the appropriate colors.
		drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, barsColorBuffer);
		drawSurface.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, barsColorBuffer);
	}

	/**
	 * @param drawSurface
	 */
	private void setBarPointers(GL10 drawSurface) {
		
		//ChroPrint.println("Calling glEnableClientState for normals array", System.out);
		drawSurface.glEnableClientState(GL10.GL_NORMAL_ARRAY);
		//load the buffer of normals into the OpenGL draw object.
		drawSurface.glNormalPointer(GL10.GL_FLOAT, ChroData._VERTEX_STRIDE, normalsBuffer);
	}

	/**
	 * @param drawSurface
	 */
	private void drawBar(GL10 drawSurface) {
		
		//Tell openGL where the vertex data is and how to use it
		//ChroPrint.println("Calling glVertexPointer", System.out);
		drawSurface.glVertexPointer(ChroData._DIMENSIONS, GL10.GL_FLOAT,
									ChroData._VERTEX_STRIDE, barVerticesBuffer);
		//Color buffer for the bars.
		//ChroPrint.println("Calling glColorPointer for bars", System.out);
		drawSurface.glColorPointer(ChroData._RGBA_COMPONENTS, GL10.GL_FLOAT,
									ChroData._VERTEX_STRIDE, barsColorBuffer);
		//Draw the bar
		//ChroPrint.println("Calling glDrawElements for bars", System.out);
		drawSurface.glDrawElements(GL10.GL_TRIANGLES, getBarDrawSequenceBufferLength(),
									GL10.GL_UNSIGNED_SHORT, barDrawSequenceBuffer);
	}

	/**
	 * @param drawSurface
	 */
	private void drawBarEdges(GL10 drawSurface) {
		
		drawSurface.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		drawSurface.glEnableClientState(GL10.GL_VERTEX_ARRAY);

		//Tell openGL where the vertex data is and how to use it
		//ChroPrint.println("Calling glVertexPointer", System.out);
		drawSurface.glVertexPointer(ChroData._DIMENSIONS, GL10.GL_FLOAT,
									ChroData._VERTEX_STRIDE, barVerticesBuffer);
		
		//Color buffer for the edges.
		//ChroPrint.println("Calling glColorPointer for edges", System.out);
		drawSurface.glColorPointer(ChroData._RGBA_COMPONENTS, GL10.GL_FLOAT,
									ChroData._VERTEX_STRIDE, edgesColorBuffer);
		
		//Draw the accented bar edges
		//ChroPrint.println("Calling glDrawElements for edges", System.out);
		drawSurface.glDrawElements(GL10.GL_LINES, getEdgeDrawSequenceBufferLength(),
									GL10.GL_UNSIGNED_SHORT, edgeDrawSequenceBuffer);
	}

	/**
	 * @param drawSurface
	 */
	private void disableStates(GL10 drawSurface) {
		//Clear the buffer space
		//ChroPrint.println("Calling glDisableClientState for vertex array", System.out);
		drawSurface.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		drawSurface.glDisableClientState(GL10.GL_COLOR_ARRAY);
		
		if(barType.is3D()) {
			//ChroPrint.println("Calling glDisableClientState for normals array", System.out);
			drawSurface.glDisableClientState(GL10.GL_NORMAL_ARRAY);
		}
		//Disable face culling.
		//ChroPrint.println("Calling glDisable", System.out);
		drawSurface.glDisable(GL10.GL_CULL_FACE);
	}

	/**
	 * 
	 */
	private void recalculateBarDimensions() {
		//Recalculate the bar dimensions in preparation for a redraw
		calculateBarWidth();
		calculateBarHeight();
	}

	private void calculateTextureDimensions() {
		//Set the left-side position of the texture plane to the edge of the bar.
		textureVertices[0] = textureVertices[6] = barVertices[0] + 0.015f;
		//Set the right-side vertices: The texture should be the width of the bar.
		textureVertices[3] = textureVertices[9] = textureVertices[0] + barWidth;
		//Set the bottom vertices to the top of the bar, plus a small margin.
		textureVertices[1] = textureVertices[4] = barVertices[1] + 0.01f;
		//The texture should have the same height as it is width.
		textureVertices[7] = textureVertices[10] = textureVertices[1] + barWidth;
		//Refill the textureverticesbuffer with the new coordinates.
		((FloatBuffer) textureVerticesBuffer.clear()).put(textureVertices).position(0);
	}

	/**
	 * @return
	 */
	private boolean shouldDrawTexture() {
//		DEBUG
//		ChroPrint.println("Looking for a texture for " + barType, System.out);
		ChroTexture number = getNumberTexture();
		int textureId = 0;
		
		//There is no texture for this time
		if(number == null)
			return false;
		//Otherwise, proceed.
		this.number = number;
		textureId = number.getTexId();
		//There is a problem with the texture if this is true.
		if(textureId == 0)
			return false;
		this.textureId = textureId;
		//Or if for some reason the instance number texture object is still null
		if(this.number == null)
			return false;
		else if(this.textureId == 0)
			return false;
//		DEBUG
//		ChroPrint.println("Texture: " + number + "\nTexture ID: " + textureId, System.out);
		
		return true;
	}
	
	/**
	 * Returns a time-based texture to display for this bar.
	 * 
	 * @return The texture to display at the appropriate position.
	 */
	private ChroTexture getNumberTexture() {
		int time = getCurrentBarTime();
		if(renderer.usesTwelveHourTime() && time == 0)
			time = 12;
//		DEBUG
//		ChroPrint.println("Get time for " + barType + ": " + time, System.out);
		ChroTexture tex = textures.get(time);
//		DEBUG
//		ChroPrint.println("Current texture: " + tex, System.out);
		return tex;
	}

	/**
	 * This returns the current bar time as an integer, which allows
	 * @return
	 */
	private int getCurrentBarTime() {
		currentTime = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
//		DEBUG
//		ChroPrint.println("Getting current time for bar: " + barType, System.out);
		switch(barType) {
		case HOUR:
		case HOUR3D:
			if(renderer.usesTwelveHourTime())
				return currentTime.get(Calendar.HOUR);
			else
				return currentTime.get(Calendar.HOUR_OF_DAY);
		case MINUTE:
		case MINUTE3D:
			return currentTime.get(Calendar.MINUTE);
		case SECOND:
		case SECOND3D:
			return currentTime.get(Calendar.SECOND);
		case MILLIS:
		case MILLIS3D:
			return currentTime.get(Calendar.MILLISECOND);
		default:
			return 0;
		}
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
		
		changeChroBarColor(Color.argb(alpha, red, green, blue));
		
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
		
		changeChroBarColor(Color.parseColor(colorstring));
	}
	
	/**
	 * Changes the barColor/edgeColor values and those of the vertices.
	 * 
	 * @param colorInt
	 */
	public void changeChroBarColor(int colorInt) {
		
		int colorArrayLength = barVertexColors.length;
		barColor = colorInt;
		
		int red = ((barColor >> 16) & 0xFF);
		int green = ((barColor >>  8) & 0xFF);
		int blue = (barColor & 0xFF);
		
		setEdgeColor(red, green, blue);
		
		int edgeRed 	= ((edgeColor >> 16) & 0xFF);
		int edgeGreen 	= ((edgeColor >>  8) & 0xFF);
		int edgeBlue 	= 		  (edgeColor & 0xFF);
//		DEBUG
//		ChroPrint.println("\nEdges:\nRed: " + edgeRed + " Green: " + edgeGreen + " Blue: " + edgeBlue, ChroPrint);
		
		for(int i = 0; i < colorArrayLength; i += 4) {
			barVertexColors[i] = (float)red/255.0f;
			edgeVertexColors[i] = (float)edgeRed/255.0f;
		}
		for(int i = 1; i < colorArrayLength; i += 4) {
			barVertexColors[i] = (float)green/255.0f;
			edgeVertexColors[i] = (float)edgeGreen/255.0f;
		}
		for(int i = 2; i < colorArrayLength; i += 4) {
			barVertexColors[i] = (float)blue/255.0f;
			edgeVertexColors[i] = (float)edgeBlue/255.0f;
		}
		for(int i = 3; i < colorArrayLength; i += 4) {
			barVertexColors[i] = (float)Color.alpha(barColor)/255.0f;
			edgeVertexColors[i] = (float)Color.alpha(edgeColor)/255.0f;
		}
		
		((FloatBuffer) barsColorBuffer.clear()).put(barVertexColors).position(0);
		((FloatBuffer) edgesColorBuffer.clear()).put(edgeVertexColors).position(0);
	}

	/**
	 * If a user changes the edge style setting, we only want to update the edges.
	 */
	public void updateEdgeColor(int edgeType) {
		
		int red = ((barColor >> 16) & 0xFF);
		int green = ((barColor >>  8) & 0xFF);
		int blue = (barColor & 0xFF);

		int colorArrayLength = edgeVertexColors.length;
		
		setEdgeColor(red, green, blue);
		
		int edgeRed 	= ((edgeColor >> 16) & 0xFF);
		int edgeGreen 	= ((edgeColor >>  8) & 0xFF);
		int edgeBlue 	= 		  (edgeColor & 0xFF);
//		DEBUG
//		ChroPrint.println("\nEdges:\nRed: " + edgeRed + " Green: " + edgeGreen + " Blue: " + edgeBlue, ChroPrint);
		
		for(int i = 0; i < colorArrayLength; i += 4)
			edgeVertexColors[i] = (float)edgeRed/255.0f;
		for(int i = 1; i < colorArrayLength; i += 4)
			edgeVertexColors[i] = (float)edgeGreen/255.0f;
		for(int i = 2; i < colorArrayLength; i += 4)
			edgeVertexColors[i] = (float)edgeBlue/255.0f;
		for(int i = 3; i < colorArrayLength; i += 4)
			edgeVertexColors[i] = (float)Color.alpha(edgeColor)/255.0f;
		
		((FloatBuffer) edgesColorBuffer.clear()).put(edgeVertexColors).position(0);
	}

	/**
	 * This section checks for color overflow, and corrects it
	 *  if it exists.
	 * @param edgeColorDiff
	 * @return
	 */
	private int checkColorOverflow(int r, int g, int b) {
//		DEBUG
//		ChroPrint.println("Red: " + r + " Green: " + g + " Blue: " + b, ChroPrint);
		
		int maxColorValue = (short) Math.max(r,	Math.max(g, b));
		
		if(maxColorValue == 255) {
			
			boolean redMax = noColorOp[0] = r == 255,
					greenMax = noColorOp[1] = g == 255,
					blueMax = noColorOp[2] = b == 255;
			
			if((redMax && greenMax) ||
			   (redMax && blueMax)  ||
			   (greenMax && blueMax)	)
				maxColorValue = redMax && greenMax ? b : redMax && blueMax ? g : r;
			else
				maxColorValue = Math.max(redMax ? 0 : r, Math.max(
											greenMax ? 0 : g, blueMax ? 0 : b));
		}
//		DEBUG
//		ChroPrint.println("Maximum color value is " + maxColorValue, ChroPrint);
		int maxColorValueDiff = 255 - maxColorValue;
		if(maxColorValueDiff < ChroData._lighter_edgeColorDifference)
			return maxColorValueDiff;
		else
			return ChroData._lighter_edgeColorDifference;
	}

	/**
	 * This section checks for color underflow, and corrects it
	 *  if it exists.
	 * @param edgeColorDiff
	 * @return
	 */
	private int checkColorUnderflow(int r, int g, int b) {
//		DEBUG
//		ChroPrint.println("Red: " + r + " Green: " + g + " Blue: " + b, ChroPrint);
		
		int minColorValue = Math.min(r,	Math.min(g, b));
		
		if(minColorValue == 0) {
			boolean redZero = r == 0, greenZero = g == 0, blueZero = b == 0;
			noColorOp[0] = redZero; noColorOp[1] = greenZero; noColorOp[2] = blueZero;
			if((redZero && greenZero) ||
			   (redZero && blueZero)  ||
			   (greenZero && blueZero)	)
				minColorValue = redZero && greenZero ? b : redZero && blueZero ? g : r;
			else 
				minColorValue = Math.min(redZero ? 256 : r, Math.min(
											greenZero ? 256 : g, blueZero ? 256 : b));
			
		}
//		DEBUG
//		ChroPrint.println("Minimum color value is " + minColorValue, ChroPrint);
		if(minColorValue < ChroData._darker_edgeColorDifference)
			return minColorValue;
		else
			return ChroData._darker_edgeColorDifference;
	}

	/**
	 * @param red
	 * @param green
	 * @param blue
	 */
	private void setEdgeColor(int red, int green, int blue) {
		
		int edgeColorDiff;
		
		//Assume that all colors will be operated on.
		for(int i = 0; i < noColorOp.length; i++)
			noColorOp[i] = false;
		
		//If we change the color of the bar,
		// we also need to change the edge color accordingly.
		switch(renderer.getBarEdgeSetting()) {
		//If the edges are supposed to be the same color
		case 0:
			edgeColor = barColor;
			break;
		//If the edges are supposed to be lighter than the bar
		case 1:
			edgeColorDiff = checkColorOverflow(red, green, blue);
			
			edgeColor = Color.argb( Color.alpha(barColor),
								    red 	+ (noColorOp[0] ? 0 : edgeColorDiff),
								    green 	+ (noColorOp[1] ? 0 : edgeColorDiff),
								    blue 	+ (noColorOp[2] ? 0 : edgeColorDiff) );
			break;
		//If the edges are supposed to be darker than the bar
		case 2:
			edgeColorDiff = checkColorUnderflow(red, green, blue);
			
			edgeColor = Color.argb( Color.alpha(barColor),
								    red		- (noColorOp[0] ? 0 : edgeColorDiff),
								    green 	- (noColorOp[1] ? 0 : edgeColorDiff),
								    blue 	- (noColorOp[2] ? 0 : edgeColorDiff) );
			break;
		//We'll just set it to the bar color if it's an unknown edge color option to be safe.
		default:
			edgeColor = barColor;
		}
	}
	
	/**
	 * 
	 * @param texs
	 */
	public void putNumberTextures(ArrayList<ChroTexture> texs) {
//		DEBUG
//		ChroPrint.println("Putting " + texs, System.out);
		textures = new SparseArray<ChroTexture>();
		for(ChroTexture tex : texs) {
			for(ChroType t : tex.getBarTypes()) {
				if(t.equals(barType)) {
//					DEBUG
//					ChroPrint.println("Appending " + tex, System.out);
					textures.append(tex.getOrderIndex(), tex);
					break;
				}
			}
		}
//		DEBUG
//		ChroPrint.println("Current ChroTextures:\n" + textures, System.out);
	}
	
	/**
	 * 
	 * @param texture
	 */
	public synchronized void putNumberTexture(ChroTexture texture) {
		for(ChroType t : texture.getBarTypes()) {
			if(t == barType) {
				textures.append(texture.getOrderIndex(), texture);
			}
		}
	}

	/**
	 * 
	 * @return
	 */
	public int getBarColor() {
		return barColor;
	}
	
	/**
	 * 
	 * @return
	 */
	public ChroType getBarType() {
		return barType;
	}
	
	/**
	 * 
	 * @param t
	 */
	private void setBarType(ChroType t) {
		barType = t;
	}

	/**
	 * Sets the wireframe property of this bar.
	 * 
	 * @param checked Whether or not to enable wireframe mode.
	 */
	public void setWireframe(boolean checked) {
		wireframe = checked;
	}
	
	/**
	 * 
	 * @return
	 */
	public static int barsCreated() {
		return barsData.getInt("barsCreated");
	}
	
	/**
	 * 
	 */
	@Override
	public String toString() {
		
		return "ChroBar Object " + this.hashCode() +
				" Type: " + barType + " Color: " + barColor;
	}

	/**
	 * 
	 * @param ct
	 * @param activityContext
	 * @return
	 */
	public static ChroBar getInstance(ChroType ct,
										ArrayList<ChroTexture> texs,
										Context activityContext) {
		if(ct.is3D())
			return new ChroBar3D(ct, texs, activityContext);
		else
			return new ChroBar2D(ct, texs, activityContext);
	}
}
