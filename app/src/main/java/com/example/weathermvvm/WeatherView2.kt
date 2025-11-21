//package com.example.weathermvvm
//
//import androidx.compose.animation.AnimatedVisibility
//import androidx.compose.animation.animateContentSize
//import androidx.compose.animation.core.FastOutSlowInEasing
//import androidx.compose.animation.core.LinearOutSlowInEasing
//import androidx.compose.animation.core.animateDpAsState
//import androidx.compose.animation.core.tween
//import androidx.compose.animation.expandVertically
//import androidx.compose.animation.fadeIn
//import androidx.compose.animation.fadeOut
//import androidx.compose.animation.shrinkVertically
//import androidx.compose.animation.slideInVertically
//import androidx.compose.animation.slideOutVertically
//import androidx.compose.foundation.ExperimentalFoundationApi
//import androidx.compose.foundation.Image
//import androidx.compose.foundation.background
//import androidx.compose.foundation.clickable
//import androidx.compose.foundation.interaction.MutableInteractionSource
//import androidx.compose.foundation.layout.Arrangement
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.BoxScope
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.IntrinsicSize
//import androidx.compose.foundation.layout.PaddingValues
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.Spacer
//import androidx.compose.foundation.layout.defaultMinSize
//import androidx.compose.foundation.layout.fillMaxHeight
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.foundation.layout.height
//import androidx.compose.foundation.layout.padding
//import androidx.compose.foundation.layout.size
//import androidx.compose.foundation.layout.width
//import androidx.compose.foundation.lazy.LazyColumn
//import androidx.compose.foundation.lazy.LazyListState
//import androidx.compose.foundation.lazy.LazyRow
//import androidx.compose.foundation.lazy.items
//import androidx.compose.foundation.lazy.itemsIndexed
//import androidx.compose.foundation.lazy.rememberLazyListState
//import androidx.compose.foundation.shape.RoundedCornerShape
//import androidx.compose.foundation.text.KeyboardActions
//import androidx.compose.foundation.text.KeyboardOptions
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.Add
//import androidx.compose.material.icons.filled.KeyboardArrowDown
//import androidx.compose.material.icons.filled.KeyboardArrowUp
//import androidx.compose.material3.Button
//import androidx.compose.material3.ButtonDefaults
//import androidx.compose.material3.FloatingActionButton
//import androidx.compose.material3.Icon
//import androidx.compose.material3.Text
//import androidx.compose.material3.TextField
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.collectAsState
//import androidx.compose.runtime.derivedStateOf
//import androidx.compose.runtime.getValue
//import androidx.compose.runtime.mutableStateOf
//import androidx.compose.runtime.remember
//import androidx.compose.runtime.rememberCoroutineScope
//import androidx.compose.runtime.setValue
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.draw.clipToBounds
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.graphics.Shape
//import androidx.compose.ui.graphics.asImageBitmap
//import androidx.compose.ui.graphics.vector.ImageVector
//import androidx.compose.ui.layout.onGloballyPositioned
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.platform.LocalDensity
//import androidx.compose.ui.platform.LocalSoftwareKeyboardController
//import androidx.compose.ui.text.input.ImeAction
//import androidx.compose.ui.text.input.TextFieldValue
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import com.example.weathermvvm.helpers.NetworkHelper
//import com.example.weathermvvm.models.ForecastDay
//import com.example.weathermvvm.models.Hour
//import com.example.weathermvvm.models.LocationBase
//import com.example.weathermvvm.models.LocationData
//import com.example.weathermvvm.models.WeatherData
//import com.example.weathermvvm.ui.theme.AccentBlue
//import com.example.weathermvvm.ui.theme.AppBg
//import com.example.weathermvvm.ui.theme.CardBg
//import com.example.weathermvvm.ui.theme.DayBg
//import com.example.weathermvvm.ui.theme.HourBg
//import com.example.weathermvvm.ui.theme.TextColor
//import kotlinx.coroutines.launch
//
//
//@Composable
//fun WeatherView(viewModel: WeatherViewModel) {
//
//    val weatherItems by viewModel.weatherData.collectAsState()
//    WeatherListWithArrows(
//        weatherResponseList = weatherItems, onCitySubmit = {
//            viewModel.fetchWeatherForLocation(
//                LocationData(
//                    city = it, basedOn = LocationBase.CITY
//                ), false
//            )
//        }
//    )
//}
//
//@Composable
//fun WeatherListWithArrows(
//    weatherResponseList: List<WeatherData>,
//    modifier: Modifier = Modifier,
//    onCitySubmit: (String) -> Unit
//) {
//    val listState = rememberLazyListState()
//    val coroutineScope = rememberCoroutineScope()
//    val showUpArrow by remember { derivedStateOf { listState.canScrollBackward } }
//    val showDownArrow by remember { derivedStateOf { listState.canScrollForward } }
//    var showAddCityInput by remember { mutableStateOf(false) }
//
//    Box(modifier = modifier.fillMaxSize()) {
//        Column(
//            modifier = modifier
//                .fillMaxSize()
//                .background(AppBg),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                "Weather test app",
//                color = TextColor, fontSize = 24.sp,
//                modifier = Modifier
//                    .padding(20.dp)
//            )
//            Box(
//                modifier = Modifier
//                    .weight(1f)
//            )
//            {
//                WeatherCardList(listState = listState, weatherResponseList = weatherResponseList)
//
//                ScrollArrow(
//                    isVisible = showUpArrow,
//                    alignment = Alignment.TopCenter,
//                    icon = Icons.Default.KeyboardArrowUp,
//                    onClick = {
//                        coroutineScope.launch {
//                            listState.animateScrollToItem(0)
//                        }
//                    }
//                )
//
//                ScrollArrow(
//                    isVisible = showDownArrow,
//                    alignment = Alignment.BottomCenter,
//                    icon = Icons.Default.KeyboardArrowDown,
//                    onClick = {
//                        coroutineScope.launch {
//                            val lastIndex = listState.layoutInfo.totalItemsCount - 1
//                            if (lastIndex >= 0) {
//                                listState
//                                    .animateScrollToItem(
//                                        index = lastIndex,
//                                        scrollOffset = listState.layoutInfo.viewportSize.height
//                                    )
//                            }
//                        }
//                    }
//                )
//            }
//        }
//
//        if (showAddCityInput) {
//            Box(
//                modifier = Modifier
//                    .fillMaxSize()
//                    .background(Color.Black.copy(alpha = 0.5f))
//                    .clickable(
//                        interactionSource = remember { MutableInteractionSource() },
//                        indication = null,
//                        onClick = {
//                            showAddCityInput = false
//                        }
//                    )
//            )
//        }
//
//        AnimatedVisibility(
//            modifier = Modifier.align(Alignment.BottomCenter),
//            visible = showAddCityInput,
//            enter = slideInVertically(initialOffsetY = { it }),
//            exit = slideOutVertically(targetOffsetY = { it })
//        ) {
//            CityInputRow(
//                onSubmit = { city ->
//                    onCitySubmit(city)
//                    showAddCityInput = false
//                },
//                modifier = Modifier.clickable(enabled = false) {}
//            )
//        }
//        if (!showAddCityInput) {
//            FloatingActionButton(
//                onClick = { showAddCityInput = true },
//                modifier = Modifier
//                    .align(Alignment.BottomEnd)
//                    .padding(16.dp),
//                containerColor = AccentBlue
//            ) {
//                Icon(Icons.Default.Add, contentDescription = "Add city")
//            }
//        }
//    }
//
//}
//
//
//@Composable
//fun WeatherCardList(
//    listState: LazyListState,
//    weatherResponseList: List<WeatherData>
//) {
//    var expandedCardKey by remember { mutableStateOf<String?>(null) }
//
//    LazyColumn(
//        state = listState,
//        modifier = Modifier.fillMaxSize(),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        contentPadding = PaddingValues(8.dp)
//    ) {
//
//        weatherResponseList.forEach { weatherData ->
//
//            val cityName = weatherData.weatherResponse.location.name
//            val cardKey = "city_$cityName"
//            val isExpanded = expandedCardKey == cardKey
//            val forecastDays = weatherData.weatherResponse.forecast.forecastday
//
//            item(key = "header_$cardKey") {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .height(IntrinsicSize.Min)
//                        .clickable {
//                            expandedCardKey = if (isExpanded) null else cardKey
//                        }
//                        .animateItem(),
//                    verticalAlignment = Alignment.CenterVertically
//                ) {
//                    InvertedCorner(
//                        corner = Corner.BottomRight,
//                        cornerRadius = 16.dp,
//                        color = CardBg,
//                        modifier = Modifier.weight(1f).fillMaxHeight()
//                    )
//
//                    Box(
//                        modifier = Modifier
//                            .defaultMinSize(minWidth = 160.dp)
//                            .background(
//                                CardBg,
//                                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
//                            )
//                            .padding(horizontal = 8.dp),
//                        contentAlignment = Alignment.Center
//                    ) {
//                        Column(
//                            modifier = Modifier.padding(8.dp),
//                            horizontalAlignment = Alignment.CenterHorizontally
//                        ) {
//                            Text(cityName, color = TextColor)
//                            Text(
//                                weatherData.weatherResponse.current.condition.text,
//                                color = TextColor
//                            )
//                        }
//                    }
//
//                    InvertedCorner(
//                        corner = Corner.BottomLeft,
//                        cornerRadius = 16.dp,
//                        color = CardBg,
//                        modifier = Modifier.weight(1f).fillMaxHeight()
//                    )
//                }
//            }
//
//            itemsIndexed(
//                items = forecastDays,
//                key = { _, day -> "forecast_${cardKey}_${day.date_epoch}" }
//            ) { index, day ->
//
//                val isFirst = index == 0
//                val isLast = index == forecastDays.lastIndex
//                val singleItem = forecastDays.size == 1
//
//                val shape = when {
//                    !isExpanded || singleItem -> RoundedCornerShape(16.dp)
//                    isFirst -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
//                    isLast -> RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
//                    else -> RoundedCornerShape(0.dp)
//                }
//
//                if (isFirst) {
//                    DailyForecastRowItem(day = day, shape = shape)
//                } else {
//                    AnimatedVisibility(
//                        visible = isExpanded,
//                        enter = fadeIn(animationSpec = tween(150)) + expandVertically(animationSpec = tween(300)),
//                        exit = fadeOut(animationSpec = tween(150)) + shrinkVertically(animationSpec = tween(300))
//                    ) {
//                        DailyForecastRowItem(day = day, shape = shape)
//                    }
//                }
//            }
//            item(key = "spacer_$cardKey") {
//                Spacer(modifier = Modifier.height(24.dp))
//            }
//        }
//    }
//}
//
//@Composable
//private fun DailyForecastRowItem(day: ForecastDay, shape: Shape) {
//    Column(
//        modifier = Modifier
//            .background(CardBg, shape)
//            .fillMaxWidth()
//            .padding(horizontal = 12.dp, vertical = 12.dp)
//    ) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(IntrinsicSize.Min),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                day.date,
//                modifier = Modifier
//                    .background(
//                        DayBg,
//                        RoundedCornerShape(12.dp, 12.dp)
//                    )
//                    .padding(8.dp),
//                color = TextColor
//            )
//            InvertedCorner(
//                corner = Corner.BottomLeft,
//                cornerRadius = 8.dp,
//                color = DayBg,
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxHeight()
//            )
//        }
//        DailyForecastRow(day = day)
//    }
//}
//
//@Composable
//private fun BoxScope.ScrollArrow(
//    isVisible: Boolean,
//    alignment: Alignment,
//    icon: ImageVector,
//    onClick: () -> Unit
//) {
//    AnimatedVisibility(
//        visible = isVisible,
//        modifier = Modifier.align(alignment),
//        enter = fadeIn(),
//        exit = fadeOut()
//    ) {
//        Icon(
//            imageVector = icon,
//            contentDescription = null,
//            tint = Color.White,
//            modifier = Modifier
//                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(50))
//                .padding(4.dp)
//                .clickable(onClick = onClick)
//        )
//    }
//}
//
//@Composable
//fun CityInputRow(
//    onSubmit: (String) -> Unit,
//    modifier: Modifier = Modifier
//) {
//    var text by remember { mutableStateOf(TextFieldValue("")) }
//    val keyboardController = LocalSoftwareKeyboardController.current
//
//    Column(
//        modifier = modifier
//            .fillMaxWidth()
//            .background(
//                color = CardBg,
//                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
//            )
//            .padding(16.dp)
//    ) {
//        Row(
//            modifier = Modifier.height(60.dp),
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            TextField(
//                value = text,
//                onValueChange = { text = it },
//                singleLine = true,
//                label = { Text("Enter city name") },
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxHeight(),
//                keyboardOptions = KeyboardOptions.Default.copy(
//                    imeAction = ImeAction.Done
//                ),
//                keyboardActions = KeyboardActions(onDone = {
//                    if (text.text.isNotBlank()) {
//                        onSubmit(text.text)
//                        keyboardController?.hide()
//                    }
//                })
//            )
//
//            Spacer(modifier = Modifier.width(8.dp))
//
//            Button(
//                modifier = Modifier.fillMaxHeight(),
//                shape = RoundedCornerShape(8.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
//                onClick = {
//                    if (text.text.isNotBlank()) {
//                        onSubmit(text.text)
//                        text = TextFieldValue("")
//                        keyboardController?.hide()
//                    }
//                }) {
//                Text("Add")
//            }
//        }
//    }
//}
//
//@Composable
//fun DailyForecastRow(day: ForecastDay) {
//    LazyRow(
//        modifier = Modifier
//            .background(
//                DayBg,
//                RoundedCornerShape(
//                    0.dp, 8.dp, 8.dp,
//                    8.dp
//                )
//            ),
//        horizontalArrangement = Arrangement.spacedBy(8.dp),
//        contentPadding = PaddingValues(8.dp)
//    ) {
//        items(
//            items = day.hour, key = { hour -> hour.time_epoch }) { hour ->
//            HourCard(hour = hour)
//        }
//    }
//}
//
//@Composable
//fun HourCard(hour: Hour) {
//    Column(
//        modifier = Modifier
//            .background(HourBg, RoundedCornerShape(8.dp))
//            .padding(8.dp),
//    ) {
//
//        val context = LocalContext.current
//        val imageBitmap by remember(hour.condition.icon) {
//            NetworkHelper.getImage(context, "https:${hour.condition.icon}")
//        }.collectAsState(initial = null)
//        imageBitmap?.let {
//            Image(
//                modifier = Modifier.size(64.dp),
//                bitmap = it.asImageBitmap(),
//                contentDescription = hour.condition.text
//            )
//        }
//
//        Text(hour.time.split(" ")[1], color = TextColor)
//        Spacer(modifier = Modifier.height(4.dp))
//        Text("${hour.temp_c}°", color = TextColor)
//    }
//}
