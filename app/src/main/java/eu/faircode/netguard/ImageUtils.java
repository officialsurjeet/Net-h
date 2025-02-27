package eu.faircode.netguard;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import androidx.core.graphics.drawable.IconCompat;

public class ImageUtils {
	
	public static IconCompat createBitmapFromString(long speed1) {
		String unit;
		String speed;
		if (speed1 < 100) {
			speed = String.valueOf((int) speed1);
			unit = "B";
			} else if (speed1 < 1024 * 999) {
			speed = String.format("%d", (int) (speed1 / 1024.0));
			unit = "KB";
			} else {
			speed = String.format("%.1f", (speed1 / (1024.0 * 1024.0)));
			unit = "MB";
		}
		
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(65);
		paint.setTextAlign(Paint.Align.CENTER);
		
		Paint unitsPaint = new Paint();
		unitsPaint.setAntiAlias(true);
		unitsPaint.setTextSize(40);
		unitsPaint.setTextAlign(Paint.Align.CENTER);
		unitsPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD)); // Set text to bold
		
		//Rect speedBounds = new Rect();
		//paint.getTextBounds(speed, 0, speed.length(), speedBounds);
		
		//Rect unitsBounds = new Rect();
		//unitsPaint.getTextBounds(unit, 0, unit.length(), unitsBounds);
		
		// Adjust the bitmap size for status bar icon
		Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
		
		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(speed, 48, 48, paint);
		canvas.drawText(unit, 48, 92, unitsPaint);
		
		return IconCompat.createWithBitmap(bitmap);
	}
}
