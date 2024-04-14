package dev.katiebarnett.myuncommoncompanion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.katiebarnett.myuncommoncompanion.ui.theme.MyUncommonCompanionTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyUncommonCompanionTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Content(stringResource(id = R.string.app_name))
                }
            }
        }
    }
}

@Composable
fun Content(name: String, modifier: Modifier = Modifier) {
    var input by remember { mutableStateOf<String>("") }
    var result by remember { mutableStateOf<String>("") }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(16.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
        )
        TextField(
            value = input,
            onValueChange = {
                input = it
            },
            label = {
                Text(text = "What type of pet would you like?")
            }
        )
        Button(onClick = {
            result = input
        }) {
            Text(text = "Submit")
        }
        Text(text = result)
    }
}

@Preview(showBackground = true)
@Composable
fun ContentPreview() {
    MyUncommonCompanionTheme {
        Content(stringResource(id = R.string.app_name))
    }
}