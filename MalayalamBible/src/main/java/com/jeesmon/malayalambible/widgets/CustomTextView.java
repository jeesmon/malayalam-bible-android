package com.jeesmon.malayalambible.widgets;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.TextView;

public class CustomTextView extends TextView {
	private TextPaint mTextPaint;
	private LineBreaker lineBreaker;
	private int mAscent;

	public CustomTextView(Context context) {
		super(context);
		init(context);
	}

	public CustomTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public CustomTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		mTextPaint = new TextPaint();
		mTextPaint.set(this.getPaint());
		mTextPaint.setTextSize(this.getTextSize());
		mTextPaint.setColor(this.getCurrentTextColor());

		lineBreaker = new LineBreaker();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(measureWidth(widthMeasureSpec),
				measureHeight(heightMeasureSpec));
	}

	@Override
	protected void onDraw(Canvas canvas) {
		List<String> lines = lineBreaker.getLines();

		float x = getPaddingLeft();
		float y = getPaddingTop() + (-mAscent);
		for (int i = 0; i < lines.size(); i++) {
			// Draw the current line.
			String line = lines.get(i);
			try {
				// System.out.println("Line: " + line);
				canvas.drawText(line, 0, line.length(), x, y, mTextPaint);
			} catch (Exception e) {
				break;
			}

			y += (-mAscent + mTextPaint.descent());
			if (y > canvas.getHeight()) {
				break;
			}
		}
	}

	private int measureWidth(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be.
			result = specSize;

			// Format the text using this exact width, and the current mode.
			breakWidth(specSize);
		} else {
			if (specMode == MeasureSpec.AT_MOST) {
				// Use the AT_MOST size - if we had very short text, we may need
				// even less
				// than the AT_MOST value, so return the minimum.
				result = breakWidth(specSize);
				result = Math.min(result, specSize);
			} else {
				// We're not given any width - so in this case we assume we have
				// an unlimited
				// width?
				breakWidth(specSize);
			}
		}

		return result;
	}

	private int measureHeight(int measureSpec) {
		int result = 0;
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		mAscent = (int) mTextPaint.ascent();
		if (specMode == MeasureSpec.EXACTLY) {
			// We were told how big to be, so nothing to do.
			result = specSize;
		} else {
			// The lines should already be broken up. Calculate our max desired
			// height
			// for our current mode.
			int numLines = lineBreaker.getLines().size();
			result = numLines * (int) (-mAscent + mTextPaint.descent())
					+ getPaddingTop() + getPaddingBottom();

			// Respect AT_MOST value if that was what is called for by
			// measureSpec.
			if (specMode == MeasureSpec.AT_MOST) {
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	private int breakWidth(int availableWidth) {
		int widthUsed = lineBreaker.breakText(getText().toString(),
				availableWidth - getPaddingLeft() - getPaddingRight(),
				mTextPaint);
		return widthUsed + getPaddingLeft() + getPaddingRight();
	}

	private static class LineBreaker {
		private ArrayList<String> mLines;

		public LineBreaker() {
			mLines = new ArrayList<String>();
		}

		public int breakText(String input, int maxWidth, TextPaint tp) {
			mLines.clear();

			if (maxWidth == -1) {
				mLines.add(input);
				return (int) (tp.measureText(input) + 0.5f);
			}

			String[] words = input.split("[\\s\\n]");
			StringBuilder l = new StringBuilder();
			StringBuilder p = new StringBuilder();

			for (String w : words) {
				l.append(w).append(" ");
				float lw = tp.measureText(l.toString());

				if (lw >= maxWidth) {
					if (p.length() == 0) {
						mLines.add(w);
						l = new StringBuilder();
						p = new StringBuilder();
					}

					mLines.add(p.toString());
					l = new StringBuilder(w).append(" ");
					p = new StringBuilder(w).append(" ");
				} else {
					p.append(w).append(" ");
				}
			}

			if (p.length() > 0) {
				mLines.add(p.toString());
			}

			return maxWidth;
		}

		public List<String> getLines() {
			return mLines;
		}
	}
}
