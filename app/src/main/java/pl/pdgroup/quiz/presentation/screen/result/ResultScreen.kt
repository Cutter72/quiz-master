package pl.pdgroup.quiz.presentation.screen.result

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.ui.theme.ErrorDark
import pl.pdgroup.quiz.ui.theme.ErrorLight
import pl.pdgroup.quiz.ui.theme.SuccessDark
import pl.pdgroup.quiz.ui.theme.SuccessLight
import kotlin.random.Random

@Composable
fun ResultScreen(
    onNavigateHome: () -> Unit,
    onNavigateSelection: () -> Unit,
    onNavigateScoreboard: () -> Unit,
    viewModel: ResultViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is ResultContract.Effect.NavigateToHome -> onNavigateHome()
                is ResultContract.Effect.NavigateToSelection -> onNavigateSelection()
                is ResultContract.Effect.NavigateToScoreboard -> onNavigateScoreboard()
            }
        }
    }

    val themeColor = when {
        state.percentage >= 80 -> if (isDark) SuccessDark else SuccessLight
        state.percentage >= 60 -> MaterialTheme.colorScheme.primary
        state.percentage >= 40 -> MaterialTheme.colorScheme.secondary
        else -> if (isDark) ErrorDark else ErrorLight
    }

    val message = when {
        state.percentage == 100 -> "Perfect Score! 🎉"
        state.percentage >= 80 -> "Excellent! 🌟"
        state.percentage >= 60 -> "Good Job! 👍"
        state.percentage >= 40 -> "Not Bad! 💪"
        else -> "Keep Practicing! 📚"
    }

    if (state.percentage == 100) {
        ConfettiEffect()
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Trophy Animation
            val scaleAnim = remember { Animatable(0f) }
            val rotateAnim = remember { Animatable(0f) }
            val pulseScaleAnim = remember { Animatable(1f) }

            LaunchedEffect(Unit) {
                scaleAnim.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))

                while (true) {
                    delay(3500)
                    // Wiggle
                    rotateAnim.animateTo(-10f, tween(150))
                    rotateAnim.animateTo(10f, tween(150))
                    rotateAnim.animateTo(-10f, tween(150))
                    rotateAnim.animateTo(0f, tween(150))

                    // Pulse
                    pulseScaleAnim.animateTo(1.1f, tween(150))
                    pulseScaleAnim.animateTo(1f, tween(150))
                }
            }

            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = "Trophy",
                tint = themeColor,
                modifier = Modifier
                    .size(120.dp)
                    .scale(scaleAnim.value * pulseScaleAnim.value)
                    .rotate(rotateAnim.value)
                    .padding(bottom = 32.dp)
            )

            // Result Card
            val cardAlphaAnim = remember { Animatable(0f) }
            val cardOffsetYAnim = remember { Animatable(20f) }

            LaunchedEffect(Unit) {
                delay(200)
                launch { cardAlphaAnim.animateTo(1f, tween(500)) }
                launch { cardOffsetYAnim.animateTo(0f, tween(500)) }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(cardAlphaAnim.value)
                    .offset(y = cardOffsetYAnim.value.dp)
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = message,
                        style = MaterialTheme.typography.headlineLarge,
                        color = themeColor,
                        modifier = Modifier.padding(bottom = 32.dp),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "${state.score}/${state.totalQuestions}",
                        style = MaterialTheme.typography.displayLarge,
                        color = themeColor,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "${state.percentage}% Correct",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 32.dp)
                    )

                    // Progress Bar
                    val animatedProgress by animateFloatAsState(
                        targetValue = state.percentage / 100f,
                        animationSpec = tween(1000, easing = FastOutSlowInEasing)
                    )

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .padding(bottom = 24.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = themeColor,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        drawStopIndicator = {}
                    )

                    // Info Panel
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Category: ${state.category}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Difficulty: ${state.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Action Buttons
            AnimatedButton(
                text = "Try Another Quiz",
                icon = Icons.Default.Refresh,
                delayMs = 400,
                isOutlined = false,
                isText = false,
                onClick = { viewModel.handleIntent(ResultContract.Intent.TryAnotherQuiz) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedButton(
                text = "View Scoreboard",
                icon = Icons.Default.Leaderboard,
                delayMs = 500,
                isOutlined = true,
                isText = false,
                onClick = { viewModel.handleIntent(ResultContract.Intent.ViewScoreboard) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedButton(
                text = "Back to Home",
                icon = Icons.Default.Home,
                delayMs = 600,
                isOutlined = false,
                isText = true,
                onClick = { viewModel.handleIntent(ResultContract.Intent.BackToHome) }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AnimatedButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    delayMs: Long,
    isOutlined: Boolean,
    isText: Boolean,
    onClick: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(500)) }
        launch { offsetYAnim.animateTo(0f, tween(500)) }
    }

    val modifier = Modifier
        .fillMaxWidth()
        .height(64.dp)
        .alpha(alphaAnim.value)
        .offset(y = offsetYAnim.value.dp)

    if (isText) {
        TextButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp)) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    } else if (isOutlined) {
        OutlinedButton(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp)) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    } else {
        Button(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(20.dp)) {
            Icon(imageVector = icon, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ConfettiEffect() {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    
    // Using simple px resolution bounds as particles flow infinitely. Using safe width.
    val w = configuration.screenWidthDp.toFloat()
    val screenWidthPx = with(density) { w.dp.toPx() }
    val h = configuration.screenHeightDp.toFloat()
    val screenHeightPx = with(density) { h.dp.toPx() }

    val particles = remember {
        List(20) {
            ConfettiParticle(
                startX = Random.nextFloat() * screenWidthPx,
                durationMs = Random.nextInt(2000, 4000),
                delayMs = Random.nextInt(0, 500)
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "Confetti")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Time"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val currentTimeMs = time * 4000f

        particles.forEach { particle ->
            val localTime = (currentTimeMs - particle.delayMs).coerceAtLeast(0f) % particle.durationMs
            val progress = localTime / particle.durationMs

            val currentY = -20.dp.toPx() + (screenHeightPx + 40.dp.toPx()) * progress
            val rotation = 360f * progress
            val alpha = 1f - progress

            if (progress > 0) {
                drawContext.canvas.save()
                drawContext.canvas.translate(particle.startX, currentY)
                drawContext.canvas.rotate(rotation)

                drawCircle(
                    color = particle.color.copy(alpha = alpha),
                    radius = 10.dp.toPx() / 2f
                )

                drawContext.canvas.restore()
            }
        }
    }
}

data class ConfettiParticle(
    val startX: Float,
    val durationMs: Int,
    val delayMs: Int,
    val color: Color = listOf(
        Color(0xFFFF6F00),
        Color(0xFFFFA726),
        Color(0xFFFFD54F)
    ).random()
)