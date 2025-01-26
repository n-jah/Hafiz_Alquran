package com.najah.qurantest.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.najah.qurantest.R
import com.najah.qurantest.model.QuranVerse
class QuestionAdapter(
    private var questions: List<List<QuranVerse>>,
) : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    private val expandedState = MutableList(questions.size) { false }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.question_item, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
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
        holder.btnToggle.setImageResource(
            if (holder.isExpanded) R.drawable.baseline_keyboard_arrow_up_24
            else R.drawable.baseline_keyboard_arrow_down_24
        )

        holder.itemView.setOnClickListener {
            expandedState[position] = !expandedState[position]
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = questions.size

    fun updateQuestions(newQuestions: List<List<QuranVerse>>) {
        val diffResult = DiffUtil.calculateDiff(QuestionDiffCallback(questions, newQuestions))
        questions = newQuestions
        expandedState.clear()
        expandedState.addAll(List(newQuestions.size) { false })
        diffResult.dispatchUpdatesTo(this)
    }

    class QuestionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvQuestionHeadOfQuesion: TextView = view.findViewById(R.id.tvQuestionHeadOfQuesion)
        val tvQuestionEndOfQuesion: TextView = view.findViewById(R.id.tvQuestionEndOfQuesion)
        val tvQuestionHeader: TextView = view.findViewById(R.id.tvQuestionHeader)
        val tvAnswer: TextView = view.findViewById(R.id.tvAnswer)
        val btnToggle: ImageView = view.findViewById(R.id.btnToggle)
        var isExpanded: Boolean = false
        val tvQuestionNumber: TextView = view.findViewById(R.id.tvQuestionNumber)
    }

    class QuestionDiffCallback(
        private val oldList: List<List<QuranVerse>>,
        private val newList: List<List<QuranVerse>>
    ) : DiffUtil.Callback() {
        override fun getOldListSize() = oldList.size
        override fun getNewListSize() = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition].hashCode() == newList[newItemPosition].hashCode()

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) =
            oldList[oldItemPosition] == newList[newItemPosition]
    }
}
