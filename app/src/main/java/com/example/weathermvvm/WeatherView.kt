package com.example.weathermvvm

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import com.example.weathermvvm.models.LocationBase
import com.example.weathermvvm.models.LocationData
import com.example.weathermvvm.models.WeatherData
import com.example.weathermvvm.models.WeatherResponse
import com.example.weathermvvm.ui.theme.AppBg
import com.example.weathermvvm.ui.theme.CardBg
import com.example.weathermvvm.ui.theme.HourBg
import kotlinx.coroutines.launch

@Composable
fun WeatherView(viewModel: WeatherViewModel, modifier: Modifier = Modifier) {

    val weatherItems = viewModel.weatherData
    val context = LocalContext.current

    WeatherListWithArrows(
        weatherResponseList = weatherItems,
        modifier = modifier,
        onCitySubmit = {
            viewModel.fetchWeatherForLocation(
                LocationData(
                    city = it,
                    basedOn = LocationBase.CITY

                ), context
                ,false
            )

            println(viewModel) }
    )




}



@Composable
fun WeatherListWithArrows(
    weatherResponseList: List<WeatherData>,
    modifier: Modifier = Modifier,
    onCitySubmit: (String) -> Unit
) {
    Column(
        modifier = modifier
            .background(AppBg)
            .fillMaxHeight()
    ) {
        val listState = rememberLazyListState()
        val coroutineScope = rememberCoroutineScope()

        val showUpArrow by remember {
            derivedStateOf {
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0
            }
        }

        val showDownArrow by remember {
            derivedStateOf {
                val layoutInfo = listState.layoutInfo
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isEmpty()) return@derivedStateOf false
                val lastVisible = visibleItems.last()
                lastVisible.offset + lastVisible.size > layoutInfo.viewportEndOffset
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(weatherResponseList) { weatherdata ->
                    WeatherCard(weatherdata.weatherResponse)
                }

            }

            if (showUpArrow) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll up",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(4.dp)
                        .clickable {
                            coroutineScope.launch {
                                val targetIndex =
                                    (listState.firstVisibleItemIndex - 1).coerceAtLeast(0)
                                listState.animateScrollToItem(targetIndex)
                            }
                        }
                )
            }

            if (showDownArrow) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Scroll down",
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(50))
                        .padding(4.dp)
                        .clickable {
                            coroutineScope.launch {
                                val targetIndex =
                                    (listState.firstVisibleItemIndex + 1).coerceAtMost(
                                        listState.layoutInfo.totalItemsCount - 1
                                    )
                                listState.animateScrollToItem(targetIndex)
                            }
                        }
                )
            }
        }

        CityInputRow(onSubmit = onCitySubmit)
    }
}

@Composable
fun CityInputRow(
    onSubmit: (String) -> Unit,
    modifier: Modifier = Modifier
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
                }
            )
        )
        Button(
            modifier = Modifier.fillMaxHeight(),
            shape = RoundedCornerShape(8.dp),
            onClick = {
                onSubmit(text.text)
                text = TextFieldValue("")
                keyboardController?.hide()
            }
        ) {
            Text("Add")
        }
    }
}

@Composable
fun WeatherCard(weatherResponse: WeatherResponse) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier
                .background(CardBg, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (weatherResponse.location.name.toLowerCase(Locale.current) != "null") {
                Text(weatherResponse.location.name)
                Spacer(modifier = Modifier.height(4.dp))
            }
            Text(weatherResponse.current.condition.text)
        }

        Column(
            modifier = Modifier
                .background(CardBg, RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            weatherResponse.forecast?.forecastday?.forEach { day ->
                Text(day.date)
                LazyRow(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(day.hour) { index, hour ->
                        Column(
                            modifier = Modifier
                                .height(80.dp)
                                .background(HourBg, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(hour.time.split(" ")[1])
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(hour.temp_c.toString())
                        }
                        if (index != day.hour.lastIndex) {
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                    }
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            }
        }
    }
}
