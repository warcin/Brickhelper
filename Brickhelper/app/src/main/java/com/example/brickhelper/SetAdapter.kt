package com.example.brickhelper

import android.content.Context
import android.database.Cursor
import android.graphics.BitmapFactory
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*


class SetAdapter(context: Context, projects_list: ArrayList<Item>, projektID: Int) : BaseAdapter() {

    private val mContext: Context = context
    private val length = projects_list.size
    private val list = projects_list
    private val proID = projektID

    override fun getCount(): Int {
        return length
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getItem(position: Int): Any {
        return list[position]
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var c: Cursor? = null
        val myDbHelper = DatabaseHelper(mContext)
        myDbHelper.createDataBase()
        myDbHelper.openDataBase()

        val layoutInflater = LayoutInflater.from(mContext)
        val row = layoutInflater.inflate(R.layout.single_item, parent, false)

        val name = row.findViewById<TextView>(R.id.brickName)
        val idCode = list[position].itemid
        c = myDbHelper.rawQuery("SELECT * FROM PARTS WHERE _id = '$idCode';")
        if( c != null && c.moveToFirst() ){
            name.text = c.getString(3)
            c.close()
        }

        val color = row.findViewById<TextView>(R.id.brickColor)
        val colorInt = list[position].color
        c = myDbHelper.rawQuery("SELECT * FROM COLORS WHERE _id = '$colorInt';")
        if( c != null && c.moveToFirst() ){
            color.text = c.getString(2)
            c.close()
        }

        val image = row.findViewById<ImageView>(R.id.imageView)
        c = myDbHelper.rawQuery("SELECT * FROM Codes WHERE ItemId=$idCode AND ColorID=$colorInt;")
        if( c != null && c.moveToFirst() ){
            val photo = c.getBlob(4)
            val bm = BitmapFactory.decodeByteArray(photo, 0, photo.size)
            image.setImageBitmap(bm)
            c.close()
        }

        val stored = row.findViewById<TextView>(R.id.brickStored)
        stored.text = list[position].stored

        val max = row.findViewById<TextView>(R.id.brickMax)
        max.text = list[position].qty

        val plus = row.findViewById<Button>(R.id.addItem)
        val sub = row.findViewById<Button>(R.id.subItem)
        var tmp = 0

        plus.setOnClickListener{
            if ((list[position].stored).toInt() < (list[position].qty).toInt()) {
                tmp = (list[position].stored).toInt()
                tmp += 1
                list[position].stored = tmp.toString()
                myDbHelper.execute("UPDATE InventoriesParts SET QuantityInStore = $tmp " +
                        "WHERE InventoryID = $proID AND ItemID = $idCode AND ColorID = $colorInt")
                notifyDataSetChanged()
            }
        }

        if ((list[position].stored).toInt() == (list[position].qty).toInt()) {
            row.setBackgroundColor(Color.parseColor("#71FF33"))
        }

        sub.setOnClickListener{
            if ((list[position].stored).toInt() > 0) {
                tmp = (list[position].stored).toInt()
                tmp -= 1
                list[position].stored = tmp.toString()
                myDbHelper.execute("UPDATE InventoriesParts SET QuantityInStore = $tmp " +
                        "WHERE InventoryID = $proID AND ItemID = $idCode AND ColorID = $colorInt")
                notifyDataSetChanged()
                if ((list[position].stored).toInt() != (list[position].qty).toInt()) {
                    row.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }

        return row
    }

}