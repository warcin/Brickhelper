package com.example.brickhelper

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.database.SQLException
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.IOException
import java.net.URL
import javax.xml.parsers.DocumentBuilderFactory


class MainActivity : AppCompatActivity() {
    var c: Cursor? = null
    private var archived = 0
    private val SETTINGS = 100
    private val ADD = 200
    private var prefix : String = "http://fcds.cs.put.poznan.pl/MyWeb/BL/"
    var itemListed = ArrayList<Item>()
    var num = ""
    private var new_project_name = ""
    var downloaded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myDbHelper = DatabaseHelper(this@MainActivity)
        try {
            myDbHelper.createDataBase()
        } catch (ioe: IOException) {
            throw Error("Unable to create database")
        }
        try {
            myDbHelper.openDataBase()
        } catch (sqle: SQLException) {
            throw sqle
        }


        val list = ArrayList<String>()
        if (archived == 1) {
            c = myDbHelper.rawQuery("SELECT * FROM Inventories")
        }
        else {
            c = myDbHelper.rawQuery("SELECT * FROM Inventories WHERE Active = 1")
        }

        if (c!!.moveToFirst()) {
            do {
                list.add(c!!.getString(1))
            } while (c!!.moveToNext())
        }

        val list_sorted = sortuj_widok(list)

        val listView = findViewById<ListView>(R.id.listProjects)
        listView.adapter = ProjectAdapter(this, list_sorted)

