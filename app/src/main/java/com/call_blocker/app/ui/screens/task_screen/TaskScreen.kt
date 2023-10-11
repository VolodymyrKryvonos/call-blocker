package com.call_blocker.app.ui.screens.task_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.call_blocker.app.R
import com.call_blocker.app.ui.background
import com.call_blocker.app.ui.bold16Sp
import com.call_blocker.app.ui.darkGrey
import com.call_blocker.app.ui.itemBackground
import com.call_blocker.app.ui.lightBlue
import com.call_blocker.app.ui.primary
import com.call_blocker.app.ui.screens.home_screen.Container
import com.call_blocker.app.ui.widgets.IconWithBackground

@Preview
@Composable
fun TaskScreen(
    state: TasksScreenState = TasksScreenState(),
    onEvent: (TasksScreenEvents) -> Unit = {}
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(background)
    ) {
        Box(
            Modifier
                .background(itemBackground)
                .fillMaxWidth()
                .padding(vertical = 22.dp)
        ) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(id = R.string.tasks),
                style = MaterialTheme.typography.h2,
            )
        }

        LazyColumn {
            items(state.taskList) { item ->
                TaskItem(item)
            }
        }
    }

}

@Composable
fun TaskItem(task: Task) {
    Box(Modifier.padding(top = 12.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)) {

        Container {
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth()) {
                    IconWithBackground(
                        iconDrawable = if (task.simSlot == 0) R.drawable.ic_sim_01 else R.drawable.ic_sim_02,
                        tint = primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = stringResource(id = R.string.smsId),
                            style = MaterialTheme.typography.body2, color = darkGrey
                        )
                        Text(
                            text = task.id,
                            style = bold16Sp
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(id = R.string.statusColon),
                            style = MaterialTheme.typography.body2,
                            color = darkGrey
                        )
                        Text(
                            text = stringResource(id = task.status),
                            style = bold16Sp,
                            color = task.statusColor
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .height(1.dp)
                        .fillMaxWidth()
                        .background(color = lightBlue)
                )
                if (task.bufferedDate != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoColumn(stringResource(id = R.string.bufferedDate), task.bufferedDate)
                }
                if (task.proceedDate != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoColumn(stringResource(id = R.string.proceedDate), task.proceedDate)
                }
                if (task.deliveredDate != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    InfoColumn(stringResource(id = R.string.deliveredDate), task.deliveredDate)
                }
            }
        }
    }
}

@Composable
fun InfoColumn(state: String, date: String) {
    Text(
        text = state,
        style = MaterialTheme.typography.body2, color = darkGrey
    )
    Text(text = date, style = bold16Sp)
}
