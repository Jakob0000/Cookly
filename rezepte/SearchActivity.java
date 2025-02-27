package com.example.rezepte;

import android.database.Cursor; // Fehlender Import hinzugefügt
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;
import android.view.View;          // Für die View-Klasse
import android.widget.ProgressBar; // Für die ProgressBar-Klasse
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private TextInputEditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private RecipeAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Recipe> allRecipes;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        progressBar = findViewById(R.id.progressBar);
        searchEditText = findViewById(R.id.editText_search);
        searchResultsRecyclerView = findViewById(R.id.recycler_view_search_results);

        dbHelper = new DatabaseHelper(this);
        allRecipes = fetchAllRecipes();

        // RecyclerView Setup
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(this, allRecipes);
        searchResultsRecyclerView.setAdapter(adapter);

        // Search Action
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchEditText.getText().toString().trim();
            if (!TextUtils.isEmpty(query)) {
                progressBar.setVisibility(View.VISIBLE); // Ladeanimation anzeigen
                filterRecipes(query);
            } else {
                Toast.makeText(this, "Bitte geben Sie einen Suchbegriff ein", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }

    private List<Recipe> fetchAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        Cursor cursor = dbHelper.getRecipes(); // Verwendet die Cursor-Klasse

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INGREDIENTS));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INSTRUCTIONS));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI));
                String allergens = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLERGENS));
                recipes.add(new Recipe(name, ingredients, instructions, imageUri, allergens));
            }
            cursor.close();
        }
        return recipes;
    }

    private void filterRecipes(String query) {
        query = query.toLowerCase();
        List<Recipe> filteredList = new ArrayList<>();
        for (Recipe recipe : allRecipes) {
            if (recipe.getName().toLowerCase().contains(query)) {
                filteredList.add(recipe);
            }
        }

        adapter.updateRecipeList(filteredList);
        progressBar.setVisibility(View.GONE); // Ladeanimation ausblenden

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "Keine Rezepte gefunden", Toast.LENGTH_SHORT).show();
        }
    }
}
