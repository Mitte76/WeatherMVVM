package com.example.weathermvvm

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weathermvvm.Constants.DAY_ITEM_HEIGHT
import com.example.weathermvvm.Constants.HOURS_ROW_HEIGHT
import com.example.weathermvvm.Constants.HOUR_CARD_HEIGHT
import com.example.weathermvvm.helpers.NetworkHelper
import com.example.weathermvvm.models.ForecastDay
import com.example.weathermvvm.models.Hour
import com.example.weathermvvm.models.LocationBase
import com.example.weathermvvm.models.LocationData
import com.example.weathermvvm.models.WeatherData
import com.example.weathermvvm.ui.theme.AccentBlue
import com.example.weathermvvm.ui.theme.AppBg
import com.example.weathermvvm.ui.theme.CardBg
import com.example.weathermvvm.ui.theme.DayBg
import com.example.weathermvvm.ui.theme.HourBg
import com.example.weathermvvm.ui.theme.TextColor
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun WeatherView(
    viewModel: WeatherViewModel,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        WeatherCardContainer(viewModel)
        CityInputOverlayController(viewModel)
    }

}

@Composable
fun BoxScope.CityInputOverlayController(viewModel: WeatherViewModel) {
    var showAddCityInput by remember { mutableStateOf(false) }

    val onDismiss = remember { { showAddCityInput = false } }

    val onCitySubmit = remember(viewModel) {
        { city: String ->

            showAddCityInput = false
            viewModel.fetchWeatherForLocation(
                LocationData(
                    city = city, basedOn = LocationBase.CITY
                ), false
            )
        }
    }

    AddCityInputOverlay(
        showAddCityInput = showAddCityInput, onDismiss = onDismiss, onCitySubmit = onCitySubmit
    )

    if (!showAddCityInput) {
        val onFabClick = remember { { showAddCityInput = true } }
        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
            containerColor = AccentBlue
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add city")
        }
    }
}

@Composable
fun WeatherCardContainer(
    viewModel: WeatherViewModel,
) {

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBg),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Weather test app",
            color = TextColor,
            fontSize = 24.sp,
            modifier = Modifier.padding(20.dp)
        )

        Box(modifier = Modifier.weight(1f)) {
            WeatherCards(
                listState = listState,
                viewModel = viewModel,
            )
            ScrollArrowWithState(listState, coroutineScope)
        }
    }
}


@Composable
private fun BoxScope.ScrollArrowWithState(
    listState: LazyListState, coroutineScope: CoroutineScope
) {
    val showUpArrow by remember { derivedStateOf { listState.canScrollBackward } }
    val showDownArrow by remember { derivedStateOf { listState.canScrollForward } }
    val onUpClick = remember(coroutineScope, listState) {
        fun() {
            coroutineScope.launch { listState.animateScrollToItem(0) }
        }
    }

    val onDownClick = remember(coroutineScope, listState) {
        fun() {
            coroutineScope.launch {
                val lastIndex = listState.layoutInfo.totalItemsCount - 1
                if (lastIndex >= 0) listState.animateScrollToItem(lastIndex)
            }
        }
    }

    ScrollArrow(
        isVisible = showUpArrow,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(end = 16.dp),
        icon = Icons.Default.KeyboardArrowUp,
        onClick = onUpClick
    )

    ScrollArrow(
        isVisible = showDownArrow,
        modifier = Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 16.dp),
        icon = Icons.Default.KeyboardArrowDown,
        onClick = onDownClick
    )
}

@Composable
private fun ScrollArrow(
    isVisible: Boolean, modifier: Modifier = Modifier, icon: ImageVector, onClick: () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible, modifier = modifier, enter = fadeIn(), exit = fadeOut()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(50))
                .padding(4.dp)
                .clickable(onClick = onClick)
        )
    }
}

@Composable
fun BoxScope.AddCityInputOverlay(
    showAddCityInput: Boolean, onDismiss: () -> Unit, onCitySubmit: (String) -> Unit
) {
    if (!showAddCityInput) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    )

    AnimatedVisibility(
        visible = showAddCityInput,
        modifier = Modifier.align(Alignment.BottomCenter),
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        CityInputRow(onCitySubmit = onCitySubmit, modifier = Modifier.clickable(enabled = false) {})
    }
}

@Composable
fun CityInputRow(
    onCitySubmit: (String) -> Unit, modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = CardBg, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.height(60.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            TextField(
                value = text,
                onValueChange = { text = it },
                singleLine = true,
                label = { Text("Enter city name") },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    if (text.text.isNotBlank()) {
                        onCitySubmit(text.text)
                        keyboardController?.hide()
                    }
                })
            )

            Spacer(modifier = Modifier.width(8.dp))
            val onButtonClick = remember(onCitySubmit, keyboardController) {
                {
                    if (text.text.isNotBlank()) {
                        onCitySubmit(text.text)
                        text = TextFieldValue("")
                        keyboardController?.hide()
                    }
                }
            }
            Button(
                modifier = Modifier.fillMaxHeight(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                onClick = onButtonClick
            ) {
                Text("Add")
            }
        }
    }
}


