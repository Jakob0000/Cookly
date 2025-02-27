package com.example.rezepte;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "recipes.db";
    private static final int DATABASE_VERSION = 4;

    // Tabellen- und Spaltennamen
    public static final String TABLE_RECIPES = "recipes";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_INGREDIENTS = "ingredients";
    public static final String COLUMN_INSTRUCTIONS = "instructions";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_ALLERGENS = "allergens";
    public static final String COLUMN_TOOLS = "tools";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_RECIPES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT NOT NULL, " +
                COLUMN_INGREDIENTS + " TEXT NOT NULL, " +
                COLUMN_INSTRUCTIONS + " TEXT NOT NULL, " +
                COLUMN_IMAGE_URI + " TEXT, " +
                COLUMN_ALLERGENS + " TEXT, " +
                COLUMN_TOOLS + " TEXT);"; // Tools korrekt hinzugefügt
        db.execSQL(createTable);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPES);
        onCreate(db);
    }



    // Rezept hinzufügen
    public void addRecipe(String name, String ingredients, String instructions, String imageUri, String allergens, String tools) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_INGREDIENTS, ingredients);
        values.put(COLUMN_INSTRUCTIONS, instructions);
        values.put(COLUMN_IMAGE_URI, imageUri);
        values.put(COLUMN_ALLERGENS, allergens);
        values.put(COLUMN_TOOLS, tools);

        db.insert(TABLE_RECIPES, null, values);
        db.close();
    }

    // Alle Rezepte abrufen
    public List<Recipe> getAllRecipes() {
        List<Recipe> recipeList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_RECIPES,
                null,
                null,
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME));
                String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INGREDIENTS));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_INSTRUCTIONS));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI));
                String allergens = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ALLERGENS));
                String tools = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOOLS)); // Tools hinzufügen

                recipeList.add(new Recipe(name, ingredients, instructions, imageUri, allergens, tools));
            }
            cursor.close(); // Sicherstellen, dass der Cursor geschlossen wird
        }

        db.close();
        return recipeList;
    }

    // Alle Rezepte als Cursor abrufen (z. B. für spezielle Abfragen)
    public Cursor getRecipes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(
                TABLE_RECIPES,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
