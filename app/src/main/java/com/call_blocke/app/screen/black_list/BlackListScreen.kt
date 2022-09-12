package com.call_blocke.app.screen.black_list

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.call_blocke.app.R
import com.rokobit.adstv.ui.element.Button
import com.rokobit.adstv.ui.element.Text
import com.rokobit.adstv.ui.primaryDimens
import com.rokobit.adstv.ui.secondaryColor
import com.rokobit.adstv.ui.secondaryDimens

@Composable
fun BlackListScreen(mViewModel: BlackListViewModel = viewModel()) {
    val context = LocalContext.current
    mViewModel.loadBlackList(context)

    val list by mViewModel.blackList.observeAsState(initial = emptyList())


    LazyColumn {
        items(list) { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(primaryDimens),
                shape = RoundedCornerShape(5),
                backgroundColor = secondaryColor,
                elevation = 6.dp,
            ) {
                Column(modifier = Modifier.padding(secondaryDimens)) {
                    Text(text = item)
                    Divider(modifier = Modifier.height(secondaryDimens), color = Color.Transparent)
                    Button(title = stringResource(id = R.string.black_list_remove_item_btn_title)) {
                        mViewModel.removeItem(context, item)
                    }
                }
            }
        }
    }
}