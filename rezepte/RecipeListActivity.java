package com.example.rezepte;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;


import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class RecipeListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecipeAdapter adapter;
    private DatabaseHelper dbHelper;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_list);

        // Toolbar setzen
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

// DrawerLayout und NavigationView einrichten
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

// NavigationView holen
        NavigationView navigationView = findViewById(R.id.navigation_view);
        View headerView = navigationView.getHeaderView(0); // Header-View holen

// App-Logo aus dem Header holen und Klick-Listener setzen
        ImageView appLogo = headerView.findViewById(R.id.iv_app_logo);
        appLogo.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class); // Zur Startseite navigieren
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP); // Verhindert mehrfaches Öffnen
            startActivity(intent);
            drawerLayout.closeDrawer(GravityCompat.START); // Drawer nach Klick schließen
        });

            // RecyclerView initialisieren
            recyclerView = findViewById(R.id.recycler_view);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            // DatabaseHelper initialisieren
            dbHelper = new DatabaseHelper(this);

            // Beispielrezepte hinzufügen, falls die Datenbank leer ist
            if (isDatabaseEmpty()) {
                Log.d("RecipeListActivity", "Datenbank ist leer. Beispielrezepte werden hinzugefügt.");
                addSampleRecipes();
            } else {
                Log.d("RecipeListActivity", "Datenbank enthält bereits Daten.");
            }

            // Rezepte aus der Datenbank abrufen
            List<Recipe> recipeList = fetchRecipesFromDatabase();
            if (recipeList.isEmpty()) {
                Toast.makeText(this, "Keine Rezepte gefunden", Toast.LENGTH_SHORT).show();
            }

            // Adapter initialisieren und setzen
            adapter = new RecipeAdapter(this, recipeList);
            recyclerView.setAdapter(adapter);
        }

    private boolean isDatabaseEmpty() {
        Cursor cursor = dbHelper.getRecipes();
        boolean isEmpty = (cursor == null || cursor.getCount() == 0);
        if (cursor != null) {
            Log.d("DatabaseCheck", "Datenbank leer: " + isEmpty);
            cursor.close();
        }
        return isEmpty;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu_list, menu);

        // Initialisiere die SearchView
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    filterRecipes(query); // Implementiere die Filtermethode
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    filterRecipes(newText); // Live-Filterung
                    return true;
                }
            });
        } else {
            Toast.makeText(this, "SearchView konnte nicht geladen werden", Toast.LENGTH_SHORT).show();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == R.id.action_menu) {
            // Drawer-Menü öffnen
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        } else if (itemId == R.id.action_search) {
            // Such-Aktivität öffnen
            Intent intent = new Intent(this, SearchActivity.class);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_sort) {
            // Sortieroptionen anzeigen
            showSortOptions();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private List<Recipe> fetchRecipesFromDatabase() {
        List<Recipe> recipeList = new ArrayList<>();
        Cursor cursor = dbHelper.getRecipes();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
                String ingredients = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INGREDIENTS));
                String instructions = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INSTRUCTIONS));
                String imageUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI));
                String allergens = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ALLERGENS));
                String tools = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOOLS)); // Tools hinzufügen

                recipeList.add(new Recipe(name, ingredients, instructions, imageUri, allergens, tools));
            }
            cursor.close();
        }
        return recipeList;
    }

    private void setupDrawer(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_recipes) {
                startActivity(new Intent(this, RecipeListActivity.class));
            } else if (itemId == R.id.menu_impressum) {
                startActivity(new Intent(this, ImpressumActivity.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void filterRecipes(String query) {
        query = query.toLowerCase().trim();
        List<Recipe> filteredList = new ArrayList<>();
        for (Recipe recipe : adapter.getRecipeList()) {
            if (recipe.getName().toLowerCase().contains(query)) {
                filteredList.add(recipe);
            }
        }

        adapter.updateRecipeList(filteredList); // Aktualisiere Adapter mit gefilterter Liste
    }

    private void addSampleRecipes() {
        dbHelper.addRecipe(
                "Spaghetti Bolognese",
                "500g Spaghetti, 400g Hackfleisch, 1 Zwiebel, 2 Knoblauchzehen, 500ml Tomatensauce",
                "1. Spaghetti kochen.\n2. Hackfleisch anbraten.\n3. Zwiebeln und Knoblauch hinzufügen.\n4. Tomatensauce hinzufügen und köcheln lassen.",
                "https://images.unsplash.com/photo-1589308078055-92c3d84f6ba2",
                "Gluten",
                "Topf, Pfanne"
        );

        dbHelper.addRecipe(
                "Caesar Salad",
                "1 Römersalat, 50g Parmesan, Croutons, Caesar-Dressing",
                "1. Salat waschen.\n2. Parmesan hobeln.\n3. Mit Dressing und Croutons vermengen.",
                "https://images.unsplash.com/photo-1568051243859-06d3a9ec27e9",
                "Milch",
                "Messer"
        );

        dbHelper.addRecipe(
                "Caprese-Salat",
                "2 Tomaten, 200g Mozzarella, frischer Basilikum, Olivenöl, Balsamico",
                "1. Tomaten und Mozzarella in Scheiben schneiden.\n2. Basilikum darüberstreuen.\n3. Mit Olivenöl und Balsamico beträufeln.",
                "https://images.unsplash.com/photo-1613470661916-0ed24d65d9ec",
                "Milch",
                "Messer"
        );

        dbHelper.addRecipe(
                "Hamburger",
                "4 Hamburger-Brötchen, 500g Hackfleisch, 1 Zwiebel, 4 Scheiben Käse, Salat",
                "1. Hackfleisch formen und braten.\n2. Brötchen toasten.\n3. Mit Käse, Zwiebeln und Salat belegen.",
                "https://images.unsplash.com/photo-1550547660-d9450f859349",
                "Gluten, Milch",
                "Pfanne, Toaster"
        );

        dbHelper.addRecipe(
                "Margherita Pizza",
                "500g Pizzateig, 200g Tomatensauce, 200g Mozzarella, frischer Basilikum",
                "1. Pizzateig ausrollen.\n2. Mit Tomatensauce bestreichen und Mozzarella belegen.\n3. Im Ofen backen.",
                "https://images.unsplash.com/photo-1588731234156-2549c2e9b3c5",
                "Gluten, Milch",
                "Ofen"
        );

        dbHelper.addRecipe(
                "Sushi",
                "250g Sushi-Reis, 200g Lachs, Noriblätter, Sojasauce, Wasabi",
                "1. Reis kochen und abkühlen lassen.\n2. Noriblätter mit Reis und Lachs belegen.\n3. Rollen und in Stücke schneiden.",
                "https://images.unsplash.com/photo-1599140785984-0bfc3a508e48",
                "Fisch",
                "Messer, Bambusmatte"
        );

        dbHelper.addRecipe(
                "Vegetarisches Chili",
                "2 Paprika, 1 Zwiebel, 1 Dose Kidneybohnen, 1 Dose Mais, 500ml Tomatensauce",
                "1. Paprika und Zwiebeln anbraten.\n2. Bohnen, Mais und Tomatensauce hinzufügen.\n3. Würzen und köcheln lassen.",
                "https://images.unsplash.com/photo-1624483424177-d5f3e5a6a434",
                "",
                "Topf, Messer"
        );

        dbHelper.addRecipe(
                "French Toast",
                "4 Scheiben Toastbrot, 2 Eier, 200ml Milch, 50g Zucker, Zimt",
                "1. Eier, Milch und Zucker verquirlen.\n2. Toast darin tränken und in der Pfanne braten.",
                "https://images.unsplash.com/photo-1560807707-8cc77767d783",
                "Gluten, Milch, Eier",
                "Schneebesen, Pfanne"
        );

        dbHelper.addRecipe(
                "Guacamole",
                "2 Avocados, 1 Tomate, 1 Limette, Salz, Pfeffer",
                "1. Avocado zerdrücken.\n2. Tomatenwürfel und Limettensaft hinzufügen.\n3. Mit Salz und Pfeffer würzen.",
                "https://images.unsplash.com/photo-1572554422986-3bfe9e1e5ba4",
                "",
                "Messer, Gabel"
        );

        dbHelper.addRecipe(
                "Tomaten-Basilikum-Pasta",
                "250g Spaghetti, 200g Kirschtomaten, frischer Basilikum, Olivenöl",
                "1. Spaghetti kochen.\n2. Tomaten in Olivenöl anbraten.\n3. Mit Basilikum vermengen.",
                "https://images.unsplash.com/photo-1629152938545-c1d6451e6a82",
                "Gluten",
                "Topf, Pfanne"
        );

        dbHelper.addRecipe(
                "Minestrone",
                "2 Karotten, 2 Zucchini, 1 Dose Bohnen, 1L Brühe, 100g Pasta",
                "1. Gemüse schneiden und in Brühe kochen.\n2. Pasta hinzufügen und garen.",
                "https://images.unsplash.com/photo-1615486176190-0ac53a75f8bc",
                "Gluten",
                "Topf, Messer"
        );

        dbHelper.addRecipe(
                "Tiramisu",
                "200g Löffelbiskuits, 500g Mascarpone, 200ml Kaffee, Kakao",
                "1. Löffelbiskuits in Kaffee tauchen.\n2. Schichten mit Mascarpone und Kakao.",
                "https://images.unsplash.com/photo-1627483263811-1c37c260ed85",
                "Milch, Eier, Gluten",
                "Mixer"
        );

        dbHelper.addRecipe(
                "Smoothie Bowl",
                "1 Banane, 200g Beeren, 100ml Kokosmilch, Toppings nach Wahl",
                "1. Banane, Beeren und Kokosmilch mixen.\n2. In einer Schüssel servieren und Toppings hinzufügen.",
                "https://images.unsplash.com/photo-1583395598535-2f89444a3c1c",
                "",
                "Mixer"
        );

        dbHelper.addRecipe(
                "Ratatouille",
                "1 Aubergine, 1 Zucchini, 2 Paprika, 1 Dose Tomaten",
                "1. Gemüse schneiden und in der Pfanne anbraten.\n2. Mit Tomaten köcheln lassen.",
                "https://images.unsplash.com/photo-1551206344-5485d090ea76",
                "",
                "Pfanne, Messer"
        );

        dbHelper.addRecipe(
                "Brownies",
                "200g Schokolade, 150g Butter, 200g Zucker, 3 Eier, 150g Mehl",
                "1. Schokolade und Butter schmelzen.\n2. Zutaten mischen und im Ofen backen.",
                "https://images.unsplash.com/photo-1573242084344-faa8c3cdb090",
                "Gluten, Eier, Milch",
                "Ofen, Schneebesen"
        );

        dbHelper.addRecipe(
                "Hummus",
                "1 Dose Kichererbsen, 2 EL Tahini, 1 Knoblauchzehe, Zitronensaft, Olivenöl",
                "1. Alle Zutaten mixen, bis eine cremige Masse entsteht.",
                "https://images.unsplash.com/photo-1592089023941-5c1c9d24d7b2",
                "",
                "Mixer"
        );

        dbHelper.addRecipe(
                "Paella",
                "300g Reis, 200g Meeresfrüchte, 1 Paprika, 1 Zwiebel, Safran",
                "1. Reis mit Safran kochen.\n2. Gemüse und Meeresfrüchte anbraten.\n3. Alles vermengen und köcheln lassen.",
                "https://images.unsplash.com/photo-1556012018-58d92cccd1f3",
                "Fisch",
                "Pfanne, Topf"
        );

        dbHelper.addRecipe(
                "Chicken Wings",
                "500g Hähnchenflügel, BBQ-Sauce, Paprikapulver, Knoblauchpulver",
                "1. Hähnchenflügel würzen.\n2. Mit BBQ-Sauce marinieren.\n3. Im Ofen backen.",
                "https://images.unsplash.com/photo-1627308595229-7830a5c91f9f",
                "",
                "Ofen"
        );

        dbHelper.addRecipe(
                "Gemüse-Wrap",
                "4 Tortilla-Wraps, 1 Avocado, 1 Paprika, 1 Karotte, Hummus",
                "1. Gemüse schneiden.\n2. Wraps mit Hummus bestreichen und Gemüse füllen.\n3. Zusammenrollen.",
                "https://images.unsplash.com/photo-1550547660-d9450f859349",
                "Gluten",
                "Messer"
        );

        dbHelper.addRecipe(
                "Bananenbrot",
                "3 reife Bananen, 200g Mehl, 100g Zucker, 1 Ei, 1 TL Backpulver",
                "1. Bananen zerdrücken.\n2. Alle Zutaten mischen.\n3. Im Ofen backen.",
                "https://images.unsplash.com/photo-1589301760014-d929f3979dbc",
                "Gluten, Eier",
                "Schneebesen, Ofen"
        );

        dbHelper.addRecipe(
                "Thai Green Curry",
                "300g Hähnchen, 200ml Kokosmilch, 2 EL grüne Currypaste, Gemüse nach Wahl",
                "1. Currypaste mit Kokosmilch erhitzen.\n2. Hähnchen und Gemüse hinzufügen.\n3. Köcheln lassen.",
                "https://images.unsplash.com/photo-1627308599681-1eac99112d38",
                "",
                "Topf"
        );

        dbHelper.addRecipe(
                "Quiche Lorraine",
                "200g Blätterteig, 200g Speck, 3 Eier, 200ml Sahne, 100g Käse",
                "1. Blätterteig in eine Form legen.\n2. Mit Speck, Käse und Eier-Sahne-Mischung füllen.\n3. Im Ofen backen.",
                "https://images.unsplash.com/photo-1598908318706-b50d1b46ae11",
                "Gluten, Milch, Eier",
                "Ofen"
        );

        dbHelper.addRecipe(
                "Falafel",
                "200g Kichererbsen, 1 Zwiebel, Petersilie, Koriander, Mehl",
                "1. Kichererbsen und Gewürze pürieren.\n2. Zu Kugeln formen und frittieren.",
                "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38",
                "Gluten",
                "Mixer, Fritteuse"
        );

        dbHelper.addRecipe(
                "Eier Benedict",
                "4 Eier, 2 Toastscheiben, 200ml Hollandaise-Sauce, Schinken, Petersilie",
                "1. Eier pochieren.\n2. Toast mit Schinken und Sauce belegen.\n3. Eier darauf platzieren.",
                "https://images.unsplash.com/photo-1617187677832-0c53e9b4a947",
                "Gluten, Eier, Milch",
                "Topf"
        );

        dbHelper.addRecipe(
                "Pad Thai",
                "200g Reisnudeln, 200g Hähnchen, 100g Sojasprossen, 2 Eier, Pad Thai-Sauce",
                "1. Nudeln kochen.\n2. Hähnchen anbraten und mit Sauce vermengen.\n3. Mit Sojasprossen und Ei servieren.",
                "https://images.unsplash.com/photo-1598511726167-cfa8f1577a7f",
                "Eier",
                "Pfanne, Topf"
        );

        dbHelper.addRecipe(
                "Gazpacho",
                "5 Tomaten, 1 Gurke, 1 Paprika, 1 Knoblauchzehe, Olivenöl",
                "1. Gemüse pürieren.\n2. Mit Olivenöl, Salz und Pfeffer abschmecken.\n3. Kalt servieren.",
                "https://images.unsplash.com/photo-1598514982664-07937758f5e9",
                "",
                "Mixer"
        );

        dbHelper.addRecipe(
                "Shakshuka",
                "4 Eier, 1 Dose Tomaten, 1 Paprika, 1 Zwiebel, Gewürze",
                "1. Gemüse anbraten.\n2. Tomaten hinzufügen und köcheln lassen.\n3. Eier hineingeben und stocken lassen.",
                "https://images.unsplash.com/photo-1598511725913-0b7c1b6c7ec7",
                "Eier",
                "Pfanne"
        );

        dbHelper.addRecipe(
                "Capuccino-Kuchen",
                "200g Mehl, 150g Zucker, 2 TL Instant-Kaffee, 200ml Milch, 1 Ei",
                "1. Trockene Zutaten mischen.\n2. Milch und Ei hinzufügen.\n3. Im Ofen backen.",
                "https://images.unsplash.com/photo-1588731234032-f632ed3dbf4d",
                "Gluten, Eier, Milch",
                "Schneebesen, Ofen"
        );

        dbHelper.addRecipe(
                "Rührei mit Schnittlauch",
                "4 Eier, 50ml Milch, frischer Schnittlauch, Butter",
                "1. Eier und Milch verquirlen.\n2. In Butter anbraten und Schnittlauch hinzufügen.",
                "https://images.unsplash.com/photo-1580559346678-49e32cf40d21",
                "Eier, Milch",
                "Pfanne, Schneebesen"
        );

        dbHelper.addRecipe(
                "Couscous-Salat",
                "200g Couscous, 1 Gurke, 1 Paprika, Minze, Zitronensaft",
                "1. Couscous mit heißem Wasser aufgießen.\n2. Gemüse würfeln und hinzufügen.\n3. Mit Zitronensaft und Minze abschmecken.",
                "https://images.unsplash.com/photo-1565895405221-b4e7f7e3fba8",
                "",
                "Topf, Messer"
        );

// Wiederhole das Muster für weitere Rezepte


    }


    private void showSortOptions() {
        String[] sortOptions = {"Alphabetisch (Aufsteigend)", "Alphabetisch (Absteigend)"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sortieren nach")
                .setItems(sortOptions, (dialog, which) -> {
                    if (which == 0) {
                        sortRecipes(true); // Aufsteigend
                    } else {
                        sortRecipes(false); // Absteigend
                    }
                })
                .create()
                .show();
    }

    private void sortRecipes(boolean ascending) {
        List<Recipe> recipeList = adapter.getRecipeList(); // Zugriff auf aktuelle Liste

        if (ascending) {
            recipeList.sort((r1, r2) -> r1.getName().compareToIgnoreCase(r2.getName()));
        } else {
            recipeList.sort((r1, r2) -> r2.getName().compareToIgnoreCase(r1.getName()));
        }

        // Adapter benachrichtigen, dass sich die Daten geändert haben
        adapter.notifyDataSetChanged();

        String order = ascending ? "aufsteigend" : "absteigend";
        Toast.makeText(this, "Rezepte " + order + " sortiert", Toast.LENGTH_SHORT).show();
    }
}
