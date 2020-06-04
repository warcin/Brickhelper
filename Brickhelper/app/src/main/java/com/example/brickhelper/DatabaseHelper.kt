package com.example.brickhelper

//import sun.text.normalizer.UTF16.append
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class DatabaseHelper(private val myContext: Context) : SQLiteOpenHelper(myContext, DB_NAME, null, 10) {
    var DB_PATH: String? = null
    private var myDataBase: SQLiteDatabase? = null

    @Throws(IOException::class)
    fun createDataBase() {
        val dbExist = checkDataBase()
        if (dbExist) {
        } else {
            this.writableDatabase
            try {
                copyDataBase()
            } catch (e: IOException) {
                throw Error("Error copying database")
            }
        }
    }

    private fun checkDataBase(): Boolean {
        var checkDB: SQLiteDatabase? = null
        try {
            val myPath = DB_PATH + DB_NAME
            checkDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE)
        } catch (e: SQLiteException) {
        }
        checkDB?.close()
        return checkDB != null
    }

    @Throws(IOException::class)
    private fun copyDataBase() {
        val myInput =
            myContext.assets.open(DB_NAME)
        val outFileName = DB_PATH + DB_NAME
        val myOutput: OutputStream = FileOutputStream(outFileName)
        val buffer = ByteArray(10)
        var length: Int
        while (myInput.read(buffer).also { length = it } > 0) {
            myOutput.write(buffer, 0, length)
        }
        myOutput.flush()
        myOutput.close()
        myInput.close()
    }

    @Throws(SQLException::class)
    fun openDataBase() {
        val myPath = DB_PATH + DB_NAME
        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE)
    }

    @Synchronized
    override fun close() {
        if (myDataBase != null) myDataBase!!.close()
        super.close()
    }

    override fun onCreate(db: SQLiteDatabase) {}
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion > oldVersion) try {
            copyDataBase()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun rawQuery (query: String): Cursor? {
        return myDataBase!!.rawQuery(query, null)
    }

    fun execute (query: String): Unit? {
        return myDataBase?.execSQL(query)
    }

    fun addProject(name : String, items: ArrayList<Item>): Int {
        // wyznacz kolejne wolne numery
        val c = myDataBase?.rawQuery("SELECT * FROM INVENTORIES ORDER BY _id ASC",null)
        var id = 0
        var tmp = false
        while (c?.moveToNext()!!){
            id = c.getInt(0)
            tmp = true
        }
        if(tmp){
            id ++
        }
        val cal = Calendar.getInstance()
        val time = cal.time.time
        myDataBase?.execSQL("INSERT INTO INVENTORIES(_id,Name,LastAccessed,Active) VALUES ($id,'$name',$time,1);")

        val c1 = myDataBase?.rawQuery("SELECT * FROM INVENTORIESPARTS ORDER BY _id ASC",null)
        var idParts = 0
        tmp = false
        while (c1?.moveToNext()!!){
            idParts = c1.getInt(0)
            tmp = true
        }
        if(tmp){
            idParts ++
        }

        val c5 = myDataBase?.rawQuery("SELECT * FROM Codes ORDER BY _id ASC",null)
        var codeID = 0
        tmp = false
        while (c5?.moveToNext()!!){
            codeID = c5.getInt(0)
            tmp = true
        }
        if(tmp){
            codeID ++
        }

        val c6 = myDataBase?.rawQuery("SELECT * FROM Codes ORDER BY Code ASC",null)
        var designID = 0
        tmp = false
        while (c6?.moveToNext()!!){
            designID = c6.getInt(3)
            tmp = true
        }
        if(tmp){
            designID ++
        }

        for(item in items){
            val idtype = myDataBase?.let { item.getTypeID(it) }
            val idid = myDataBase?.let { item.getitemidID(it) }
            val idcolor = myDataBase?.let { item.getColorID(it) }
            val qis = item.qty

            // jeśli kolcek to dodaj
            if (idtype == 2){
                myDataBase?.execSQL("INSERT INTO INVENTORIESPARTS(_id,INVENTORYID,TYPEID,ITEMID,QuantityInSet,COLORID) VALUES ($idParts,$id,$idtype,$idid,$qis,$idcolor)")
                idParts++

                // pobierz zdjęcie
                val url = szukajURL(item)
                var c3 = myDataBase?.rawQuery("SELECT * FROM Codes WHERE ItemID = $idid AND ColorID = $idcolor;",null)
                if (c3 != null){
                    val bitmap = getBitmapFromURL(url)
                    val photo = getBytesFromBitmap(bitmap)

                    val values = ContentValues()
                    values.put("Image",photo)
                    val strFilter = "ItemID=$idid AND ColorID=$idcolor"

                    val updated = myDataBase?.update("Codes",values,strFilter,null)
                    // jeśli nie było rekordu o klocku to dodaj
                    if (updated == 0){
                        val values2 = ContentValues()
                        values2.put("Image",photo)
                        values2.put("_id", codeID)
                        values2.put("ItemID", idid)
                        values2.put("ColorID", idcolor)
                        values2.put("Code", designID)

                        myDataBase?.insert("Codes", null, values2)

                        codeID ++
                        designID ++
                    }
                }
            }
        }
        return id
    }

    private fun szukajURL(inputItem: Item): String {

        val brickId = myDataBase?.let { inputItem.getitemidID(it) }
        val brickColor = myDataBase?.let { inputItem.getColorID(it) }
        var url = ""
        var color = ""
        var id = ""



        var c2 = myDataBase?.rawQuery("SELECT * FROM PARTS WHERE _id = '$brickId';",null)
        if( c2 != null && c2.moveToFirst() ){
            id = c2.getString(2)
            c2.close()
        }

        c2 = myDataBase?.rawQuery("SELECT * FROM COLORS WHERE _id = '$brickColor';",null)
        if( c2 != null && c2.moveToFirst() ){
            color = c2.getInt(1).toString()
            c2.close()
        }

        if (color == "0"){
            url = "https://www.bricklink.com/PL/$id.jpg"
        }
        else{
            url = "http://img.bricklink.com/P/$color/$id.jpg"
        }

        return url
    }

    private fun getBitmapFromURL(src: String): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url
                .openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.inputStream
           // Log.e("Information", "Pobrano obraz z adresu $src")
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun getBytesFromBitmap(bitmap: Bitmap?): ByteArray? {
        if (bitmap != null) {
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream)
            return stream.toByteArray()
        }
        return null
    }


    companion object {
        private const val DB_NAME = "BrickList"
    }

    init {
        DB_PATH = "/data/data/" + myContext.packageName + "/" + "databases/"
        Log.e("DB Path", DB_PATH)
    }
}