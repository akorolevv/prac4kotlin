package com.example.prac4kotlin


import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.prac4kotlin.databinding.FragmentCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class CameraFragment : Fragment() {

    // Объявление переменных для binding (привязки) и других компонентов камеры
    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    // Объявление переменной для захвата изображения
    private var imageCapture: ImageCapture? = null

    // Объявление переменной для выполнения операций камеры в отдельном потоке
    private lateinit var cameraExecutor: ExecutorService


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Инициализация binding
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        // Возврат корневого view из binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Проверка разрешений и запуск камеры
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            // Запрос разрешений, если они не предоставлены
            requestPermissions(REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        // Установка слушателя кликов на кнопку захвата фото
        binding.cameraCaptureButton.setOnClickListener { takePhoto() }

        // Инициализация исполнителя для операций камеры
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    // Метод для захвата фото
    private fun takePhoto() {
        // Получение текущего экземпляра ImageCapture
        val imageCapture = imageCapture ?: return

        // Создание имени файла для сохранения фото
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())

        // Создание ContentValues для сохранения метаданных фото
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraX-Image")
            }
        }

        // Создание опций вывода для сохранения фото (объединяет метаданные и место, где должно быть сохранено фото)
        // Где сохранить (внешнее хранилище, определяемое EXTERNAL_CONTENT_URI)
        // Как взаимодействовать с хранилищем (через ContentResolver)
        // Какую дополнительную информацию о файле сохранить (метаданные в ContentValues)
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(requireContext().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues)
            .build()

        // Захват фото с использованием настроенных опций
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                // Метод, вызываемый при ошибке сохранения фото
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                // Метод, вызываемый при успешном сохранении фото
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"

                    // Отображение сообщения об успешном сохранении
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)


                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // создание шаблона даты
                    val currentDate = dateFormat.format(Date()) // получаем дату и время с помощью Date и форматируем ее

                    // Создание директории для хранения фото, если она не существует
                    val photosDir = File(requireContext().filesDir, "photos") // requireContext().filesDir указывает на корневую директорию приватного хранилища приложения
                    if (!photosDir.exists()) {
                        photosDir.mkdirs()
                    }

                    // Запись даты в файл
                    val dateFile = File(photosDir, "date")
                    dateFile.appendText("$currentDate\n")
                }
            }
        )
    }

    // Метод для запуска камеры
    private fun startCamera() {
        // Получение экземпляра ProcessCameraProvider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        // Добавление слушателя для настройки камеры после получения ProcessCameraProvider
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Создание и настройка Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Создание ImageCapture
            imageCapture = ImageCapture.Builder().build()

            // Выбор задней камеры по умолчанию
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    // Метод для проверки, предоставлены ли все необходимые разрешения
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Метод, вызываемый после запроса разрешений
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray
    ) {
        // Проверка кода запроса разрешений
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Если все разрешения предоставлены, запускаем камеру
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                // Если разрешения не предоставлены, показываем сообщение
                Toast.makeText(
                    requireContext(),
                    "Permissions not granted by the user.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // Метод, вызываемый при уничтожении view фрагмента
    override fun onDestroyView() {
        super.onDestroyView()
        // Обнуление binding для предотвращения утечек памяти
        _binding = null
    }

    // Метод, вызываемый при уничтожении фрагмента
    override fun onDestroy() {
        super.onDestroy()
        // Остановка исполнителя операций камеры
        cameraExecutor.shutdown()
    }

    // Объявление констант и переменных-компаньонов
    companion object {
        private const val TAG = "CameraXApp"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            mutableListOf (
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}