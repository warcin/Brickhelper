package com.example.brickhelper

import android.database.sqlite.SQLiteDatabase

class Item{
    var itemtype : String
    var itemid : String
    var qty : String
    var stored : String
    var color : String
    var extra : String
    constructor(itemtype: String, itemid: String, qty: String, stored: String, color: String, extra: String){
        this.itemtype = itemtype
        this.itemid = itemid
        this.qty = qty
        this.stored = stored
        this.color = color
        this.extra = extra
    }

    fun getTypeID(db: SQLiteDatabase): Int{
        val typetmp = this.itemtype
        var c_type = db.rawQuery("SELECT _id FROM ITEMTYPES WHERE CODE = '$typetmp';",null)
        c_type.moveToNext()
        var type = c_type.getInt(0)
        return type
    }

    fun getitemidID(db: SQLiteDatabase): Int{
        val typetmp = this.itemid
        var type = 1
        var c_type = db.rawQuery("SELECT _id FROM PARTS WHERE CODE = '$typetmp';",null)
        if(c_type.moveToNext())
        {
            type = c_type.getInt(0)
        }

        return type
    }

    fun getColorID(db: SQLiteDatabase): Int{
        val typetmp = this.color
        var c_type = db.rawQuery("SELECT _id FROM COLORS WHERE CODE = '$typetmp';",null)
        c_type.moveToNext()
        var type = c_type.getInt(0)
        return type
    }

}