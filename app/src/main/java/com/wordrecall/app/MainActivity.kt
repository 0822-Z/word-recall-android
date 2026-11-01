package com.wordrecall.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.UUID

private val Cream = Color(0xFFF7F3EA)
private val Ink = Color(0xFF26332E)
private val Moss = Color(0xFF326B57)
private val MossDark = Color(0xFF245040)
private val Clay = Color(0xFFD7814D)
private val PaleMoss = Color(0xFFE4EEE7)
private val SoftClay = Color(0xFFFFE8D7)
private val MutedInk = Color(0xFF68736E)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = WordRepository(applicationContext)
        setContent {
            WordRecallTheme {
                WordRecallApp(repository)
            }
        }
    }
}

private enum class AppTab(val label: String) {
    IMPORT("1  导入"),
    RECALL("2  回忆"),
    BOOK("3  单词本")
}

private enum class BookFilter {
    ALL,
    LEARNED,
    NEEDS_WORK
}

@Composable
private fun WordRecallTheme(content: @Composable () -> Unit) {
    val colors = lightColorScheme(
        primary = Moss,
        onPrimary = Color.White,
        primaryContainer = PaleMoss,
        onPrimaryContainer = MossDark,
        secondary = Clay,
        onSecondary = Color.White,
        secondaryContainer = SoftClay,
        onSecondaryContainer = Color(0xFF6D3517),
        background = Cream,
        onBackground = Ink,
        surface = Color.White,
        onSurface = Ink,
        surfaceVariant = Color(0xFFF0EADF),
        onSurfaceVariant = MutedInk,
        outline = Color(0xFFC9C2B6)
    )
    MaterialTheme(
        colorScheme = colors,
        typography = Typography(
            headlineLarge = Typography().headlineLarge.copy(fontWeight = FontWeight.Bold),
            titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.Bold),
            titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold)
        ),
        content = content
    )
}

