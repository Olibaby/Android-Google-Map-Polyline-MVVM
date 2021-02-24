//package com.example.map
//
//import android.os.AsyncTask
//import android.widget.Toast
//import androidx.test.core.app.ApplicationProvider.getApplicationContext
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.PolylineOptions
//import org.json.JSONException
//import org.json.JSONObject
//import java.io.BufferedReader
//import java.io.IOException
//import java.io.InputStream
//import java.io.InputStreamReader
//import java.net.HttpURLConnection
//import java.net.URL
//
//
//fun getRequestedUrl(origin: LatLng, destination: LatLng): String? {
//    val strOrigin = "origin=" + origin.latitude + "," + origin.longitude
//    val strDestination = "destination=" + destination.latitude + "," + destination.longitude
//    val sensor = "sensor=false"
//    val mode = "mode=driving"
//    val param = "$strOrigin&$strDestination&$sensor&$mode"
//    val output = "json"
//    val APIKEY = "AIzaSyCseO4eeGw8HXc0kZ603qYC3nMUBZ4igdg"
//
//    return "https://maps.googleapis.com/maps/api/directions/$output?$param$APIKEY"
//}
//
// fun requestDirection(requestedUrl: String): String? {
//    var responseString = ""
//    var inputStream: InputStream? = null
//    var httpURLConnection: HttpURLConnection? = null
//    try {
//        val url = URL(requestedUrl)
//        httpURLConnection = url.openConnection() as HttpURLConnection
//        httpURLConnection.connect()
//        inputStream = httpURLConnection.getInputStream()
//        val reader = InputStreamReader(inputStream)
//        val bufferedReader = BufferedReader(reader)
//        val stringBuffer = StringBuffer()
//        var line: String? = ""
//        while (bufferedReader.readLine().also({ line = it }) != null) {
//            stringBuffer.append(line)
//        }
//        responseString = stringBuffer.toString()
//        bufferedReader.close()
//        reader.close()
//    } catch (e: Exception) {
//        e.printStackTrace()
//    } finally {
//        if (inputStream != null) {
//            try {
//                inputStream.close()
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//        }
//    }
//    httpURLConnection.disconnect()
//    return responseString
//}
//
//class TaskDirectionRequest : AsyncTask<String?, Void?, String?>() {
//    protected override fun doInBackground(vararg strings: String): String? {
//        var responseString: String? = ""
//        try {
//            responseString = requestDirection(strings[0])
//        } catch (e: java.lang.Exception) {
//            e.printStackTrace()
//        }
//        return responseString
//    }
//
//    override fun onPostExecute(responseString: String?) {
//        super.onPostExecute(responseString)
//        //Json object parsing
//        val parseResult = TaskParseDirection()
//        parseResult.execute(responseString)
//    }
//}
//
// class TaskParseDirection : AsyncTask<String, Void, List<List<HashMap<String, String>>>> {
//     fun doInBackground(vararg jsonString: String?): List<List<HashMap<String?, String?>?>?>? {
//         var routes: List<List<HashMap<String?, String?>?>?>? = null
//         var jsonObject: JSONObject? = null
//         try {
//             jsonObject = JSONObject(jsonString[0])
//             val parser = DirectionParser()
//             routes = parser.parse(jsonObject)
//         } catch (e: JSONException) {
//             e.printStackTrace()
//         }
//         return routes
//     }
//
//     fun onPostExecute(lists: List<List<HashMap<String?, String>>>) {
//         super.onPostExecute(lists)
//         var points: ArrayList? = null
//         var polylineOptions: PolylineOptions? = null
//         for (path in lists) {
//             points = ArrayList()
//             polylineOptions = PolylineOptions()
//             for (point in path) {
//                 val lat = point["lat"]!!.toDouble()
//                 val lon = point["lng"]!!.toDouble()
//                 points.add(LatLng(lat, lon))
//             }
//             polylineOptions.addAll(points)
//             polylineOptions.width(15f)
//             polylineOptions.color(Color.BLUE)
//             polylineOptions.geodesic(true)
//         }
//         if (polylineOptions != null) {
//             mMap.addPolyline(polylineOptions)
//         } else {
//             Toast.makeText(ApplicationProvider.getApplicationContext(), "Direction not found", Toast.LENGTH_LONG).show()
//         }
//     }
//
// }
//
//
