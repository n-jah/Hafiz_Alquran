package com.najah.qurantest.adapter

import android.annotation.SuppressLint
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import com.google.android.material.slider.Slider
import com.najah.qurantest.R
import com.najah.qurantest.model.QuranVerse

class QuestionAdapter(
    private var questions: List<List<QuranVerse>>,
    private val onGradesUpdated: (Int, Float) -> Unit // Callback to notify activit

) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {
    private val expandedState = MutableList(questions.size) { false }
    private var lastAnimatedPosition = -1
    private var selectedGrades = mutableMapOf<Int, Int?>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.question_item, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, @SuppressLint("RecyclerView") position: Int) {
        val question = questions[position]
        val questionNumber = position + 1
        val firstWords = question.firstOrNull()?.ayaText?.split(" ")?.take(6)?.joinToString(" ") ?: ""
        val lastWords = question.lastOrNull()?.ayaText?.split(" ")?.takeLast(6)?.joinToString(" ") ?: ""
        val surahName = question.firstOrNull()?.suraNameAr ?: ""
        val ayahRange = if (question.isNotEmpty()) "${question.first().ayaNo} - ${question.last().ayaNo}" else "N/A"

        holder.tvQuestionNumber.text = questionNumber.toString()
        holder.tvQuestionHeader.text = " $ayahRange سورة $surahName"
        holder.tvQuestionHeadOfQuesion.text = "$firstWords ..."
        holder.tvQuestionEndOfQuesion.text = "... $lastWords"
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

        // Set the slider value
        val savedGrade = selectedGrades[position]
        holder.gradeSlider.value = savedGrade?.toFloat() ?: 100f // Default to 100 if unanswered
        updateGradeText(holder.tvGrade, savedGrade)
        // Handle slider changes
        holder.gradeSlider.addOnChangeListener { _, value, _ ->
            val grade = value.toInt()
            selectedGrades[position] = grade
            //update the degree in the question
            updateGradeText(holder.tvGrade, grade)
            val (totalAnswered, averageGrade) = calculateGrades()
            onGradesUpdated(totalAnswered, averageGrade)
        }
    }

    override fun getItemCount(): Int = questions.size

    fun updateQuestions(newQuestions: List<List<QuranVerse>>) {

        Log.d("QuestionAdapter", "befselectedGrades: $selectedGrades")
        selectedGrades.clear()

        newQuestions.indices.forEach { selectedGrades[it] = null } // Reset all grades to null (unanswered)
        Log.d("QuestionAdapter", "afterselectedGrades: $selectedGrades")

        //Update the questions list and notify the adapter
        val diffResult = DiffUtil.calculateDiff(QuestionDiffCallback(questions, newQuestions))
        questions = newQuestions
        expandedState.clear()
        expandedState.addAll(List(newQuestions.size) { false })
        lastAnimatedPosition = -1
        diffResult.dispatchUpdatesTo(this)
        // Notify the activity that the grades have been reset
        val (totalAnswered, averageGrade) = calculateGrades()
        onGradesUpdated(totalAnswered, averageGrade)
    }

    private fun calculateGrades(): Pair<Int, Float> {
        var totalAnswered = 0
        var totalGrade = 0f

        selectedGrades.forEach { (_, grade) ->
            if (grade != null) {
                totalAnswered++
                totalGrade += grade.toFloat()
            }
        }


        val averageGrade = if (totalAnswered > 0) totalGrade / totalAnswered else 0f
        return Pair(totalAnswered, averageGrade)
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




    class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestionHeadOfQuesion: TextView = view.findViewById(R.id.tvQuestionHeadOfQuesion)
        val tvQuestionEndOfQuesion: TextView = view.findViewById(R.id.tvQuestionEndOfQuesion)
        val tvQuestionHeader: TextView = view.findViewById(R.id.tvQuestionHeader)
        val tvAnswer: TextView = view.findViewById(R.id.tvAnswer)
        val btnToggle: ImageView = view.findViewById(R.id.btnToggle)
        val gradeSlider: Slider = view.findViewById(R.id.grade_slider)
        val tvGrade: TextView = view.findViewById(R.id.tvGrade)
        var isExpanded: Boolean = false
        val tvQuestionNumber: TextView = view.findViewById(R.id.tvQuestionNumber)
    }


}