@Composable
fun WeatherCards(
    listState: LazyListState,
    viewModel: WeatherViewModel,
) {
    val weatherResponseList by viewModel.weatherData.collectAsState()

    val onCityRemove = remember(viewModel) {
        { city: String ->
            viewModel.removeCity(city)
        }
    }

    var expandedCardIndices by remember {
        mutableStateOf<PersistentMap<String, Boolean>>(
            persistentMapOf()
        )
    }
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(8.dp)
    ) {
        items(weatherResponseList) { weatherData ->
            val name = weatherData.weatherResponse.location.name
            val isExpanded = expandedCardIndices[name] ?: false
            WeatherCard(
                weatherData, isExpanded, onHeaderClick = {
                    expandedCardIndices = expandedCardIndices.put(name, !isExpanded)
                }, onCityRemove = onCityRemove
            )
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }

}

@Composable
fun WeatherCard(
    weatherData: WeatherData,
    isExpanded: Boolean,
    onHeaderClick: () -> Unit,
    onCityRemove: (String) -> Unit
) {

    Column(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .animateContentSize(),

        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        val cityName = weatherData.weatherResponse.location.name
        val forecastDays = weatherData.weatherResponse.forecast.forecastday

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),

            verticalAlignment = Alignment.CenterVertically
        ) {
            InvertedCorner(
                corner = Corner.BottomRight,
                cornerRadius = 16.dp,
                color = CardBg,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            Box(
                modifier = Modifier
                    .defaultMinSize(minWidth = 160.dp)
                    .background(
                        CardBg, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row {
                        Spacer(modifier = Modifier.width(48.dp))
                        Text(cityName, color = TextColor)
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            tint = TextColor.copy(alpha = 0.7f),
                            modifier = Modifier
                                .width(48.dp)
                                .clickable { onCityRemove(cityName) }
                                .padding(horizontal = 12.dp),
                        )
                    }
                    Text(
                        weatherData.weatherResponse.current.condition.text, color = TextColor
                    )

                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isExpanded) 180f else 0f, label = "rotationAnimation"
                    )

                    Spacer(Modifier.height(8.dp))
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = TextColor.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer {
                                rotationZ = rotationAngle
                            }
                            .clickable { onHeaderClick() },

                        )

                }
            }

            InvertedCorner(
                corner = Corner.BottomLeft,
                cornerRadius = 16.dp,
                color = CardBg,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }

        Column(
            modifier = Modifier
                .clipToBounds()
                .background(CardBg, RoundedCornerShape(16.dp))
        ) {
            DayItem(forecastDays.first())

            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = tween(250)) /*+ fadeIn()*/,
                exit = shrinkVertically(animationSpec = tween(250)) + fadeOut()
            ) {
                Column {
                    forecastDays.drop(1).forEach { day ->
                            DayItem(day)
                        }
                }
            }
        }
    }
}

@Composable
private fun DayItem(day: ForecastDay) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(DAY_ITEM_HEIGHT.dp)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                modifier = Modifier
                    .height(36.dp)
                    .background(
                        DayBg, RoundedCornerShape(12.dp, 12.dp)
                    )
                    .padding(8.dp),
                text = day.date,
                color = TextColor,
                fontSize = 16.sp,
                softWrap = false,
                style = LocalTextStyle.current.copy(
                    platformStyle = PlatformTextStyle(
                        includeFontPadding = false
                    )
                )
            )

            InvertedCorner(
                corner = Corner.BottomLeft,
                cornerRadius = 8.dp,
                color = DayBg,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
        HoursRow(day = day)
    }
}

@Composable
fun HoursRow(day: ForecastDay) {
    LazyRow(
        modifier = Modifier
            .height(HOURS_ROW_HEIGHT.dp)
            .background(
                DayBg, RoundedCornerShape(
                    0.dp, 8.dp, 8.dp, 8.dp
                )
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(
            items = day.hour, key = { hour -> hour.time_epoch }) { hour ->
            Hour(hour = hour)
        }
    }
}

@Composable
fun Hour(hour: Hour) {
    Column(
        modifier = Modifier
            .background(HourBg, RoundedCornerShape(8.dp))
            .height(HOUR_CARD_HEIGHT.dp)
            .padding(8.dp),
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

        Text(
            modifier = Modifier.height(20.dp),
            text = hour.time.split(" ")[1],
            color = TextColor,
            fontSize = 16.sp,
            softWrap = false,
            style = LocalTextStyle.current.copy(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.height(20.dp),
            text = "${hour.temp_c}°",
            color = TextColor,
            fontSize = 16.sp,
            softWrap = false,
            style = LocalTextStyle.current.copy(
                platformStyle = PlatformTextStyle(
                    includeFontPadding = false
                )
            )
        )
    }
}
