package Sinisa.Stevanovic.eventsApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "EventsApp.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_USERS =
            "CREATE TABLE users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT UNIQUE NOT NULL, " +
                    "email TEXT UNIQUE NOT NULL, " +
                    "lozinka TEXT NOT NULL" +
                    ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    @Override
    public void onConfigure (SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }


    public long registerUser(String username, String email, String password) {

        String hashedPassword = PasswordHasher.hashPassword(password);

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("lozinka", hashedPassword);

        db.close();
        return db.insert("users", null, values);
    }


    public String checkUserLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users",
                new String[]{"email", "lozinka"},
                "username=?",
                new String[]{username},
                null, null, null);

        String storedEmail = null;

        if (cursor != null && cursor.moveToFirst()) {
            storedEmail = cursor.getString(cursor.getColumnIndexOrThrow("email"));
            String storedPasswordData = cursor.getString(cursor.getColumnIndexOrThrow("lozinka"));
            cursor.close();


            if (!PasswordHasher.verifyPassword(password, storedPasswordData)) {
                storedEmail = null;
            }
        } else {
            if (cursor != null) cursor.close();
        }

        db.close();
        cursor.close();
        return storedEmail;

    }
}