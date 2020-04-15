package com.example.project2019.classes

class makes(private val discipline: String, private val make: String, private val date: String,
            private val teacher: String, private val trem: String, private var isActive: Boolean = true) {



    fun getDiscipline(): String {
        return this.discipline
    }

    fun  getMake(): String {
        return this.make
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

    fun getActive(): Boolean {
        return this.isActive
    }

    fun setActive(active: Boolean) {
        this.isActive = active
    }

}