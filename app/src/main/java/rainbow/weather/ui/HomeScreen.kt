package rainbow.weather.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rainbow.weather.MainViewModel
import kotlin.math.max
import kotlin.math.min

class HomeScreen(private val viewModel: MainViewModel) : Screen {
    private lateinit var scope: CoroutineScope
    private val tabs = listOf("現在", "預報", "觀測")
    private val scaffoldState = ScaffoldState(DrawerState(DrawerValue.Closed), SnackbarHostState())
    private var tabIndex by mutableStateOf(0)

    private fun openDrawer() {
        scope.launch {
            scaffoldState.drawerState.open()
        }
    }

    private fun closeDrawer() {
        scope.launch {
            scaffoldState.drawerState.close()
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun invoke() {
        scope = rememberCoroutineScope()
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(::openDrawer) {
                            Icon(Icons.Default.Menu, null)
                        }
                    },
                    title = { Text("${viewModel.location.first}${viewModel.location.second}") },
                    actions = {
                        IconButton({ viewModel.push(LocationsScreen(viewModel)) }) {
                            Icon(Icons.Default.Place, null)
                        }
                        IconButton({ viewModel.push(AlertsScreen(viewModel)) }) {
                            Icon(Icons.Default.Warning, null)
                        }
                    }
                )
            },
            drawerContent = {
                Surface(::closeDrawer) {
                    ListItem(icon = { Icon(Icons.Default.Home, null) }) {
                        Text("首頁")
                    }
                }
            }
        ) {
            Column(Modifier.padding(it)) {
                TabRow(tabIndex) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(tabIndex == index, { tabIndex = index }, text = { Text(tab) })
                    }
                }
                when (tabIndex) {
                    1 -> ForecastPage()
                }
            }
        }
    }

    @Composable
    private fun ForecastPage(modifier: Modifier = Modifier) {
        LazyColumn(modifier) {
            item {
                Surface(color = Color(0xFF3666A0), contentColor = Color.White) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("逐3小時預報")
                            Spacer(Modifier.weight(1f))
                            IconButton({}) {
                                Icon(Icons.Default.Menu, null)
                            }
                        }
                        ShortForecastChart(
                            Modifier
                                .height(200.dp)
                                .horizontalScroll(rememberScrollState())
                                .width(2000.dp)
                        )
                        Divider()
                        TextButton(
                            onClick = { viewModel.push(ShortForecastScreen(viewModel)) },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                        ) {
                            Text("逐3小時詳細預報")
                            Icon(Icons.Default.ArrowForward, null)
                        }
                    }
                }
            }
        }
        LaunchedEffect(Unit) {
            viewModel.updateShortForecast()
        }
    }

    @Composable
    private fun ShortForecastChart(modifier: Modifier) {
        Canvas(modifier) {
            viewModel.shortForecast?.run {
                inset(30f) {
                    val xList = List(timeList.size) { size.width / (timeList.size - 1) * it }
                    val max = max(tList.max(), atList.max())
                    val min = min(tList.min(), atList.min())
                    val tPoints = tList.mapIndexed { index, t ->
                        Offset(xList[index], size.height - size.height / (max - min) * (t - min))
                    }
                    val atPoints = atList.mapIndexed { index, t ->
                        Offset(xList[index], size.height - size.height / (max - min) * (t - min))
                    }
                    drawPoints(tPoints, PointMode.Polygon, Color.White, 5f)
                    drawPoints(tPoints, PointMode.Points, Color.White, 20f, StrokeCap.Round)
                    drawPoints(
                        points = tPoints.flatMap { listOf(it, Offset(it.x, size.height)) },
                        pointMode = PointMode.Lines,
                        color = Color.White,
                        strokeWidth = 5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                    drawPoints(atPoints, PointMode.Polygon, Color.Yellow, 5f)
                    drawPoints(atPoints, PointMode.Points, Color.Yellow, 20f, StrokeCap.Round)
                    drawIntoCanvas { canvas ->
                        val tPaint = Paint().apply {
                            color = Color.White.toArgb()
                            textSize = 50f
                            textAlign = Paint.Align.CENTER
                        }
                        tList.forEachIndexed { index, t ->
                            canvas.nativeCanvas.drawText(
                                "$t",
                                tPoints[index].x,
                                tPoints[index].y - 20,
                                tPaint
                            )
                        }
                        val atPaint = Paint().apply {
                            color = Color.Yellow.toArgb()
                            textSize = 50f
                            textAlign = Paint.Align.CENTER
                        }
                        atList.forEachIndexed { index, t ->
                            canvas.nativeCanvas.drawText(
                                "$t",
                                atPoints[index].x,
                                atPoints[index].y - 20,
                                atPaint
                            )
                        }
                    }
                }
            }
        }
    }
}
