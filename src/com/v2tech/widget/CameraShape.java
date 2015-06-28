package com.v2tech.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class CameraShape extends View {
	
	private static int MARGIN = 50;
	
	private float cent;
	private Paint p;
	private Path pathLeft;
	private Path pathRight;
	private RectF arcLeftTop;
	private RectF arcRightTop;
	private RectF arcLeftBottom;
	private RectF arcRightBottom;

	public CameraShape(Context context) {
		super(context);
		init();
	}

	public CameraShape(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CameraShape(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}
	
	private void init() {
		 p = new Paint();
		 p.setStyle(Style.STROKE);
		 pathLeft = new Path();
		 pathRight = new Path();
		 
		 arcLeftTop = new RectF();
		arcRightTop = new RectF();
		 arcLeftBottom = new RectF();
		arcRightBottom = new RectF();
	}
	
	public void updatePrecent(float cent) {
		this.cent = cent;
		if (Float.compare(cent, 100.0F) == 0) {
			p.setColor(Color.WHITE);
		} else {
			 p.setColor(Color.GRAY);
		}
		
	}
	
	
	private void prepareLeftPart(int left, int top, int right, int bottom, int midX, int midY) {
		
		int topLineStartX =  midX;
		int topLineStartY =  MARGIN;
		int topLineEndX =  topLineStartX -  (midX - left) / 3 * 2;
		int topLineEndY =  MARGIN;

		pathLeft.reset();
		
		pathLeft.setLastPoint(topLineStartX, topLineStartY);
		if (cent > 0F) {
			if (cent >= 20.0F) {
				pathLeft.lineTo(topLineEndX, topLineEndY);
			} else {
				pathLeft.lineTo(topLineEndX *  (cent / 20.0F), topLineEndY);
			}
		} 
		
		
		int leftArcTop = top;
		int leftArcLeft = left;
		int leftArcRight = topLineEndX;
		int leftArcBottom = topLineEndX;
		
		
		
		if (cent > 20.0F) {
			//add connect line
			pathLeft.lineTo(leftArcLeft + (leftArcRight - leftArcLeft) / 2 , leftArcTop);
			
			arcLeftTop.top = leftArcTop;
			arcLeftTop.left = leftArcLeft;
			arcLeftTop.right = leftArcRight;
			arcLeftTop.bottom = leftArcBottom;
			if (cent >= 40.0F) {
				pathLeft.addArc(arcLeftTop, 180.0F, 90.0F);
			} else {
				pathLeft.addArc(arcLeftTop, 180.0F, 90.0F * (cent / 40.0F));
			}

		}

		
		int leftVerticalLineStartX = leftArcLeft;
		int leftVerticalLineStartY = leftArcTop + (leftArcBottom - leftArcTop) / 2;
		
		int leftVerticalLineEndX = leftArcLeft;
		int leftVerticalLineEndY = bottom- (topLineEndX - leftArcLeft);
		if (cent > 40.0F) {
			pathLeft.moveTo(leftVerticalLineStartX , leftVerticalLineStartY);
			if (cent >= 60.0F ) {
				pathLeft.lineTo(leftVerticalLineEndX, leftVerticalLineEndY);
			} else {
				pathLeft.lineTo(leftVerticalLineEndX, leftVerticalLineStartY + ((leftVerticalLineEndY - leftVerticalLineStartY) * (cent / 60.0F)));
			}
		}
		
		
		int leftBottomArcTop = leftVerticalLineEndY;
		int leftBottomArcLeft = leftArcLeft;
		int leftBottomArcRight = topLineEndX;
		int leftBottomArcBottom = bottom;
		
		if (cent > 60.0F) {
			pathLeft.lineTo(leftVerticalLineEndX, leftBottomArcTop + (leftBottomArcBottom - leftBottomArcTop) / 2);
			//add connect line
			pathLeft.moveTo(leftBottomArcRight, leftBottomArcBottom);
			
			arcLeftBottom.top = leftBottomArcTop;
			arcLeftBottom.left = leftBottomArcLeft;
			arcLeftBottom.right = leftBottomArcRight;
			arcLeftBottom.bottom = leftBottomArcBottom;
			if (cent >= 80.0F) {
				pathLeft.addArc(arcLeftBottom, 90.0F, 90.0F);
			} else {
				pathLeft.addArc(arcLeftBottom, 90.0F, 90.0F * (cent / 60.0F));
			}


		}
		
		
		int bottomLineStartX =  leftBottomArcLeft + (leftBottomArcRight - leftBottomArcLeft) / 2 ;
		int bottomLineStartY =  bottom;
		int bottomLineEndX =  midX;
		int bottomLineEndY =  bottom;

		if (cent > 80.0F) {
			pathLeft.moveTo(bottomLineStartX, bottomLineStartY);
			if (cent >= 100.0F) {
				pathLeft.lineTo(bottomLineEndX, bottomLineEndY);
			} else {
				pathLeft.lineTo(bottomLineStartX *  (cent / 100.0F), bottomLineEndY);
			}
		} 
		
		
	}
	
	
	
	private void prepareRightPart(int left, int top, int right, int bottom, int midX, int midY) {
		
		int topLineStartX =  midX;
		int topLineStartY =  MARGIN;
		int topLineEndX =  midX +  (right - midX) / 3 * 2;
		int topLineEndY =  MARGIN;

		pathRight.reset();
		
		pathRight.moveTo(topLineStartX, topLineStartY);
		if (cent > 0F) {
			if (cent >= 20.0F) {
				pathRight.lineTo(topLineEndX, topLineEndY);
			} else {
				pathRight.lineTo(topLineEndX *  (cent / 20.0F), topLineEndY);
			}
		} 
		
		
		int rightArcTop = top;
		int rightArcLeft = topLineEndX;
		int rightArcRight = right;
		int rightArcBottom = rightArcTop + (rightArcRight - rightArcLeft);
		
		
		
		if (cent > 20.0F) {
			//add connect line
			pathRight.lineTo(rightArcLeft + (rightArcRight - rightArcLeft) / 2, rightArcTop);
			
			arcRightTop.top = rightArcTop;
			arcRightTop.left = rightArcLeft;
			arcRightTop.right = rightArcRight;
			arcRightTop.bottom = rightArcBottom;
			if (cent >= 40.0F) {
				pathRight.addArc(arcRightTop, 270.0F, 90.0F);
			} else {
				pathRight.addArc(arcRightTop, 270.0F, 90.0F * (cent / 40.0F));
			}

		}

		
		int leftVerticalLineStartX = right;
		int leftVerticalLineStartY = rightArcTop + (rightArcBottom - rightArcTop) / 2;
		
		int leftVerticalLineEndX = right;
		int leftVerticalLineEndY = bottom- (bottom  - midY ) / 3 * 2;
		
		if (cent > 40.0F) {
			pathRight.moveTo(leftVerticalLineStartX , leftVerticalLineStartY);
			if (cent >= 60.0F ) {
				pathRight.lineTo(leftVerticalLineEndX, leftVerticalLineEndY);
			} else {
				pathRight.lineTo(leftVerticalLineEndX, leftVerticalLineStartY + ((leftVerticalLineEndY - leftVerticalLineStartY) * (cent / 60.0F)));
			}
		}
		
		
		int rightBottomArcTop = bottom - (rightArcRight - rightArcLeft);
		int rightBottomArcLeft = right - (rightArcRight - rightArcLeft);
		int rightBottomArcRight = right;
		int rightBottomArcBottom = bottom;
		
		if (cent > 60.0F) {
			pathRight.lineTo(leftVerticalLineEndX, rightBottomArcTop + (rightBottomArcBottom - rightBottomArcTop) / 2);
			//add connect line
			pathRight.moveTo(rightBottomArcRight, rightBottomArcBottom);
			
			arcRightBottom.top = rightBottomArcTop;
			arcRightBottom.left = rightBottomArcLeft;
			arcRightBottom.right = rightBottomArcRight;
			arcRightBottom.bottom = rightBottomArcBottom;
			if (cent >= 80.0F) {
				pathRight.addArc(arcRightBottom, 0.0F, 90.0F);
			} else {
				pathRight.addArc(arcRightBottom, 0.0F, 90.0F * (cent / 60.0F));
			}


		}
		
		
		int bottomLineStartX =  rightBottomArcLeft + (rightBottomArcRight - rightBottomArcLeft) / 2 ;
		int bottomLineStartY =  bottom;
		int bottomLineEndX =  midX;
		int bottomLineEndY =  bottom;

		if (cent > 80.0F) {
			pathRight.moveTo(bottomLineStartX, bottomLineStartY);
			if (cent >= 100.0F) {
				pathRight.lineTo(bottomLineEndX, bottomLineEndY);
			} else {
				pathRight.lineTo(bottomLineStartX *  (cent / 100.0F), bottomLineEndY);
			}
		}
		
		
	}


	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int midX = (getRight() - getLeft()) / 2;
		int midY = (getRight() - getLeft()) / 2;
		int left = MARGIN;
		int top = MARGIN;
		int bottom = getBottom() - MARGIN;
		int right = getRight() - MARGIN;
		prepareLeftPart(left, top, right, bottom, midX, midY);
		
		prepareRightPart(left, top, right, bottom, midX, midY);
		
		canvas.drawPath(pathLeft, p);
		
		canvas.drawPath(pathRight, p);
		
	}
	
	
	

}
