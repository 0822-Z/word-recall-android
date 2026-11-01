package com.wordrecall.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class WordRepository(context: Context) {
    private val preferences =
        context.getSharedPreferences("word_recall_store", Context.MODE_PRIVATE)

    fun load(): List<WordEntry> {
        val raw = preferences.getString(WORDS_KEY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    add(
                        WordEntry(
                            id = item.getString("id"),
                            english = item.getString("english"),
                            chinese = item.getString("chinese"),
                            learned = item.optBoolean("learned", false),
                            needsReview = item.optBoolean("needsReview", false)
                        )
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun save(words: List<WordEntry>) {
        val array = JSONArray()
        words.forEach { word ->
            array.put(
                JSONObject()
                    .put("id", word.id)
                    .put("english", word.english)
                    .put("chinese", word.chinese)
                    .put("learned", word.learned)
                    .put("needsReview", word.needsReview)
            )
        }
        preferences.edit().putString(WORDS_KEY, array.toString()).apply()
    }

    private companion object {
        const val WORDS_KEY = "words"
    }
}
