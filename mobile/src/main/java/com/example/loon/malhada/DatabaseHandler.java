package com.example.loon.malhada;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by loon on 2016-11-14.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private  static final int DATABASE_VERSION = 1;
    private static final String DATABAES_NAME = "PlugManager";
    private static final String TABLE_CONTACTS = "Plug";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME ="name";
    private static final String KEY_IR ="ir";
    private static final String KEY_IP ="ip";
    private static final String KEY_CONDITION ="condition";
    private static final String KEY_LOCATION ="location";
    public DatabaseHandler(Context context) {
        super(context, DATABAES_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HEALTH_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "(" + KEY_ID + "INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_LOCATION + " TEXT," + KEY_IR + " TEXT" + KEY_IP + " TEXT" + KEY_CONDITION + " TEXT" + ")";
        db.execSQL(CREATE_HEALTH_TABLE);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTS);
        onCreate(db);
    }
    void addContact(Plug_Info plug_info)    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, plug_info.getName());
        values.put(KEY_LOCATION,  plug_info.getLocation());
        values.put(KEY_IR,  Integer.toString(plug_info.getIR()));
        values.put(KEY_IP,  plug_info.getIR());
        values.put(KEY_CONDITION,  Integer.toString(plug_info.getIR()));

        db.insert(TABLE_CONTACTS,null,values);
        db.close();
    }
    private List<Plug_Info> getAllCustomer_Info()    {
        List<Plug_Info> pulg_infoList = new ArrayList<Plug_Info>();
        String selectQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        if(cursor.moveToFirst()){
            do{
                Plug_Info plug_info = new Plug_Info();
                plug_info.setName(cursor.getString(1));
                plug_info.setLocation(cursor.getString(2));
                plug_info.setIR(Integer.parseInt(cursor.getString(3)));
                plug_info.setIp(cursor.getString(4));
                plug_info.setCondition(Integer.parseInt(cursor.getString(3)));
                pulg_infoList.add(plug_info);
            }while (cursor.moveToNext());
        }
        return pulg_infoList;
    }
    public int getCount(){
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery,null);
        // cursor.close();
        return cursor.getCount();
    }

}
