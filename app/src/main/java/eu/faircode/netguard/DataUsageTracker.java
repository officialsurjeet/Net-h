package eu.faircode.netguard;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.TrafficStats;

public class DataUsageTracker {

    private Context context;

    public DataUsageTracker(Context context) {
        this.context = context;
    }

    public long getMobileDataUsage() {
        return TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes();
    }

    public long getWifiDataUsage() {
        return TrafficStats.getTotalTxBytes() + TrafficStats.getTotalRxBytes() - getMobileDataUsage();
    }
}

