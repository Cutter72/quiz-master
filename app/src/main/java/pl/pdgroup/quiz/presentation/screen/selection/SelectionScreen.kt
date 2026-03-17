package pl.pdgroup.quiz.presentation.screen.selection

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.R
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.ui.theme.SuccessLight
import pl.pdgroup.quiz.ui.theme.SuccessDark
import pl.pdgroup.quiz.ui.theme.ErrorLight
import pl.pdgroup.quiz.ui.theme.ErrorDark
import pl.pdgroup.quiz.ui.theme.QuizTheme

@Composable
fun SelectionScreen(
    onNavigateBack: () -> Unit,
    onNavigateToQuiz: (String, Difficulty) -> Unit,
    viewModel: SelectionViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is SelectionContract.Effect.NavigateToQuiz -> {
                    onNavigateToQuiz(effect.category, effect.difficulty)
                }
                is SelectionContract.Effect.ShowError -> {
                    // Handled by AlertDialog below
                }
            }
        }
    }

    SelectionScreenContent(
        state = state,
        onNavigateBack = onNavigateBack,
        onIntent = { viewModel.handleIntent(it) }
    )
}

@Composable
fun SelectionScreenContent(
    state: SelectionContract.State,
    onNavigateBack: () -> Unit,
    onIntent: (SelectionContract.Intent) -> Unit
) {
    if (state.error != null) {
        SelectionErrorDialog(
            error = state.error,
            onDismiss = { onIntent(SelectionContract.Intent.ClearError) }
        )
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SelectionHeader(onNavigateBack = onNavigateBack)

            Spacer(modifier = Modifier.height(32.dp))

            CategorySelection(
                categories = state.categories,
                selectedCategory = state.selectedCategory,
                onCategorySelected = { onIntent(SelectionContract.Intent.SelectCategory(it)) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            DifficultySelection(
                difficulties = state.difficulties,
                selectedDifficulty = state.selectedDifficulty,
                onDifficultySelected = { onIntent(SelectionContract.Intent.SelectDifficulty(it)) }
            )

            Spacer(modifier = Modifier.height(32.dp))

            SelectionInfoPanel(
                selectedCategory = state.selectedCategory,
                selectedDifficulty = state.selectedDifficulty,
                availableQuestionsCount = state.availableQuestionsCount
            )

            Spacer(modifier = Modifier.height(32.dp))

            StartQuizButton(
                isStartEnabled = state.isStartEnabled,
                isLoading = state.isLoading,
                onStartClick = { onIntent(SelectionContract.Intent.StartQuiz) }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SelectionErrorDialog(
    error: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.dialog_notice_title)) },
        text = { Text(error ?: "") },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.dialog_ok))
            }
        }
    )
}

@Composable
fun SelectionHeader(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.content_desc_back),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = stringResource(R.string.selection_title),
            style = MaterialTheme.typography.headlineLarge
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategorySelection(
    categories: List<String>,
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    SelectionCard(
        title = stringResource(R.string.selection_choose_category),
        delayMs = 0
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 3
        ) {
            categories.forEachIndexed { index, category ->
                val isSelected = selectedCategory == category
                Box(modifier = Modifier.weight(1f)) {
                    SelectableChip(
                        text = category,
                        isSelected = isSelected,
                        selectedColor = MaterialTheme.colorScheme.primary,
                        delayMs = index * 50L,
                        onClick = { onCategorySelected(category) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DifficultySelection(
    difficulties: List<Difficulty>,
    selectedDifficulty: Difficulty?,
    onDifficultySelected: (Difficulty) -> Unit
) {
    SelectionCard(
        title = stringResource(R.string.selection_choose_difficulty),
        delayMs = 200
    ) {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            maxItemsInEachRow = 3
        ) {
            difficulties.forEachIndexed { index, difficulty ->
                val isSelected = selectedDifficulty == difficulty
                val isDark = MaterialTheme.colorScheme.background.red < 0.5f
                val color = when (difficulty) {
                    Difficulty.EASY -> if (isDark) SuccessDark else SuccessLight
                    Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
                    Difficulty.HARD -> MaterialTheme.colorScheme.error
                }
                
                Box(modifier = Modifier.weight(1f)) {
                    SelectableChip(
                        text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                        isSelected = isSelected,
                        selectedColor = color,
                        delayMs = 200L + (index * 50L),
                        onClick = { onDifficultySelected(difficulty) }
                    )
                }
            }
        }
    }
}

@Composable
fun SelectionInfoPanel(
    selectedCategory: String?,
    selectedDifficulty: Difficulty?,
    availableQuestionsCount: Int
) {
    AnimatedVisibility(
        visible = selectedCategory != null && selectedDifficulty != null,
        enter = fadeIn(tween(500))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (selectedCategory != null && selectedDifficulty != null) {
                    Text(
                        text = stringResource(
                            R.string.selection_info_selected,
                            selectedCategory,
                            selectedDifficulty.name.lowercase().replaceFirstChar { it.uppercase() }
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.selection_info_available, availableQuestionsCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun StartQuizButton(
    isStartEnabled: Boolean,
    isLoading: Boolean,
    onStartClick: () -> Unit
) {
    val startButtonAlpha = remember { Animatable(0f) }
    val startButtonOffsetY = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(400)
        launch { startButtonAlpha.animateTo(1f, tween(300)) }
        launch { startButtonOffsetY.animateTo(0f, tween(300)) }
    }

    Button(
        onClick = onStartClick,
        enabled = isStartEnabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .alpha(startButtonAlpha.value)
            .offset(y = startButtonOffsetY.value.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.DarkGray
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
            Text(
                text = stringResource(R.string.selection_start_quiz),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    delayMs: Long,
    content: @Composable () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetYAnim = remember { Animatable(20f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(500)) }
        launch { offsetYAnim.animateTo(0f, tween(500)) }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim.value)
            .offset(y = offsetYAnim.value.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(32.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            content()
        }
    }
}

@Composable
fun SelectableChip(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    delayMs: Long,
    onClick: () -> Unit
) {
    val alphaAnim = remember { Animatable(0f) }
    val scaleAnimEntry = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(300)) }
        launch { scaleAnimEntry.animateTo(1f, spring(stiffness = Spring.StiffnessMedium)) }
    }

    val interactionSource = remember { MutableInteractionSource() }
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .alpha(alphaAnim.value)
            .scale(scaleAnimEntry.value * scaleAnim.value)
            .background(
                color = if (isSelected) selectedColor else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                coroutineScope.launch {
                    scaleAnim.animateTo(0.95f, spring(stiffness = Spring.StiffnessHigh))
                    scaleAnim.animateTo(1f, spring(stiffness = Spring.StiffnessMedium))
                    onClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            textAlign = TextAlign.Center
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SelectionScreenPreview() {
    QuizTheme {
        Surface {
            SelectionScreenContent(
                state = SelectionContract.State(
                    categories = listOf("Science", "History", "Geography"),
                    difficulties = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD),
                    selectedCategory = "Science",
                    selectedDifficulty = Difficulty.MEDIUM,
                    availableQuestionsCount = 50
                ),
                onNavigateBack = {},
                onIntent = {}
            )
        }
    }
}