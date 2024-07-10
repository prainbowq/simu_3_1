package rainbow.weather.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import rainbow.weather.MainViewModel

@OptIn(ExperimentalMaterialApi::class)
class LocationsScreen(private val viewModel: MainViewModel) : Screen {
    lateinit var scope: CoroutineScope
    private val sheetState = ModalBottomSheetState(ModalBottomSheetValue.Hidden)
    private var keyword by mutableStateOf("")
    private var county by mutableStateOf(viewModel.counties.first())
    private var township by mutableStateOf(county.townships.first())

    private fun showSheet() {
        scope.launch {
            sheetState.show()
        }
    }

    private fun hideSheet() {
        scope.launch {
            sheetState.hide()
        }
    }

    private fun addLocation() {
        viewModel.addLocation(county.name to township.name)
        hideSheet()
    }

    @Composable
    override fun invoke() {
        scope = rememberCoroutineScope()
        ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetContent = {
                Row {
                    TextButton(::hideSheet) {
                        Text("取消")
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(::addLocation) {
                        Text("確定")
                    }
                }
                Row(Modifier.height(160.dp)) {
                    Wheel(
                        items = viewModel.counties,
                        onItemSelect = ::county::set,
                        itemToString = { it.name },
                        modifier = Modifier.weight(1f)
                    )
                    Wheel(
                        items = county.townships,
                        onItemSelect = ::township::set,
                        itemToString = { it.name },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        ) {
            Scaffold(topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(viewModel::pop) {
                            Icon(Icons.Default.ArrowBack, null)
                        }
                    },
                    title = { Text("地點編輯") }
                )
            }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(it)
                ) {
                    TextField(
                        value = keyword,
                        onValueChange = ::keyword::set,
                        placeholder = { Text("搜尋") },
                        trailingIcon = {
                            IconButton({}) {
                                Icon(Icons.Default.Search, null)
                            }
                        }
                    )
                    Row {
                        Button(::showSheet) {
                            Icon(Icons.Default.Search, null)
                            Text("選擇鄉鎮")
                        }
                        Button({}) {
                            Icon(Icons.Default.Place, null)
                            Text("選擇育樂")
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("我的最愛")
                        Spacer(Modifier.weight(1f))
                        TextButton({}) {
                            Text("編輯")
                        }
                    }
                    Divider()
                    LazyColumn {
                        item {
                            ListItem(
                                icon = { Icon(Icons.Default.Place, null) },
                                trailing = { Icon(Icons.Default.ArrowForward, null) }
                            ) {
                                Text("重新定位")
                            }
                            Divider()
                        }
                        items(viewModel.locations) {
                            Surface({
                                viewModel.location = it
                                viewModel.pop()
                            }) {
                                ListItem(trailing = { Icon(Icons.Default.ArrowForward, null) }) {
                                    Text("${it.first}${it.second}")
                                }
                            }
                            Divider()
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun <T> Wheel(
        items: List<T>,
        onItemSelect: (T) -> Unit,
        modifier: Modifier = Modifier,
        itemToString: (T) -> String = { it.toString() }
    ) {
        val state = rememberLazyListState()
        LazyColumn(
            state = state,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(50.dp),
            modifier = modifier.drawWithContent {
                drawContent()
                listOf(-50, 50).forEach {
                    drawLine(
                        color = Color.Black,
                        start = Offset(0f, center.y + it),
                        end = Offset(size.width, center.y + it),
                        strokeWidth = 5f
                    )
                }
            }
        ) {
            item {
                Text("")
            }
            items(items) {
                Text(itemToString(it))
            }
            item {
                Text("")
            }
        }
        LaunchedEffect(items) {
            state.scrollToItem(0)
        }
        val item by remember(items) {
            derivedStateOf {
                items[state.firstVisibleItemIndex +
                        if (state.firstVisibleItemScrollOffset > 90) 1 else 0]
            }
        }
        LaunchedEffect(item) {
            onItemSelect(item)
        }
    }
}
