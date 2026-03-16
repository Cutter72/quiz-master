package pl.pdgroup.quiz.presentation.screen.scoreboard

import androidx.compose.animation.*
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.ui.theme.ErrorDark
import pl.pdgroup.quiz.ui.theme.ErrorLight
import pl.pdgroup.quiz.ui.theme.SuccessDark
import pl.pdgroup.quiz.ui.theme.SuccessLight
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScoreboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: ScoreboardViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.background.red < 0.5f

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = 900.dp)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
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
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Scoreboard",
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Stats Overview
                if (state.allScores.isNotEmpty()) {
                    val statsAlpha = remember { Animatable(0f) }
                    val statsOffsetY = remember { Animatable(-20f) }

                    LaunchedEffect(Unit) {
                        launch { statsAlpha.animateTo(1f, tween(500)) }
                        launch { statsOffsetY.animateTo(0f, tween(500)) }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .alpha(statsAlpha.value)
                            .offset(y = statsOffsetY.value.dp)
                            .padding(bottom = 32.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            modifier = Modifier.weight(1f),
                            value = "${state.totalQuizzes}",
                            label = "Quizzes",
                            color = MaterialTheme.colorScheme.primary
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            value = String.format(Locale.getDefault(), "%.1f%%", state.averageScore),
                            label = "Average",
                            color = if (isDark) SuccessDark else SuccessLight
                        )
                        StatCard(
                            modifier = Modifier.weight(1f),
                            value = "${state.bestScore}%",
                            label = "Best",
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                // Tabs
                val tabsAlpha = remember { Animatable(0f) }
                LaunchedEffect(Unit) {
                    delay(100)
                    tabsAlpha.animateTo(1f, tween(500))
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .alpha(tabsAlpha.value)
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    ScrollableTabRow(
                        selectedTabIndex = state.categories.indexOf(state.selectedCategory).coerceAtLeast(0),
                        modifier = Modifier.padding(8.dp),
                        containerColor = Color.Transparent,
                        edgePadding = 8.dp,
                        divider = {}
                    ) {
                        state.categories.forEachIndexed { index, category ->
                            val isSelected = category == state.selectedCategory
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.handleIntent(ScoreboardContract.Intent.SelectCategory(category)) },
                                modifier = Modifier
                                    .height(48.dp)
                                    .padding(horizontal = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        else Color.Transparent
                                    )
                            ) {
                                Text(
                                    text = category,
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }

                // Content
                AnimatedContent(
                    targetState = state.filteredScores.isEmpty(),
                    transitionSpec = { fadeIn(tween(500)).togetherWith(fadeOut(tween(500))) }
                ) { isEmpty ->
                    if (isEmpty) {
                        EmptyState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(bottom = 32.dp)
                        ) {
                            itemsIndexed(state.filteredScores) { index, score ->
                                ScoreCard(score = score, delayMs = index * 50L)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier = Modifier, value: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = color,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EmptyState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )
            Text(
                text = "No scores yet",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Complete a quiz to see your scores here",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScoreCard(score: QuizScore, delayMs: Long) {
    val alphaAnim = remember { Animatable(0f) }
    val offsetXAnim = remember { Animatable(-20f) }

    LaunchedEffect(Unit) {
        delay(delayMs)
        launch { alphaAnim.animateTo(1f, tween(300)) }
        launch { offsetXAnim.animateTo(0f, tween(300)) }
    }

    val isDark = MaterialTheme.colorScheme.background.red < 0.5f
    val percentage = ((score.score.toFloat() / score.totalQuestions) * 100).toInt()
    
    val scoreColor = when {
        percentage >= 80 -> if (isDark) SuccessDark else SuccessLight
        percentage >= 60 -> MaterialTheme.colorScheme.primary
        percentage >= 40 -> MaterialTheme.colorScheme.secondary
        else -> if (isDark) ErrorDark else ErrorLight
    }

    val diffColor = when (score.difficulty) {
        Difficulty.EASY -> if (isDark) SuccessDark else SuccessLight
        Difficulty.MEDIUM -> MaterialTheme.colorScheme.secondary
        Difficulty.HARD -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(alphaAnim.value)
            .offset(x = offsetXAnim.value.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score Circle
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(scoreColor),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${score.score}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/${score.totalQuestions}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = score.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatDate(score.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                AssistChip(
                    onClick = { },
                    label = { Text(score.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = diffColor.copy(alpha = 0.1f)),
                    modifier = Modifier.height(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Percentage
            Text(
                text = "$percentage%",
                style = MaterialTheme.typography.headlineLarge,
                color = scoreColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

fun formatDate(isoString: String): String {
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
        val date = parser.parse(isoString) ?: return isoString
        val formatter = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.US).apply { timeZone = TimeZone.getDefault() }
        formatter.format(date)
    } catch (e: Exception) {
        isoString
    }
}