package eu.faircode.netguard;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.TrafficStats;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Stream;

public class DataUsageService extends Service {
	
	private static final String CHANNEL_ID = "TrafficMonitorChannel";
	private static final int NOTIFICATION_ID = 1;
	
	private long lastRxBytes = 0;
	private long lastTxBytes = 0; 
	
	private Timer timer;
	private Handler handler = new Handler(); // used to post to main thread
	private NotificationManager notificationManager;
	
	private DataUsageTracker dataUsageTracker;
	private TotalDataUsage totalDataUsage ;
	RemoteViews customView;

	@Override
	public void onCreate() {
		super.onCreate();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		createNotificationChannel();
		dataUsageTracker = new DataUsageTracker(this);
		totalDataUsage = new TotalDataUsage(this);
		customView=new RemoteViews(getPackageName(),R.layout.custom_notification);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		startForeground(NOTIFICATION_ID, createNotification("Starting...",0,0)); //Initial notification
		startTrafficMonitoring();
		return START_STICKY;  //restart the service if killed by system
	}
	
	private void createNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel channel = new NotificationChannel(
			CHANNEL_ID,
			"Traffic Monitor Channel",
			NotificationManager.IMPORTANCE_HIGH //Low importance for background tracking
			);
			channel.setDescription("Channel for network traffic monitoring");
			channel.enableLights(true);
			channel.setLightColor(Color.GREEN);
			channel.enableVibration(false);
			notificationManager.createNotificationChannel(channel);
		}
	}
	private Notification createNotification(String text,long total,long totalDataUsage) {
		
		// Intent to launch when notification is clicked
		Intent notificationIntent = new Intent(this, ActivityMain.class); // Replace MainActivity if not your launcher activity
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
		

                customView.setTextViewText(R.id.notification_icon,"100 \nKB/S");
		customView.setTextViewText(R.id.total_data_mobile,"5tf6t");
		customView.setTextViewText(R.id.total_data_wifi,"gd5gg");
		customView.setTextViewText(R.id.down_speed,"ygjjjjg");
		customView.setTextViewText(R.id.up_speed,"fhg");
	
		
		NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
		//.setContentTitle(formatBytes(total))
		.setOnlyAlertOnce(true)
		//.setContentText(text)
	        .setContent(customView)
		.setSmallIcon(ImageUtils.createBitmapFromString(total))// replace with your icon
		.setContentIntent(pendingIntent)
		.setPriority(NotificationCompat.PRIORITY_MAX)  //Low priority for background tracking
		.setOngoing(true)
		//.setLargeIcon(ImageUtils.createBitmapFromString(total))
	        .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
		.setShowWhen(false);
		
		return builder.build();
		
	}
	
	
	private void startTrafficMonitoring() {
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				long currentRxBytes = TrafficStats.getTotalRxBytes();
				long currentTxBytes = TrafficStats.getTotalTxBytes();
				
				long deltaRx = currentRxBytes - lastRxBytes;
				long deltaTx = currentTxBytes - lastTxBytes;
				
				lastRxBytes = currentRxBytes;
				lastTxBytes = currentTxBytes;
				long total=deltaRx+deltaTx;
				String notificationText = String.format("↓ %s/s  ↑ %s/s", formatBytes(deltaRx), formatBytes(deltaTx));

				long mobileData = dataUsageTracker.getMobileDataUsage();
                                long wifiData = dataUsageTracker.getWifiDataUsage();

				long mobileDataUsage = totalDataUsage.getTotalMobileDataUsage();
long wifiDataUsage = totalDataUsage.getTotalWifiDataUsage();
				long tott=mobileDataUsage+wifiDataUsage;
//notificationText = notificationText + tott;
                        

				
				handler.post(() -> { // Posting to the main thread so it updates UI
					notificationManager.notify(NOTIFICATION_ID, createNotification(notificationText,total,tott));
				});
				
			}
		}, 0, 2000);  // Update every 5 seconds
	}
	private String getDailyDataUsage()
		{
			String datat;
			long receivedBytes = TrafficStats.getMobileRxBytes();
        long transmittedBytes = TrafficStats.getMobileTxBytes();
		return formatBytes(receivedBytes+transmittedBytes);
		}
	
	private String formatBytes(long bytes) {
		if (bytes < 1024) {
			return bytes + " B";
			} else if (bytes < 1024 * 1024) {
			return String.format("%.2f KB", bytes / 1024.0);
			} else if (bytes < 1024 * 1024 * 1024)
					 {
			return String.format("%.2f MB", bytes / (1024.0 * 1024.0));

		}
		else{
			return String.format("%.2f GB", bytes / (1024.0 * 1024.0*1024.0));

		}
	
	}
	
	@Override
	public void onDestroy() {
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
	public IBinder onBind(Intent intent) {
		return null;
	}
}
