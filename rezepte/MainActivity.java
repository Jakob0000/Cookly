package com.example.rezepte;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private DatabaseHelper dbHelper;
    private RecyclerView recyclerView;
    private RecipeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Entferne Standardtitel
        toolbar.setNavigationIcon(R.drawable.ic_menu); // Setze dein Menü-Icon
        toolbar.setNavigationOnClickListener(v -> toggleDrawer());

        // Navigation Drawer
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        setupDrawer(navigationView);

        // Floating Action Button
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> openAddRecipeActivity());

        // RecyclerView für Random-Rezepte
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)); // Diagonal versetzt

        dbHelper = new DatabaseHelper(this);

        // Lade Rezepte in zufälliger Reihenfolge
        loadRandomRecipes();
    }

    private void toggleDrawer() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
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

    private void openAddRecipeActivity() {
        Intent intent = new Intent(MainActivity.this, AddRecipeActivity.class);
        startActivity(intent);
    }

    private void loadRandomRecipes() {
        List<Recipe> recipeList = dbHelper.getAllRecipes();
        if (recipeList.isEmpty()) {
            Toast.makeText(this, "Keine Rezepte gefunden", Toast.LENGTH_SHORT).show();
            return;
        }

        // Zufällige Reihenfolge der Rezepte
        List<Recipe> shuffledList = new ArrayList<>(recipeList);
        Collections.shuffle(shuffledList);

        // RecyclerView mit Adapter verbinden
        adapter = new RecipeAdapter(this, shuffledList);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) { // Menü
            toggleDrawer();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
