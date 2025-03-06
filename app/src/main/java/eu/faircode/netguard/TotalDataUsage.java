package eu.faircode.netguard;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Build;

import java.util.Calendar;

public class TotalDataUsage {
    private NetworkStatsManager networkStatsManager;
    private Context context;

    public TotalDataUsage(Context context) {
        this.context = context;
        networkStatsManager = (NetworkStatsManager) context.getSystemService(Context.NETWORK_STATS_SERVICE);
    }

    public long getTotalMobileDataUsage() {
        long totalMobileData = 0;
        try {
            // Get the current time
            long endTime = System.currentTimeMillis();
            // Set the start time to the beginning of the current day (00:00 hours)
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startTime = calendar.getTimeInMillis();

            // Query the mobile data usage
            NetworkStats networkStats = networkStatsManager.querySummary(
                    ConnectivityManager.TYPE_MOBILE,
                    "",
                    startTime,
                    endTime
            );

            // Iterate through the results and sum the bytes
            NetworkStats.Bucket bucket;
            while (networkStats.hasNextBucket()) {
                bucket = new NetworkStats.Bucket();
                networkStats.getNextBucket(bucket);
                totalMobileData += bucket.getTxBytes() + bucket.getRxBytes();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return totalMobileData;
    }

    public long getTotalWifiDataUsage() {
        long totalWifiData = 0;
        try {
            // Get the current time
            long endTime = System.currentTimeMillis();
            // Set the start time to the beginning of the current day (00:00 hours)
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            long startTime = calendar.getTimeInMillis();

            // Query the Wi-Fi data usage
            NetworkStats networkStats = networkStatsManager.querySummary(
                    ConnectivityManager.TYPE_WIFI,
                    "",
                    startTime,
                    endTime
            );

            // Iterate through the results and sum the bytes
            NetworkStats.Bucket bucket;
            while (networkStats.hasNextBucket()) {
                bucket = new NetworkStats.Bucket();
                networkStats.getNextBucket(bucket);
                totalWifiData += bucket.getTxBytes() + bucket.getRxBytes();
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        return totalWifiData;
    }
}
