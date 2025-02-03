package com.najah.qurantest.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.slider.Slider
import com.najah.qurantest.R
import com.najah.qurantest.model.QuranVerse
import com.najah.qurantest.viewModel.QuranViewModel

class QuestionAdapter(
    private var questions: List<List<QuranVerse>>,
    private val viewModel: QuranViewModel
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {
    private val expandedState = MutableList(questions.size) { false }
    private var lastAnimatedPosition = -1
    private var selectedGrades = mutableMapOf<Int, Int?>()
    private var number_of_words_header = 7
    private var number_of_words_fotter = 7
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.question_item, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: QuestionViewHolder,
        @SuppressLint("RecyclerView") position: Int
    ) {
        val question = questions[position]
        val questionNumber = position + 1
        var firstWords =
            question.firstOrNull()?.ayaText?.split(" ")?.take(number_of_words_header)?.joinToString(" ") ?: ""
        var lastWords =
            question.lastOrNull()?.ayaText?.split(" ")?.takeLast(number_of_words_fotter)?.joinToString(" ") ?: ""
        val surahName = question.firstOrNull()?.suraNameAr ?: ""
        val ayahRange =
            if (question.isNotEmpty()) "${question.first().ayaNo} - ${question.last().ayaNo}" else "N/A"

        holder.tvQuestionNumber.text = questionNumber.toString()
        holder.tvQuestionHeader.text = " $ayahRange سورة $surahName"
        holder.tvQuestionHeadOfQuesion.text = "$firstWords   ..."
        holder.tvQuestionEndOfQuesion.text = "...   $lastWords"
        holder.tvAnswer.text = question.joinToString(separator = "") { it.ayaText ?: "" }

        holder.isExpanded = expandedState[position]
        holder.tvAnswer.visibility = if (holder.isExpanded) View.VISIBLE else View.GONE
        holder.btnToggle.rotation = if (holder.isExpanded) 180f else 0f

        if (position > lastAnimatedPosition) {
            holder.itemView.alpha = 0f
            holder.itemView.animate()
                .alpha(1f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
            lastAnimatedPosition = position
        }

        holder.gradeSlider.clearOnChangeListeners() // Prevent duplicate listeners

        holder.itemView.setOnClickListener {
            expandedState[position] = !expandedState[position]
            TransitionManager.beginDelayedTransition(holder.itemView as ViewGroup)
            holder.tvAnswer.visibility = if (expandedState[position]) View.VISIBLE else View.GONE
            holder.btnToggle.animate()
                .rotation(if (expandedState[position]) 180f else 0f)
                .setDuration(300)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
            notifyItemChanged(position)
        }

      // Clear listeners
        holder.gradeSlider.clearOnChangeListeners()
        holder.confirmCheckBox.setOnCheckedChangeListener(null)

        val savedGrade = selectedGrades[position]
// Ensure the slider resets properly
        if (savedGrade == null) {
            holder.gradeSlider.value = 100f
            updateGradeText(holder.tvGrade, null)
            holder.confirmCheckBox.visibility = View.GONE // Hide checkbox initially
            holder.confirmCheckBox.isChecked = false

        } else {

            holder.gradeSlider.value = savedGrade.toFloat()
            updateGradeText(holder.tvGrade, savedGrade)
            holder.confirmCheckBox.visibility = View.VISIBLE // Show checkbox if there's a value
            holder.confirmCheckBox.isChecked = true
        }
        updateGradeText(holder.tvGrade, savedGrade)

        // Handle slider changes
        holder.gradeSlider.addOnChangeListener { _, value, _ ->

            val grade = value.toInt()
            selectedGrades[position] = grade
            if(selectedGrades[position] ==  null){
                holder.confirmCheckBox.isChecked = false
                holder.confirmCheckBox.visibility = View.GONE

            }else{
                holder.confirmCheckBox.isChecked = true
                holder.confirmCheckBox.visibility = View.VISIBLE
            }
            //update the degree in the question
            updateGradeText(holder.tvGrade, grade)
            viewModel.updateGrades(selectedGrades)
        }

        // Handle checkbox unchecking (Reset value to null)
        holder.confirmCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                holder.gradeSlider.value = holder.gradeSlider.valueTo
                selectedGrades[position] = null
                holder.confirmCheckBox.visibility = View.GONE
                viewModel.updateGrades(selectedGrades)
                updateGradeText(holder.tvGrade, null)
            }
        }

        //handel the increasing number of word in questions
        holder.counterHeaderBtn.setOnClickListener {
            number_of_words_header ++
             firstWords =
                question.firstOrNull()?.ayaText?.split(" ")?.take(number_of_words_header)?.joinToString(" ") ?: ""
            holder.tvQuestionHeadOfQuesion.text = "$firstWords ..."

        }

        //handel the decreasing number of word in questions
        holder.counterFooterBtn.setOnClickListener {
            number_of_words_fotter ++
             lastWords =
                question.lastOrNull()?.ayaText?.split(" ")?.takeLast(number_of_words_fotter)?.joinToString(" ") ?: ""
            holder.tvQuestionEndOfQuesion.text = "... $lastWords"

        }



    }


    override fun getItemCount(): Int = questions.size
