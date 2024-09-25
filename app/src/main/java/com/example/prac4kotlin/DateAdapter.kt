package com.example.prac4kotlin

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.prac4kotlin.databinding.ItemDateBinding

// адаптер используется для отображения списка дат в RecyclerView
class DateAdapter(private val dates: List<String>) : RecyclerView.Adapter<DateAdapter.DateViewHolder>() {

    // Внутренний класс DateViewHolder, который представляет каждый элемент в списке
    class DateViewHolder(private val binding: ItemDateBinding) : RecyclerView.ViewHolder(binding.root) {

        // Метод для привязки данных к view
        fun bind(date: String)
        {
            // Устанавливаем текст даты в TextView
            binding.dateTextView.text = date
        }
    }

    // Метод для создания нового ViewHolder
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DateViewHolder
    {
        // Создаем новый экземпляр binding для item_date.xml
        val binding = ItemDateBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        // Возвращаем новый DateViewHolder с созданным binding
        return DateViewHolder(binding)
    }

    // Метод для привязки данных к ViewHolder
    override fun onBindViewHolder(holder: DateViewHolder, position: Int) {
        // Вызываем метод bind у ViewHolder, передавая ему дату из списка
        holder.bind(dates[position])
    }

    // Метод для получения количества элементов в списке
    override fun getItemCount() = dates.size
}