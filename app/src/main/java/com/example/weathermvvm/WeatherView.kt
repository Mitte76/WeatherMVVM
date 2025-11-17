package com.example.weathermvvm

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.weathermvvm.helpers.NetworkHelper
import com.example.weathermvvm.models.Hour
import com.example.weathermvvm.models.LocationBase
import com.example.weathermvvm.models.LocationData
import com.example.weathermvvm.models.WeatherData
import com.example.weathermvvm.ui.theme.CardBg
import com.example.weathermvvm.ui.theme.DayBg
import com.example.weathermvvm.ui.theme.HourBg
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun WeatherView(viewModel: WeatherViewModel, modifier: Modifier = Modifier) {

    val weatherItems = viewModel.weatherData
    val context = LocalContext.current

    WeatherListWithArrows(
        weatherResponseList = weatherItems, modifier = modifier, onCitySubmit = {
            viewModel.fetchWeatherForLocation(
                LocationData(
                    city = it, basedOn = LocationBase.CITY

                ), context, false
            )
        })
}

@Composable
fun WeatherListWithArrows(
    weatherResponseList: List<WeatherData>,
    modifier: Modifier = Modifier,
    onCitySubmit: (String) -> Unit
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    Column(modifier = modifier) {
        Box(modifier = Modifier.weight(1f)) {
            WeatherCardList(listState = listState, weatherResponseList = weatherResponseList)
            ScrollArrowUp(listState, coroutineScope)
            ScrollArrowDown(listState, coroutineScope)
        }
        CityInputRow(onSubmit = onCitySubmit)
    }

}

@Composable
fun WeatherCardList(listState: LazyListState, weatherResponseList: List<WeatherData>) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(8.dp)
    ) {
        weatherResponseList.forEach { weatherData ->
            val weatherResponse = weatherData.weatherResponse
            item(key = "header_${weatherResponse.location.name}") {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minWidth = 160.dp)
                        .background(
                            CardBg,
                            RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        )
                        .padding(top = 8.dp, start = 8.dp, end = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(weatherResponse.location.name)
                        Text(weatherResponse.current.condition.text)
                    }
                }
            }
            itemsIndexed(weatherResponse.forecast.forecastday) { index, day ->
                val shape = when (index) {
                    0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    weatherResponse.forecast.forecastday.lastIndex -> RoundedCornerShape(
                        bottomStart = 16.dp, bottomEnd = 16.dp
                    )

                    else -> RoundedCornerShape(0.dp)
                }
                Row(
                    modifier = Modifier
                        .background(
                            CardBg, shape
                        )
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    Column {
                        Text(
                            day.date, modifier = Modifier
                                .background(
                                    DayBg,
                                    RoundedCornerShape(8.dp, 8.dp)
                                )
                                .padding(8.dp)
                        )
                        DailyForecastRow(day = day)
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun BoxScope.ScrollArrowUp(listState: LazyListState, scope: CoroutineScope) {
    val showUp by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
        }
    }

    if (showUp) {
        Icon(

            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = "Scroll up",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(50))
                .padding(4.dp)
                .clickable {
                    scope.launch {
                        listState.animateScrollToItem(0)
                    }
                })
    }
}

@Composable
private fun BoxScope.ScrollArrowDown(listState: LazyListState, scope: CoroutineScope) {
    val showDown by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visible = layoutInfo.visibleItemsInfo
            if (visible.isEmpty()) return@derivedStateOf false
            val last = visible.last()
            last.offset + last.size > layoutInfo.viewportEndOffset
        }
    }

    if (showDown) {

        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Scroll down",
            tint = Color.White,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(50))
                .padding(4.dp)
                .clickable {
                    scope.launch {
                        val lastIndex = listState.layoutInfo.totalItemsCount - 1
                        if (lastIndex >= 0) {
                            listState.animateScrollToItem(
                                index = lastIndex,
                                scrollOffset = listState.layoutInfo.viewportSize.height
                            )
                        }
                    }
                })
    }
}

@Composable
fun CityInputRow(
    onSubmit: (String) -> Unit, modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(modifier = modifier.height(60.dp)) {
        TextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            label = { Text("Enter city name") },
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    onSubmit(text.text)
                    text = TextFieldValue("")
                    keyboardController?.hide()
                })
        )
        Button(
            modifier = Modifier.fillMaxHeight(), shape = RoundedCornerShape(8.dp), onClick = {
                onSubmit(text.text)
                text = TextFieldValue("")
                keyboardController?.hide()
            }) {
            Text("Add")
        }
    }
}

@Composable
fun DailyForecastRow(day: com.example.weathermvvm.models.ForecastDay) {
    LazyRow(
        modifier = Modifier
            .background(
                DayBg,
                RoundedCornerShape(
                    0.dp, 8.dp, 8.dp,
                    8.dp
                )
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = day.hour, key = { hour -> hour.time_epoch }) { hour ->
            HourCard(hour = hour)
        }
    }
}

@Composable
fun HourCard(hour: Hour) {
    Column(
        modifier = Modifier
            .background(HourBg, RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Center
    ) {

        val context = LocalContext.current
        val imageBitmap by remember(hour.condition.icon) {
            NetworkHelper.getImage(context, "https:${hour.condition.icon}")
        }.collectAsState(initial = null)
        imageBitmap?.let {
            Image(
                modifier = Modifier.size(64.dp),
                bitmap = it.asImageBitmap(),
                contentDescription = hour.condition.text
            )
        }

        Text(hour.time.split(" ")[1])
        Spacer(modifier = Modifier.height(4.dp))
        Text("${hour.temp_c}°")
    }
}
