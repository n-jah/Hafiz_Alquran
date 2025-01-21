package com.najah.qurantest.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.najah.qurantest.model.QuranVerse
import com.najah.qurantest.repository.QuranRepository
import kotlinx.coroutines.launch
class QuranViewModel(private val repository: QuranRepository) : ViewModel() {

    private val _questions = MutableLiveData<List<List<QuranVerse>>>()
    val questions: LiveData<List<List<QuranVerse>>> get() = _questions

    fun generateQuestions(
        startJuz: Int,
        endJuz: Int,
        numOfQuestions: Int,
        numOfLines: Int
    ) {
        viewModelScope.launch {
            val selectedJuzRange = (startJuz..endJuz).toList()
            val generatedQuestions = mutableListOf<List<QuranVerse>>()
            val usedSurahs = mutableSetOf<Int>() // Track used Surahs

            // Divide the selected Juz range into parts (top, middle, bottom)
            val segments = divideJuzRange(selectedJuzRange, numOfQuestions)

            repeat(numOfQuestions) { index ->
                val selectedJuz = segments[index]
                val surahsInJuz = repository.getSurahsInJuz(selectedJuz)

                // Filter out Surahs that have already been used
                val availableSurahs = surahsInJuz.filter { it !in usedSurahs }

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
                    // If no available Surahs are left, log a warning or handle it as needed
                    Log.w("QuranViewModel", "No available Surahs left in Juz $selectedJuz")
                }
            }

            // Post the sorted questions
            _questions.postValue(generatedQuestions)
        }
    }

    // Function to divide the Juz range into segments for question generation
    private fun divideJuzRange(selectedJuzRange: List<Int>, numOfQuestions: Int): List<Int> {
        return if (selectedJuzRange.size < numOfQuestions) {
            // Repeat the same Juz for each question
            List(numOfQuestions) { selectedJuzRange.first() }
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
            segments
        }
    }

    // Function to dynamically adjust the number of lines to fit within the available Surah content
    private fun adjustNumOfLines(verses: List<QuranVerse>, numOfLines: Int, selectedJuz: Int): Int {
        val availableLines = verses.size

        // Special case for Juz 30, adjust the number of lines more conservatively
        if (selectedJuz == 30) {
            // For Juz 30, if you don't have enough lines, reduce the number of lines proportionally
            if (availableLines < numOfLines) {
                // Ensure at least 1 line is selected, even if the requested lines are more than available
                return availableLines.coerceAtLeast(1)
            }
        }

        // In other Juzs, adjust by 75% of requested lines
        val adjustedLines = (numOfLines.toFloat() * 0.75).toInt()

        // If the available lines are fewer than requested, return the available lines
        return availableLines.coerceAtMost(adjustedLines)
    }

    // Function to generate a question within a single Surah
    private fun generateQuestionWithinSurah(
        verses: List<QuranVerse>,
        randomStartIndex: Int,
        numOfLines: Int
    ): List<QuranVerse> {
        return if (randomStartIndex + numOfLines <= verses.size) {
            // Case 1: Enough lines after the random start index
            verses.subList(randomStartIndex, randomStartIndex + numOfLines)
        } else {
            // Case 2: Not enough lines, adjust to ensure the question stays within the Surah
            val adjustedStartIndex = (verses.size - numOfLines).coerceAtLeast(0)
            verses.subList(adjustedStartIndex, verses.size)
        }
    }
}
