package rainbow.weather

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import rainbow.weather.data.Alert
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class AlertWorker(context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {

    override suspend fun doWork(): Result {
        val manager = applicationContext
            .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("alert", "警報", NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)
        val sharedPreferences =
            applicationContext.getSharedPreferences("weather", Context.MODE_PRIVATE)
        var alertsId = sharedPreferences.getInt("alertsId", 0)
        while (true) {
            val alerts = get()
            val newAlertsId = alerts.hashCode()
            if (newAlertsId != alertsId) {
                alerts.forEach {
                    val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.ROOT)
                    val notification = Notification.Builder(applicationContext, "alert")
                        .setSmallIcon(R.drawable.ic_launcher_foreground)
                        .setContentTitle("【${it.description}・${dateFormat.format(it.issueTime)}】")
                        .setContentText("${it.content}(~ ${dateFormat.format(it.endTime)})")
                        .setStyle(Notification.BigTextStyle())
                        .build()
                    manager.notify(Random.nextInt(), notification)
                }
                alertsId = newAlertsId
                sharedPreferences.edit().putInt("alertsId", alertsId).apply()
            }
            delay(60000)
        }
    }

    private val client = OkHttpClient()
    private val call = run {
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("opendata.cwa.gov.tw")
            .addPathSegments("api/v1/rest/datastore/W-C0033-002")
            .addQueryParameter("Authorization", "CWA-6AA2EEEE-CCFD-4B33-9E9F-433C8128499D")
            .addQueryParameter("format", "XML")
            .build()
        val request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request)
    }

    suspend fun get(): List<Alert> = withContext(Dispatchers.IO) {
        XmlPullParserFactory.newInstance().newPullParser().run {
            setInput(call.clone().execute().body!!.charStream())
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
            val result = mutableListOf<Alert>()
            var alert = Alert("", Date(), Date(), "")
            while (true) {
                when (eventType) {
                    XmlPullParser.START_TAG -> when (name) {
                        "datasetDescription" -> alert = alert.copy(description = nextText())
                        "issueTime" -> alert = alert.copy(
                            issueTime = dateFormat.parse(nextText())!!
                        )
                        "endTime" -> alert = alert.copy(
                            endTime = dateFormat.parse(nextText())!!
                        )
                        "contentText" -> alert = alert.copy(content = nextText().trim())
                        else -> next()
                    }
                    XmlPullParser.END_TAG -> {
                        when (name) {
                            "record" -> result += alert
                        }
                        next()
                    }
                    XmlPullParser.END_DOCUMENT -> break
                    else -> next()
                }
            }
            result
        }
    }
}
