package com.lyc.englishtools.ui.study.vocabulary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lyc.englishtools.R
import com.lyc.englishtools.data.entities.WordEntity

class VocabularyAdapter(private val onRemoveClick: (Int) -> Unit) :
    ListAdapter<WordEntity, VocabularyAdapter.ViewHolder>(WordDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vocabulary, parent, false)
        return ViewHolder(view, onRemoveClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View, private val onRemoveClick: (Int) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val tvEnglish: TextView = itemView.findViewById(R.id.tvEnglish)
        private val tvChinese: TextView = itemView.findViewById(R.id.tvChinese)
        private val btnRemove: View = itemView.findViewById(R.id.btnRemove)

        fun bind(word: WordEntity) {
            tvEnglish.text = word.english
            tvChinese.text = word.chinese
            btnRemove.setOnClickListener {
                // 点击时调用回调函数
                onRemoveClick(word.id)
            }
        }
    }
}

private class WordDiffCallback : DiffUtil.ItemCallback<WordEntity>() {
    override fun areItemsTheSame(oldItem: WordEntity, newItem: WordEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: WordEntity, newItem: WordEntity): Boolean {
        return oldItem == newItem
    }
}