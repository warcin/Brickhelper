package com.example.brickhelper

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_export_xml.*
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult


class Export : AppCompatActivity() {

    var exportFileName = ""
    var inventoryId = 0
    val itemsList = arrayListOf<Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export_xml)

        isStoragePermissionGranted()

        val extras = intent.extras ?: return
        inventoryId = extras.getInt("inventoryID")

        val myDbHelper = DatabaseHelper(this@Export)
        myDbHelper.createDataBase()
        myDbHelper.openDataBase()

        // utwórz listę obiektów do eksportu
        val c = myDbHelper.rawQuery("SELECT * FROM InventoriesParts WHERE InventoryID = $inventoryId")
        if (c!!.moveToFirst()) {
            do {
                val itemType = c.getInt(2)
                var c_type = myDbHelper.rawQuery("SELECT * FROM ITEMTYPES WHERE _id = $itemType;")
                c_type!!.moveToNext()
                val itemTypeStr = c_type.getString(1)

                val itemID = c.getInt(3)
                c_type = myDbHelper.rawQuery("SELECT * FROM PARTS WHERE _id = $itemID;")
                c_type!!.moveToNext()
                val itemIDStr = c_type.getString(2)

                val itemQtyStr = c.getInt(4).toString()
                val itemStoredStr = c.getInt(5).toString()

                val itemColor = c.getInt(6)
                c_type = myDbHelper.rawQuery("SELECT * FROM COLORS WHERE _id = $itemColor;")
                c_type!!.moveToNext()
                val itemColorStr = c_type.getInt(1).toString()

                val itemExtraStr = c.getInt(7).toString()

                val item = Item(itemTypeStr, itemIDStr, itemQtyStr, itemStoredStr, itemColorStr, itemExtraStr)
                itemsList.add(item)
                c_type.close()
            } while (c.moveToNext())
        }

    }

    private fun prepareXml(){
        try{
            val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            val doc = docBuilder.newDocument()

            val rootElement = doc.createElement("INVENTORY")
            for(brick in itemsList) {
                val tmp = (brick.qty).toInt() - (brick.stored).toInt()
                if (tmp != 0) {

                    val item = doc.createElement("ITEM")

                    val itemtype = doc.createElement("ITEMTYPE")
                    itemtype.appendChild(doc.createTextNode(brick.itemtype))
                    item.appendChild(itemtype)

                    val itemID = doc.createElement("ITEMID")
                    itemID.appendChild(doc.createTextNode(brick.itemid))
                    item.appendChild(itemID)

                    val color = doc.createElement("COLOR")
                    color.appendChild(doc.createTextNode(brick.color))
                    item.appendChild(color)

                    val qtyfilled = doc.createElement("QTYFILLED")
                    qtyfilled.appendChild(doc.createTextNode(tmp.toString()))
                    item.appendChild(qtyfilled)

                    rootElement.appendChild(item)
                }
            }
            doc.appendChild(rootElement)
            val transformer = TransformerFactory.newInstance().newTransformer()

            transformer.setOutputProperty(OutputKeys.INDENT,"yes")
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amout","2")

            // internal storage
            val path =  this.filesDir
            val outDir = File(path, "LegoExportXML")
            outDir.mkdir()
            val filePath = File(outDir, exportFileName)
            transformer.transform(DOMSource(doc), StreamResult(filePath))

            // external storage
            val pathExternal = this.getExternalFilesDir(null)
            val exportDir = File(pathExternal, "LegoExportXML")
            exportDir.mkdirs()
            val file = File(exportDir, exportFileName)
            if (isStoragePermissionGranted()){
                transformer.transform(DOMSource(doc), StreamResult(file))
                Toast.makeText(this@Export, "Plik XML został wyeksportowany: \n $file", Toast.LENGTH_LONG).show()
            }


        }catch (e : Exception){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show()
        }

    }


    fun cancel(v : View){
        finish()
    }

    fun send(v: View) {
        exportFileName = plainName.text.toString() + ".xml"
        if (exportFileName == ".xml"){
            Toast.makeText(this@Export, "Podaj nazwę pliku", Toast.LENGTH_LONG).show()
        }
        else{
            prepareXml()
        }
    }

    fun isStoragePermissionGranted(): Boolean {
        val TAG = "Storage Permission"
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted")
                return true
            } else {
                Log.v(TAG, "Permission is revoked")
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                return false
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted")
            return true
        }
    }

}
