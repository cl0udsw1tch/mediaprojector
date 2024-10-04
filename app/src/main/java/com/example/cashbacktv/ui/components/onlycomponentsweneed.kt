package com.example.cashbacktv.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import com.example.cashbacktv.viewmodel.MPViewModel

@Composable
fun CashBackTvApp(modifier: Modifier = Modifier, viewmodel: MPViewModel, onButtonClick: () -> Unit) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(modifier = Modifier
            .fillMaxSize(),
            contentAlignment = Alignment.Center,

            ) {
            Column(
                modifier = Modifier.width(300.dp).wrapContentSize(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top,

                ) {
                Text(modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .wrapContentSize(Alignment.Center)
                    ,
                    text = "Current Frame: ")
                ImageDisplay(modifier = Modifier.align(Alignment.CenterHorizontally), viewmodel=viewmodel)
                Greeting(
                    modifier = Modifier.padding(innerPadding), viewmodel=viewmodel, onButtonClick=onButtonClick
                )
            }
        }

    }
}

@Composable
fun Greeting(modifier: Modifier = Modifier, viewmodel: MPViewModel, onButtonClick: () -> Unit) {

    val frame: Int? by viewmodel.count.observeAsState(0)

    Surface(
        modifier = Modifier,
    ) {

        Box(
            modifier = Modifier,
            contentAlignment = Alignment.Center)
        {
            Column(modifier= Modifier
                .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Frame: $frame",
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(10.dp)
                )
                Button(onClick = { onButtonClick() }) {
                    Text("Start Screen Projection")
                }
            }
        }
    }
}

@Composable
fun ImageDisplay(modifier: Modifier, viewmodel: MPViewModel) {


    val currImage: ImageBitmap? by viewmodel.currImage.observeAsState(null)


    Box(modifier = Modifier
        .size(300.dp)
        .border(1.dp, Color.Magenta)
        .fillMaxWidth()
        , contentAlignment = Alignment.Center

    ) {
        if (currImage != null) {
            Image(bitmap = currImage!!, contentDescription = null)
        } else {
            // Placeholder if no image is available
            Text(
                text = "No Image", color = Color.Gray)
        }
    }
}