package com.example.rezepte;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;


import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.gridlayout.widget.GridLayout;

import com.bumptech.glide.Glide;

import java.io.IOException;

public class EditRecipeActivity extends AppCompatActivity {

    private Recipe currentRecipe;
    private DatabaseHelper dbHelper;
    private EditText nameEditText, instructionsEditText;
    private ImageView recipeImageView;
    private String imageUri;

    // Dynamischer Zutatenbereich: Container und Button zum Hinzufügen neuer Zeilen
    private LinearLayout llIngredientsContainer;
    private Button btnAddIngredient;

    // Launcher für die Bildauswahl
    private final ActivityResultLauncher<String> pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri.toString();
                    Glide.with(this)
                            .load(imageUri)
                            .placeholder(R.drawable.ic_placeholder)
                            .into(recipeImageView);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_recipe);

        dbHelper = new DatabaseHelper(this);

        // Views initialisieren
        nameEditText = findViewById(R.id.et_recipe_name);
        instructionsEditText = findViewById(R.id.et_recipe_instructions);
        recipeImageView = findViewById(R.id.iv_edit_recipe_image);
        llIngredientsContainer = findViewById(R.id.ll_ingredients_container);
        btnAddIngredient = findViewById(R.id.btn_add_ingredient);
        Button selectImageButton = findViewById(R.id.btn_select_image);
        Button saveButton = findViewById(R.id.btn_save_recipe);

        // Rezept aus dem Intent laden
        currentRecipe = (Recipe) getIntent().getSerializableExtra("recipe");
        if (currentRecipe != null) {
            nameEditText.setText(currentRecipe.getName());
            instructionsEditText.setText(currentRecipe.getInstructions());
            imageUri = currentRecipe.getImageUri();
            if (imageUri != null && !imageUri.isEmpty()) {
                Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_placeholder)
                        .into(recipeImageView);
            } else {
                recipeImageView.setImageResource(R.drawable.ic_placeholder);
            }
            // Zutaten laden und in Zeilen zerlegen
            loadIngredients(currentRecipe.getIngredients());

            // Allergene und Tools als CheckBoxen anzeigen
            setupKitchenTools();
            setupAllergens();
        }

        // Listener setzen
        selectImageButton.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        btnAddIngredient.setOnClickListener(v -> addIngredientRow());
        saveButton.setOnClickListener(v -> updateRecipe());
    }

    /**
     * Zerlegt den gespeicherten Zutaten-String und fügt für jede Zeile eine Eingabezeile hinzu.
     */
    private void loadIngredients(String ingredientsString) {
        llIngredientsContainer.removeAllViews();
        if (!TextUtils.isEmpty(ingredientsString)) {
            String[] lines = ingredientsString.split("\n");
            for (String line : lines) {
                String[] parts = line.split(": ");
                String quantity = parts.length > 0 ? parts[0] : "";
                String ingredient = parts.length > 1 ? parts[1] : "";
                addIngredientRow(quantity, ingredient);
            }
        }
        // Leere Zeile hinzufügen
        addIngredientRow();
    }

    /**
     * Fügt dem Zutaten-Container eine neue Zeile hinzu.
     */
    private void addIngredientRow(String quantity, String ingredient) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        row.setLayoutParams(rowParams);
        row.setPadding(0, 8, 0, 8);

        EditText etQuantity = new EditText(this);
        LayoutParams quantityParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        etQuantity.setLayoutParams(quantityParams);
        etQuantity.setHint("Menge (z.B. 200g, 1L)");
        etQuantity.setText(quantity);

        // AutoCompleteTextView für Zutat – Vorschläge aus arrays.xml
        AutoCompleteTextView etIngredient = new AutoCompleteTextView(this);
        LayoutParams ingredientParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 2);
        etIngredient.setLayoutParams(ingredientParams);
        etIngredient.setHint("Zutat");
        etIngredient.setText(ingredient);
        String[] ingredientSuggestions = getResources().getStringArray(R.array.ingredient_suggestions);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, ingredientSuggestions);
        etIngredient.setAdapter(adapter);
        etIngredient.setThreshold(1);

        row.addView(etQuantity);
        row.addView(etIngredient);
        llIngredientsContainer.addView(row);
    }

    // Überladene Methode für eine leere Zeile
    private void addIngredientRow() {
        addIngredientRow("", "");
    }


    private void setupKitchenTools() {
        GridLayout toolsLayout = findViewById(R.id.grid_tools);
        toolsLayout.setColumnCount(3);
        String[] kitchenTools = {"Ofen", "Topf", "Pfanne", "Messer", "Schäler", "Mixer", "Raffel", "Nudelholz", "Waage"};
        for (String tool : kitchenTools) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(tool);
            toolsLayout.addView(checkBox);
        }
    }

    private void setupAllergens() {
        GridLayout allergensLayout = findViewById(R.id.grid_allergens);
        allergensLayout.setColumnCount(3);
        String[] allergensArray = {"Gluten", "Milch", "Eier", "Fisch", "Schalentiere", "Erdnüsse",
                "Soja", "Nüsse", "Sellerie", "Senf", "Sesam", "Lupine", "Sulfite", "Weichtiere"};
        for (String allergen : allergensArray) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(allergen);
            // Setze Klicklistener, um visuelles Feedback zu geben
            checkBox.setOnClickListener(v -> {
                if (((CheckBox)v).isChecked()) {
                    Toast.makeText(EditRecipeActivity.this, allergen + " ausgewählt", Toast.LENGTH_SHORT).show();
                    v.setBackgroundColor(getResources().getColor(R.color.selectedColor));
                } else {
                    Toast.makeText(EditRecipeActivity.this, allergen + " entfernt", Toast.LENGTH_SHORT).show();
                    v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                }
            });
            allergensLayout.addView(checkBox);
        }
    }

    /**
     * Liest alle Zutatenzeilen aus dem Container und baut einen String im Format "Menge: Zutat".
     */
    private String collectIngredients() {
        StringBuilder ingredientsBuilder = new StringBuilder();
        int rowCount = llIngredientsContainer.getChildCount();
        for (int i = 0; i < rowCount; i++) {
            View row = llIngredientsContainer.getChildAt(i);
            if (row instanceof LinearLayout) {
                LinearLayout rowLayout = (LinearLayout) row;
                if (rowLayout.getChildCount() >= 2) {
                    EditText etQuantity = (EditText) rowLayout.getChildAt(0);
                    EditText etIngredient = (EditText) rowLayout.getChildAt(1);
                    String quantity = etQuantity.getText().toString().trim();
                    String ingredient = etIngredient.getText().toString().trim();
                    if (!TextUtils.isEmpty(quantity) && !TextUtils.isEmpty(ingredient)) {
                        ingredientsBuilder.append(quantity)
                                .append(": ")
                                .append(ingredient)
                                .append("\n");
                    }
                }
            }
        }
        return ingredientsBuilder.toString().trim();
    }

    private void updateRecipe() {
        String name = nameEditText.getText().toString().trim();
        String ingredients = collectIngredients();
        String instructions = instructionsEditText.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(ingredients) || TextUtils.isEmpty(instructions)) {
            Toast.makeText(this, "Alle Felder müssen ausgefüllt sein!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Küchenwerkzeuge sammeln
        GridLayout toolsLayout = findViewById(R.id.grid_tools);
        StringBuilder toolsBuilder = new StringBuilder();
        for (int i = 0; i < toolsLayout.getChildCount(); i++) {
            CheckBox cb = (CheckBox) toolsLayout.getChildAt(i);
            if (cb.isChecked()) {
                toolsBuilder.append(cb.getText()).append(", ");
            }
        }
        String selectedTools = toolsBuilder.length() > 0 ? toolsBuilder.substring(0, toolsBuilder.length() - 2) : "";

        // Allergene sammeln
        GridLayout allergensLayout = findViewById(R.id.grid_allergens);
        StringBuilder allergensBuilder = new StringBuilder();
        for (int i = 0; i < allergensLayout.getChildCount(); i++) {
            CheckBox cb = (CheckBox) allergensLayout.getChildAt(i);
            if (cb.isChecked()) {
                allergensBuilder.append(cb.getText()).append(", ");
            }
        }
        String selectedAllergens = allergensBuilder.length() > 0 ? allergensBuilder.substring(0, allergensBuilder.length() - 2) : "";

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_INGREDIENTS, ingredients);
        values.put(DatabaseHelper.COLUMN_INSTRUCTIONS, instructions);
        values.put(DatabaseHelper.COLUMN_IMAGE_URI, imageUri);
        values.put(DatabaseHelper.COLUMN_ALLERGENS, selectedAllergens);
        values.put(DatabaseHelper.COLUMN_TOOLS, selectedTools);

        db.update(DatabaseHelper.TABLE_RECIPES, values, DatabaseHelper.COLUMN_NAME + "=?",
                new String[]{ currentRecipe.getName() });
        db.close();

        Toast.makeText(this, "Rezept aktualisiert!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra("recipe", new Recipe(name, ingredients, instructions, imageUri, selectedAllergens, selectedTools));
        startActivity(intent);
        finish();
    }
}
