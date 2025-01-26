package com.najah.qurantest.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.najah.qurantest.model.QuranVerse
import com.najah.qurantest.repository.QuranRepository
import kotlinx.coroutines.launch
import com.najah.qurantest.model.Result

class QuranViewModel(private val repository: QuranRepository) : ViewModel() {
    private val _state = MutableLiveData<Result<List<List<QuranVerse>>>>(Result.Loading())
    val state: LiveData<Result<List<List<QuranVerse>>>> get() = _state

    fun generateQuestions(startJuz: Int, endJuz: Int, numOfQuestions: Int, numOfLines: Int) {
        viewModelScope.launch {
            _state.value = Result.Loading()

            try {
                val selectedJuzRange = (startJuz..endJuz).toList()
                if (selectedJuzRange.isEmpty()) {
                    _state.value = Result.Error("Invalid Juz range.")
                    return@launch
                }

                val generatedQuestions = mutableListOf<List<QuranVerse>>()
                val usedSurahs = mutableSetOf<Int>()
                val segments = divideJuzRange(selectedJuzRange, numOfQuestions)

                for (selectedJuz in segments) {
                    if (generatedQuestions.size >= numOfQuestions) break

                    val surahsInJuz = repository.getSurahsInJuz(selectedJuz)
                    val availableSurahs = surahsInJuz.filterNot { it == 1 || it in usedSurahs }
                    val surahPool = if (availableSurahs.isEmpty()) surahsInJuz else availableSurahs

                    if (surahPool.isEmpty()) continue

                    val randomSurah = surahPool.random()
                    usedSurahs.add(randomSurah)

                    val verses = repository.getVersesInSurah(randomSurah)
                    val randomStartIndex = verses.indices.random()
                    val adjustedNumOfLines = adjustNumOfLines(verses, numOfLines)

                    val question = generateQuestionWithinSurah(verses, randomStartIndex, adjustedNumOfLines)
                    if (question.isNotEmpty()) {
                        generatedQuestions.add(question)
                    }
                }

                // After all questions are generated, sort the entire list globally by Surah number and Ayah number
                val sortedQuestions = generatedQuestions.flatten()
                    .sortedWith(compareBy({ it.suraNo }, { it.ayaNo }))
                    .chunked(numOfLines) // Re-group into original chunks

                if (sortedQuestions.size < numOfQuestions) {
                    Log.w("QuranViewModel", "Generated fewer questions (${sortedQuestions.size}) than requested ($numOfQuestions).")
                }

                _state.value = Result.Success(sortedQuestions)

            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error generating questions", e)
                _state.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun divideJuzRange(selectedJuzRange: List<Int>, numOfQuestions: Int): List<Int> {
        val segments = mutableListOf<Int>()
        val juzCount = selectedJuzRange.size

        // If the number of Juzes is less than the number of questions, repeat Juzes
        if (juzCount < numOfQuestions) {
            val repeatCount = numOfQuestions / juzCount
            val remainder = numOfQuestions % juzCount

            // Add repeated Juzes
            for (juz in selectedJuzRange) {
                repeat(repeatCount) {
                    segments.add(juz)
                }
            }

            // Handle remaining questions by randomly adding Juzes from the range
            repeat(remainder) {
                segments.add(selectedJuzRange.random())
            }
        } else {
            // If Juzes are more than or equal to questions, randomly select
            repeat(numOfQuestions) {
                segments.add(selectedJuzRange.random())
            }
        }

        return segments
    }

    private fun adjustNumOfLines(verses: List<QuranVerse>, numOfLines: Int): Int {
        return numOfLines.coerceAtMost(verses.size).coerceAtLeast(1)
    }

    private fun generateQuestionWithinSurah(
        verses: List<QuranVerse>,
        randomStartIndex: Int,
        numOfLines: Int
    ): List<QuranVerse> {
        val endIndex = (randomStartIndex + numOfLines).coerceAtMost(verses.size)
        return verses.subList(randomStartIndex, endIndex)
    }
}
