package com.bitcode.locationbasedservices

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bitcode.locationbasedservices.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var locationManager : LocationManager
    lateinit var binding: ActivityMainBinding
    lateinit var locationListener: LocationListener

    var locationBR = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            log("Location received")
            var location =
                intent!!.getParcelableExtra<Location>(LocationManager.KEY_LOCATION_CHANGED)
            binding.txtLocationInfo.setText("current location: ${location?.latitude} , ${location?.longitude}")

        }
    }

    var proximityBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if(intent!!.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false)) {
                mt("You entered home premises...")
            }
            else {
                mt("You left home premises...")
            }
        }
    }

    private fun mt(text : String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager


        for(locationProviderName in locationManager.allProviders) {
            binding.txtLocationInfo.append(locationProviderName + "\n")

            var locationProvider : LocationProvider? =
                locationManager.getProvider(locationProviderName)

            log("***** ${locationProvider!!.name} ***")
            log("Power: ${locationProvider!!.powerRequirement}")
            log("Supports Alt: ${locationProvider!!.supportsAltitude()}")
            log("Cost: ${locationProvider.hasMonetaryCost()}")
            log("Req Cell: ${locationProvider.requiresCell()}")
            log("Req Network: ${locationProvider.requiresNetwork()}")
            log("Req Satellite: ${locationProvider.requiresSatellite()}")
            log("Accuracy: ${locationProvider.accuracy}")

            var location : Location? = locationManager.getLastKnownLocation(locationProviderName)
            if(location != null) {
                log("Last Location: ${location.latitude} , ${location.longitude}")
            }

            log("----------------------------------------------------------------")
        }


        var criteria = Criteria()
        criteria.powerRequirement = Criteria.POWER_LOW
        criteria.accuracy = Criteria.ACCURACY_FINE
        criteria.isAltitudeRequired = true
        criteria.isCostAllowed = true

        var locationProviderNames = locationManager.getProviders(criteria, false)
        var bestProvider = locationManager.getBestProvider(criteria, false)

        binding.txtLocationInfo.append("\n\n Best Provider: ${bestProvider}")

        /*locationListener = MyLocationListener()
        locationManager.requestLocationUpdates(
            bestProvider!!,
            1000,
            100F,
            locationListener
        )*/

        registerReceiver(
            locationBR,
            IntentFilter("in.bitcode.KR")
        )

        /*locationManager.requestLocationUpdates(
            bestProvider!!,
            1000,
            100F,
            PendingIntent.getBroadcast(
                MainActivity@this,
                1,
                Intent("in.bitcode.KR"),
                0
            )
        )*/




        locationManager.requestSingleUpdate(
            bestProvider!!,
            PendingIntent.getBroadcast(
                MainActivity@this,
                1,
                Intent("in.bitcode.KR"),
                0
            )
        )

        registerReceiver(
            proximityBroadcastReceiver,
            IntentFilter("in.bitcode.HOME")
        )


        var proximityPendingIntent = PendingIntent.getBroadcast(
            this,
            1,
            Intent("in.bitcode.HOME"),
            0
        )
        locationManager.addProximityAlert(
            18.5226,
            73.7679,
            500F,
            -1,
            //System.currentTimeMillis() + 2 * 60 * 60 * 1000,
            proximityPendingIntent
        )

        //locationManager.removeProximityAlert(proximityPendingIntent)


        //locationManager.removeUpdates(locationListener)
    }

    private inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            binding.txtLocationInfo.setText("Location: ${location.latitude} , ${location.longitude}")
        }

    }

    private fun log(text : String) {
        Log.e("tag", text)

    }
}