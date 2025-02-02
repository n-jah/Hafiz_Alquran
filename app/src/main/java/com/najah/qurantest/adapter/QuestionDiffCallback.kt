package com.najah.qurantest.adapter

import androidx.recyclerview.widget.DiffUtil
import com.najah.qurantest.model.QuranVerse

class QuestionDiffCallback(
    private val oldList: List<List<QuranVerse>>,
    private val newList: List<List<QuranVerse>>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size
    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].first().id == newList[newItemPosition].first().id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
