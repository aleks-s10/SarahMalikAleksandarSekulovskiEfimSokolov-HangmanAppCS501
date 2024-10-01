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
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

class HangmanState(
    index: Int = -1,
    initialWord: String = "",
    initialHint: String = "",
    initialGuessedLetters: Set<Char> = setOf(),
    initialRemainingAttempts: Int = 6,
    initialGameState: GameState = GameState.PLAYING,
    initialHintState: HintState = HintState.INITIAL
) {
    private val words = listOf("KOTLIN", "COMPOSE", "ANDROID", "JETPACK", "MOBILE")
    private val hints = listOf(
        "A modern programming language",
        "A declarative UI toolkit",
        "A mobile operating system",
        "A suite of libraries for Android development",
        "Relating to portable devices"
    )

    var word by mutableStateOf(initialWord)
    var hint by mutableStateOf(initialHint)
    var guessedLetters by mutableStateOf(initialGuessedLetters)
    var remainingAttempts by mutableIntStateOf(initialRemainingAttempts)
    var gameState by mutableStateOf(initialGameState)
    var hintState by mutableStateOf(initialHintState)
    var index by mutableStateOf(index)

    init {
        if (index == -1){
            newGame()
        }
    }

    fun newGame() {
        index = words.indices.random()
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


val hangmanStateSaver = Saver<HangmanState, Bundle>(
    save = { state ->
        val bundle = Bundle().apply {
            putInt("index", state.index)
            putString("word", state.word)
            putString("hint", state.hint)
            putCharArray("guessedLetters", state.guessedLetters.toCharArray())
            putInt("remainingAttempts", state.remainingAttempts)
            putString("gameState", state.gameState.name)
            putString("hintState", state.hintState.name)
        }
        bundle
    },
    restore = { bundle ->
        val index = bundle.getInt("index")
        val word = bundle.getString("word") ?: return@Saver null
        val hint = bundle.getString("hint") ?: return@Saver null
        val guessedLetters = bundle.getCharArray("guessedLetters")?.toSet() ?: setOf()
        val remainingAttempts = bundle.getInt("remainingAttempts")
        val gameState = bundle.getString("gameState")?.let { GameState.valueOf(it) } ?: GameState.PLAYING
        val hintState = bundle.getString("hintState")?.let { HintState.valueOf(it) } ?: HintState.INITIAL

        HangmanState(
            index = index,
            initialWord = word,
            initialHint = hint,
            initialGuessedLetters = guessedLetters,
            initialRemainingAttempts = remainingAttempts,
            initialGameState = gameState,
            initialHintState = hintState
        )
    }
)

@Composable
fun HangmanGame(modifier: Modifier = Modifier) {
    
    val hangmanState = rememberSaveable(saver = hangmanStateSaver) { HangmanState() }
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
            Row {
                NewGameButton(hangmanState, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(5.dp))
                HintButton(hangmanState, modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(5.dp))
            HintDisplay(hangmanState)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            LetterButtons(hangmanState)
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
        Row {
            NewGameButton(hangmanState, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(5.dp))
            HintButton(hangmanState, modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(5.dp))
        HintDisplay(hangmanState)
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
    val letters = ('A'..'Z').chunked(5)
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
                            .weight(1f),
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
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewGameButton(hangmanState: HangmanState, modifier: Modifier = Modifier) {
    Button(
        onClick = { hangmanState.newGame() },
        modifier = modifier,
    ) {
        Text("New Game")
    }
}

@Composable
fun HintButton(hangmanState: HangmanState, modifier: Modifier = Modifier) {
    Button(
        onClick = { hangmanState.useHint() },
        modifier = modifier,
        enabled = hangmanState.gameState == GameState.PLAYING && hangmanState.hintState != HintState.VOWELS_SHOWN && hangmanState.remainingAttempts > 1
    ) {
        Text("Hint")
    }
}

@Composable
fun HintDisplay(hangmanState: HangmanState){
    val message: String
    when (hangmanState.hintState) {
        HintState.HINT_SHOWN -> message = hangmanState.hint
        HintState.HALF_LETTERS_DISABLED -> message = "Half of the remaining letters have been disabled"
        HintState.VOWELS_SHOWN -> message = "All vowels have been revealed"
        else -> message = ""
    }
    Text(
        message,
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Preview(showBackground = true)
@Composable
fun HangmanGamePreview() {
    SarahMalikAleksandarSekulovskiEfimSokolovHangmanAppTheme {
        HangmanGame()
    }
}