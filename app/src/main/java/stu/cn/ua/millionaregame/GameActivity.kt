package stu.cn.ua.millionaregame

import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.content.DialogInterface

class GameActivity : AppCompatActivity() {

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
    private var fiftyFiftyUsedOnce: Boolean = false // Додаємо змінну для глобального стану використання 50/50
    private var questionIndex: Int = 0

    private val questions = listOf(
        Pair("Яке найбільше місто у світі?", listOf("Нью-Йорк", "Токіо", "Лондон", "Париж")),
        Pair("Яка столиця України?", listOf("Київ", "Львів", "Одеса", "Харків")),
        Pair("Скільки кольорів у веселці?", listOf("5", "6", "7", "8")),
        Pair("Хто написав 'Гамлета'?", listOf("Шекспір", "Достоєвський", "Гоголь", "Твен")),
        Pair("Яка найбільша планета в Сонячній системі?", listOf("Марс", "Земля", "Юпітер", "Сатурн")),
        Pair("Яка національна валюта Японії?", listOf("Долар", "Євро", "Єна", "Фунт")),
        Pair("Хто є автором 'Війни і миру'?", listOf("Лев Толстой", "Антон Чехов", "Федір Достоєвський", "Олександр Пушкін")),
        Pair("Скільки континентів на Землі?", listOf("5", "6", "7", "8"))
    )

    private val correctAnswers = listOf(
        2, // Токіо
        1, // Київ
        3, // 7
        1, // Шекспір
        3, // Юпітер
        3, // Єна
        1, // Лев Толстой
        3  // 7
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Ініціалізація елементів
        questionText = findViewById(R.id.question_text)
        answer1 = findViewById(R.id.answer_1)
        answer2 = findViewById(R.id.answer_2)
        answer3 = findViewById(R.id.answer_3)
        answer4 = findViewById(R.id.answer_4)
        takeMoneyButton = findViewById(R.id.take_money_button)
        earnedMoneyText = findViewById(R.id.earned_money_text)
        fiftyFiftyCheckBox = findViewById(R.id.fifty_fifty_checkbox)

        // Встановлюємо питання
        setQuestion()

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

    private fun showWinDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Вітаємо!")
        builder.setMessage("Ви виграли гру!")
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss() // Закрити діалог
            finish()  // Повернутися до головного меню
        })
        builder.setCancelable(false) // Заборонити закриття діалогу через натискання поза ним
        builder.show()
    }

    private fun showLoseDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Гру закінчено")
        builder.setMessage("Ви програли. Спробуйте ще раз!")
        builder.setPositiveButton("OK", DialogInterface.OnClickListener { dialog, _ ->
            dialog.dismiss() // Закрити діалог
            finish()  // Повернутися до головного меню
        })
        builder.setCancelable(false)
        builder.show()
    }

    private fun checkAnswer(selectedAnswer: Int) {
        if (selectedAnswer == correctAnswerIndex) {
            currentMoney += 100  // Додаємо гроші
            earnedMoneyText.text = "Виграно: ${currentMoney}$"
            Toast.makeText(this, "Правильна відповідь!", Toast.LENGTH_SHORT).show()
            questionIndex++  // Переходити до наступного запитання
            if (questionIndex == questions.size) {
                showWinDialog() // Показуємо вікно виграшу, коли всі питання відповіли
            } else {
                setQuestion()  // Оновлюємо запитання
            }
        } else {
            showLoseDialog() // Показуємо вікно програшу
        }
    }

    private fun finishGame() {
        Toast.makeText(this, "Ви виграли ${currentMoney}$", Toast.LENGTH_LONG).show()
        finish()
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
}