        listView.isClickable = true
        listView.onItemClickListener = OnItemClickListener { arg0, arg1, position, arg3 ->
            val o: Any = listView.getItemAtPosition(position)
            val selectedName: String = listView.getItemAtPosition(position) as String

            val intent = Intent(this, Set::class.java)
            intent.putExtra("name", selectedName)
            startActivity(intent)

        }
    }

    override fun onResume() {
        super.onResume()

        val myDbHelper = DatabaseHelper(this@MainActivity)
        myDbHelper.createDataBase()
        myDbHelper.openDataBase()

        val list = ArrayList<String>()
        if (archived == 1) {
            c = myDbHelper.rawQuery("SELECT * FROM Inventories")
        }
        else {
            c = myDbHelper.rawQuery("SELECT * FROM Inventories WHERE Active = 1")
        }

        if (c!!.moveToFirst()) {
            do {
                list.add(c!!.getString(1))
            } while (c!!.moveToNext())
        }

        val list_sorted = sortuj_widok(list)

        val listView = findViewById<ListView>(R.id.listProjects)
        listView.adapter = ProjectAdapter(this, list_sorted)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val myDbHelper = DatabaseHelper(this@MainActivity)
        myDbHelper.createDataBase()
        myDbHelper.openDataBase()

        if (resultCode == Activity.RESULT_OK && requestCode == SETTINGS) {
            archived = data!!.getIntExtra("archived", 0)
            prefix = data.getStringExtra("prefix")

            val list = ArrayList<String>()
            if (archived == 1) {
                c = myDbHelper.rawQuery("SELECT * FROM Inventories")
            }
            else {
                c = myDbHelper.rawQuery("SELECT * FROM Inventories WHERE Active = 1")
            }

            if (c!!.moveToFirst()) {
                do {
                    list.add(c!!.getString(1))
                } while (c!!.moveToNext())
            }

            val list_sorted = sortuj_widok(list)

            val listView = findViewById<ListView>(R.id.listProjects)
            listView.adapter = ProjectAdapter(this, list_sorted)
        }

        if (resultCode == Activity.RESULT_OK && requestCode == ADD) {
            num = data!!.getStringExtra("number")
            new_project_name = data.getStringExtra("name")

            val list = ArrayList<String>()
            if (archived == 1) {
                c = myDbHelper.rawQuery("SELECT * FROM Inventories")
            }
            else {
                c = myDbHelper.rawQuery("SELECT * FROM Inventories WHERE Active = 1")
            }

            if (c!!.moveToFirst()) {
                do {
                    list.add(c!!.getString(1))
                } while (c!!.moveToNext())
            }

            val list_sorted = sortuj_widok(list)

            val listView = findViewById<ListView>(R.id.listProjects)
            listView.adapter = ProjectAdapter(this, list_sorted)

            val c = myDbHelper.rawQuery("SELECT * FROM Inventories WHERE Name = '$new_project_name'")
            if (c?.moveToNext()!!){
                Toast.makeText(this@MainActivity, "Projekt o podanej nazwie ($new_project_name)już istnieje, podaj inną", Toast.LENGTH_SHORT).show()
            }
            else{
                val my_task = MyTask()
                my_task.execute()
            }
        }
    }

    fun settings(v : View){
        val intent = Intent(this, Settings::class.java)
        startActivityForResult(intent, SETTINGS)
    }

    fun add(v : View){
        val intent = Intent(this, Add::class.java)
        startActivityForResult(intent, ADD)
    }

    private inner class MyTask: AsyncTask<Void, Void, Void>() {
        override fun doInBackground(vararg params: Void?): Void? {

            downloaded = download()

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            val myDbHelper = DatabaseHelper(this@MainActivity)
            myDbHelper.createDataBase()
            myDbHelper.openDataBase()

            if (downloaded){
                var list = ArrayList<String>()
                if (archived == 1) {
                    c = myDbHelper.rawQuery("SELECT * FROM Inventories")
                }
                else {
                    c = myDbHelper.rawQuery("SELECT * FROM Inventories WHERE Active = 1")
                }

                if (c!!.moveToFirst()) {
                    do {
                        list.add(c!!.getString(1))
                    } while (c!!.moveToNext())
                }
                val listSorted = sortuj_widok(list)

                val listView = findViewById<ListView>(R.id.listProjects)
                listView.adapter = ProjectAdapter(this@MainActivity, listSorted)

                Toast.makeText(this@MainActivity, "Projekt $new_project_name ($num) został utworzony", Toast.LENGTH_SHORT).show()
                downloaded = false
            }
            else{
                Toast.makeText(this@MainActivity, "Brak danych do pobrania lub zły numer zestawu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun download():Boolean{
        val urlString = "$prefix$num.xml"
        //Toast.makeText(this@MainActivity, "adres $urlString", Toast.LENGTH_SHORT).show()
        val items = ArrayList<Item>()
        try {
            val url = URL(urlString)
            val dbf = DocumentBuilderFactory.newInstance()
            val db = dbf.newDocumentBuilder()
            val doc = db.parse(InputSource(url.openStream()))
            doc.documentElement.normalize()

            var itemList: NodeList = doc.getElementsByTagName("ITEM")

            for(i in 0 until itemList.length)
            {
                var bookNode: Node = itemList.item(i)

                if (bookNode.nodeType == Node.ELEMENT_NODE) {
                    val elem = bookNode as Element
                    val childNodes = elem.childNodes

                    var itemtype = ""
                    var id = ""
                    var qis= ""
                    var color = ""
                    var extra = ""

                    for(j in 0 until childNodes.length){
                        val node = childNodes.item(j)
                        if(node is Element){
                            when(node.nodeName){
                                "ITEMTYPE" -> {itemtype = node.textContent}
                                "ITEMID" -> {id = node.textContent}
                                "QTY" -> {qis = node.textContent}
                                "COLOR" -> {color = node.textContent}
                                "EXTRA" -> {extra = node.textContent}
                            }
                        }
                    }

                    val item = Item(itemtype,id,qis,"0",color,extra)
                    items.add(item)





                }
            }
            itemListed = items

            val myDbHelper = DatabaseHelper(this@MainActivity)
            myDbHelper.createDataBase()
            myDbHelper.openDataBase()

            if(itemListed.isNotEmpty()){
                myDbHelper.addProject(new_project_name, itemListed)
            }

        }catch (er: Exception){
            Log.e("ERR",er.toString())
        }

        if (itemListed.size > 0) {
            return true
        }
        return false

    }

    fun sortuj_widok(lista: ArrayList<String>): ArrayList<String> {
        val return_list = ArrayList<String>()

        val myDbHelper = DatabaseHelper(this@MainActivity)
        myDbHelper.createDataBase()
        myDbHelper.openDataBase()

        c = myDbHelper.rawQuery("SELECT * FROM Inventories ORDER BY LastAccessed DESC")

        if (c!!.moveToFirst()) {
            do {
                if (c!!.getString(1) in lista){
                    return_list.add(c!!.getString(1))
                }
            } while (c!!.moveToNext())
        }

        return return_list
    }



}