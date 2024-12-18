package com.example.firebaseperfmoni

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import org.json.JSONArray
import org.json.JSONException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {

    private val TAG = "FirebasePerformanceMonitoring"
    private val recyclerView: RecyclerView? = null
    private var adapter: MyAdapter? = null
    private val itemList: MutableList<String> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val recyclerView = findViewById<RecyclerView>(R.id.data_rv)
        recyclerView.setLayoutManager(LinearLayoutManager(this))
        adapter = MyAdapter(itemList)
        recyclerView.setAdapter(adapter)

        // Start a custom trace for the network call

        // Start a custom trace for the network call
        val networkTrace = FirebasePerformance.getInstance().newTrace("network_call_trace")
        networkTrace.start()

        // Perform the network call

        // Perform the network call
        fetchItems(networkTrace)
        // Start a custom trace
        // Start a custom trace
        val trace: Trace = FirebasePerformance.getInstance().newTrace("test_trace")
        trace.start()

        // Simulate some work

        // Simulate some work
        performTask(trace)
    }

    private fun fetchItems(trace: Trace) {
        Thread {
            try {
                val url = URL("https://jsonplaceholder.typicode.com/posts")
                val connection =
                    url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connect()
                val responseCode = connection.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader =
                        BufferedReader(InputStreamReader(connection.inputStream))
                    val result = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        result.append(line)
                    }
                    reader.close()
                    parseAndDisplayItems(result.toString())

                    // Increment a metric for successful responses
                    trace.incrementMetric("successful_responses", 1)
                } else {
                    Log.e(TAG, "Failed to fetch items: HTTP $responseCode")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching items", e)
            } finally {
                trace.stop()
            }
        }.start()
    }

    private fun parseAndDisplayItems(jsonResponse: String) {
        try {
            val jsonArray = JSONArray(jsonResponse)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val title = jsonObject.getString("title")
                itemList.add(title)
            }
            runOnUiThread { adapter!!.notifyDataSetChanged() }
        } catch (e: JSONException) {
            Log.e(TAG, "Error parsing JSON response", e)
        }
    }
    private fun performTask(trace: Trace) {
        try {
            // Simulate a task by sleeping for 2 seconds
            Thread.sleep(2000)

            // Increment a counter in the trace
            trace.incrementMetric("task_count", 1)
            Log.d(TAG, "Task completed successfully")
        } catch (e: InterruptedException) {
            Log.e(TAG, "Task interrupted", e)
        } finally {
            // Stop the trace
            trace.stop()
        }
    }
}