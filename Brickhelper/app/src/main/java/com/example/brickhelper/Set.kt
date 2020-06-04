package com.example.brickhelper

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.ArrayList


class Set : AppCompatActivity() {

    private var projectName = ""
    private var projectID: Int = 0
    private var active: Int = 0
    var c: Cursor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set)

        val extras = intent.extras ?: return
        projectName = extras.getString("name").toString()

        val title = findViewById<TextView>(R.id.title)
        title.text = projectName

        val myDbHelper = DatabaseHelper(this@Set)
        myDbHelper.createDataBase()
        myDbHelper.openDataBase()
        c = myDbHelper.rawQuery("SELECT * FROM Inventories WHERE Name = '$projectName'")
        c!!.moveToFirst()
        projectID = c!!.getString(0).toInt()
        active = c!!.getInt(2)

        if (active == 1){
            val archivebtn = findViewById<Button>(R.id.buttonArchive)
            archivebtn.text = "Archiwizuj"
        }
        else{
            val archivebtn = findViewById<Button>(R.id.buttonArchive)
            archivebtn.text = "Od-archiwizuj"
        }

        val cal = Calendar.getInstance()
        val time = cal.time.time
        myDbHelper.execute("UPDATE Inventories SET LastAccessed = $time WHERE _id = $projectID")

        val list = ArrayList<Item>()

        c = myDbHelper.rawQuery("SELECT * FROM InventoriesParts WHERE InventoryID = $projectID")
        if (c!!.moveToFirst()) {
            do {
                val itemType = c!!.getString(2)
                val itemID = c!!.getString(3)
                val itemQty = c!!.getString(4)
                val itemStored = c!!.getString(5)
                val itemColor = c!!.getString(6)
                val itemExtra = c!!.getString(7)
                val item = Item(itemType, itemID, itemQty, itemStored, itemColor, itemExtra)
                list.add(item)
            } while (c!!.moveToNext())
        }

        val listView = findViewById<ListView>(R.id.setItemsList)
        val adapter = SetAdapter(this, list, projectID)
        adapter.notifyDataSetChanged()
        listView.adapter = adapter


    }

    fun archive(v: View){
        val myDbHelper = DatabaseHelper(this@Set)
        myDbHelper.createDataBase()
        myDbHelper.openDataBase()

        if (active == 1){
            myDbHelper.execute("UPDATE INVENTORIES SET ACTIVE = 0 WHERE _id = $projectID;")
            Toast.makeText(this@Set, "Zarchiwizowano projekt '$projectName'", Toast.LENGTH_SHORT).show()
        }
        else{
            myDbHelper.execute("UPDATE INVENTORIES SET ACTIVE = 1 WHERE _id = $projectID;")
            Toast.makeText(this@Set, "Aktywowano projekt '$projectName'", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    fun export(v: View){
        val intent = Intent(this, Export::class.java)
        intent.putExtra("inventoryID", projectID)
        startActivity(intent)
    }
}
