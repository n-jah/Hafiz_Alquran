package com.najah.qurantest.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.najah.qurantest.model.QuranVerse
import com.najah.qurantest.model.Result
import com.najah.qurantest.repository.QuranRepository
import kotlinx.coroutines.launch

class QuranViewModel(private val repository: QuranRepository) : ViewModel() {
    private val _state = MutableLiveData<Result<List<List<QuranVerse>>>>(Result.Loading())
    val state: LiveData<Result<List<List<QuranVerse>>>> get() = _state

    fun generateQuestions(startJuz: Int, endJuz: Int, numOfQuestions: Int, numOfLines: Int) {
        viewModelScope.launch {
            _state.value = Result.Loading()
            try {
                val selectedJuzRange = (startJuz..endJuz).toList()
                val generatedQuestions = mutableListOf<List<QuranVerse>>()
                val usedSurahs = mutableSetOf<Int>()
                val usedRanges = mutableSetOf<Pair<Int, Int>>()

                val segments = divideJuzRange(selectedJuzRange, numOfQuestions)
                Log.d("QuranViewModel", "Generated segments: $segments")

                repeat(numOfQuestions) { index ->
                    val selectedJuz = segments[index]
                    val surahsInJuz = repository.getSurahsInJuz(selectedJuz)

                    if (selectedJuz == 1) {
                        usedSurahs.add(1) // Exclude Fatiha
                    }

                    var availableSurahs = surahsInJuz.filter { it !in usedSurahs }
                    if (availableSurahs.isEmpty()) {
                        // If all Surahs are used, retry with previously used ones
                        availableSurahs = surahsInJuz
                        Log.w("QuranViewModel", "Retrying with same Surah in Juz $selectedJuz")
                    }

                    if (availableSurahs.isNotEmpty()) {
                        val randomSurah = availableSurahs.random()
                        if (randomSurah !in setOf(2, 3)) {
                            usedSurahs.add(randomSurah)
                        }

                        val verses = repository.getVersesInSurah(randomSurah)
                        val adjustedNumOfLines = adjustNumOfLines(verses, numOfLines, selectedJuz)

                        var question: List<QuranVerse> = emptyList()
                        var questionRange: Pair<Int, Int> = Pair(-1, -1)
                        var attempt = 0
                        val maxAttempts = 10

                        do {
                            if (attempt >= maxAttempts) {
                                Log.w("QuranViewModel", "Max attempts reached. Retrying with the same Surah.")
                                break
                            }

                            question = generateQuestionWithinSurah(verses, adjustedNumOfLines, usedRanges)
                            if (question.isNotEmpty()) {
                                val startId = question.first().id ?: -1
                                val endId = question.last().id ?: -1
                                questionRange = Pair(startId, endId)
                            } else {
                                questionRange = Pair(-1, -1)
                            }
                            attempt++
                        } while (usedRanges.contains(questionRange))

                        if (question.isNotEmpty()) {
                            usedRanges.add(questionRange)
                            generatedQuestions.add(question)
                            Log.d("QuranViewModel", "Generated Question: $question")
                        } else {
                            Log.w("QuranViewModel", "Failed to generate a valid question in Surah $randomSurah")
                        }
                    } else {
                        Log.w("QuranViewModel", "No available Surahs left in Juz $selectedJuz")
                    }
                }

                if (generatedQuestions.size < numOfQuestions) {
                    Log.w("QuranViewModel", "Not enough unique Surahs. Reducing the number of questions.")
                }

                val sortedGeneratedQuestions = generatedQuestions.map { question ->
                    question.sortedBy { it.id }
                }.sortedBy { it.firstOrNull()?.id ?: Int.MAX_VALUE }

                _state.value = Result.Success(sortedGeneratedQuestions)
            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error generating questions", e)
                _state.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun divideJuzRange(selectedJuzRange: List<Int>, numOfQuestions: Int): List<Int> {
        val result = mutableListOf<Int>()

        if (selectedJuzRange.isEmpty()) {
            return result
        }

        if (numOfQuestions >= selectedJuzRange.size) {
            while (result.size < numOfQuestions) {
                result.addAll(selectedJuzRange)
            }
            return result.take(numOfQuestions).sorted()
        }

        // Calculate step size for fair distribution
        val step = selectedJuzRange.size / numOfQuestions.toDouble()
        var currentIndex = 0.0

        repeat(numOfQuestions) {
            val lowerBound = currentIndex.toInt()
            val upperBound = (currentIndex + step).toInt().coerceAtMost(selectedJuzRange.lastIndex)
            val randomIndex = (lowerBound..upperBound).random()

            result.add(selectedJuzRange[randomIndex])
            currentIndex += step
        }

        return result.shuffled().sorted()
    }

    private fun adjustNumOfLines(verses: List<QuranVerse>, numOfLines: Int, selectedJuz: Int): Int {
        val availableLines = verses.size

        if (selectedJuz == 30) {
            return availableLines.coerceAtLeast(1).coerceAtMost(numOfLines)
        }

        val adjustedLines = (numOfLines.toFloat() * 0.75).toInt()
        return availableLines.coerceAtMost(adjustedLines)
    }

    private fun generateQuestionWithinSurah(
        verses: List<QuranVerse>,
        numOfLines: Int,
        usedRanges: MutableSet<Pair<Int, Int>> // Track used verse ranges
    ): List<QuranVerse> {
        val availableRanges = mutableListOf<Pair<Int, Int>>()
        val minDistance = when {
            verses.size > 100 -> 10 // Longer Surahs
            verses.size > 50 -> 5  // Medium-length Surahs
            else -> 3              // Shorter Surahs
        }

        // Find all possible non-overlapping ranges with minimum distance
        for (i in 0 until verses.size - numOfLines) {
            val range = Pair(verses[i].id ?: -1, verses[i + numOfLines - 1].id ?: -1)

            // Check if this range is too close to any used range
            val isTooClose = usedRanges.any { usedRange ->
                val usedStart = usedRange.first
                val usedEnd = usedRange.second
                val currentStart = range.first
                val currentEnd = range.second

                // Check if the current range is within `minDistance` of any used range
                (currentStart in (usedStart - minDistance)..(usedEnd + minDistance)) ||
                        (currentEnd in (usedStart - minDistance)..(usedEnd + minDistance))
            }

            if (!isTooClose) {
                availableRanges.add(range)
            }
        }

        if (availableRanges.isEmpty()) {
            Log.w("QuranViewModel", "No unique ranges left in this Surah with minimum distance.")
            return emptyList() // No more unique questions can be generated
        }

        // Select a non-repeating range
        val (startId, endId) = availableRanges.random()
        val startIndex = verses.indexOfFirst { it.id == startId }
        val endIndex = verses.indexOfFirst { it.id == endId }

        return verses.subList(startIndex, endIndex + 1)
    }
}