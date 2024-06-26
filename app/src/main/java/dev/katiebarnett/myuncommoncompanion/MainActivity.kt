package dev.katiebarnett.myuncommoncompanion

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.BlockThreshold
import com.google.ai.client.generativeai.type.HarmCategory
import com.google.ai.client.generativeai.type.SafetySetting
import com.google.ai.client.generativeai.type.generationConfig
import dev.katiebarnett.myuncommoncompanion.ui.theme.MyUncommonCompanionTheme
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

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
fun Content(
    name: String,
    firstTextChangeInitialValue: Boolean = false,
    modifier: Modifier = Modifier
) {
    var home by remember { mutableStateOf<String>("") }
    var hobbies by remember { mutableStateOf<String>("") }
    var family by remember { mutableStateOf<String>("") }
    var result by remember { mutableStateOf<String>("") }
    val pet by remember {
        derivedStateOf {
            getPet(result)
        }
    }
    var firstTextChangeHome by remember { mutableStateOf<Boolean>(firstTextChangeInitialValue) }
    var firstTextChangeHobbies by remember { mutableStateOf<Boolean>(firstTextChangeInitialValue) }
    var firstTextChangeFamily by remember { mutableStateOf<Boolean>(firstTextChangeInitialValue) }

    val input by remember { derivedStateOf {
        "Recommend one pet for me that is not a usual domesticated animal, I live in $home, I like to $hobbies and my family is $family. Return the result as a json object that fits the format of this data class:" +
                "data class Pet(\n" +
                "    val name: String, \n" +
                "    val description: String, \n" +
                "    val photoUrl: String,\n" +
                ")"
        }
    }

    val coroutineScope = rememberCoroutineScope()

    val harassmentSafety = SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH)
    val hateSpeechSafety = SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE)

    val config = generationConfig {
        temperature = 0.9f
        topK = 16
        topP = 0.1f
        maxOutputTokens = 200
        stopSequences = listOf("dog", "cat")
    }

    val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = BuildConfig.apiKey,
        generationConfig = config,
        safetySettings = listOf(harassmentSafety, hateSpeechSafety)
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
        )
        TextField(
            value = home,
            onValueChange = {
                home = it
                firstTextChangeHome = true
            },
            isError = firstTextChangeHome && home.isNullOrBlank(),
            supportingText = {
                if (home.isNullOrBlank() && firstTextChangeHome) {
                    Text(
                        text = "Please enter some text",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            label = {
                Text(text = "What type of home do you live in?")
            }
        )
        TextField(
            value = hobbies,
            onValueChange = {
                hobbies = it
                firstTextChangeHobbies = true
            },
            isError = firstTextChangeHobbies && hobbies.isNullOrBlank(),
            supportingText = {
                if (hobbies.isNullOrBlank() && firstTextChangeHobbies) {
                    Text(
                        text = "Please enter some text",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            label = {
                Text(text = "What are your hobbies?")
            }
        )
        TextField(
            value = family,
            onValueChange = {
                family = it
                firstTextChangeFamily = true
            },
            isError = firstTextChangeFamily && family.isNullOrBlank(),
            supportingText = {
                if (family.isNullOrBlank() && firstTextChangeFamily) {
                    Text(
                        text = "Please enter some text",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            label = {
                Text(text = "What is your family like?")
            }
        )
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        result = generativeModel.generateContent(input).text.orEmpty()
                    } catch (e: Exception) {
                        Log.e("ERROR", "Error fetching result", e)
                    }
                }
            },
            enabled = firstTextChangeHome && firstTextChangeHobbies && firstTextChangeFamily
                    && !home.isNullOrBlank() && !hobbies.isNullOrBlank() && !family.isNullOrBlank()
        ) {
            Text(text = "Submit")
        }
        PetDisplay(pet = pet)
    }
}

fun getPet(rawResult: String): Pet? {
    return if (!rawResult.isNullOrEmpty()) {
        val cleanedResult = rawResult
            .replace("```json", "")
            .replace("```", "")
        Json.decodeFromString<Pet>(cleanedResult)
    } else {
        null
    }
}

@Composable
fun PetDisplay(pet: Pet?) {
    if (pet == null) {
        Text(text = "No pet returned")
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Name:", fontWeight = FontWeight.Bold)
                Text(text = pet.name)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Description:", fontWeight = FontWeight.Bold)
                Text(text = pet.description)
            }
            AsyncImage(
                model = pet.photoUrl,
                contentDescription = pet.name,
                modifier = Modifier.fillMaxWidth().aspectRatio(1f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContentPreview() {
    MyUncommonCompanionTheme {
        Content(stringResource(id = R.string.app_name))
    }
}

@Preview(showBackground = true)
@Composable
fun ContentPreviewInError() {
    MyUncommonCompanionTheme {
        Content(stringResource(id = R.string.app_name), firstTextChangeInitialValue = true)
    }
}