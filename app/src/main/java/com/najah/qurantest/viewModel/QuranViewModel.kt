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
            _state.value = Result.Loading() // Set loading state before starting

            try {
                val selectedJuzRange = (startJuz..endJuz).toList()
                val generatedQuestions = mutableListOf<List<QuranVerse>>()
                val usedSurahs = mutableSetOf<Int>() // Track used Surahs

                // Divide the selected Juz range into parts (top, middle, bottom)
                val segments = divideJuzRange(selectedJuzRange, numOfQuestions)

                repeat(numOfQuestions) { index ->
                    val selectedJuz = segments[index]
                    val surahsInJuz = repository.getSurahsInJuz(selectedJuz)

                    // Filter out Surahs that have already been used
                    val availableSurahs = surahsInJuz.filter { it != 1 && it !in usedSurahs }

                    if (availableSurahs.isNotEmpty()) {
                        val randomSurah = availableSurahs.random()
                        usedSurahs.add(randomSurah) // Mark this Surah as used

                        val verses = repository.getVersesInSurah(randomSurah)
                        val randomStartIndex = verses.indices.random()

                        // Dynamically adjust the number of lines based on available verses
                        val adjustedNumOfLines = adjustNumOfLines(verses, numOfLines, selectedJuz)

                        val question = generateQuestionWithinSurah(
                            verses,
                            randomStartIndex,
                            adjustedNumOfLines
                        )

                        // Only add the question if it meets the required number of lines
                        if (question.size >= adjustedNumOfLines) {
                            // Sort the question by Surah number and Ayah number
                            val sortedQuestion = question.sortedWith(compareBy({ it.suraNo }, { it.ayaNo }))
                            generatedQuestions.add(sortedQuestion)
                        }
                    } else {
                        Log.w("QuranViewModel", "No available Surahs left in Juz $selectedJuz")
                    }
                }

                // Post the sorted questions as a success
                _state.value = Result.Success(generatedQuestions)

            } catch (e: Exception) {
                Log.e("QuranViewModel", "Error generating questions", e)
                // Post an error state
                _state.value = Result.Error(e.message ?: "Unknown error")
            }
        }
    }

    // Remaining methods unchanged
    private fun divideJuzRange(selectedJuzRange: List<Int>, numOfQuestions: Int): List<Int> {
        if (selectedJuzRange.size < numOfQuestions) {
            // Repeat the same Juz for each question
            return List(numOfQuestions) { selectedJuzRange.first() }
        } else {
            // Normal division logic
            val segmentSize = selectedJuzRange.size / numOfQuestions
            val segments = mutableListOf<Int>()

            for (i in 0 until numOfQuestions) {
                if (i == numOfQuestions - 1) {
                    segments.add(selectedJuzRange.subList(i * segmentSize, selectedJuzRange.size).random())
                } else {
                    segments.add(selectedJuzRange.subList(i * segmentSize, (i + 1) * segmentSize).random())
                }
            }
            return segments
        }
    }

    private fun adjustNumOfLines(verses: List<QuranVerse>, numOfLines: Int, selectedJuz: Int): Int {
        val availableLines = verses.size

        // Handle special case for Juz 30
        if (selectedJuz == 30) {
            return if (availableLines < numOfLines) {
                availableLines.coerceAtLeast(1)
            } else {
                numOfLines
            }
        }

        val adjustedLines = (numOfLines.toFloat() * 0.75).toInt()
        return availableLines.coerceAtMost(adjustedLines)
    }

    private fun generateQuestionWithinSurah(
        verses: List<QuranVerse>,
        randomStartIndex: Int,
        numOfLines: Int
    ): List<QuranVerse> {
        return if (randomStartIndex + numOfLines <= verses.size) {
            verses.subList(randomStartIndex, randomStartIndex + numOfLines)
        } else {
            val adjustedStartIndex = (verses.size - numOfLines).coerceAtLeast(0)
            verses.subList(adjustedStartIndex, verses.size)
        }
    }
}