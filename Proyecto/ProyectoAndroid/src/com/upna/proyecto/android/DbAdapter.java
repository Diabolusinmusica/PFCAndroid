package com.upna.proyecto.android;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbAdapter {

    public static final String KEY_NOMBRE = "nombre";
    public static final String KEY_INDICE = "indice";
    public static final String KEY_TRESGE = "tresge";
    public static final String KEY_WIFI = "wifi";
    public static final String KEY_BLUETOOTH = "bluetooth";
    public static final String KEY_NOTIFICACION = "notificacion";
    public static final String KEY_HORA = "hora";
    public static final String KEY_MINUTO = "minuto";
    public static final String KEY_MODO = "modo";
    public static final String KEY_ROWID = "_id";
    
    public static final String KEY_DIASEMANA = "diasemana";

    private static final String TAG = "NotesDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;

    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
        "create table horario (_id integer primary key autoincrement, indice integer, nombre text, tresge integer, wifi integer, bluetooth integer, notificacion text, hora integer, minuto integer, modo text, diasemana text);";

    private static final String DATABASE_NAME = "data";
    private static final String DATABASE_TABLE = "horario";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new note using the title and body provided. If the note is
     * successfully created return the new rowId for that note, otherwise return
     * a -1 to indicate failure.
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long crearEntrada(int horaInd, String nombreDB, int tresgeDB, int wifiDB, int bluetoothDB, String notificacionDB, int horaDB, int minutoDB, String modoDB, String diasemanaDB) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_INDICE,horaInd); //"Columna",valor.
		initialValues.put(KEY_NOMBRE, nombreDB);
		initialValues.put(KEY_TRESGE, tresgeDB);
		initialValues.put(KEY_WIFI, wifiDB);
		initialValues.put(KEY_BLUETOOTH, bluetoothDB);
		initialValues.put(KEY_NOTIFICACION, notificacionDB);
		initialValues.put(KEY_HORA, horaDB);
		initialValues.put(KEY_MINUTO, minutoDB);
		initialValues.put(KEY_MODO, modoDB);
		
		initialValues.put(KEY_DIASEMANA, diasemanaDB);

        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the note with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean borrarEntrada(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all notes in the database
     * 
     * @return Cursor over all notes
     */
    public Cursor getTodasEntradas() {

        return mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_INDICE,
        		KEY_NOMBRE, KEY_TRESGE, KEY_WIFI, KEY_BLUETOOTH, KEY_NOTIFICACION, KEY_HORA, KEY_MINUTO, KEY_MODO, KEY_MODO, KEY_DIASEMANA}, null, null, null, null, KEY_INDICE);
    }

    /**
     * Return a Cursor positioned at the note that matches the given rowId
     * 
     * @param rowId id of note to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor getEntrada(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_INDICE,
            		KEY_NOMBRE, KEY_TRESGE, KEY_WIFI, KEY_BLUETOOTH, KEY_NOTIFICACION, KEY_HORA, KEY_MINUTO, KEY_MODO, KEY_MODO, KEY_DIASEMANA}, KEY_ROWID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    /**
     * Update the note using the details provided. The note to be updated is
     * specified using the rowId, and it is altered to use the title and body
     * values passed in
     * 
     * @param rowId id of note to update
     * @param title value to set note title to
     * @param body value to set note body to
     * @return true if the note was successfully updated, false otherwise
     */
    public boolean actualizarEntrada(long rowId, int horaInd, String nombreDB, int tresgeDB, int wifiDB, int bluetoothDB, String notificacionDB, int horaDB, int minutoDB, String modoDB, String diasemanaDB) {
        ContentValues args = new ContentValues();
        args.put(KEY_INDICE,horaInd); //"Columna",valor.
        args.put(KEY_NOMBRE, nombreDB);
        args.put(KEY_TRESGE, tresgeDB);
        args.put(KEY_WIFI, wifiDB);
        args.put(KEY_BLUETOOTH, bluetoothDB);
        args.put(KEY_NOTIFICACION, notificacionDB);
        args.put(KEY_HORA, horaDB);
		args.put(KEY_MINUTO, minutoDB);
		args.put(KEY_MODO, modoDB);
		
		args.put(KEY_DIASEMANA, diasemanaDB);

        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
}
