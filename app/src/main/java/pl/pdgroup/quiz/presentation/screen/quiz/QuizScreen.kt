package pl.pdgroup.quiz.presentation.screen.quiz

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.R
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.ui.theme.ErrorDark
import pl.pdgroup.quiz.ui.theme.ErrorLight
import pl.pdgroup.quiz.ui.theme.SuccessDark
import pl.pdgroup.quiz.ui.theme.SuccessLight

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizScreen(
    onNavigateBack: () -> Unit,
    onNavigateToResults: (String, Difficulty, Int, Int) -> Unit,
    viewModel: QuizViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is QuizContract.Effect.NavigateToResults -> {
                    onNavigateToResults(effect.category, effect.difficulty, effect.score, effect.total)
                }
                is QuizContract.Effect.ShowError -> {
                    // Show error snackbar or handled elsewhere
                }
            }
        }
    }

    if (showExitDialog) {
        ExitQuizDialog(
            onDismiss = { showExitDialog = false },
            onExit = onNavigateBack
        )
    }

    if (state.isLoading) {
        QuizLoadingState()
        return
    }

    if (state.questions.isEmpty()) {
        QuizErrorState(state.error)
        return
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            QuizHeader(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.questions.size,
                score = state.score,
                onExitClick = { showExitDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuizCategoryChips(
                category = state.category,
                difficulty = state.difficulty,
                isDark = isDark
            )

            Spacer(modifier = Modifier.height(16.dp))

            QuizProgressBar(
                currentQuestionIndex = state.currentQuestionIndex,
                totalQuestions = state.questions.size
            )

            Spacer(modifier = Modifier.height(24.dp))

            QuizQuestionContent(
                state = state,
                viewModel = viewModel
            )

            QuizResultFeedback(
                state = state,
                isDark = isDark,
                viewModel = viewModel
            )

            QuizActionButtons(
                state = state,
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun ExitQuizDialog(onDismiss: () -> Unit, onExit: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_exit_title)) },
        text = { Text(stringResource(R.string.dialog_exit_message)) },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onExit()
            }) {
                Text(stringResource(R.string.dialog_exit_confirm), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_exit_resume))
            }
        }
    )
}

@Composable
fun QuizLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun QuizErrorState(error: String?) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(error ?: stringResource(R.string.quiz_error_no_questions))
    }
}

@Composable
fun QuizHeader(
    currentQuestionIndex: Int,
    totalQuestions: Int,
    score: Int,
    onExitClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onExitClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.quiz_question_count, currentQuestionIndex + 1, totalQuestions),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f)
        )

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$score",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(R.string.quiz_score_label),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun QuizCategoryChips(category: String, difficulty: Difficulty, isDark: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AssistChip(
            onClick = { },
            label = { Text(category) },
            colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
        )
        
        val diffColor = when (difficulty) {
            Difficulty.EASY -> if (isDark) SuccessDark else SuccessLight
            Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
            Difficulty.HARD -> MaterialTheme.colorScheme.error
        }
        
        AssistChip(
            onClick = { },
            label = { Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() }) },
            colors = AssistChipDefaults.assistChipColors(containerColor = diffColor.copy(alpha = 0.1f))
        )
    }
}

