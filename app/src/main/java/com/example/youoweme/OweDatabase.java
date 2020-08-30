package com.example.youoweme;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.youoweme.classes.Person;
import com.example.youoweme.classes.Schuld;

import java.util.ArrayList;
import java.util.List;


public class OweDatabase extends SQLiteOpenHelper {             // Initialisierung der Variablen
    private static final String DB_NAME = "Schuld.db";
    private static final int VERSION = 1;

    private static final String TABELLE_PERSON = "person";
    private static final String SPALTE_ID = "ID";
    private static final String SPALTE_NAME = "name";
    private static final String SPALTE_BETRAG = "betrag";

    private static final String TABELLE_SCHULD = "schuld";
    private static final String SPALTE_SCHULDENNR = "schuldennr";
    private static final String SPALTE_BETREFF = "betreff";
    private static final String SPALTE_SCHULDBETRAG = "schuldbetrag";
    private static final String SPALTE_DATUM = "datum";


    public OweDatabase(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(
                "CREATE TABLE " + TABELLE_PERSON + " (" +     // Tabelle Person erzeugen
                        SPALTE_ID + " INTEGER PRIMARY KEY, " +
                        SPALTE_NAME + " TEXT NOT NULL, " +
                        SPALTE_BETRAG + " REAL " +
                        ")"
        );
        sqLiteDatabase.execSQL(
                "CREATE TABLE " + TABELLE_SCHULD + " (" +                 // Tabelle Schuld erzeugen
                        SPALTE_SCHULDENNR + " INTEGER PRIMARY KEY, " +
                        SPALTE_BETREFF + " TEXT NOT NULL, " +
                        SPALTE_SCHULDBETRAG + " REAL, " +
                        SPALTE_DATUM + " TEXT NOT NULL ,  " +
                        SPALTE_ID + " INTEGER " +
                        ")"
        );
    }

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int i, final int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABELLE_PERSON);
        db.execSQL("DROP TABLE IF EXISTS " + TABELLE_SCHULD);


        // Create tables again
        onCreate(db);
    }

    public void addSchuld(Schuld schuld, Person person) {
        ContentValues values = new ContentValues();
        values.put(SPALTE_BETREFF, schuld.getBetreff());
        values.put(SPALTE_DATUM, schuld.getDatum());

        values.put(SPALTE_SCHULDBETRAG, schuld.getSchuldenbetrag());
        values.put(SPALTE_ID, person.getID());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABELLE_SCHULD, null, values);

        db.close();
    }

    public void insertPerson(Person person) {

        ContentValues values = new ContentValues();

        values.put(SPALTE_NAME, person.getName());
        values.put(SPALTE_BETRAG, person.getBetrag());


        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABELLE_PERSON, null, values);

        db.close();
    }


    public Person getPerson(String personas) {

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABELLE_PERSON, new String[]{SPALTE_ID, SPALTE_NAME, SPALTE_BETRAG
                }, SPALTE_NAME + "=?",
                new String[]{String.valueOf(personas)}, null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();

        Person person = new Person(Integer.parseInt(cursor.getString(0)), cursor.getString(1),
                Double.parseDouble(cursor.getString(2)));
        // return person
        return person;
    }


    public List<Schuld> getallSchulden(Person person) {

        List<Schuld> schulden = new ArrayList<Schuld>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor meinZeiger = db.rawQuery("SELECT * FROM " + TABELLE_SCHULD + " WHERE " + SPALTE_ID + "=" + person.getID(), null);


        if(meinZeiger!=null &&  meinZeiger.moveToFirst()){
            do {
                Schuld tmp = new Schuld(meinZeiger.getInt(0),meinZeiger.getDouble(2), meinZeiger.getString(1), meinZeiger.getString(3), meinZeiger.getInt(4));
                schulden.add(tmp);
            } while (meinZeiger.moveToNext());
        }



        return schulden;
    }

    public List<Person> getAllPerson() {


        List<Person> personList = new ArrayList<Person>();
        // Select *
        String selectQuery = "SELECT  * FROM " + TABELLE_PERSON;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // alle Personen hinzufügen
        if (cursor.moveToFirst()) {
            do {
                Person person = new Person();
                person.setID(Integer.parseInt(cursor.getString(0)));
                person.setName(cursor.getString(1));
                person.setBetrag(Double.parseDouble(cursor.getString(2)));

                // Adding person to list
                personList.add(person);
            } while (cursor.moveToNext());
        }

        // return person list
        return personList;
    }

    public void deletePerson(Person person) {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABELLE_SCHULD, SPALTE_ID + " = ?",
                new String[]{String.valueOf(person.getID())});

        sqLiteDatabase.delete(TABELLE_PERSON, SPALTE_NAME + " = ?",
                new String[]{String.valueOf(person.getName())});
        sqLiteDatabase.close();
    }

    public void deleteSchuld(Schuld schuld){
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        sqLiteDatabase.delete(TABELLE_SCHULD, SPALTE_SCHULDENNR + " = ?",
                new String[]{String.valueOf(schuld.getSchuldenNR())});
        sqLiteDatabase.close();
    }

    public int updatePerson(Person person) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(SPALTE_NAME, person.getName());
        return db.update(TABELLE_PERSON, cv, SPALTE_ID + " = ?", new String[]{String.valueOf(person.getID())});
    }


}
