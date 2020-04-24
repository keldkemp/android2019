package com.example.project2019.classes

class MakesInTrem(private val discipline: String, private val tema: String,
                  private val type_of_work: String, private val type_of_control: String,
                  private val make: String, private val passing_score: String, private val max_score: String,
                  private val date: String, private val teacher: String, private val trem: String,
                  private var is_bad_make: Boolean = false, private var all_score: Double = 0.0,
                  private var is_all_score: Boolean = false) {

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

    fun isBadMake(): Boolean {
        return this.is_bad_make
    }

    fun setIsBadMake(flag: Boolean) {
        this.is_bad_make = flag
    }

    fun isAllScore(): Boolean {
        return this.is_all_score
    }

    fun setIsAllScore(flag: Boolean) {
        this.is_all_score = flag
    }

    fun getAllScore(): Double {
        return this.all_score
    }

    fun setAllScore(make: Double) {
        this.all_score = make
    }
}