@Composable
private fun WordRecallApp(repository: WordRepository) {
    var words by remember { mutableStateOf(repository.load()) }
    var selectedTab by rememberSaveable { mutableStateOf(AppTab.IMPORT) }

    fun updateWords(updated: List<WordEntry>) {
        words = updated
        repository.save(updated)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Cream,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            AppHeader(
                wordCount = words.size,
                unmarkedCount = words.count { !it.learned }
            )
            AppNavigation(
                selected = selectedTab,
                onSelected = { selectedTab = it }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (selectedTab) {
                    AppTab.IMPORT -> ImportScreen(
                        words = words,
                        onAdd = { english, chinese ->
                            updateWords(
                                words + WordEntry(
                                    id = UUID.randomUUID().toString(),
                                    english = english,
                                    chinese = chinese
                                )
                            )
                        },
                        onOpenBook = { selectedTab = AppTab.BOOK }
                    )

                    AppTab.RECALL -> RecallScreen(
                        words = words,
                        onMarkLearned = { id ->
                            updateWords(
                                words.map {
                                    if (it.id == id) {
                                        it.copy(learned = true, needsReview = false)
                                    } else {
                                        it
                                    }
                                }
                            )
                        },
                        onNoImpression = { id ->
                            updateWords(
                                words.map {
                                    if (it.id == id) it.copy(needsReview = true) else it
                                }
                            )
                        },
                        onHasImpression = { id ->
                            updateWords(
                                words.map {
                                    if (it.id == id) it.copy(needsReview = false) else it
                                }
                            )
                        },
                        onOpenImport = { selectedTab = AppTab.IMPORT },
                        onOpenBook = { selectedTab = AppTab.BOOK }
                    )

                    AppTab.BOOK -> WordBookScreen(
                        words = words,
                        onToggleLearned = { id ->
                            updateWords(
                                words.map {
                                    if (it.id == id) {
                                        if (it.learned) {
                                            it.copy(learned = false)
                                        } else {
                                            it.copy(learned = true, needsReview = false)
                                        }
                                    } else {
                                        it
                                    }
                                }
                            )
                        },
                        onOpenImport = { selectedTab = AppTab.IMPORT }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppHeader(wordCount: Int, unmarkedCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(Moss, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "词",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 21.sp
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "词兮",
                style = MaterialTheme.typography.headlineLarge,
                color = Ink
            )
            Text(
                text = "记住，而不是被评分",
                style = MaterialTheme.typography.bodySmall,
                color = MutedInk
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$unmarkedCount 待回忆",
                style = MaterialTheme.typography.labelLarge,
                color = Moss,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "共 $wordCount 个词",
                style = MaterialTheme.typography.labelSmall,
                color = MutedInk
            )
        }
    }
}

@Composable
private fun AppNavigation(
    selected: AppTab,
    onSelected: (AppTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        AppTab.entries.forEach { tab ->
            val isSelected = tab == selected
            if (isSelected) {
                Button(
                    onClick = { onSelected(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(13.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(tab.label, maxLines = 1, fontSize = 13.sp)
                }
            } else {
                OutlinedButton(
                    onClick = { onSelected(tab) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(13.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp)
                ) {
                    Text(tab.label, maxLines = 1, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun ImportScreen(
    words: List<WordEntry>,
    onAdd: (String, String) -> Unit,
    onOpenBook: () -> Unit
) {
    var english by rememberSaveable { mutableStateOf("") }
    var chinese by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var messageIsError by rememberSaveable { mutableStateOf(false) }

    fun submit() {
        val cleanEnglish = english.trim()
        val cleanChinese = chinese.trim()
        when {
            cleanEnglish.isEmpty() -> {
                message = "请先填写英文单词或短语"
                messageIsError = true
            }

            cleanChinese.isEmpty() -> {
                message = "请填写对应的汉语意思"
                messageIsError = true
            }

            words.any {
                it.english.equals(cleanEnglish, ignoreCase = true) &&
                    it.chinese == cleanChinese
            } -> {
                message = "这个词和它的释义已经导入过了"
                messageIsError = true
            }

            else -> {
                onAdd(cleanEnglish, cleanChinese)
                english = ""
                chinese = ""
                message = "已加入单词本，可以去回忆练习了"
                messageIsError = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 18.dp)
    ) {
        Text(
            text = "导入新词",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(Modifier.height(5.dp))
        Text(
            text = "一次录入一个单词或短语，数据只保存在这台手机上。",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedInk
        )
        Spacer(Modifier.height(18.dp))

        StatsCard(
            total = words.size,
            learned = words.count { it.learned },
            onOpenBook = onOpenBook
        )
        Spacer(Modifier.height(18.dp))

        OutlinedTextField(
            value = english,
            onValueChange = {
                english = it
                message = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("英文单词或短语") },
            placeholder = { Text("例如：perseverance") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = chinese,
            onValueChange = {
                chinese = it
                message = ""
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("汉语意思") },
            placeholder = { Text("例如：坚持不懈") },
            minLines = 2,
            maxLines = 4,
            shape = RoundedCornerShape(16.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() })
        )
        if (message.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = if (messageIsError) Color(0xFF9B3B2B) else Moss,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { submit() },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("加入单词本", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "提示：回忆页只显示英文，不会显示这里的答案，也不会自动评分。",
            style = MaterialTheme.typography.bodySmall,
            color = MutedInk
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatsCard(
    total: Int,
    learned: Int,
    onOpenBook: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$total",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text("已导入单词", style = MaterialTheme.typography.bodySmall, color = MutedInk)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$learned",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Clay
                )
                Text("已标记熟记", style = MaterialTheme.typography.bodySmall, color = MutedInk)
            }
            TextButton(onClick = onOpenBook) {
                Text("查看全部")
            }
        }
    }
}

@Composable
private fun RecallScreen(
    words: List<WordEntry>,
    onMarkLearned: (String) -> Unit,
    onNoImpression: (String) -> Unit,
    onHasImpression: (String) -> Unit,
    onOpenImport: () -> Unit,
    onOpenBook: () -> Unit
) {
    val unmarkedWords = words.filter { !it.learned }
    val sampleSizes = listOf(10, 15, 20, 30, 50)
    var selectedCount by rememberSaveable { mutableIntStateOf(10) }
    var sessionIds by remember { mutableStateOf<List<String>>(emptyList()) }
    var currentIndex by rememberSaveable { mutableIntStateOf(0) }
    val sessionWords = sessionIds.mapNotNull { id ->
        words.firstOrNull { it.id == id }
    }

    when {
        words.isEmpty() -> RecallEmptyState(
            title = "还没有可回忆的单词",
            detail = "先导入几个单词，再回来进行乱序回忆。",
            actionLabel = "去导入单词",
            onAction = onOpenImport
        )

        unmarkedWords.isEmpty() -> RecallEmptyState(
            title = "所有单词都已标记熟记",
            detail = "如果之后想重新练习，可以在单词本里取消熟记标记。",
            actionLabel = "查看单词本",
            onAction = onOpenBook
        )

        sessionIds.isEmpty() -> RecallSetup(
            availableCount = unmarkedWords.size,
            selectedCount = selectedCount,
            sampleSizes = sampleSizes,
            onSelected = { selectedCount = it },
            onStart = {
                sessionIds = unmarkedWords
                    .shuffled()
                    .take(selectedCount)
                    .map { it.id }
                currentIndex = 0
            }
        )

        currentIndex >= sessionWords.size -> RecallEmptyState(
            title = "这一轮已经完成",
            detail = "这一轮共回忆 ${sessionWords.size} 个单词。你可以重新选择数量，再随机抽取一组。",
            actionLabel = "重新选择数量",
            onAction = {
                sessionIds = emptyList()
                currentIndex = 0
            }
        )

        else -> {
            val current = sessionWords[currentIndex]
            var revealed by rememberSaveable(current.id) { mutableStateOf(false) }
            var resultLabel by rememberSaveable(current.id) { mutableStateOf("") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 18.dp, vertical = 18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "乱序回忆",
                            style = MaterialTheme.typography.headlineLarge
                        )
                        Text(
                            text = "先凭记忆判断，再查看中文释义",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk
                        )
                    }
                    Surface(
                        color = PaleMoss,
                        shape = RoundedCornerShape(99.dp)
                    ) {
                        Text(
                            text = "${currentIndex + 1} / ${sessionWords.size}",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            color = MossDark,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Spacer(Modifier.height(18.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MossDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "ENGLISH",
                            color = Color(0xFFB9D1C4),
                            style = MaterialTheme.typography.labelSmall,
                            letterSpacing = 1.6.sp
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = current.english,
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            fontSize = 31.sp,
                            lineHeight = 38.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))

                if (!revealed) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                resultLabel = "没印象"
                                revealed = true
                                onNoImpression(current.id)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Clay)
                        ) {
                            Text("没印象", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                resultLabel = "有印象"
                                revealed = true
                                onHasImpression(current.id)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(54.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("有印象", fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "点击任一选项都会立即显示答案。",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedInk,
                        textAlign = TextAlign.Center
                    )
                } else {
                    AnswerReveal(
                        english = current.english,
                        originalChinese = current.chinese,
                        resultLabel = resultLabel
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { currentIndex += 1 },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("下一个单词", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { onMarkLearned(current.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Clay)
                ) {
                    Text("标记已熟记，不再出现")
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "“有印象”和“没印象”都是你的自主判断，应用不会自动评分。",
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MutedInk,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RecallSetup(
    availableCount: Int,
    selectedCount: Int,
    sampleSizes: List<Int>,
    onSelected: (Int) -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 20.dp)
    ) {
        Text(
            text = "选择本轮数量",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "从尚未熟记的单词中随机抽取，顺序也会打乱。",
            style = MaterialTheme.typography.bodyMedium,
            color = MutedInk
        )
        Spacer(Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "可回忆单词",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedInk
                )
                Text(
                    text = "$availableCount 个",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = MossDark
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "本轮数量",
                    style = MaterialTheme.typography.labelMedium,
                    color = MutedInk
                )
                Text(
                    text = "${minOf(selectedCount, availableCount)} 个",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Clay
                )
                if (selectedCount > availableCount) {
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = "当前词量不足，将练习全部可用的 $availableCount 个单词。",
                        style = MaterialTheme.typography.bodySmall,
                        color = Clay
                    )
                }
            }
        }
        Spacer(Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            sampleSizes.forEach { size ->
                val selected = selectedCount == size
                if (selected) {
                    Button(
                        onClick = { onSelected(size) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Text("$size", fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = { onSelected(size) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(13.dp)
                    ) {
                        Text("$size")
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("开始回忆", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "开始后，每个词先显示英文。你可以选择“没印象”或“有印象”，两者都会立即显示原始中文释义。",
            style = MaterialTheme.typography.bodySmall,
            color = MutedInk
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AnswerReveal(
    english: String,
    originalChinese: String,
    resultLabel: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "答案对照",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    color = if (resultLabel == "没印象") SoftClay else PaleMoss,
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = resultLabel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = if (resultLabel == "没印象") Color(0xFF7D3D1B) else MossDark,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            AnswerLine("英文", english, Ink)
            Spacer(Modifier.height(12.dp))
            AnswerLine("原始汉语", originalChinese, Moss)
        }
    }
}

@Composable
private fun AnswerLine(label: String, value: String, color: Color) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MutedInk
    )
    Spacer(Modifier.height(3.dp))
    Text(
        text = value,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun RecallEmptyState(
    title: String,
    detail: String,
    actionLabel: String,
    onAction: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(74.dp),
            shape = RoundedCornerShape(24.dp),
            color = PaleMoss
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("✓", fontSize = 34.sp, color = Moss, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = detail,
            style = MaterialTheme.typography.bodyMedium,
            color = MutedInk,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = onAction,
            shape = RoundedCornerShape(15.dp)
        ) {
            Text(actionLabel)
        }
    }
}

@Composable
private fun WordBookScreen(
    words: List<WordEntry>,
    onToggleLearned: (String) -> Unit,
    onOpenImport: () -> Unit
) {
    var filter by rememberSaveable { mutableStateOf(BookFilter.ALL) }
    val learnedCount = words.count { it.learned }
    val needsReviewCount = words.count { it.needsReview }
    val filteredWords = when (filter) {
        BookFilter.ALL -> words
        BookFilter.LEARNED -> words.filter { it.learned }
        BookFilter.NEEDS_WORK -> words.filter { it.needsReview }
    }

    if (words.isEmpty()) {
        RecallEmptyState(
            title = "单词本还是空的",
            detail = "导入的单词会全部保存在这里，标记熟记的单词也会单独归类。",
            actionLabel = "去导入第一个单词",
            onAction = onOpenImport
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, top = 18.dp, end = 18.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(
                text = "我的单词本",
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(Modifier.height(5.dp))
            Text(
                text = "查看全部内容、熟记单词和需要加强的单词。",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == BookFilter.ALL,
                    onClick = { filter = BookFilter.ALL },
                    label = { Text("全部 ${words.size}") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PaleMoss,
                        selectedLabelColor = MossDark
                    )
                )
                FilterChip(
                    selected = filter == BookFilter.LEARNED,
                    onClick = { filter = BookFilter.LEARNED },
                    label = { Text("已熟记 $learnedCount") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SoftClay,
                        selectedLabelColor = Color(0xFF7D3D1B)
                    )
                )
                FilterChip(
                    selected = filter == BookFilter.NEEDS_WORK,
                    onClick = { filter = BookFilter.NEEDS_WORK },
                    label = { Text("需要加强 $needsReviewCount") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = SoftClay,
                        selectedLabelColor = Color(0xFF7D3D1B)
                    )
                )
            }
            Spacer(Modifier.height(4.dp))
        }

        if (filteredWords.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when (filter) {
                                BookFilter.ALL -> "单词本还是空的"
                                BookFilter.LEARNED -> "还没有标记为熟记的单词"
                                BookFilter.NEEDS_WORK -> "“需要加强”列表还是空的"
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = when (filter) {
                                BookFilter.ALL -> "先导入一些单词。"
                                BookFilter.LEARNED -> "在回忆页或全部单词中使用“标记已熟记”。"
                                BookFilter.NEEDS_WORK -> "回忆时点击“没印象”，单词就会记录到这里。"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MutedInk,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        items(filteredWords, key = { it.id }) { word ->
            WordCard(
                word = word,
                onToggleLearned = { onToggleLearned(word.id) }
            )
        }
    }
}

@Composable
private fun WordCard(
    word: WordEntry,
    onToggleLearned: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = word.english,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        text = word.chinese,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedInk
                    )
                }
                Spacer(Modifier.width(10.dp))
                Surface(
                    color = if (word.learned || word.needsReview) SoftClay else PaleMoss,
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = when {
                            word.learned && word.needsReview -> "已熟记 · 需要加强"
                            word.learned -> "已熟记"
                            word.needsReview -> "需要加强"
                            else -> "回忆中"
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = if (word.learned || word.needsReview) Color(0xFF7D3D1B) else MossDark,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            TextButton(
                onClick = onToggleLearned,
                contentPadding = PaddingValues(horizontal = 2.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (word.learned) "取消熟记标记" else "标记已熟记",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
