package com.example.loadassist.ui_

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.loadassist.R



@Composable
//login screen - onLoginSuccess: () - a function taking no arguments, used
//by navControllers to navigate to the next screen
fun Login(onLoginSuccess: () -> Unit, modifier: Modifier = Modifier) {
    var username by remember { mutableStateOf("")}
    var password by remember { mutableStateOf("")}
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(500.dp)
        )
        Text(
            text = "Login: "
        )
        TextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("") }
        )
        Text(
            text = "Password: "
        )
        TextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("") }
        )
        Button(
            onClick = { onLoginSuccess() },
            modifier = Modifier.padding(10.dp))
        {
            Text(text = "Login")
        }
    }
}