@Composable
fun QuizProgressBar(currentQuestionIndex: Int, totalQuestions: Int) {
    val animatedProgress by animateFloatAsState(
        targetValue = (currentQuestionIndex + 1).toFloat() / totalQuestions,
        animationSpec = tween(500, easing = LinearOutSlowInEasing)
    )
    LinearProgressIndicator(
        progress = { animatedProgress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
        drawStopIndicator = {}
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun QuizQuestionContent(state: QuizContract.State, viewModel: QuizViewModel) {
    AnimatedContent(
        targetState = state.currentQuestionIndex,
        transitionSpec = {
            if (targetState > initialState) {
                (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(animationSpec = tween(300))).togetherWith(
                    slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut(animationSpec = tween(300))
                )
            } else {
                fadeIn(tween(300)).togetherWith(fadeOut(tween(300)))
            }
        }
    ) { index ->
        val question = state.questions[index]

        Column {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = question.question,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 28.sp,
                    modifier = Modifier.padding(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val letters = listOf("A", "B", "C", "D")
            state.shuffledAnswers.forEachIndexed { ansIndex, answer ->
                AnswerOption(
                    answer = answer,
                    letter = letters.getOrElse(ansIndex) { "" },
                    isSelected = state.selectedAnswer == answer,
                    isLocked = state.isAnswerLocked,
                    isCorrect = answer == question.correctAnswer,
                    delayMs = ansIndex * 100L,
                    onClick = { viewModel.handleIntent(QuizContract.Intent.SelectAnswer(answer)) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun QuizResultFeedback(state: QuizContract.State, isDark: Boolean, viewModel: QuizViewModel) {
    AnimatedVisibility(
        visible = state.isAnswerLocked,
        enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn() + slideInVertically(initialOffsetY = { 50 })
    ) {
        val isCorrect = state.isCorrect == true
        val bgColor = if (isCorrect) (if (isDark) SuccessDark else SuccessLight).copy(alpha = 0.2f)
                      else (if (isDark) ErrorDark else ErrorLight).copy(alpha = 0.2f)
        val strokeColor = if (isCorrect) (if (isDark) SuccessDark else SuccessLight)
                          else (if (isDark) ErrorDark else ErrorLight)
        val icon = if (isCorrect) Icons.Default.CheckCircle else Icons.Default.Close

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(containerColor = bgColor),
            border = BorderStroke(1.dp, strokeColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = icon, contentDescription = null, tint = strokeColor, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isCorrect) stringResource(R.string.quiz_feedback_correct) else stringResource(R.string.quiz_feedback_incorrect),
                        style = MaterialTheme.typography.titleMedium,
                        color = strokeColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                if (!isCorrect) {
                    Spacer(modifier = Modifier.height(8.dp))
                    if (!state.showCorrectAnswer) {
                        OutlinedButton(
                            onClick = { viewModel.handleIntent(QuizContract.Intent.ShowCorrectAnswer) },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = strokeColor)
                        ) {
                            Text(stringResource(R.string.quiz_show_correct_answer))
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.quiz_correct_answer_is, state.currentQuestion?.correctAnswer ?: ""),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun QuizActionButtons(state: QuizContract.State, viewModel: QuizViewModel) {
    AnimatedVisibility(
        visible = state.isAnswerLocked,
        enter = fadeIn(tween(300)) + slideInVertically(initialOffsetY = { 50 }, animationSpec = tween(300))
    ) {
        Button(
            onClick = { viewModel.handleIntent(QuizContract.Intent.NextQuestion) },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(bottom = 32.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = if (state.isLastQuestion) stringResource(R.string.quiz_view_results) else stringResource(R.string.quiz_next_question),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun AnswerOption(
    answer: String,
    letter: String,
    isSelected: Boolean,
    isLocked: Boolean,
    isCorrect: Boolean,
    delayMs: Long,
    onClick: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetXAnim = remember { Animatable(-20f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(300)) }
        launch { offsetXAnim.animateTo(0f, tween(300)) }
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    
    // States after lock
    val bgColor = when {
        isLocked && isSelected && isCorrect -> (if (isDark) SuccessDark else SuccessLight).copy(alpha = 0.2f)
        isLocked && isSelected && !isCorrect -> (if (isDark) ErrorDark else ErrorLight).copy(alpha = 0.2f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val borderColor = when {
        isLocked && isSelected && isCorrect -> if (isDark) SuccessDark else SuccessLight
        isLocked && isSelected && !isCorrect -> if (isDark) ErrorDark else ErrorLight
        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val iconBgColor = when {
        isLocked && isSelected && isCorrect -> if (isDark) SuccessDark else SuccessLight
        isLocked && isSelected && !isCorrect -> if (isDark) ErrorDark else ErrorLight
        isSelected && !isLocked -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
    }

    val finalAlpha = if (isLocked && !isSelected) 0.6f else alphaAnim.value

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(finalAlpha)
            .offset(x = offsetXAnim.value.dp)
            .clickable(enabled = !isLocked, onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(if (isSelected && isLocked) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                if (isLocked && isSelected && isCorrect) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else if (isLocked && isSelected && !isCorrect) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                } else {
                    Text(text = letter, color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = answer,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}