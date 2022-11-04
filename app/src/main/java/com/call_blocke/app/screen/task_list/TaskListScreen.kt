package com.call_blocke.app.screen.task_list

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.LoadState
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.call_blocke.app.R
import com.call_blocke.db.entity.TaskEntity
import com.rokobit.adstv.ui.element.Text
import com.rokobit.adstv.ui.element.TextNormal
import com.rokobit.adstv.ui.element.Title
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor
import com.rokobit.adstv.ui.secondaryDimens
import java.util.*

@Composable
private fun TaskItemView(item: TaskEntity) = Card(
    modifier = Modifier
        .fillMaxWidth()
        .padding(secondaryDimens),
    shape = RoundedCornerShape(5),
    backgroundColor = secondaryColor,
    elevation = 6.dp,
) {

    Column(modifier = Modifier.padding(primaryDimens)) {
        Text(text = "Sms id: ${item.id}")
        // Text(text = "Send to: ${item.sendTo}")
        Text(text = "Status: ${item.status.name}")
        Text(text = "Sim number: ${item.simSlot?.plus(1)}")

        Divider(Modifier.height(10.dp), color = Color.Transparent)

        TextNormal(text = "Buffered in app at: ${Date(item.bufferedAt)}")
        Divider(Modifier.height(5.dp), color = Color.Transparent)
        if (item.processAt != 0L) {
            TextNormal(text = "Proceed at: ${Date(item.processAt)}")
        }
        Divider(Modifier.height(5.dp), color = Color.Transparent)
        if (item.deliveredAt != 0L) {
            TextNormal(text = "Delivered at: ${Date(item.deliveredAt)}")
        }
    }
}

@Composable
fun TaskListScreen(mViewModel: TaskListViewModel = viewModel()) {
    val pager = remember {
        Pager(
            PagingConfig(
                pageSize = 5,
                enablePlaceholders = true,
                maxSize = 200
            )
        ) { mViewModel.taskListPaged() }
    }

    val lazyPagingItems = pager.flow.collectAsLazyPagingItems()
    Column {
        Title(
            modifier = Modifier.padding(10.dp),
            text = stringResource(id = R.string.main_menu_task_list)
        )
        LazyColumn {
            if (lazyPagingItems.loadState.refresh == LoadState.Loading) {
                item {
                    Text(text = stringResource(id = R.string.task_list_loading_text))
                }
            }

            itemsIndexed(lazyPagingItems) { _, item ->
                if (item != null)
                    TaskItemView(item)
                else
                    TextNormal(text = stringResource(id = R.string.task_list_loading_text))
            }

            if (lazyPagingItems.loadState.append == LoadState.Loading) {
                item {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }

}