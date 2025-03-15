package eu.faircode.netguard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

public class DataUsageService extends Service 
{
        private static final String CHANNEL_ID = "DataSpeedChannel";
	private static final int NOTIFICATION_ID = 1;

	private Handler handler = new Handler();
	private long lastRxBytes = 0;
	private long lastTxBytes = 0;
	private NotificationManager notificationManager;
        RemoteViews customView;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
        notificationManager = getSystemService(NotificationManager.class);
		createNotificationChannel();
		startForeground(NOTIFICATION_ID, createNotification(0,0,0));
		startDataMonitoring();
		return START_STICKY;
	}

	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
		{
			CharSequence name = "Data Speed Monitor";
			String description = "Monitors data speed";
			int importance = NotificationManager.IMPORTANCE_MAX; // Set to LOW to minimize intrusiveness
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
			channel.setDescription(description);
			channel.setSound(null, null); // Disable sound
			channel.enableLights(false); // Disable blinking light
			notificationManager.createNotificationChannel(channel);
		}
	}

	private Notification createNotification(long totalSpeed, long rxSpeed, long txSpeed) 
	{
		// Intent to open app when notification is clicked
		Intent notificationIntent = new Intent(this, ActivityMain.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,PendingIntent.FLAG_IMMUTABLE);

		customView = new RemoteViews(getPackageName(), R.layout.custom_notification);
		customView.setTextViewText(R.id.notification_icon,formatSpeedIcon(totalSpeed));
		customView.setTextViewText(R.id.down_speed,formatSpeed(rxSpeed));
		customView.setTextViewText(R.id.up_speed,formatSpeed(txSpeed));
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
				//.setSmallIcon(createBitmapFromString(totalSpeed)) // Placeholder icon, won't be used
				.setContent(customView)
				.setContentIntent(pendingIntent)
				.setOngoing(true) // Make the notification persistent
				.setPriority(NotificationCompat.PRIORITY_LOW)
				.setOnlyAlertOnce(true); // Set the priority to low
		
		    	return builder.build();

	}

	private void startDataMonitoring(){
		handler.postDelayed(updateNotificationRunnable, 1000); // Update every second
	}

	private Runnable updateNotificationRunnable = new Runnable() 
	{
		@Override
		public void run() {
			long currentRxBytes = TrafficStats.getTotalRxBytes();
			long currentTxBytes = TrafficStats.getTotalTxBytes();
			
			long deltaRx = currentRxBytes - lastRxBytes;
			long deltaTx = currentTxBytes - lastTxBytes;
			
			lastRxBytes = currentRxBytes;
			lastTxBytes = currentTxBytes;
			long total = deltaRx + deltaTx;

			Notification notification = createNotification(total,deltaRx,deltaTx);
			notificationManager.notify(NOTIFICATION_ID, notification);
			handler.postDelayed(this, 1000);
		}
	};

    private String formatSpeedIcon(double speed) {
		if (speed < 1024) {
			return String.format("%.0f\nB/s", speed);
		} else if (speed < 1024 * 1024) {
			return String.format("%.1f\nKB/s", speed / 1024);
		} else if (speed < 1024 * 1024 * 1024) {
			return String.format("%.1f\nMB/s", speed / (1024 * 1024));
		} else {
			return String.format("%.1f\nGB/s", speed / (1024 * 1024 * 1024));
		}
	}

	private String formatSpeed(double speed) {
		if (speed < 1024) {
			return String.format("%.0f B/s", speed);
		} else if (speed < 1024 * 1024) {
			return String.format("%.1f KB/s", speed / 1024);
		} else if (speed < 1024 * 1024 * 1024) {
			return String.format("%.1f MB/s", speed / (1024 * 1024));
		} else {
			return String.format("%.1f GB/s", speed / (1024 * 1024 * 1024));
		}
	}

	
	public static IconCompat createBitmapFromString(long speed1) {
		String unit;
		String speed;
		if (speed1 < 100) {
			speed = String.valueOf((int) speed1);
			unit = "B/s";
		} else if (speed1 < 1024 * 999) {
			speed = String.format("%d", (int) (speed1 / 1024.0));
			unit = "KB";
		} else {
			speed = String.format("%.1f", (speed1 / (1024.0 * 1024.0)));
			unit = "MB";
		}

		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTextSize(70);
		paint.setTextAlign(Paint.Align.CENTER);
		//paint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD)); // Set text to bold
		paint.setTypeface(Typeface.DEFAULT_BOLD);

		Paint unitsPaint = new Paint();
		unitsPaint.setAntiAlias(true);
		unitsPaint.setTextSize(50);
		unitsPaint.setTextAlign(Paint.Align.CENTER);
		//unitsPaint.setTypeface(Typeface.create(Typeface.SERIF, Typeface.BOLD)); // Set text to bold
		unitsPaint.setTypeface(Typeface.DEFAULT_BOLD);
		/*	Rect speedBounds = new Rect();
		//	paint.getTextBounds(speed, 0, speed.length(), speedBounds);
			
		//	Rect unitsBounds = new Rect();
		//	unitsPaint.getTextBounds(unit, 0, unit.length(), unitsBounds);
		//	*/
		// Adjust the bitmap size for status bar icon
		Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(bitmap);
		canvas.drawText(speed, 48, 52, paint);
		canvas.drawText(unit, 48, 95, unitsPaint);
		return IconCompat.createWithBitmap(bitmap);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		handler.removeCallbacks(updateNotificationRunnable);
	}
	/*
	private static final String CHANNEL_ID = "TrafficMonitorChannel";
	private static final int NOTIFICATION_ID = 1;

	private long lastRxBytes = 0;
	private long lastTxBytes = 0;

	private Timer timer;
	private Handler handler = new Handler(); // used to post to main thread
	private NotificationManager notificationManager;

	private DataUsageTracker dataUsageTracker;
	private TotalDataUsage totalDataUsage;
	RemoteViews customView;

	@Override
	public void onCreate() 
	{
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		createNotificationChannel();
		totalDataUsage = new TotalDataUsage(this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) 
	{
		startForeground(NOTIFICATION_ID, createNotification("Starting...", 0, 0, 0)); //Initial notification
		startTrafficMonitoring();
		return START_STICKY; //restart the service if killed by system
	}

	private void createNotificationChannel() 
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) 
		{
			NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Traffic Monitor Channel",
					NotificationManager.IMPORTANCE_HIGH //Low importance for background tracking
			);
			channel.setDescription("Channel for network traffic monitoring");
			channel.enableLights(true);
			channel.setLightColor(Color.GREEN);
			channel.enableVibration(false);
			notificationManager.createNotificationChannel(channel);
		}
	}

	private Notification createNotification(String text, long total, long mob, long wif) 
	{
		// Intent to launch when notification is clicked
		Intent notificationIntent = new Intent(this, ActivityMain.class); // Replace MainActivity if not your launcher activity
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
				PendingIntent.FLAG_IMMUTABLE);
		
		customView = new RemoteViews(getPackageName(), R.layout.custom_notification);

		customView.setTextViewText(R.id.notification_icon, formatBytes(total));
		customView.setTextViewText(R.id.total_data_mobile, formatBytes(mob));
		customView.setTextViewText(R.id.total_data_wifi, formatBytes(wif));
		customView.setTextViewText(R.id.down_speed, "ygjjjjg");
		customView.setTextViewText(R.id.up_speed, "fhg");

		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
				.setSmallIcon(ImageUtils.createBitmapFromString(total))
			        .setOnlyAlertOnce(true)
				.setContent(customView)
				// replace with your icon
				.setContentIntent(pendingIntent)
			        .setPriority(NotificationCompat.PRIORITY_MAX) //Low priority for background tracking
				.setOngoing(true);
	
		return builder.build();

	}

	private void startTrafficMonitoring() 
	{
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() 
		{
			@Override
			public void run() 
			{
				long currentRxBytes = TrafficStats.getTotalRxBytes();
				long currentTxBytes = TrafficStats.getTotalTxBytes();

				long deltaRx = currentRxBytes - lastRxBytes;
				long deltaTx = currentTxBytes - lastTxBytes;

				lastRxBytes = currentRxBytes;
				lastTxBytes = currentTxBytes;
				long total = deltaRx + deltaTx;
				
				String notificationText = String.format("↓ %s/s  ↑ %s/s", formatBytes(deltaRx), formatBytes(deltaTx));
				
				long mobileDataUsage = totalDataUsage.getTotalMobileDataUsage();
				long wifiDataUsage = totalDataUsage.getTotalWifiDataUsage();
				long tott = mobileDataUsage + wifiDataUsage;

				handler.post(() -> { // Posting to the main thread so it updates UI
					notificationManager.notify(NOTIFICATION_ID,
							createNotification(notificationText, total, mobileDataUsage, wifiDataUsage));
				});

			}
		}, 0, 2000); // Update every 5 seconds
	}

	private String formatBytes(long bytes) 
	{
		if (bytes < 1024) 
		{
			return bytes + " B";
		} else if (bytes < 1024 * 1024) 
		{
			return String.format("%.2f KB", bytes / 1024.0);
		} else if (bytes < 1024 * 1024 * 1024) 
		{
			return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
		} else 
		{
			return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
		}
	}

	@Override
	public void onDestroy() 
	{
		super.onDestroy();
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		stopForeground(true);
		stopSelf();

	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) 
	{
		return null;
	}
	*/
}
