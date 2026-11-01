package com.wordrecall.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.weight
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
    LEARNED
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
                                    if (it.id == id) it.copy(learned = true) else it
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
                                    if (it.id == id) it.copy(learned = !it.learned) else it
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
                text = "词忆",
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
    onOpenImport: () -> Unit,
    onOpenBook: () -> Unit
) {
    val unmarkedWords = words.filter { !it.learned }
    val wordSetKey = unmarkedWords.joinToString("|") { "${it.id}:${it.english}:${it.chinese}" }
    var shuffleRound by rememberSaveable { mutableIntStateOf(0) }
    val shuffledWords = remember(wordSetKey, shuffleRound) {
        unmarkedWords.shuffled()
    }
    var currentIndex by rememberSaveable(wordSetKey, shuffleRound) {
        mutableIntStateOf(0)
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

        currentIndex >= shuffledWords.size -> RecallEmptyState(
            title = "这一轮已经完成",
            detail = "没有评分。你可以重新打乱顺序，再回忆一次尚未标记的单词。",
            actionLabel = "重新打乱本轮",
            onAction = {
                shuffleRound += 1
                currentIndex = 0
            }
        )

        else -> {
            val current = shuffledWords[currentIndex]
            var answer by rememberSaveable(current.id) { mutableStateOf("") }
            var submitted by rememberSaveable(current.id) { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
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
                            text = "根据英文写出你记得的汉语意思",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MutedInk
                        )
                    }
                    Surface(
                        color = PaleMoss,
                        shape = RoundedCornerShape(99.dp)
                    ) {
                        Text(
                            text = "${currentIndex + 1} / ${shuffledWords.size}",
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

                OutlinedTextField(
                    value = answer,
                    onValueChange = { answer = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("写下汉语意思") },
                    placeholder = { Text("不用完全一致，写出你想到的意思") },
                    minLines = 3,
                    maxLines = 5,
                    enabled = !submitted,
                    shape = RoundedCornerShape(17.dp)
                )
                Spacer(Modifier.height(12.dp))

                if (!submitted) {
                    Button(
                        onClick = { submitted = true },
                        enabled = answer.isNotBlank(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("提交并查看答案", fontWeight = FontWeight.Bold)
                    }
                } else {
                    AnswerReveal(
                        english = current.english,
                        originalChinese = current.chinese,
                        userAnswer = answer
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
                    text = "本应用只展示你填写的内容和原始答案，不判断对错。",
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
private fun AnswerReveal(
    english: String,
    originalChinese: String,
    userAnswer: String
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
                    color = SoftClay,
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = "不评分",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = Color(0xFF7D3D1B),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            AnswerLine("英文", english, Ink)
            Spacer(Modifier.height(12.dp))
            AnswerLine("原始汉语", originalChinese, Moss)
            Spacer(Modifier.height(12.dp))
            AnswerLine("你填写的内容", userAnswer.trim(), Clay)
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
    val filteredWords = when (filter) {
        BookFilter.ALL -> words
        BookFilter.LEARNED -> words.filter { it.learned }
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
                text = "查看全部导入内容，也可以随时改变熟记状态。",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedInk
            )
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            text = "还没有标记为熟记的单词",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "在回忆页或全部单词中使用“标记已熟记”。",
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
                    color = if (word.learned) SoftClay else PaleMoss,
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = if (word.learned) "已熟记" else "回忆中",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        color = if (word.learned) Color(0xFF7D3D1B) else MossDark,
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
