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

class QuestionService : Service() {

    private val binder = LocalBinder()
    private val observers = mutableListOf<QuestionObserver>()
    private val handler = Handler(Looper.getMainLooper()) // Додаємо Handler для роботи з UI

    inner class LocalBinder : Binder() {
        fun getService(): QuestionService = this@QuestionService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    // Реєструємо спостерігачів
    fun registerObserver(observer: QuestionObserver) {
        observers.add(observer)
    }

    fun unregisterObserver(observer: QuestionObserver) {
        observers.remove(observer)
    }

    // Метод для завантаження питань з текстового файлу
    fun loadQuestions() {
        Thread {
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

                // Сповіщення всіх спостерігачів про нові питання
                notifyObservers(questions, answers)

            } catch (e: Exception) {
                Log.e("QuestionService", "Error reading questions file", e)
            }
        }.start()
    }

    // Сповіщуємо спостерігачів
    private fun notifyObservers(questions: List<Pair<String, List<String>>>, answers: List<Int>) {
        // Викликаємо onQuestionsLoaded на основному потоці
        handler.post {
            observers.forEach { it.onQuestionsLoaded(questions, answers) }
        }
    }

    interface QuestionObserver {
        fun onQuestionsLoaded(questions: List<Pair<String, List<String>>>, answers: List<Int>)
    }
}

