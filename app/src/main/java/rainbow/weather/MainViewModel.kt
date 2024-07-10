package rainbow.weather

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import rainbow.weather.data.Alert
import rainbow.weather.data.County
import rainbow.weather.data.ShortForecast
import rainbow.weather.ui.HomeScreen
import rainbow.weather.ui.Screen
import java.text.SimpleDateFormat
import java.util.*

class MainViewModel(context: Context) : ViewModel() {
    private val sharedPreferences = context.getSharedPreferences("weather", Context.MODE_PRIVATE)

    val counties = context.assets.open("counties.json")
        .readBytes()
        .decodeToString()
        .let(::JSONArray)
        .toList(County::fromJson)

    private val _locations = mutableStateListOf<Pair<String, String>>().apply {
        sharedPreferences
            .getString("locations", null)
            ?.split("`,`")
            ?.map { it.substringBefore("`!`") to it.substringAfter("`!`") }
            ?.forEach(::add)
    }
    val locations: List<Pair<String, String>> get() = _locations
    fun addLocation(location: Pair<String, String>) {
        if (location in locations) return
        _locations += location
        sharedPreferences.edit()
            .putString("locations", locations.joinToString("`,`") { "${it.first}`!`${it.second}" })
            .apply()
    }

    var location by mutableStateOf("臺北市" to "大安區")

    var shortForecast by mutableStateOf<ShortForecast?>(null)
    fun updateShortForecast() {
        viewModelScope.launch {
            shortForecast = getShortForecast(location)
        }
    }

    private val client = OkHttpClient()
    private suspend fun getShortForecast(
        location: Pair<String, String>
    ) = withContext(Dispatchers.IO) {
        val id = counties.first { it.name == location.first }.shortId
        val url = HttpUrl.Builder()
            .scheme("https")
            .host("opendata.cwa.gov.tw")
            .addPathSegments("api/v1/rest/datastore/$id")
            .addQueryParameter("Authorization", "CWA-6AA2EEEE-CCFD-4B33-9E9F-433C8128499D")
            .addQueryParameter("format", "XML")
            .addQueryParameter("locationName", location.second)
            .build()
        val request = Request.Builder()
            .url(url)
            .build()
        XmlPullParserFactory.newInstance().newPullParser().run {
            setInput(client.newCall(request).execute().body!!.charStream())
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
            var shortForecast = ShortForecast(
                timeList = emptyList(),
                tList = emptyList(),
                wxList = emptyList(),
                atList = emptyList(),
                popList = emptyList(),
                rhList = emptyList()
            )
            val timeList = mutableListOf<Date>()
            var elementName = ""
            val values = mutableListOf<String>()
            while (true) {
                when (eventType) {
                    XmlPullParser.START_TAG -> when (name) {
                        "dataTime" ->
                            if (elementName == "T") timeList += dateFormat.parse(nextText())!!
                            else next()
                        "elementName" -> elementName = nextText()
                        "value" -> values += nextText()
                        else -> next()
                    }
                    XmlPullParser.END_TAG -> {
                        when (name) {
                            "weatherElement" -> {
                                when (elementName) {
                                    "T" -> shortForecast = shortForecast.copy(
                                        timeList = timeList.toList(),
                                        tList = values.map { it.toInt() }
                                    )
                                    "Wx" -> shortForecast = shortForecast.copy(
                                        wxList = values.chunked(2) { it.last() to it.first() }
                                    )
                                    "AT" -> shortForecast = shortForecast.copy(
                                        atList = values.map { it.toInt() }
                                    )
                                    "PoP6h" -> shortForecast = shortForecast.copy(
                                        popList = values.flatMap { value ->
                                            List(2) { value.toInt() }
                                        }
                                    )
                                    "RH" -> shortForecast = shortForecast.copy(
                                        rhList = values.map { it.toInt() }
                                    )
                                }
                                values.clear()
                            }
                        }
                        next()
                    }
                    XmlPullParser.END_DOCUMENT -> break
                    else -> next()
                }
            }
            shortForecast
        }
    }

    val alertsFlow = flow {
        emit(getAlerts())
        delay(60000)
    }

    private val alertCall = run {
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

    suspend fun getAlerts(): List<Alert> = withContext(Dispatchers.IO) {
        XmlPullParserFactory.newInstance().newPullParser().run {
            setInput(alertCall.clone().execute().body!!.charStream())
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

    private val screens = mutableStateListOf<Screen>(HomeScreen(this))
    val screen get() = screens.lastOrNull()

    fun push(value: Screen) {
        screens += value
    }

    fun pop() {
        screens.removeLast()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>) = MainViewModel(context) as T
    }
}
