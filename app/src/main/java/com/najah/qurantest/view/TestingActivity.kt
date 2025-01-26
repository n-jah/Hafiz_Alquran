package com.najah.qurantest.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.najah.qurantest.R
import com.najah.qurantest.adapter.QuestionAdapter
import com.najah.qurantest.database.QuranDao
import com.najah.qurantest.database.QuranDatabase
import com.najah.qurantest.databinding.ActivityTestingBinding
import com.najah.qurantest.model.Question
import com.najah.qurantest.model.Result

import com.najah.qurantest.repository.QuranRepository
import com.najah.qurantest.viewModel.QuranViewModel
import com.najah.qurantest.viewModel.QuranViewModelFactory

class TestingActivity : AppCompatActivity() {
    private lateinit var _binding: ActivityTestingBinding
    private val binding get() = _binding
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: QuestionAdapter
    private lateinit var viewModel: QuranViewModel
    private lateinit var dao: QuranDao
    private lateinit var repository: QuranRepository
    private lateinit var viewModelFactory: QuranViewModelFactory
    private var startJuz: Int = 1
    private var endJuz: Int = 2
    private var numOfQuestions: Int = 10
    private var numOfLines: Int = 5

    companion object {
        private const val TAG = "TestingActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        _binding = ActivityTestingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getIntentData()
        initViewModel()
        initRV()
        initObservers()
        binding.swipeRefreshLayout.setOnRefreshListener {

            viewModel.generateQuestions(startJuz, endJuz, numOfQuestions, numOfLines)
            binding.swipeRefreshLayout.isRefreshing = false
            binding.loadingIndecator.visibility = View.VISIBLE
            binding.questionsRecyclerView.visibility = View.GONE
            binding.loadingIndecator.visibility = View.GONE
            binding.questionsRecyclerView.visibility = View.GONE
        }

    }

    private fun getIntentData() {

        // Retrieve parameters from the Intent
         startJuz = intent.getIntExtra("START_JUZ", 1)
         endJuz = intent.getIntExtra("END_JUZ", 2)
         numOfQuestions = intent.getIntExtra("NUM_OF_QUESTIONS", 10)
        numOfLines = intent.getIntExtra("NUM_OF_LINES", 5)

    }

    private fun initObservers() {

        viewModel.state.observe(this) { result ->
            when (result) {
                is Result.Loading -> {
                    // Show loading indicator
                    binding.loadingIndecator.visibility = View.VISIBLE
                    binding.questionsRecyclerView.visibility = View.GONE
                }
                is Result.Success -> {
                    binding.loadingIndecator.visibility = View.GONE
                    binding.questionsRecyclerView.visibility = View.VISIBLE
                    // Update the adapter with the questions
                    adapter.updateQuestions(result.data ?: emptyList())
                }
                is Result.Error -> {
                    binding.loadingIndecator.visibility = View.GONE
                    binding.questionsRecyclerView.visibility = View.VISIBLE
                    // Show error message
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun initRV() {

        recyclerView = binding.questionsRecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Initialize Adapter
        adapter = QuestionAdapter(emptyList()) // Initially empty, will update in ViewModel
        recyclerView.adapter = adapter


    }

    private fun initViewModel() {

        // Initialize ViewModel and Repository
        dao = QuranDatabase.getInstance(application).quranDao()
        repository = QuranRepository(dao)
        viewModelFactory = QuranViewModelFactory(repository)
        viewModel = ViewModelProvider(this, viewModelFactory)[QuranViewModel::class.java]
        viewModel.generateQuestions(startJuz, endJuz, numOfQuestions, numOfLines)
    }
}