package rainbow.weather.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import rainbow.weather.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class ShortForecastScreen(private val viewModel: MainViewModel) : Screen {
    private val dateDateFormat = SimpleDateFormat("MM/dd(EEEEE)", Locale.TAIWAN)
    private val timeDateFormat = SimpleDateFormat("ha", Locale.ROOT)

    @Composable
    override fun invoke() {
        Scaffold(topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(viewModel::pop) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                title = { Text("${viewModel.location.first}${viewModel.location.second}") }
            )
        }) {
            LazyColumn(Modifier.padding(it)) {
                item {
                    Text("逐3小時預報")
                }
                if (viewModel.shortForecast != null) {
                    viewModel.shortForecast!!.timeList
                        .groupingBy(dateDateFormat::format)
                        .eachCount().forEach { date, size ->
                            items(size) { index ->
                                Surface(color = Color(0xFF3666A0), contentColor = Color.White) {
                                    Box(Modifier.fillMaxWidth()) {
                                        Column {
                                            Text(date)
                                            Text(
                                                text = timeDateFormat.format(
                                                    viewModel.shortForecast!!.timeList[index]
                                                ),
                                                color = Color.Yellow
                                            )
                                            Text("${viewModel.shortForecast!!.tList[index]}°C")
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                WeatherIcon(
                                                    code = viewModel.shortForecast!!.wxList[index].first,
                                                    modifier = Modifier.size(40.dp)
                                                )
                                                Text(viewModel.shortForecast!!.wxList[index].second)
                                            }
                                            Text("體感 ${viewModel.shortForecast!!.atList[index]}°C")
                                        }
                                        Column(Modifier.align(Alignment.BottomEnd)) {
                                            Text("降雨機率 ${viewModel.shortForecast!!.popList[index]}%")
                                            Text("相對濕度 ${viewModel.shortForecast!!.rhList[index]}%")
                                        }
                                    }
                                }
                                Divider()
                            }
                            item {
                                Spacer(Modifier.height(20.dp))
                            }
                        }
                } else item {
                    CircularProgressIndicator()
                }
            }
        }
        LaunchedEffect(Unit) {
            viewModel.updateShortForecast()
        }
    }
}
