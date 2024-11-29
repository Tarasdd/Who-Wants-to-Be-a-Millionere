package stu.cn.ua.millionaregame

interface QuestionObserver {
    fun onQuestionsLoaded(questions: List<Pair<String, List<String>>>, answers: List<Int>)
}


