/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This file has been modified by Jonathan Hyry for use with the ChroBars 
 * android app project.
 */

package com.psoft.chrobars.settings;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

/**
 * 
 * @author Google
 * @author Jonathan Hyry
 *
 */
public class ColorPickerDialog extends Dialog {

    /**
     * 
     * @author Google
     * @author Jonathan Hyry
     */
    private static class ColorPickerView extends View {

        private static final int CENTER_X = 100;
        private static final int CENTER_Y = 100;
        private static final int CENTER_RADIUS = 32;
        
        private static final float PI = 3.1415926f;

        private boolean saturationChanged;
        private boolean mTrackingCenter;
        private boolean mHighlightCenter;
		private int windowHeight;
		private int _BOTTOM_DIALOG_MARGIN = 15;
		private int _SATURATION = 1;
		private int _LIGHTNESS = 2;
		private int _SIDE_MARGIN = 20;
    	private float lightnessRectConstraint, saturationGradientTopMargin, rectStrokeWidth,
						circleConstraint, lightnessGradientTopMargin, saturationRectConstraint;
        private double maxLightness_Angle, minLightness_Angle,
        			   maxSaturation_Angle, minSaturation_Angle;
    	
        private final int[] mColors, mLightness;
		private int[] mSaturation;
		
        private Paint mPaint;
        private Paint mCenterPaint;
        private Paint mLightnessPaint;
        private Paint mSaturationPaint;
        private Shader saturationGradient;
    	
        private RectF mOuterColorsRectF, mLightnessRectF, mSaturationRectF;
        
        private OnColorChangedListener mListener;
        
        /**
         * 
         * @param c
         * @param l
         * @param color
         */
        ColorPickerView(Context c, OnColorChangedListener l, int color) {
        	
            super(c);
            mListener = l;
            
            mColors = new int[] {
                0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
                0xFFFFFF00, 0xFFFF0000
            };
            
            mLightness = new int[] { 0x00000000, 0xFFFFFFFF };
            
            float[] initColorHSV = new float[3];
            Color.colorToHSV(color, initColorHSV);
            initColorHSV[1] = 0.0f;
            int zeroSaturation = Color.HSVToColor(initColorHSV);
            
            mSaturation = new int[] { zeroSaturation, color };
            
            Shader outerRing = new SweepGradient(0, 0, mColors, null);
            
            mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mPaint.setShader(outerRing);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeWidth(32);
            
            mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mCenterPaint.setColor(color);
            mCenterPaint.setAlpha(0xFF);
            mCenterPaint.setStrokeWidth(5);

            rectStrokeWidth = mPaint.getStrokeWidth()*0.5f;
            lightnessRectConstraint = CENTER_Y + mPaint.getStrokeWidth() + 0.75f*(rectStrokeWidth);
            lightnessGradientTopMargin = mPaint.getStrokeWidth() + 0.75f*(rectStrokeWidth);
            saturationRectConstraint = lightnessRectConstraint + _BOTTOM_DIALOG_MARGIN*2 + rectStrokeWidth;
            saturationGradientTopMargin = _BOTTOM_DIALOG_MARGIN*2;
            
            Shader lightnessGradient = new LinearGradient(-CENTER_X, lightnessRectConstraint,
            											   CENTER_X, lightnessRectConstraint +
            																rectStrokeWidth,
            											   mLightness, null, Shader.TileMode.CLAMP);
            
            saturationGradient = new LinearGradient(-CENTER_X, saturationRectConstraint,
													 CENTER_X, saturationRectConstraint +
																			rectStrokeWidth,
													 mSaturation, null, Shader.TileMode.CLAMP);
            
            mLightnessPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mLightnessPaint.setShader(lightnessGradient);
            mLightnessPaint.setStyle(Paint.Style.STROKE);
            mLightnessPaint.setStrokeWidth(rectStrokeWidth);
            
            mSaturationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mSaturationPaint.setShader(saturationGradient);
            mSaturationPaint.setStyle(Paint.Style.STROKE);
            mSaturationPaint.setStrokeWidth(rectStrokeWidth);
            
            circleConstraint = CENTER_X - mPaint.getStrokeWidth()*0.5f;
            
            mOuterColorsRectF = new RectF(-circleConstraint,
            							  -circleConstraint,
            							  circleConstraint,
            							  circleConstraint  );
            
            mLightnessRectF = new RectF(-CENTER_X+_SIDE_MARGIN, lightnessRectConstraint,
            							 CENTER_X-_SIDE_MARGIN, lightnessRectConstraint -
            							 			mLightnessPaint.getStrokeWidth());
            mSaturationRectF = new RectF(-CENTER_X+_SIDE_MARGIN, saturationRectConstraint,
										 CENTER_X-_SIDE_MARGIN, saturationRectConstraint -
										 			mSaturationPaint.getStrokeWidth());
            
            windowHeight = CENTER_Y+(int)lightnessRectConstraint+
            	(int)(saturationRectConstraint/4.0f)+_BOTTOM_DIALOG_MARGIN;
        }

