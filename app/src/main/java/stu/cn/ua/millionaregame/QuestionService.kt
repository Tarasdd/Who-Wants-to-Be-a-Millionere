package stu.cn.ua.millionaregame

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class QuestionService : Service() {

    private val binder = LocalBinder()
    private val observers = mutableListOf<QuestionObserver>()
    private val handler = Handler(Looper.getMainLooper()) // Handler для роботи з основним потоком
    private val executorService = Executors.newSingleThreadExecutor() // Пул потоків

    inner class LocalBinder : Binder() {
        fun getService(): QuestionService = this@QuestionService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    // Реєстрація спостерігачів
    fun registerObserver(observer: QuestionObserver) {
        observers.add(observer)
    }

    fun unregisterObserver(observer: QuestionObserver) {
        observers.remove(observer)
    }

    // Метод для завантаження питань з файлу за допомогою ExecutorService
    fun loadQuestions() {
        executorService.execute {
            val questions = mutableListOf<Pair<String, List<String>>>()
            val answers = mutableListOf<Int>()
            try {
                val inputStream = resources.openRawResource(R.raw.questions)
                val reader = BufferedReader(InputStreamReader(inputStream))

                reader.forEachLine { line ->
                    val parts = line.split(";")
                    if (parts.size == 6) {
                        val question = parts[0]
                        val options = listOf(parts[1], parts[2], parts[3], parts[4])
                        val correctAnswer = parts[5].toInt()

                        questions.add(Pair(question, options))
                        answers.add(correctAnswer)
                    }
                }
                reader.close()

                // Сповіщення спостерігачів на основному потоці
                notifyObservers(questions, answers)

            } catch (e: Exception) {
                Log.e("QuestionService", "Error reading questions file", e)
            }
        }
    }

    // Сповіщення спостерігачів про завантаження питань
    private fun notifyObservers(questions: List<Pair<String, List<String>>>, answers: List<Int>) {
        handler.post {
            observers.forEach { it.onQuestionsLoaded(questions, answers) }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService.shutdown() // Закриваємо пул потоків при зупинці сервісу
    }

    interface QuestionObserver {
        fun onQuestionsLoaded(questions: List<Pair<String, List<String>>>, answers: List<Int>)
    }
}

