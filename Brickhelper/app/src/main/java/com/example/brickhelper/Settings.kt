package com.example.brickhelper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_settings.*


class Settings : AppCompatActivity() {

    private var archived: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        radioGroup.setOnCheckedChangeListener { radioGroup, i ->
            when(radioGroup.checkedRadioButtonId){
                Nie.id ->{
                    archived = 0
                }
                Tak.id ->{
                    archived = 1
                }
            }
        }
    }

    fun end(v: View){
        val returnIntent = Intent()
        returnIntent.putExtra("archived", archived)
        returnIntent.putExtra("prefix", prefix.text.toString())
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}