        /**
         * 
         */
        @Override 
        protected void onDraw(Canvas canvas) {

        	//Only set a new shader if the actual color changed.
            if(!saturationChanged)
            	mSaturationPaint.setShader(getNewSaturationGradient());
        	
            canvas.translate(CENTER_X, CENTER_X);
            
            canvas.drawOval(mOuterColorsRectF, mPaint);
            canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
            canvas.drawRect(mLightnessRectF, mLightnessPaint);
            canvas.drawRect(mSaturationRectF, mSaturationPaint);
            
            if (mTrackingCenter) {
                int c = mCenterPaint.getColor();
                mCenterPaint.setStyle(Paint.Style.STROKE);
                
                if (mHighlightCenter) {
                    mCenterPaint.setAlpha(0xFF);
                } else {
                    mCenterPaint.setAlpha(0x80);
                }
                canvas.drawCircle(0, 0,
                                  CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
                                  mCenterPaint);
                
                mCenterPaint.setStyle(Paint.Style.FILL);
                mCenterPaint.setColor(c);
            }
//          DEBUG
//          System.out.println("Lightness angles | Min: " + minLightness_Angle + "Max: " + maxLightness_Angle);
        }
        
        /**
         * 
         * @return
         */
        private Shader getNewSaturationGradient() {
        	
        	float[] initColorHSV = new float[3];
            Color.colorToHSV(mCenterPaint.getColor(), initColorHSV);
            initColorHSV[1] = 0.0f;
            int zeroSaturation = Color.HSVToColor(initColorHSV);
            
            mSaturation = new int[] { zeroSaturation, mCenterPaint.getColor() };
			
			return new LinearGradient(-CENTER_X, saturationRectConstraint,
										 CENTER_X, saturationRectConstraint +
										 rectStrokeWidth,
										 mSaturation, null, Shader.TileMode.CLAMP);
		}

        /**
         * 
         */
		@Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            
        	setMeasuredDimension(CENTER_X*2, windowHeight);
            
            int height = (int) (getMeasuredHeight()-_BOTTOM_DIALOG_MARGIN-(CENTER_Y/2)-saturationGradientTopMargin);
            int width = getMeasuredWidth();
            
            double bottomLeftX = -((double)width/2), bottomLeftY = -(double)height;
            double bottomRightX = (double)width/2, bottomRightY = -(double)height;
            
            maxSaturation_Angle = PI+Math.atan2(bottomRightY, bottomRightX);
            minSaturation_Angle = PI+Math.atan2(bottomLeftY, bottomLeftX);
            
            height -= rectStrokeWidth;
            height -= lightnessGradientTopMargin;
            
            bottomLeftX = -((double)width/2);
            bottomLeftY = -(double)height;
            bottomRightX = (double)width/2;
            bottomRightY = -(double)height;
            
            maxLightness_Angle = PI+Math.atan2(bottomRightY, bottomRightX);
            minLightness_Angle = PI+Math.atan2(bottomLeftY, bottomLeftX);
        }

		/**
		 * 
		 * @param x
		 * @return
		 */
        private int floatToByte(float x) {
            int n = java.lang.Math.round(x);
            return n;
        }
        
        /**
         * 
         * @param n
         * @return
         */
        private int pinToByte(int n) {
            if (n < 0) {
                n = 0;
            } else if (n > 255) {
                n = 255;
            }
            return n;
        }
        
        /**
         * 
         * @param s
         * @param d
         * @param p
         * @return
         */
        private int ave(int s, int d, float p) {
            return s + java.lang.Math.round(p * (d - s));
        }
        
        /**
         * 
         * @param colors
         * @param unit
         * @return
         */
        private int interpColor(int colors[], float unit) {
            if (unit <= 0) {
                return colors[0];
            }
            if (unit >= 1) {
                return colors[colors.length - 1];
            }
            
            float p = unit * (colors.length - 1);
            int i = (int)p;
            p -= i;

            // now p is just the fractional part [0...1) and i is the index
            int c0 = colors[i];
            int c1 = colors[i+1];
            int a = ave(Color.alpha(c0), Color.alpha(c1), p);
            int r = ave(Color.red(c0), Color.red(c1), p);
            int g = ave(Color.green(c0), Color.green(c1), p);
            int b = ave(Color.blue(c0), Color.blue(c1), p);
            
            return Color.argb(a, r, g, b);
        }

