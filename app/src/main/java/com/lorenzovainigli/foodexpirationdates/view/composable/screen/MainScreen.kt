package com.lorenzovainigli.foodexpirationdates.view.composable.screen

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.lorenzovainigli.foodexpirationdates.R
import com.lorenzovainigli.foodexpirationdates.model.entity.ExpirationDate
import com.lorenzovainigli.foodexpirationdates.ui.theme.FoodExpirationDatesTheme
import com.lorenzovainigli.foodexpirationdates.view.MainActivity
import com.lorenzovainigli.foodexpirationdates.view.composable.FoodCard
import java.util.Calendar
import kotlin.math.min

@Composable
fun MainScreen(
    activity: MainActivity? = null,
    navController: NavHostController,
    showSnackbar: MutableState<Boolean>? = null,
    searchQuery: MutableState<String> = mutableStateOf("")
) {
    Box(
        modifier = Modifier
            .padding(start = 4.dp, end = 4.dp)
            .fillMaxSize()
    ) {
        val itemsState = activity?.viewModel?.getDates()?.collectAsState(emptyList())
        val items = itemsState?.value ?: getItemsForPreview(LocalContext.current)

        AnimatedVisibility(
            visible = items.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            ListOfItems(
                activity = activity,
                items = items,
                navController = navController,
                showSnackbar = showSnackbar,
                searchQuery = searchQuery
            )
        }
        AnimatedVisibility(
            visible = items.isEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            EmptyList()
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp),
            onClick = {
                navController.navigate(Screen.InsertScreen.route)
            },
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = stringResource(id = R.string.insert)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview
@Composable
fun MainScreenPreview() {
    FoodExpirationDatesTheme {
        Surface {
            MainScreen(
                navController = rememberNavController(),
            )
        }
    }
}

@Composable
fun ListOfItems(
    activity: MainActivity? = null,
    items: List<ExpirationDate>,
    navController: NavHostController,
    showSnackbar: MutableState<Boolean>?,
    searchQuery: MutableState<String> = mutableStateOf("")
) {
    LazyColumn {
        items(items, key = { it.id }) { item ->
            AnimatedVisibility(
                modifier = Modifier.animateItem(),
                visible = item.foodName.contains(searchQuery.value, ignoreCase = true),
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                FoodCard(
                    item = item,
                    onClickEdit = {
                        navController.navigate(Screen.InsertScreen.route + "?itemId=${item.id}")
                    },
                    onClickDelete = {
                        showSnackbar?.value = true
                        activity?.viewModel?.deleteExpirationDate(item)
                    }
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}

@Composable
@PreviewLightDark
fun ListOfItemsPreview() {
    FoodExpirationDatesTheme {
        Surface {
            ListOfItems(
                items = getItemsForPreview(LocalContext.current),
                navController = rememberNavController(),
                showSnackbar = null
            )
        }
    }
}

@Composable
fun EmptyList() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(id = R.string.no_items_found),
                style = MaterialTheme.typography.displaySmall,
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(id = R.string.please_insert_one),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
@PreviewLightDark
fun EmptyListPreview() {
    FoodExpirationDatesTheme {
        Surface {
            EmptyList()
        }
    }
}

fun getItemsForPreview(context: Context): List<ExpirationDate> {
    val items = ArrayList<ExpirationDate>()
    val foods = context.resources.getStringArray(R.array.example_foods)
    val quantities = arrayOf(3, 1, 1, 2, 1, 7, 4)
    val daysLeft = arrayOf(-1, 0, 1, 3, 7, 10, 30)
    for (i in 0 until min(foods.size, daysLeft.size)) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DATE, daysLeft[i])
        items.add(
            ExpirationDate(
                id = i,
                foodName = foods[i],
                expirationDate = cal.time.time,
                quantity = quantities[i],
            )
        )
    }
    return items
}