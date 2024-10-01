package com.example.sarahmalikaleksandarsekulovskiefimsokolov_hangmanapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sarahmalikaleksandarsekulovskiefimsokolov_hangmanapp.ui.theme.SarahMalikAleksandarSekulovskiEfimSokolovHangmanAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SarahMalikAleksandarSekulovskiEfimSokolovHangmanAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HangmanGame(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

class HangmanState {
    private val words = listOf("KOTLIN", "COMPOSE", "ANDROID", "JETPACK", "MOBILE")
    private val hints = listOf(
        "A modern programming language",
        "A declarative UI toolkit",
        "A mobile operating system",
        "A suite of libraries for Android development",
        "Relating to portable devices"
    )

    var word by mutableStateOf("")
    var hint by mutableStateOf("")
    var guessedLetters by mutableStateOf(setOf<Char>())
    var remainingAttempts by mutableIntStateOf(6)
    var gameState by mutableStateOf(GameState.PLAYING)
    var hintState by mutableStateOf(HintState.INITIAL)

    init {
        newGame()
    }

    fun newGame() {
        val index = words.indices.random()
        word = words[index]
        hint = hints[index]
        guessedLetters = setOf()
        remainingAttempts = 6
        gameState = GameState.PLAYING
        hintState = HintState.INITIAL
    }

    fun guessLetter(letter: Char) {
        if (gameState != GameState.PLAYING) return

        guessedLetters = guessedLetters + letter
        if (!word.contains(letter)) {
            remainingAttempts--
        }

        updateGameState()
    }

    fun useHint() {
        when (hintState) {
            HintState.INITIAL -> hintState = HintState.HINT_SHOWN
            HintState.HINT_SHOWN -> {
                if (remainingAttempts > 1) {
                    hintState = HintState.HALF_LETTERS_DISABLED
                    remainingAttempts--
                }
            }
            HintState.HALF_LETTERS_DISABLED -> {
                if (remainingAttempts > 1) {
                    hintState = HintState.VOWELS_SHOWN
                    remainingAttempts--
                }
            }
            HintState.VOWELS_SHOWN -> {} // Do nothing
        }
        updateGameState()
    }

    private fun updateGameState() {
        if (word.all { it in guessedLetters }) {
            gameState = GameState.WON
        } else if (remainingAttempts == 0) {
            gameState = GameState.LOST
        }
    }
}

enum class GameState { PLAYING, WON, LOST }
enum class HintState { INITIAL, HINT_SHOWN, HALF_LETTERS_DISABLED, VOWELS_SHOWN }

@Composable
fun HangmanGame(modifier: Modifier = Modifier) {
    val hangmanState = remember { HangmanState() }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    if (isLandscape) {
        LandscapeLayout(hangmanState, modifier)
    } else {
        PortraitLayout(hangmanState, modifier)
    }
}

@Composable
fun LandscapeLayout(hangmanState: HangmanState, modifier: Modifier = Modifier) {
    Row(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            GamePlayScreen(hangmanState)
            Spacer(modifier = Modifier.height(16.dp))
            NewGameButton(hangmanState)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            LetterButtons(hangmanState)
            Spacer(modifier = Modifier.height(16.dp))
            HintButton(hangmanState)
        }
    }
}

@Composable
fun PortraitLayout(hangmanState: HangmanState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        GamePlayScreen(hangmanState)
        Spacer(modifier = Modifier.height(16.dp))
        LetterButtons(hangmanState)
        Spacer(modifier = Modifier.height(16.dp))
        NewGameButton(hangmanState)
    }
}

@Composable
fun GamePlayScreen(hangmanState: HangmanState) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = hangmanState.word.map { if (it in hangmanState.guessedLetters) it else '_' }.joinToString(" "),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Remaining attempts: ${hangmanState.remainingAttempts}")
        Spacer(modifier = Modifier.height(16.dp))
        when (hangmanState.gameState) {
            GameState.WON -> Text("Congratulations! You won!", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            GameState.LOST -> Text("Game Over. The word was ${hangmanState.word}", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            else -> {}
        }
    }
}

@Composable
fun LetterButtons(hangmanState: HangmanState) {
    val letters = ('A'..'Z').chunked(7)
    Column {
        letters.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { letter ->
                    Button(
                        onClick = { hangmanState.guessLetter(letter) },
                        modifier = Modifier
                            .padding(4.dp)
                            .size(48.dp),
                        enabled = letter !in hangmanState.guessedLetters && hangmanState.gameState == GameState.PLAYING,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = letter.toString(),
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewGameButton(hangmanState: HangmanState) {
    Button(
        onClick = { hangmanState.newGame() },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("New Game")
    }
}

@Composable
fun HintButton(hangmanState: HangmanState) {
    Button(
        onClick = { hangmanState.useHint() },
        modifier = Modifier.fillMaxWidth(),
        enabled = hangmanState.gameState == GameState.PLAYING && hangmanState.hintState != HintState.VOWELS_SHOWN && hangmanState.remainingAttempts > 1
    ) {
        Text("Hint")
    }

    when (hangmanState.hintState) {
        HintState.HINT_SHOWN -> Text(hangmanState.hint)
        HintState.HALF_LETTERS_DISABLED -> Text("Half of the remaining letters have been disabled")
        HintState.VOWELS_SHOWN -> Text("All vowels have been revealed")
        else -> {}
    }
}

@Preview(showBackground = true)
@Composable
fun HangmanGamePreview() {
    SarahMalikAleksandarSekulovskiEfimSokolovHangmanAppTheme {
        HangmanGame()
    }
}