        /**
         * 
         * @param color
         * @param rad
         * @return
         */
		@SuppressWarnings("unused")
		private int rotateColor(int color, float rad) {
            float deg = rad * 180 / 3.1415927f;
            int r = Color.red(color);
            int g = Color.green(color);
            int b = Color.blue(color);
            
            ColorMatrix cm = new ColorMatrix();
            ColorMatrix tmp = new ColorMatrix();

            cm.setRGB2YUV();
            tmp.setRotate(0, deg);
            cm.postConcat(tmp);
            tmp.setYUV2RGB();
            cm.postConcat(tmp);
            
            final float[] a = cm.getArray();

            int ir = floatToByte(a[0] * r +  a[1] * g +  a[2] * b);
            int ig = floatToByte(a[5] * r +  a[6] * g +  a[7] * b);
            int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);
            
            return Color.argb(Color.alpha(color), pinToByte(ir),
                              		pinToByte(ig), pinToByte(ib));
        }

		/**
		 * 
		 */
        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX() - CENTER_X;
            float y = event.getY() - CENTER_Y;
            boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= CENTER_RADIUS;
            
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTrackingCenter = inCenter;
                    if (inCenter) {
                        mHighlightCenter = true;
                        invalidate();
                        break;
                    }
                case MotionEvent.ACTION_MOVE:
                    if (mTrackingCenter) {
                        if (mHighlightCenter != inCenter) {
                            mHighlightCenter = inCenter;
                            invalidate();
                        }
                    } else {
                        float angle = (float)java.lang.Math.atan2(y, x);
//                      DEBUG
//                      System.out.println("Angle: " + angle);
                        // need to turn angle [-PI ... PI] into unit [0....1]
                        float unit = angle/(2*PI);
//                      DEBUG
                        System.out.println("Unit: " + unit);
                        
                        if (unit < 0) {
                            unit += 1;
                        }
                        
                        float wheelLimit = lightnessRectConstraint + mPaint.getStrokeWidth() + lightnessGradientTopMargin;
                        float lightnessLimit = wheelLimit + rectStrokeWidth/2.0f + saturationGradientTopMargin;
                        
                        if(event.getY() < wheelLimit) {
                        	mCenterPaint.setColor(interpColor(mColors, unit));
                        	saturationChanged = false;
                        }
                        else if(angle >= minLightness_Angle && angle <= maxLightness_Angle && event.getY() < lightnessLimit) {
                        
                        	double percentLightness = (maxLightness_Angle - angle)/
                        								(maxLightness_Angle - minLightness_Angle);
                        	mCenterPaint.setColor(setColor_SorL(mCenterPaint.getColor(), percentLightness, _LIGHTNESS));
                        	saturationChanged = false;
                        }
                        else if(angle >= minSaturation_Angle && angle <= maxSaturation_Angle) {
                        	
                        	double percentSaturation = (maxSaturation_Angle - angle)/
    								(maxSaturation_Angle - minSaturation_Angle);
                    		mCenterPaint.setColor(setColor_SorL(mCenterPaint.getColor(), percentSaturation, _SATURATION));
                    		saturationChanged = true;
                        }
                        
                        invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    if (mTrackingCenter) {
                        if (inCenter) {
                            mListener.colorChanged(mCenterPaint.getAlpha(),
                            					   mCenterPaint.getColor() );
                        }
                        mTrackingCenter = false;    // so we draw w/o halo
                        
                        invalidate();
                    }
                    break;
            }
            return true;
        }

        /**
         * 
         * @param color
         * @param percent
         * @param valueToSet
         * @return
         */
		private int setColor_SorL(int color, double percent, int valueToSet) {
			
			float[] hsv = new float[3];
			Color.colorToHSV(color, hsv);
			hsv[valueToSet] = (float)percent;
			
			return Color.HSVToColor(Color.alpha(color), hsv);
		}
    }

    private int mInitialColor;
    private OnColorChangedListener mListener;

	/**
	 * 
	 * @author Google
	 * @author Jonathan hyry
	 *
	 */
    public interface OnColorChangedListener {
        void colorChanged(int alpha, int color);
    }

    /**
     * 
     * @param context
     * @param listener
     * @param initialColor
     */
    public ColorPickerDialog(Context context,
                             OnColorChangedListener listener,
                             int initialColor) {
        super(context);
        
        mListener = listener;
        mInitialColor = initialColor;
    }

    /**
     * 
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        
        OnColorChangedListener l = new OnColorChangedListener() {
            public void colorChanged(int alpha, int color) {
                mListener.colorChanged(alpha, color);
                dismiss();
            }
        };

        setContentView(new ColorPickerView(getContext(), l, mInitialColor));
        setTitle("Pick a Color");
    }
}
