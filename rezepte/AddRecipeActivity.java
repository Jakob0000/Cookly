package com.example.rezepte;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.AutoCompleteTextView;
import android.widget.ArrayAdapter;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AddRecipeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;

    private EditText recipeNameEditText;
    private EditText recipePreparationEditText;
    private ImageView recipeImageView;
    private Uri imageUri;
    private DatabaseHelper dbHelper;

    // Container für dynamische Zutatenzeilen und Button zum Hinzufügen
    private LinearLayout llIngredientsContainer;
    private Button btnAddIngredient;

    // Listen für ausgewählte Allergene und Tools
    private List<String> selectedAllergens;
    private List<String> selectedTools;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Datenbank und Listen initialisieren
        dbHelper = new DatabaseHelper(this);
        selectedAllergens = new ArrayList<>();
        selectedTools = new ArrayList<>();

        // UI-Elemente referenzieren
        recipeNameEditText = findViewById(R.id.et_recipe_name);
        recipePreparationEditText = findViewById(R.id.et_recipe_preparation);
        recipeImageView = findViewById(R.id.iv_recipe_image);
        btnAddIngredient = findViewById(R.id.btn_add_ingredient);
        llIngredientsContainer = findViewById(R.id.ll_ingredients_container);
        Button chooseImageButton = findViewById(R.id.btn_choose_image);
        Button saveRecipeButton = findViewById(R.id.btn_save_recipe);

        // Icons (Allergene & Tools) initialisieren
        setupAllergenIcons();
        setupToolIcons();

        // Füge initial eine Zutatenzeile hinzu
        addIngredientRow();

        // Bild auswählen
        chooseImageButton.setOnClickListener(v -> openImagePicker());

        // Rezept speichern
        saveRecipeButton.setOnClickListener(v -> saveRecipe());

        // Weitere Zutatenzeile hinzufügen
        btnAddIngredient.setOnClickListener(v -> addIngredientRow());
    }

    /**
     * Fügt eine neue Zutatenzeile zum Container hinzu.
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


    /**
     * Initialisiert die Allergen-Icons und setzt die Klicklistener.
     */
    private void setupAllergenIcons() {
        int[] allergenIconIds = {
                R.id.ic_gluten,
                R.id.ic_milch,
                R.id.ic_eier,
                R.id.ic_fisch,
                R.id.ic_schalentiere,
                R.id.ic_erdnuesse,
                R.id.ic_soja,
                R.id.ic_nuesse,
                R.id.ic_sellerie,
                R.id.ic_senf,
                R.id.ic_sesam,
                R.id.ic_lupine,
                R.id.ic_sulfite,
                R.id.ic_weichtiere
        };

        String[] allergenNames = {
                "Gluten", "Milch", "Eier", "Fisch", "Schalentiere", "Erdnüsse",
                "Soja", "Nüsse", "Sellerie", "Senf", "Sesam",
                "Lupine", "Sulfite", "Weichtiere"
        };

        for (int i = 0; i < allergenIconIds.length; i++) {
            FrameLayout allergenIcon = findViewById(allergenIconIds[i]);
            String allergenName = allergenNames[i];
            setupAllergenClickListener(allergenIcon, allergenName);
        }
    }

    private void setupAllergenClickListener(FrameLayout allergenIcon, String allergenName) {
        allergenIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isSelected = v.isSelected();
                v.setSelected(!isSelected);
                if (!isSelected) {
                    selectedAllergens.add(allergenName);
                } else {
                    selectedAllergens.remove(allergenName);
                }
                Toast.makeText(AddRecipeActivity.this,
                        allergenName + (v.isSelected() ? " hinzugefügt" : " entfernt"),
                        Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Initialisiert die Tool-Icons und setzt die Klicklistener.
     */
    private void setupToolIcons() {
        int[] toolIconIds = {
                R.id.ic_ofen,
                R.id.ic_topf,
                R.id.ic_pfanne,
                R.id.ic_messer,
                R.id.ic_schaeler,
                R.id.ic_mixer,
                R.id.ic_raffel,
                R.id.ic_nudelholz,
                R.id.ic_waage
        };

        String[] toolNames = {
                "Ofen", "Topf", "Pfanne", "Messer", "Schäler",
                "Mixer", "Raffel", "Nudelholz", "Waage"
        };

        for (int i = 0; i < toolIconIds.length; i++) {
            FrameLayout toolIcon = findViewById(toolIconIds[i]);
            String toolName = toolNames[i];
            setupToolClickListener(toolIcon, toolName);
        }
    }

    private void setupToolClickListener(FrameLayout toolIcon, String toolName) {
        toolIcon.setOnClickListener(v -> {
            boolean isSelected = v.isSelected();
            v.setSelected(!isSelected);
            if (!isSelected) {
                selectedTools.add(toolName);
            } else {
                selectedTools.remove(toolName);
            }
            Toast.makeText(AddRecipeActivity.this,
                    toolName + (v.isSelected() ? " hinzugefügt" : " entfernt"),
                    Toast.LENGTH_SHORT).show();
        });
    }


    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Bild auswählen"), PICK_IMAGE_REQUEST);
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    bitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), imageUri));
                } else {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                }
                recipeImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Fehler beim Laden des Bildes", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveRecipe() {
        String recipeName = recipeNameEditText.getText().toString().trim();
        String ingredients = collectIngredients();
        String recipePreparation = recipePreparationEditText.getText().toString().trim();
        String imageUriString = imageUri != null ? imageUri.toString() : "";

        if (TextUtils.isEmpty(recipeName)) {
            Toast.makeText(this, "Bitte einen Rezeptnamen eingeben", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(ingredients)) {
            Toast.makeText(this, "Bitte mindestens eine Zutat eingeben", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(recipePreparation)) {
            Toast.makeText(this, "Bitte eine Zubereitung eingeben", Toast.LENGTH_SHORT).show();
            return;
        }

        String allergens = TextUtils.join(", ", selectedAllergens);
        String tools = TextUtils.join(", ", selectedTools);

        dbHelper.addRecipe(
                recipeName,
                ingredients,
                recipePreparation,
                imageUriString,
                allergens,
                tools
        );
        Toast.makeText(this, "Rezept erfolgreich gespeichert!", Toast.LENGTH_SHORT).show();

        // Felder zurücksetzen
        recipeNameEditText.setText("");
        llIngredientsContainer.removeAllViews();
        addIngredientRow();
        recipePreparationEditText.setText("");
        recipeImageView.setImageResource(R.drawable.ic_placeholder);
        selectedAllergens.clear();
        selectedTools.clear();

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Geht alle Zutatenzeilen im Container durch und baut einen String im Format "Menge: Zutat".
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
}
