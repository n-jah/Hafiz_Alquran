package com.najah.qurantest.viewModel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.najah.qurantest.model.QuranVerse
import com.najah.qurantest.repository.QuranRepository
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class QuranViewModel(private val repository: QuranRepository) : ViewModel() {

    private val _questions = MutableLiveData<List<List<QuranVerse>>>()
    val questions: LiveData<List<List<QuranVerse>>> get() = _questions






    fun generateQuiz(
        juzzStart: Int,
        juzzEnd: Int,
        numOfQuestions: Int,
        numOfLines: Int
    ) {
        viewModelScope.launch {
            val generatedJuzzs = mutableListOf<Int>()
            val questions = mutableListOf<List<QuranVerse>>()

            repeat(numOfQuestions) {
                var selectedJuzz: Int
                do {
                    selectedJuzz = (juzzStart..juzzEnd).random()
                } while (generatedJuzzs.contains(selectedJuzz))

                generatedJuzzs.add(selectedJuzz)
                if (generatedJuzzs.size == juzzEnd - juzzStart + 1) {
                    generatedJuzzs.clear()
                }

                val versesInJuzz = repository.getVersesByJuzz(selectedJuzz)

                var questionLines = mutableListOf<QuranVerse>()
                var startingIndex = -1

                do {
                    val surahVerses = versesInJuzz.groupBy { it.suraNo }.values.random()
                    Log.d("QuranViewModel", "Surah Verses: $surahVerses")
                    startingIndex = surahVerses.indices.random()
                    questionLines = surahVerses.subList(
                        startingIndex,
                        minOf(startingIndex + numOfLines, surahVerses.size)
                    ).toMutableList()
                } while (questionLines.size < numOfLines)

                questions.add(questionLines)
            }

            _questions.postValue(questions)
        }
    }



}
