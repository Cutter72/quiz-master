package pl.pdgroup.quiz.presentation.screen.selection

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.ui.theme.SuccessLight
import pl.pdgroup.quiz.ui.theme.SuccessDark
import pl.pdgroup.quiz.ui.theme.ErrorLight
import pl.pdgroup.quiz.ui.theme.ErrorDark

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

    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.handleIntent(SelectionContract.Intent.ClearError) },
            title = { Text("Notice") },
            text = { Text(state.error ?: "") },
            confirmButton = {
                TextButton(onClick = { viewModel.handleIntent(SelectionContract.Intent.ClearError) }) {
                    Text("OK")
                }
            }
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Select Quiz Options",
                    style = MaterialTheme.typography.headlineLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Category Selection
            SelectionCard(
                title = "Choose a Category",
                delayMs = 0
            ) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachRow = 3 // responsive adjustment can be refined
                ) {
                    state.categories.forEachIndexed { index, category ->
                        val isSelected = state.selectedCategory == category
                        Box(modifier = Modifier.weight(1f)) {
                            SelectableChip(
                                text = category,
                                isSelected = isSelected,
                                selectedColor = MaterialTheme.colorScheme.primary,
                                delayMs = index * 50L,
                                onClick = { viewModel.handleIntent(SelectionContract.Intent.SelectCategory(category)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Difficulty Selection
            SelectionCard(
                title = "Choose Difficulty",
                delayMs = 200
            ) {
                @OptIn(ExperimentalLayoutApi::class)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    maxItemsInEachRow = 3
                ) {
                    state.difficulties.forEachIndexed { index, difficulty ->
                        val isSelected = state.selectedDifficulty == difficulty
                        val isDark = MaterialTheme.colorScheme.background.red < 0.5f // simple dark mode check
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
                                onClick = { viewModel.handleIntent(SelectionContract.Intent.SelectDifficulty(difficulty)) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Info Panel
            AnimatedVisibility(
                visible = state.selectedCategory != null && state.selectedDifficulty != null,
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
                        Text(
                            text = "You selected ${state.selectedCategory} with ${state.selectedDifficulty?.name?.lowercase()?.replaceFirstChar { it.uppercase() }} difficulty",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${state.availableQuestionsCount} question(s) available • Complete all to save your score",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Start Quiz Button
            val startButtonAlpha = remember { Animatable(0f) }
            val startButtonOffsetY = remember { Animatable(20f) }

            LaunchedEffect(Unit) {
                delay(400)
                launch { startButtonAlpha.animateTo(1f, tween(300)) }
                launch { startButtonOffsetY.animateTo(0f, tween(300)) }
            }

            Button(
                onClick = { viewModel.handleIntent(SelectionContract.Intent.StartQuiz) },
                enabled = state.isStartEnabled,
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
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "Start Quiz",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
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
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}