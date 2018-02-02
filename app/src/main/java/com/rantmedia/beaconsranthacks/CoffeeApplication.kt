package com.rantmedia.beaconsranthacks

import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.powersave.BackgroundPowerSaver;
import org.altbeacon.beacon.startup.RegionBootstrap;
import org.altbeacon.beacon.startup.BootstrapNotifier;


/** (C) Copyright 2018 Rantmedia Ltd (http://www.rantmedia.com)
 * Created by Russell Hicks on 02/02/2018
 * russell@rantmedia.com
 * All Rights Reserved
 */
class CoffeeApplication : Application(), BootstrapNotifier {
    private var regionBootstrap: RegionBootstrap? = null
    private var backgroundPowerSaver: BackgroundPowerSaver? = null
    private var haveDetectedBeaconsSinceBoot = false
    private var mainActivity: MainActivity? = null


    override fun onCreate() {
        super.onCreate()
        val beaconManager = org.altbeacon.beacon.BeaconManager.getInstanceForApplication(this)

        // By default the AndroidBeaconLibrary will only find AltBeacons.  If you wish to make it
        // find a different type of beacon, you must specify the byte layout for that beacon's
        // advertisement with a line like below.  The example shows how to find a beacon with the
        // same byte layout as AltBeacon but with a beaconTypeCode of 0xaabb.  To find the proper
        // layout expression for other beacon types, do a web search for "setBeaconLayout"
        // including the quotes.
        //
        beaconManager.getBeaconParsers().clear()
        //beaconManager.getBeaconParsers().add(new BeaconParser().
        //        setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        beaconManager!!.getBeaconParsers().add(BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));


        Log.d(TAG, "setting up background monitoring for beacons and power saving")
        // wake up the app when a beacon is seen
        val region = Region("backgroundRegion", null, null, null)
        regionBootstrap = RegionBootstrap(this, region)

        // simply constructing this class and holding a reference to it in your custom Application
        // class will automatically cause the BeaconLibrary to save battery whenever the application
        // is not visible.  This reduces bluetooth power usage by about 60%
        backgroundPowerSaver = BackgroundPowerSaver(this)

        // If you wish to test beacon detection in the Android Emulator, you can use code like this:
        // BeaconManager.setBeaconSimulator(new TimedBeaconSimulator() );
        // ((TimedBeaconSimulator) BeaconManager.getBeaconSimulator()).createTimedSimulatedBeacons();
    }

    override fun didEnterRegion(arg0: Region) {
        // In this example, this class sends a notification to the user whenever a Beacon
        // matching a Region (defined above) are first seen.
        Log.d(TAG, "did enter region.")
        if (!haveDetectedBeaconsSinceBoot) {
            Log.d(TAG, "auto launching MainActivity")

            // The very first time since boot that we detect an beacon, we launch the
            // MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            // Important:  make sure to add android:launchMode="singleInstance" in the manifest
            // to keep multiple copies of this activity from getting created if the user has
            // already manually launched the app.
            this.startActivity(intent)
            haveDetectedBeaconsSinceBoot = true
        } else {
            if (mainActivity != null) {
                // If the Monitoring Activity is visible, we log info about the beacons we have
                // seen on its display
            } else {
                // If we have already seen beacons before, but the monitoring activity is not in
                // the foreground, we send a notification to the user on subsequent detections. ")
                sendNotification()
            }
        }


    }

    override fun didExitRegion(region: Region) {
        if (mainActivity != null) {
        }
    }

    override fun didDetermineStateForRegion(state: Int, region: Region) {
        if (mainActivity != null) {
        }
    }

    private fun sendNotification() {
        val builder = NotificationCompat.Builder(this)
                .setContentTitle("FREE COFFEE")
                .setContentText("You lucky thing - you've won a free coffee. Press the button on the machine for a delicious cup of Joe (but do put a cup in place first).")
                .setSmallIcon(R.drawable.ic_local_cafe_black_24dp)

        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addNextIntent(Intent(this, MainActivity::class.java))
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        builder.setContentIntent(resultPendingIntent)
        val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, builder.build())
    }

    fun setMonitoringActivity(activity: MainActivity) {
        this.mainActivity = activity
    }

    companion object {
        private val TAG = "BeaconsTestApp"
    }

}