fun updateQuestions(newQuestions: List<List<QuranVerse>>) {
    selectedGrades.clear() // Completely clear all previous grades

    // Reset all grades to null (unanswered)
    selectedGrades = mutableMapOf<Int, Int?>().apply {
        newQuestions.indices.forEach { this[it] = null }
    }

    viewModel.updateGrades(selectedGrades) // Notify ViewModel of reset

    // Update questions list and notify adapter
    val diffResult = DiffUtil.calculateDiff(QuestionDiffCallback(questions, newQuestions))
    questions = newQuestions
    expandedState.clear()
    expandedState.addAll(List(newQuestions.size) { false })
    lastAnimatedPosition = -1
    diffResult.dispatchUpdatesTo(this)
}


    private fun updateGradeText(tvGrade: TextView, grade: Int?) {
        if (grade == null) {
            tvGrade.text = "لم تتم الاجابة"
            tvGrade.setTextColor(Color.BLACK)
        } else {
            tvGrade.text = "$grade%"
            tvGrade.setTextColor(
                when {
                    grade < 25 -> Color.parseColor("#B71C1C") // Red for 0-24
                    grade < 50 -> Color.parseColor("#FF9800") // Orange for 25-49
                    grade < 75 -> Color.parseColor("#FF9900") // Yellow for 50-74
                    grade < 100 -> Color.parseColor("#1E88E5") // Blue for 75-99
                    else -> Color.parseColor("#388E3C") // Green for 100
                }
            )
        }
    }
    fun clearSelectedGrades() {
        selectedGrades.clear() // Fully clear previous selections
        notifyDataSetChanged() // Refresh UI
    }


    class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestionHeadOfQuesion: TextView = view.findViewById(R.id.tvQuestionHeadOfQuesion)
        val tvQuestionEndOfQuesion: TextView = view.findViewById(R.id.tvQuestionEndOfQuesion)
        val tvQuestionHeader: TextView = view.findViewById(R.id.tvQuestionHeader)
        val tvAnswer: TextView = view.findViewById(R.id.tvAnswer)
        val btnToggle: ImageView = view.findViewById(R.id.btnToggle)
        val gradeSlider: Slider = view.findViewById(R.id.grade_slider)
        val tvGrade: TextView = view.findViewById(R.id.tvGrade)
        val confirmCheckBox: MaterialCheckBox = view.findViewById(R.id.confirmCheckBox) // New Checkbox
        var isExpanded: Boolean = false
        val tvQuestionNumber: TextView = view.findViewById(R.id.tvQuestionNumber)
        val counterHeaderBtn : ImageButton = view.findViewById(R.id.btn_increace_word_question)
        val counterFooterBtn : ImageButton = view.findViewById(R.id.btn_increace_word_question_fotter)
    }



}