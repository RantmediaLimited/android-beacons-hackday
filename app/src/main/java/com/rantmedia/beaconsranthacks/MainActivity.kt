package com.rantmedia.beaconsranthacks

import android.Manifest
import android.animation.ObjectAnimator
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.RemoteException
import android.util.Log
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import org.altbeacon.beacon.*
import kotlinx.android.synthetic.main.activity_main.*
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.RangeNotifier





class MainActivity : AppCompatActivity(), BeaconConsumer {
    private var beaconManager: BeaconManager? = null
    private val PERMISSION_REQUEST_COARSE_LOCATION = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android M Permission check
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("This app needs location access")
                builder.setMessage("Please grant location access so this app can detect beacons in the background.")
                builder.setPositiveButton(android.R.string.ok, null)
                builder.setOnDismissListener {
                    requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                            PERMISSION_REQUEST_COARSE_LOCATION)
                }
                builder.show()
            }
        }


        setContentView(R.layout.activity_main)
        beaconManager = BeaconManager.getInstanceForApplication(this)
        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager!!.getBeaconParsers().add(BeaconParser().
                setBeaconLayout(BeaconParser.EDDYSTONE_UID_LAYOUT));
        beaconManager!!.bind(this)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSION_REQUEST_COARSE_LOCATION -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "coarse location permission granted")
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Functionality limited")
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.")
                    builder.setPositiveButton(android.R.string.ok, null)
                    builder.setOnDismissListener { }
                    builder.show()
                }
                return
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        beaconManager!!.unbind(this)
    }


    override fun onBeaconServiceConnect() {
        beaconManager!!.setRangeNotifier(object : RangeNotifier {
            override fun didRangeBeaconsInRegion(beacons: Collection<Beacon>, region: Region) {
                if (beacons.size > 0) {
                    //EditText editText = (EditText)RangingActivity.this.findViewById(R.id.rangingText);
                    val firstBeacon = beacons.iterator().next()

                    runOnUiThread {
                        beaconsLog.setText("The coffee beacon " + firstBeacon.toString() + " is about " + firstBeacon.distance + " meters away.")
                        updateDistance(firstBeacon.distance)
                    }
                }
            }

        })

        try {
            beaconManager!!.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
        } catch (e: RemoteException) {
        }

    }

    fun updateDistance(distance: Double){

        if(distance > 10.0){
            coffeeProgressBar.visibility = View.INVISIBLE
            setProgressBarPercentage(coffeeProgressBar, 0);
        } else if (distance > 9.0){
            coffeeProgressBar.visibility = View.VISIBLE
            setProgressBarPercentage(coffeeProgressBar, 10);
            coffeeProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED))
        }else if (distance > 8.0){
            coffeeProgressBar.visibility = View.VISIBLE
            setProgressBarPercentage(coffeeProgressBar, 20);
            coffeeProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED))
        }else if (distance > 7.0){
            coffeeProgressBar.visibility = View.VISIBLE
            setProgressBarPercentage(coffeeProgressBar, 30);
            coffeeProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED))
        }else if (distance > 6.0){
            coffeeProgressBar.visibility = View.VISIBLE
            coffeeProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED))
            setProgressBarPercentage(coffeeProgressBar, 40);
        }else if (distance > 5.0){
            coffeeProgressBar.visibility = View.VISIBLE
            coffeeProgressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW))
            setProgressBarPercentage(coffeeProgressBar, 50);
        }else if (distance > 4.0){
            coffeeProgressBar.visibility = View.VISIBLE
            coffeeProgressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW))
            setProgressBarPercentage(coffeeProgressBar, 60);
        }else if (distance > 3.0){
            coffeeProgressBar.visibility = View.VISIBLE
            coffeeProgressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW))
            setProgressBarPercentage(coffeeProgressBar, 70);
        }else if (distance > 2.0){
            coffeeProgressBar.visibility = View.VISIBLE
            coffeeProgressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN))
            setProgressBarPercentage(coffeeProgressBar, 80);
        }else if (distance > 1.0){
            coffeeProgressBar.visibility = View.VISIBLE
            coffeeProgressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN))
            setProgressBarPercentage(coffeeProgressBar, 90);
        }else if (distance > 0.0){
            coffeeProgressBar.visibility = View.VISIBLE
            coffeeProgressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN))
            setProgressBarPercentage(coffeeProgressBar, 100);
        }

    }

    private fun setProgressBarPercentage(progressBar: ProgressBar, percentage: Int) {
        //https://stackoverflow.com/questions/27213381/how-to-create-circular-progressbar-in-android
        val animation = ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, percentage * 100 /*We times by 100 here because we set the 'max' to 10,000 in the xml to make transitions smoother / less choppy*/)
        animation.duration = 2000
        animation.interpolator = DecelerateInterpolator()
        animation.start()
    }


    companion object {
        protected val TAG = "MonitoringActivity"
    }

}
