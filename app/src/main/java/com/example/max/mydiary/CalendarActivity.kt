package com.example.max.mydiary

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View

import kotlinx.android.synthetic.main.activity_calendar.*


class CalendarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)
    }

    fun backToMainActivity(view: View) {
        //Create an Intent to go back to main activity
        val backToMainActivityIntent = Intent(this, MainActivity::class.java)
        //Start the activity
        startActivity(backToMainActivityIntent)
    }


}
