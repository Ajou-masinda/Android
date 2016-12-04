package com.example.loon.malhada;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by loon on 2016-11-14.
 */

public class DatabaseHandler extends SQLiteOpenHelper {

    private  static final int DATABASE_VERSION = 1;
    private static final String DATABAES_NAME = "plug_manager";
    private static final String TABLE_CONTACTS = "plug";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME ="name";
    private static final String KEY_TYPE ="type";
    private static final String KEY_VENDOR ="vendor";
    private static final String KEY_SERIAL ="serial";
    private static final String KEY_STATUS ="status";
    private static final String KEY_REGIST ="regist";
    private static final String KEY_LOCATION ="location";
    public DatabaseHandler(Context context) {
        super(context, DATABAES_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_HEALTH_TABLE = "CREATE TABLE " + TABLE_CONTACTS + "(" + KEY_ID + "INTEGER PRIMARY KEY," + KEY_NAME + " TEXT," + KEY_LOCATION + " TEXT," + KEY_TYPE + " TEXT," + KEY_VENDOR + " TEXT," + KEY_SERIAL + " TEXT," + KEY_STATUS + " TEXT,"+ KEY_REGIST + " TEXT" + ")";
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
        values.put(KEY_TYPE,  Integer.toString(plug_info.getType()));
        values.put(KEY_VENDOR,  Integer.toString(plug_info.getVendor()));
        values.put(KEY_SERIAL,  plug_info.getSerial());
        values.put(KEY_STATUS,  Integer.toString(plug_info.getStatus()));
        values.put(KEY_REGIST,  Integer.toString(plug_info.getRegister()));
        db.insert(TABLE_CONTACTS,null,values);
        db.close();
    }
    public List<Plug_Info> getAllCustomer_Info()    {
        List<Plug_Info> pulg_infoList = new ArrayList<Plug_Info>();
        String selectQuery = "SELECT * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery,null);
        if(cursor.moveToFirst()){
            do{
                Plug_Info plug_info = new Plug_Info();
                plug_info.setName(cursor.getString(1));
                plug_info.setLocation(cursor.getString(2));
                plug_info.setType(Integer.parseInt(cursor.getString(3)));
                plug_info.setVendor(Integer.parseInt(cursor.getString(4)));
                plug_info.setSerial(cursor.getString(5));
                plug_info.setStatus(Integer.parseInt(cursor.getString(6)));
                plug_info.setRegister(Integer.parseInt(cursor.getString(7)));
                pulg_infoList.add(plug_info);
            }while (cursor.moveToNext());
        }
        cursor.close();
        return pulg_infoList;
    }
    public int getCount(){
        String countQuery = "SELECT  * FROM " + TABLE_CONTACTS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery,null);
        return cursor.getCount();
    }
    public void deleteContacnt(Plug_Info plug_info){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS,KEY_SERIAL + "=?", new String[] {plug_info.getSerial()});
        db.close();
    }
    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_CONTACTS, null, null);
    }

    public  int updatePlug(Plug_Info plug_info, String name, String location,int type, int vendor,int regist){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME,name);
        values.put(KEY_LOCATION,location);
        values.put(KEY_TYPE,type);
        values.put(KEY_VENDOR,vendor);
        values.put(KEY_REGIST,regist);
        return db.update(TABLE_CONTACTS,values,KEY_SERIAL + " =?",new String[] {plug_info.getSerial()});
    }
}
