package stu.cn.ua.millionaregame

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class GameFragment : Fragment(), QuestionService.QuestionObserver {

    private lateinit var questionText: TextView
    private lateinit var answer1: Button
    private lateinit var answer2: Button
    private lateinit var answer3: Button
    private lateinit var answer4: Button
    private lateinit var takeMoneyButton: Button
    private lateinit var earnedMoneyText: TextView
    private lateinit var fiftyFiftyCheckBox: CheckBox

    private var currentMoney: Int = 0
    private var correctAnswerIndex: Int = 0
    private var fiftyFiftyUsedOnce: Boolean = false
    private var questionIndex: Int = 0

    private lateinit var questions: List<Pair<String, List<String>>>
    private lateinit var correctAnswers: List<Int>

    // Зв'язок зі службою
    private var questionService: QuestionService? = null
    private var isBound: Boolean = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as QuestionService.LocalBinder
            questionService = binder.getService()
            questionService?.registerObserver(this@GameFragment)
            questionService?.loadQuestions()
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            questionService = null
            isBound = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_game, container, false)

        // Ініціалізація елементів інтерфейсу
        questionText = view.findViewById(R.id.question_text)
        answer1 = view.findViewById(R.id.answer_1)
        answer2 = view.findViewById(R.id.answer_2)
        answer3 = view.findViewById(R.id.answer_3)
        answer4 = view.findViewById(R.id.answer_4)
        takeMoneyButton = view.findViewById(R.id.take_money_button)
        earnedMoneyText = view.findViewById(R.id.earned_money_text)
        fiftyFiftyCheckBox = view.findViewById(R.id.fifty_fifty_checkbox)

        // Біндинг до служби
        val intent = Intent(requireContext(), QuestionService::class.java)
        requireActivity().bindService(intent, connection, Context.BIND_AUTO_CREATE)

        // Обробка відповідей
        answer1.setOnClickListener { checkAnswer(1) }
        answer2.setOnClickListener { checkAnswer(2) }
        answer3.setOnClickListener { checkAnswer(3) }
        answer4.setOnClickListener { checkAnswer(4) }

        // Кнопка "Забрати гроші"
        takeMoneyButton.setOnClickListener {
            finishGame()
        }

        // Чекбокс 50/50
        fiftyFiftyCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked && !fiftyFiftyUsedOnce) {
                useFiftyFifty()
                fiftyFiftyUsedOnce = true
                fiftyFiftyCheckBox.isEnabled = false
            } else {
                fiftyFiftyCheckBox.isChecked = false
            }
        }

        return view
    }

    // Отримуємо завантажені питання та оновлюємо інтерфейс
    override fun onQuestionsLoaded(questions: List<Pair<String, List<String>>>, answers: List<Int>) {
        this.questions = questions
        this.correctAnswers = answers
        setQuestion()
    }

    private fun setQuestion() {
        if (questionIndex < questions.size) {
            resetAnswers()
            val (question, answers) = questions[questionIndex]
            questionText.text = question
            answer1.text = answers[0]
            answer2.text = answers[1]
            answer3.text = answers[2]
            answer4.text = answers[3]
            correctAnswerIndex = correctAnswers[questionIndex]
        } else {
            finishGame()
        }
    }

    private fun checkAnswer(selectedAnswer: Int) {
        if (selectedAnswer == correctAnswerIndex) {
            currentMoney += 100
            earnedMoneyText.text = "Виграно: ${currentMoney}$"
            Toast.makeText(requireContext(), "Правильна відповідь!", Toast.LENGTH_SHORT).show()
            questionIndex++
            if (questionIndex == questions.size) {
                showWinDialog()
            } else {
                setQuestion()
            }
        } else {
            showLoseDialog()
        }
    }

    private fun showWinDialog() {
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Вітаємо, ви виграли!")
            .setCancelable(false)
            .setPositiveButton("ОК") { dialog, _ ->
                dialog.dismiss()
                finishGame()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Виграш")
        alert.show()
    }

    private fun showLoseDialog() {
        val dialogBuilder = androidx.appcompat.app.AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("На жаль, ви програли.")
            .setCancelable(false)
            .setPositiveButton("ОК") { dialog, _ ->
                dialog.dismiss()
                requireActivity().supportFragmentManager.popBackStack()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Програш")
        alert.show()
    }

    private fun finishGame() {
        Toast.makeText(requireContext(), "Ви виграли ${currentMoney}$", Toast.LENGTH_LONG).show()
        requireActivity().supportFragmentManager.popBackStack() // Повернення до попереднього фрагмента
    }

    private fun useFiftyFifty() {
        val incorrectAnswers = mutableListOf<Button>()
        if (correctAnswerIndex != 1) incorrectAnswers.add(answer1)
        if (correctAnswerIndex != 2) incorrectAnswers.add(answer2)
        if (correctAnswerIndex != 3) incorrectAnswers.add(answer3)
        if (correctAnswerIndex != 4) incorrectAnswers.add(answer4)

        incorrectAnswers.shuffle()
        for (i in 0..1) {
            if (incorrectAnswers.size > i) {
                incorrectAnswers[i].text = ""
                incorrectAnswers[i].isEnabled = false
            }
        }
    }

    private fun resetAnswers() {
        answer1.isEnabled = true
        answer2.isEnabled = true
        answer3.isEnabled = true
        answer4.isEnabled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            questionService?.unregisterObserver(this)
            requireActivity().unbindService(connection)
            isBound = false
        }
    }
}

