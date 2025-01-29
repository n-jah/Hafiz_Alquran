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
            _state.value = Result.Loading() // Set loading state before starting

            try {
                val selectedJuzRange = (startJuz..endJuz).toList()
                val generatedQuestions = mutableListOf<List<QuranVerse>>()
                val usedSurahs = mutableSetOf<Int>() // Track used Surahs
                val usedRanges = mutableSetOf<Pair<Int, Int>>() // Track used verse ranges

                // Divide the selected Juz range into parts
                val segments = divideJuzRange(selectedJuzRange, numOfQuestions)
                Log.d("QuranViewModel", "Generated segments: $segments")

                repeat(numOfQuestions) { index ->
                    val selectedJuz = segments[index]
                    val surahsInJuz = repository.getSurahsInJuz(selectedJuz)

                    // Prevent Fatiha from appearing in questions
                    if (selectedJuz == 1) {
                        usedSurahs.add(1)
                    }

                    // Filter out Surahs that have already been used
                    val availableSurahs = surahsInJuz.filter { it !in usedSurahs }
                    Log.d("QuranViewModel", "Available Surahs in Juz $selectedJuz: $availableSurahs")

                    if (availableSurahs.isNotEmpty()) {
                        val randomSurah = availableSurahs.random()
                        if (randomSurah !in listOf(2, 3, 4)) {
                            usedSurahs.add(randomSurah) // Mark this Surah as used
                        }

                        val verses = repository.getVersesInSurah(randomSurah)
                        val randomStartIndex = verses.indices.random()

                        // Dynamically adjust the number of lines based on available verses
                        val adjustedNumOfLines = adjustNumOfLines(verses, numOfLines, selectedJuz)

                        val question = generateQuestionWithinSurah(
                            verses,
                            randomStartIndex,
                            adjustedNumOfLines,
                            usedRanges // Pass usedRanges to avoid duplicate ranges
                        )

                        // Only add the question if it meets the required number of lines
                        if (question.size >= adjustedNumOfLines) {
                            val sortedQuestion = question.sortedBy { it.id } // Sort by ID
                            generatedQuestions.add(sortedQuestion)
                            Log.d("QuranViewModel", "Generated Question: $sortedQuestion")
                        }
                    } else {
                        Log.w("QuranViewModel", "No available Surahs left in Juz $selectedJuz")
                    }
                }

                // If the number of questions exceeds available Surahs, repeat some Surahs
                if (generatedQuestions.size < numOfQuestions) {
                    Log.w("QuranViewModel", "Not enough unique Surahs. Repeating some Surahs.")
                    val remainingQuestions = numOfQuestions - generatedQuestions.size
                    val repeatedQuestions = mutableListOf<List<QuranVerse>>()

                    repeat(remainingQuestions) {
                        val randomQuestion = generatedQuestions.random()
                        repeatedQuestions.add(randomQuestion)
                    }
                    generatedQuestions.addAll(repeatedQuestions)
                }

                Log.d("QuranViewModels", "Generated Questions: ${generatedQuestions.map {
                        question -> question.map { it.id }
                }}")

                // Sort each question's verses by ID, then sort the list of questions by the first verse's ID
                val sortedGeneratedQuestions = generatedQuestions.map { question ->
                    question.sortedBy { it.id }
                }.sortedBy { it.firstOrNull()?.id ?: Int.MAX_VALUE }

                Log.d("QuranViewModels", "sorted Questions: ${sortedGeneratedQuestions.map {
                        question -> question.map { it.id }
                }}")

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
        randomStartIndex: Int,
        numOfLines: Int,
        usedRanges: MutableSet<Pair<Int, Int>> // Track used verse ranges
    ): List<QuranVerse> {
        val availableRanges = mutableListOf<Pair<Int, Int>>()

        // Find all possible non-overlapping ranges
        for (i in 0 until verses.size - numOfLines) {
            val range = Pair(i, i + numOfLines - 1)
            if (!usedRanges.contains(range)) {
                availableRanges.add(range)
            }
        }

        if (availableRanges.isEmpty()) {
            Log.w("QuranViewModel", "No unique ranges left in this Surah.")
            return emptyList() // No more unique questions can be generated
        }

        // Select a non-repeating range
        val (start, end) = availableRanges.random()
        usedRanges.add(Pair(start, end))

        return verses.subList(start, end + 1)
    }
}