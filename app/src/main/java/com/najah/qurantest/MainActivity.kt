package com.najah.qurantest

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.najah.qurantest.database.QuranDatabase
import com.najah.qurantest.repository.QuranRepository
import com.najah.qurantest.viewModel.QuranViewModel
import com.najah.qurantest.viewModel.QuranViewModelFactory

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: QuranViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize ViewModel
        val dao = QuranDatabase.getInstance(application).quranDao()
        val repository = QuranRepository(dao)
        val viewModelFactory = QuranViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[QuranViewModel::class.java]

        val stringBuffer = StringBuffer()
        // Observe Questions LiveData
        viewModel.questions.observe(this) { questions ->
            // Handle the questions (log them for now)
            val sortedQuestions = questions.sortedBy { it.first().suraNo }

            sortedQuestions.forEach { question ->
                val firstWords = question.first().ayaText?.split(" ")?.take(6)?.joinToString(" ")
                val lastWords = question.last().ayaText?.split(" ")?.takeLast(6)?.joinToString(" ")
                val surahName = question.first().suraNameAr
                val ayahRange = "${question.first().ayaNo} - ${question.last().ayaNo}"
                stringBuffer.
                append("السؤال : $ayahRange سورة ${surahName.toString()}\n" +
                        " $firstWords ... $lastWords \n")


            }

            Log.d("Questions", stringBuffer.toString())
            findViewById<TextView>(R.id.tv).text = stringBuffer.toString()
        }

        // Trigger Quiz Generation
        viewModel.generateQuestions(
            startJuz = 1,
            endJuz = 30,
            numOfQuestions = 5,
            numOfLines = 130
        )
    }
}
