package com.destiny.when2go.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.destiny.when2go.model.DepartureInfo
import green
import tableBorderColor
import tableRowBackground
import tableRowHighlightedBackground


@Composable
fun DepartureTable(departures: List<DepartureInfo>, highlightRow: Int) {

    Column(modifier = Modifier.fillMaxWidth()) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(vertical = 0.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // You can adjust this fontSize to be even smaller if needed, e.g., 10.sp
            val headerFontSize = 10.sp
            Text("Travelling to", modifier = Modifier.weight(0.35f), fontWeight = FontWeight.Bold, fontSize = headerFontSize, color = Color.White)
            Text("Departing in", modifier = Modifier.weight(0.35f), fontWeight = FontWeight.Bold, fontSize = headerFontSize, textAlign = TextAlign.Center, color = Color.White)
            Text("Leave in", modifier = Modifier.weight(0.3f), fontWeight = FontWeight.Bold, fontSize = headerFontSize, textAlign = TextAlign.End, color = Color.White)
        }

        // Table Rows
        departures.forEachIndexed { index, departure ->
            val isHighlightedRow = index == highlightRow
            DepartureRow(departure, isHighlightedRow)
        }
    }
}

@Composable
private fun DepartureRow(departure: DepartureInfo, isHighlightedRow: Boolean) {
    val backgroundColor = if (isHighlightedRow) tableRowHighlightedBackground else tableRowBackground
    val fontSize = if (isHighlightedRow) 18.sp else 14.sp
    val iconSize = if (isHighlightedRow) 20.dp else 16.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .border(0.5.dp, tableBorderColor)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(departure.travellingTo, modifier = Modifier.weight(0.35f), fontSize = fontSize)
        Row(modifier = Modifier.weight(0.35f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text(departure.departingIn.formatTime(), fontSize = fontSize, textAlign = TextAlign.Start)
            if (departure.isRealtime) {
                Icon(
                    imageVector = Icons.Filled.Sensors,
                    contentDescription = "Realtime",
                    tint = green,
                    modifier = Modifier.size(iconSize).padding(start = 4.dp)
                )
            }
        }
        Text(departure.leaveIn.formatTime(), modifier = Modifier.weight(0.3f), fontSize = fontSize, textAlign = TextAlign.End)
    }
}

private fun Long.formatTime(): String = "$this min"