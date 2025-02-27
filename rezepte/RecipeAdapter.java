package com.example.rezepte;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private final Context context;
    private final List<Recipe> recipeList;

    public RecipeAdapter(Context context, List<Recipe> recipeList) {
        this.context = context;
        this.recipeList = recipeList;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.recipeName.setText(recipe.getName());
        holder.recipeDescription.setText(recipe.getIngredients());

        // URI-String aus der Datenbank in Uri umwandeln
        String imageUriString = recipe.getImageUri();
        Uri imageUri = Uri.EMPTY; // Standardwert

        if (!TextUtils.isEmpty(imageUriString)) {
            try {
                imageUri = Uri.parse(imageUriString);
            } catch (Exception e) {
                e.printStackTrace();
                // Fehlerbehandlung: Logge den Fehler oder zeige ein Standardbild an
                imageUri = Uri.EMPTY; // Setze auf Standardwert, falls Parsing fehlschlägt
            }
        }

        // Bild laden (mit Platzhalter, falls URI leer oder ungültig)
        Glide.with(context)
                .load(imageUri)
                .placeholder(R.drawable.ic_placeholder) // Platzhalter, falls kein Bild
                .error(R.drawable.ic_placeholder) // Fehlerbild, falls URI ungültig
                .into(holder.recipeImage);

        // Klick-Listener für die Detailansicht
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, RecipeDetailActivity.class);
            intent.putExtra("recipe", recipe);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    // Methode zum Abrufen der aktuellen Rezeptliste
    public List<Recipe> getRecipeList() {
        return recipeList;
    }

    // Methode zum Aktualisieren der Rezeptliste
    public void updateRecipeList(List<Recipe> updatedList) {
        recipeList.clear();
        recipeList.addAll(updatedList);
        notifyDataSetChanged();
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {
        ImageView recipeImage;
        TextView recipeName;
        TextView recipeDescription;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            recipeImage = itemView.findViewById(R.id.recipe_image);
            recipeName = itemView.findViewById(R.id.recipe_name);
            recipeDescription = itemView.findViewById(R.id.recipe_description);
        }
    }
}
