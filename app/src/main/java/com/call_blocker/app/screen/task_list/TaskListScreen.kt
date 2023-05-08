package com.call_blocker.app.screen.task_list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocker.adstv.ui.element.Text
import com.call_blocker.adstv.ui.element.TextNormal
import com.call_blocker.adstv.ui.element.Title
import com.call_blocker.adstv.ui.primaryDimens
import com.call_blocker.adstv.ui.secondaryColor
import com.call_blocker.adstv.ui.secondaryDimens
import com.call_blocker.app.R
import com.call_blocker.db.entity.TaskEntity
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
    val tasks = mViewModel.taskListPaged.collectAsState(initial = listOf())
    Column {
        Title(
            modifier = Modifier.padding(10.dp),
            text = stringResource(id = R.string.main_menu_task_list)
        )
        LazyColumn {
            items(tasks.value) { item ->
                TaskItemView(item)
            }
        }
    }

}