package com.example.prac4kotlin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.prac4kotlin.databinding.FragmentListBinding
import java.io.File

class ListFragment : Fragment() {

    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Чтение дат из файла
        val dates = readDatesFromFile()

        // Создание адаптера
        val adapter = DateAdapter(dates)

        // Настройка RecyclerView
        binding.dateRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            this.adapter = adapter
        }
    }

    // Метод для чтения дат из файла

    private fun readDatesFromFile(): List<String> {

        // Получение директории для хранения фото

        val photosDir = File(requireContext().filesDir, "photos")

        // Получение файла с датами
        val dateFile = File(photosDir, "date")

        return if (dateFile.exists()) {
            dateFile.readLines().reversed() // Чтение в обратном порядке для отображения новых дат сверху
        } else {
            emptyList()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}