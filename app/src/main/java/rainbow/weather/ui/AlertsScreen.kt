package rainbow.weather.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import rainbow.weather.MainViewModel
import java.text.SimpleDateFormat
import java.util.*

class AlertsScreen(private val viewModel: MainViewModel) : Screen {
    private val dateFormat = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.ROOT)

    @OptIn(ExperimentalMaterialApi::class)
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
            val alerts by viewModel.alertsFlow.collectAsState(emptyList())
            LazyColumn(Modifier.padding(it)) {
                items(alerts) {
                    Surface({}) {
                        ListItem(
                            icon = { Icon(Icons.Default.Warning, null) },
                            secondaryText = { Text(dateFormat.format(it.issueTime)) },
                            trailing = { Icon(Icons.Default.ArrowForward, null) }
                        ) {
                            Text(it.description)
                        }
                    }
                }
            }
        }
    }
}
