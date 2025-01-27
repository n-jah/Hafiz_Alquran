package com.najah.qurantest.view

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.slider.RangeSlider
import com.najah.qurantest.R
import com.najah.qurantest.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var dropdown: AutoCompleteTextView
    private lateinit var rangeSlider: RangeSlider
    private lateinit var selectedRangeTextView: TextView
    private val ranges = listOf(
        Pair(1, 5) to "الأجزاء الخمسة الأولى",
        Pair(26, 30) to "الأجزاء الخمسة الأخيرة",
        Pair(1, 10) to "الأجزاء العشرة الأولى",
        Pair(21, 30) to "الأجزاء العشرة الأخيرة",
        Pair(6, 10) to "الأجزاء من السادس إلى العاشر",
        Pair(11, 20) to "الأجزاء من الحادي عشر إلى العشرين",
        Pair(21, 30) to "الأجزاء من الحادي والعشرين إلى الثلاثين",
        Pair(1, 15) to "الأجزاء الخمسة عشر الأولى",
        Pair(16, 30) to "الأجزاء الخمسة عشر الأخيرة",
        Pair(1, 30) to "القرآن الكريم كاملاً"
    )
    private lateinit var dropdownAdapter: ArrayAdapter<String>
    private val juzRangeMin = 1
    private val juzRangeMax = 30
    private var currentQuestionCount = 10 // Default initial value
    private var currentLineCount = 5      // Default initial value
    private val minQuestionCount = 1      // Minimum limit
    private val maxQuestionCount = 30    // Maximum limit
    private val minLineCount = 5        // Minimum limit
    private val maxLineCount = 25         // Maximum limit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dropdown = binding.juzeDropdown
        rangeSlider = binding.rangeSlider
        selectedRangeTextView = binding.selectedRangeTextView

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setupCounters()
        setupDropdown()
        setupRangeSlider()
        adjustDialogDimensions()
        binding.startQuizButton.setOnClickListener{

            binding.progressBar.visibility = View.VISIBLE
            val startJuz = rangeSlider.values[0].toInt()
            val endJuz = rangeSlider.values[1].toInt()
            val numOfQuestions = currentQuestionCount
            val numOfLines = currentLineCount

            val intent = Intent(this, TestingActivity::class.java)
            intent.putExtra("START_JUZ", startJuz)
            intent.putExtra("END_JUZ", endJuz)
            intent.putExtra("NUM_OF_QUESTIONS", numOfQuestions)
            intent.putExtra("NUM_OF_LINES", numOfLines)
            binding.progressBar.visibility = View.GONE
            startActivity(intent)
            Log.d("MainActivity", "Start Quiz button clicked $startJuz $endJuz $numOfQuestions $numOfLines")
        }
    }

    private fun setupDropdown() {
        val font = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) resources.getFont(R.font.authmany_font) else null

        dropdownAdapter = object : ArrayAdapter<String>(
            this, android.R.layout.simple_dropdown_item_1line, ranges.map { it.second }
        ) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getView(position, convertView, parent)
                font?.let { view.findViewById<TextView>(android.R.id.text1)?.typeface = it }
                return view
            }

            override fun getDropDownView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = super.getDropDownView(position, convertView, parent)
                font?.let { view.findViewById<TextView>(android.R.id.text1)?.typeface = it }
                return view
            }
        }

        dropdown.setAdapter(dropdownAdapter)
        dropdown.setOnClickListener { dropdown.showDropDown() }

        dropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedRange = ranges[position].first
            rangeSlider.values = listOf(selectedRange.first.toFloat(), selectedRange.second.toFloat())
        }
    }

    private fun setupRangeSlider() {
        rangeSlider.setValues(juzRangeMin.toFloat(), juzRangeMax.toFloat())
        rangeSlider.addOnChangeListener { slider, _, _ ->
            val start = slider.values[0].toInt()
            val end = slider.values[1].toInt()

            val rangeText = when {
                start == end -> if (start == juzRangeMax) "الجزء الأخير" else "الجزء $start"
                else -> "من الجزء $start إلى الجزء $end"
            }
            selectedRangeTextView.text = rangeText

            val matchingIndex = ranges.indexOfFirst { it.first.first == start && it.first.second == end }
            dropdown.setText(
                if (matchingIndex != -1) ranges[matchingIndex].second
                else "عدد الأجزاء: ${end - start + 1}", false
            )
        }
    }

    private fun adjustDialogDimensions() {
        val screenWidth = resources.displayMetrics.widthPixels
        val screenHeight = resources.displayMetrics.heightPixels
        val params = findViewById<FrameLayout>(R.id.dialogContentContainer).layoutParams
        params.width = (screenWidth * 0.9).toInt()
        params.height = (screenHeight * 0.9).toInt()
        findViewById<FrameLayout>(R.id.dialogContentContainer).layoutParams = params
    }
    private fun setupCounters() {
        // Question Count Handlers
        binding.increaseQuestionsButton.setOnClickListener {
            if (currentQuestionCount < maxQuestionCount) {
                currentQuestionCount++
                binding.questionCountEditText.setText(currentQuestionCount.toString())
            }
        }

        binding.decreaseQuestionsButton.setOnClickListener {
            if (currentQuestionCount > minQuestionCount) {
                currentQuestionCount--
                binding.questionCountEditText.setText(currentQuestionCount.toString())
            }
        }

        // Line Count Handlers
        binding.increaseLinesButton.setOnClickListener {
            if (currentLineCount < maxLineCount) {
                currentLineCount++
                binding.lineCountEditText.setText(currentLineCount.toString())
            }
        }

        binding.decreaseLinesButton.setOnClickListener {
            if (currentLineCount > minLineCount) {
                currentLineCount--
                binding.lineCountEditText.setText(currentLineCount.toString())
            }
        }

        // Sync EditText Changes
        binding.questionCountEditText.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return

                isEditing = true
                val input = s?.toString()?.toIntOrNull()
                currentQuestionCount = when {
                    input == null -> minQuestionCount
                    input < minQuestionCount -> minQuestionCount
                    input > maxQuestionCount -> maxQuestionCount
                    else -> input
                }
                binding.questionCountEditText.setText(currentQuestionCount.toString()) // Normalize input
                isEditing = false
            }
        })

        binding.lineCountEditText.addTextChangedListener(object : TextWatcher {
            private var isEditing = false

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isEditing) return

                isEditing = true
                val input = s?.toString()?.toIntOrNull()
                currentLineCount = when {
                    input == null -> minLineCount
                    input < minLineCount -> minLineCount
                    input > maxLineCount -> maxLineCount
                    else -> input
                }
                binding.lineCountEditText.setText(currentLineCount.toString()) // Normalize input
                isEditing = false
            }
        })
    }

}
