package com.example.loadassist.ui_
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.loadassist.ui_.Login
import com.example.loadassist.R
import com.example.loadassist.ui.theme.LoadAssistTheme


/**
 * Composable that allows the user to select the desired cupcake quantity and expects
 * [onNextButtonClicked] lambda that expects the selected quantity and triggers the navigation to
 * next screen
 */
@Preview
@Composable
fun WorkerMenuScreen(modifier: Modifier = Modifier,
    onManualInputClick: () -> Unit = {}
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.menu_image),
            contentDescription = "Logo",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.size(500.dp)
        )
        Text(
            text =
                "Please choose ASN File to upload or choose to input invoice manually",
                textAlign = TextAlign.Center,
            modifier = Modifier.padding(10.dp)

        )
        Button(
            onClick = ::TODO,
            modifier = Modifier.padding(10.dp)
        )
        {
            Text(text = "UPLOAD ASN")
        }
        Button(
            onClick = onManualInputClick,
            modifier = Modifier.padding(10.dp)
        )
        {
            Text(text = "Manually Input Load Items and Quantities")
        }


    }

    /**
     * Customizable button composable that displays the [labelResourceId]
     * and triggers [onClick] lambda when this composable is clicked
     */


    @Preview(showSystemUi = false)
    @Composable
    fun WorkerMenuPreview() {
        LoadAssistTheme {
            WorkerMenuScreen()
        }
    }
}
