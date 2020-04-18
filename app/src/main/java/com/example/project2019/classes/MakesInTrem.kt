package com.example.project2019.classes

class MakesInTrem(private val discipline: String, private val tema: String,
                  private val type_of_work: String, private val type_of_control: String,
                  private val make: String, private val passing_score: String, private val max_score: String,
                  private val date: String, private val teacher: String, private val trem: String) {

    fun getDiscipline(): String {
        return this.discipline
    }

    fun  getTema(): String {
        return this.tema
    }

    fun getTypeOfWork(): String {
        return this.type_of_work
    }

    fun getTypeOfControl(): String {
        return this.type_of_control
    }

    fun getMake(): String {
        return this.make
    }

    fun getPassingScore(): String {
        return this.passing_score
    }

    fun  getMaxScore(): String {
        return this.max_score
    }

    fun getDate(): String {
        return this.date
    }

    fun getTeacher(): String {
        return this.teacher
    }

    fun getTrem(): String {
        return this.trem
    }
}