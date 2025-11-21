package com.destiny.when2go

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.destiny.when2go.model.DepartureInfo
import com.destiny.when2go.theming.Theme
import com.destiny.when2go.view.DepartureTable
import com.destiny.when2go.view.StopSelectionDropdown
import com.destiny.when2go.viewmodel.MainViewModel
import com.destiny.when2go.viewmodel.mainViewModelFactory
import green
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun ScrollableContent(
    departures: List<DepartureInfo>,
    stopName: String,
    walkingTimeMins: Int,
    nextLeaveTime: Int,
    targetDepartureRow: Int,
    allStopNames: List<String>,
    onStopSelected: (Int) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val scrollState = rememberScrollState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "To catch the next Light Rail..." text
            Text(
                text = buildAnnotatedString {
                    append("To catch the next Light Rail, leave in ")
                    withStyle(style = SpanStyle(color = green, fontWeight = FontWeight.Bold)) {
                        if (nextLeaveTime == 0) {
                            append("now")
                        } else {
                            append("$nextLeaveTime min")
                        }
                    }
                },
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 48.dp)
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 48.dp)
                    .fillMaxWidth(),
            )

            // Stop title with dropdown
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stopName,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                StopSelectionDropdown(
                    stops = allStopNames,
                    onStopSelected = onStopSelected
                )
            }

            Spacer(modifier = Modifier.height(8.dp)) // Spacing between Haymarket and table

            // Departure Table
            DepartureTable(departures, targetDepartureRow)

            Spacer(modifier = Modifier.height(16.dp)) // Spacing between table and walking info

            // Walking time info
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                    contentDescription = "Walking",
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "$walkingTimeMins min",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenContent(viewModel: MainViewModel = viewModel(factory = mainViewModelFactory)) {
    val departures by viewModel.nextDepartures.collectAsState()
    val stopName by viewModel.stopName.collectAsState()
    val walkingTime by viewModel.stopDistanceMins.collectAsState()
    val nextLeaveTime by viewModel.nextLeaveTimeMins.collectAsState()
    val targetDepartureRow by viewModel.targetDepartureRow.collectAsState()
    val stopNames by viewModel.stopNames.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
                title = {
                    Text(
                        "When2Go",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = { showMenu = !showMenu }) { // Toggle menu visibility
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More settings",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    // DropdownMenu
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false } // Hide menu when dismissed
                    ) {
                        DropdownMenuItem(
                            text = { Text("Refresh") },
                            onClick = {
                                viewModel.getNextDepartures()
                                showMenu = false // Hide menu after click
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Swap direction") },
                            onClick = {
                                viewModel.switchSpotPoint()
                                showMenu = false // Hide menu after click
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear preferences") },
                            onClick = {
                                viewModel.clearPreferences()
                                showMenu = false // Hide menu after click
                            }
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues->
        Box(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                ScrollableContent(
                    departures,
                    stopName,
                    walkingTime,
                    nextLeaveTime,
                    targetDepartureRow,
                    stopNames,
                    onStopSelected = { index -> viewModel.getNextDepartures(index) }
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 740)
@Composable
fun App() {
    Theme {
        ScreenContent()
    }
}
