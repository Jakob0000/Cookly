package com.example.rezepte;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;

public class RecipeDetailActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private Recipe currentRecipe;

    // Mapping der Allergene zu Icons
    private final HashMap<String, Integer> allergenIconMap = new HashMap<String, Integer>() {{
        put("Erdnüsse", R.drawable.ic_erdnuesse);
        put("Milch", R.drawable.ic_milch);
        put("Gluten", R.drawable.ic_gluten);
        put("Fisch", R.drawable.ic_fisch);
        put("Sesam", R.drawable.ic_sesam);
        put("Senf", R.drawable.ic_senf);
        put("Sellerie", R.drawable.ic_sellerie);
        put("Eier", R.drawable.ic_eier);
        put("Lupine", R.drawable.ic_lupine);
        put("Sulfite", R.drawable.ic_sulfite);
        put("Schalentiere", R.drawable.ic_schalentiere);
        put("Weichtiere", R.drawable.ic_weichtiere);
        put("Nüsse", R.drawable.ic_nuesse);
        put("Soja", R.drawable.ic_soja);
    }};

    // Mapping der Tools zu Icons
    private final HashMap<String, Integer> toolsIconMap = new HashMap<String, Integer>() {{
        put("Ofen", R.drawable.ic_ofen);
        put("Topf", R.drawable.ic_topf);
        put("Pfanne", R.drawable.ic_pfanne);
        put("Messer", R.drawable.ic_messer);
        put("Schäler", R.drawable.ic_schaeler);
        put("Mixer", R.drawable.ic_mixer);
        put("Raffel", R.drawable.ic_raffel);
        put("Nudelholz", R.drawable.ic_nudelholz);
        put("Waage", R.drawable.ic_waage);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);

        dbHelper = new DatabaseHelper(this);

        ImageView recipeImage = findViewById(R.id.iv_recipe_detail_image);
        TextView recipeName = findViewById(R.id.tv_recipe_detail_name);
        TextView recipeDescription = findViewById(R.id.tv_recipe_detail_description);
        TextView recipePreparation = findViewById(R.id.tv_recipe_detail_preparation);
        LinearLayout allergenIconsLayout = findViewById(R.id.ll_allergen_icons);
        LinearLayout toolsIconsLayout = findViewById(R.id.ll_tools_icons);

        ImageButton shareButton = findViewById(R.id.share_button);
        ImageButton deleteButton = findViewById(R.id.delete_button);
        ImageButton editButton = findViewById(R.id.edit_button);
        ImageButton translateButton = findViewById(R.id.translate_button);

        // Hole die Rezeptdaten aus dem Intent
        currentRecipe = (Recipe) getIntent().getSerializableExtra("recipe");

        if (currentRecipe != null) {
            Log.d("RecipeDetailActivity", "Loaded recipe: " + currentRecipe.getName());

            recipeName.setText(currentRecipe.getName());
            // Da wir jetzt Zutaten anstelle einer "Beschreibung" speichern, zeigen wir diese an:
            recipeDescription.setText(currentRecipe.getIngredients());
            recipePreparation.setText(currentRecipe.getInstructions());

            Glide.with(this)
                    .load(currentRecipe.getImageUri() == null || currentRecipe.getImageUri().isEmpty()
                            ? R.drawable.ic_placeholder
                            : Uri.parse(currentRecipe.getImageUri()))
                    .placeholder(R.drawable.ic_placeholder)
                    .into(recipeImage);

            // Füge die Icons für Tools und Allergene hinzu
            displayIcons(toolsIconsLayout, currentRecipe.getTools(), toolsIconMap);
            displayIcons(allergenIconsLayout, currentRecipe.getAllergens(), allergenIconMap);

            shareButton.setOnClickListener(v -> shareRecipe());
            deleteButton.setOnClickListener(v -> deleteRecipe());
            editButton.setOnClickListener(v -> editRecipe());
            translateButton.setOnClickListener(v -> translateRecipe());

        } else {
            Toast.makeText(this, "Rezept konnte nicht geladen werden.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void translateRecipe() {
        String textToTranslate = currentRecipe.getName() + "\n\n" +
                "Zutaten:\n" + currentRecipe.getIngredients() + "\n\n" +
                "Zubereitung:\n" + currentRecipe.getInstructions();
        openGoogleTranslate(textToTranslate);
    }

    public void openGoogleTranslate(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        intent.setPackage("com.google.android.apps.translate"); // Direkt Google Translate öffnen
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            String url = "https://translate.google.com/?sl=auto&tl=en&text=" + Uri.encode(text);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(webIntent);
        }
    }

    private void displayIcons(LinearLayout layout, String items, HashMap<String, Integer> iconMap) {
        if (items == null || items.isEmpty()) return;
        String[] itemList = items.split(",");
        layout.removeAllViews();
        for (String originalItem : itemList) {
            final String item = originalItem.trim();
            final Integer iconResId = iconMap.get(item);
            if (iconResId != null) {
                ImageView iconView = new ImageView(this);
                iconView.setImageResource(iconResId);
                int iconSize = getResources().getDimensionPixelSize(R.dimen.allergen_icon_size);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(iconSize, iconSize);
                params.setMargins(8, 0, 8, 0);
                iconView.setLayoutParams(params);
                iconView.setOnLongClickListener(v -> {
                    showAllergenPopup(item, iconResId, getInfoText(item));
                    return true;
                });
                layout.addView(iconView);
            }
        }
    }

    private void showAllergenPopup(String allergenName, int allergenIcon, String allergenDescription) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.CustomBottomSheetDialog);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_allergen_detail, null);
        ImageView icon = view.findViewById(R.id.iv_allergen_icon);
        TextView title = view.findViewById(R.id.tv_allergen_title);
        TextView description = view.findViewById(R.id.tv_allergen_description);
        icon.setImageResource(allergenIcon);
        title.setText(allergenName);
        description.setText(allergenDescription);
        dialog.setContentView(view);
        dialog.show();
    }

    private String getInfoText(String name) {
        HashMap<String, String> infoMap = new HashMap<>();
        infoMap.put("Gluten", "Gluten ist ein Eiweiß, das in Weizen, Gerste und Roggen vorkommt.");
        infoMap.put("Milch", "Milch enthält Laktose und kann bei Menschen mit Laktoseintoleranz Probleme verursachen.");
        infoMap.put("Eier", "Eier sind eine häufige Quelle von Allergien und kommen in vielen Lebensmitteln vor.");
        infoMap.put("Fisch", "Fischallergien können schwerwiegend sein und betreffen oft verschiedene Fischarten.");
        infoMap.put("Schalentiere", "Schalentiere wie Krabben, Garnelen und Hummer können starke allergische Reaktionen hervorrufen.");
        infoMap.put("Erdnüsse", "Erdnüsse sind eine häufige Ursache für allergische Reaktionen.");
        infoMap.put("Soja", "Soja ist in vielen Lebensmitteln enthalten und kann Allergien auslösen.");
        infoMap.put("Nüsse", "Baumnüsse wie Mandeln, Haselnüsse und Walnüsse können starke allergische Reaktionen hervorrufen.");
        infoMap.put("Sellerie", "Sellerieallergien sind in Europa weit verbreitet und können schwere Reaktionen auslösen.");
        infoMap.put("Senf", "Senf kann starke allergische Reaktionen hervorrufen und ist in vielen Gewürzmischungen enthalten.");
        infoMap.put("Sesam", "Sesam ist eine wachsende Allergiequelle und findet sich in Brot, Hummus und Ölen.");
        infoMap.put("Lupine", "Lupinenmehl wird in Backwaren verwendet und kann starke allergische Reaktionen auslösen.");
        infoMap.put("Sulfite", "Sulfite werden als Konservierungsstoffe in Wein, Trockenfrüchten und Fertiggerichten verwendet.");
        infoMap.put("Weichtiere", "Weichtiere wie Muscheln, Schnecken und Tintenfische können Allergien auslösen.");
        infoMap.put("Ofen", "Ein Ofen wird zum Backen und Garen bei hohen Temperaturen verwendet.");
        infoMap.put("Topf", "Ein Topf wird zum Kochen von Suppen, Saucen und anderen Gerichten genutzt.");
        infoMap.put("Pfanne", "Eine Pfanne eignet sich zum Braten von Fleisch, Gemüse und Pfannkuchen.");
        infoMap.put("Messer", "Ein Messer ist ein essenzielles Werkzeug zum Schneiden und Zerkleinern von Zutaten.");
        infoMap.put("Schäler", "Ein Schäler wird verwendet, um die Schale von Obst und Gemüse zu entfernen.");
        infoMap.put("Mixer", "Ein Mixer wird zum Pürieren und Mischen von Zutaten genutzt.");
        infoMap.put("Raffel", "Eine Raffel wird zum Reiben von Käse, Gemüse und anderen Zutaten verwendet.");
        infoMap.put("Nudelholz", "Ein Nudelholz wird zum Ausrollen von Teig für Pizza, Kekse und Gebäck verwendet.");
        infoMap.put("Waage", "Eine Waage wird verwendet, um Zutaten genau abzumessen.");
        return infoMap.getOrDefault(name, "Keine weiteren Informationen verfügbar.");
    }

    private void shareRecipe() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View shareView = inflater.inflate(R.layout.share_recipe_layout, null);

        ImageView recipeImage = shareView.findViewById(R.id.share_recipe_image);
        TextView recipeName = shareView.findViewById(R.id.share_recipe_name);
        TextView recipeDescription = shareView.findViewById(R.id.share_recipe_description);
        TextView recipePreparation = shareView.findViewById(R.id.share_recipe_preparation);
        LinearLayout toolsIconsLayout = shareView.findViewById(R.id.share_tools_icons);
        LinearLayout allergenIconsLayout = shareView.findViewById(R.id.share_allergen_icons);

        recipeName.setText(currentRecipe.getName());
        recipeDescription.setText("Beschreibung:\n" + currentRecipe.getIngredients());
        recipePreparation.setText("Zubereitung:\n" + currentRecipe.getInstructions());

        Glide.with(this)
                .load(currentRecipe.getImageUri() == null || currentRecipe.getImageUri().isEmpty()
                        ? R.drawable.ic_placeholder
                        : Uri.parse(currentRecipe.getImageUri()))
                .placeholder(R.drawable.ic_placeholder)
                .into(recipeImage);

        displayIcons(toolsIconsLayout, currentRecipe.getTools(), toolsIconMap);
        displayIcons(allergenIconsLayout, currentRecipe.getAllergens(), allergenIconMap);

        shareView.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        shareView.layout(0, 0, shareView.getMeasuredWidth(), shareView.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(shareView.getMeasuredWidth(), shareView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        shareView.draw(canvas);

        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            File file = new File(cachePath, "recipe_share.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(this, "com.example.rezepte.fileprovider", file);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("image/png");
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(Intent.createChooser(shareIntent, "Rezept teilen"));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Fehler beim Teilen des Rezepts", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteRecipe() {
        dbHelper.getWritableDatabase().delete(DatabaseHelper.TABLE_RECIPES,
                DatabaseHelper.COLUMN_NAME + "=?", new String[]{ currentRecipe.getName() });
        Toast.makeText(this, currentRecipe.getName() + " wurde gelöscht.", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void editRecipe() {
        Intent intent = new Intent(this, EditRecipeActivity.class);
        intent.putExtra("recipe", currentRecipe);
        startActivity(intent);
    }
}
