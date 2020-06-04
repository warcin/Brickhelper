package com.example.brickhelper

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged


class Add : AppCompatActivity() {

    private var number: String = ""
    private var name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add)

        val name_field = findViewById<EditText>(R.id.name)
        val lego_no = findViewById<EditText>(R.id.number)

        name_field.doAfterTextChanged {
            name = name_field.text.toString()
        }
        lego_no.doAfterTextChanged {
            number = lego_no.text.toString()
        }
    }

    fun end(v: View){
        val returnIntent = Intent()
        returnIntent.putExtra("number", number)
        returnIntent.putExtra("name", name)
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}
