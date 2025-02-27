package com.example.rezepte;

import java.io.Serializable;

public class Recipe implements Serializable {
    private final String name;
    private final String ingredients;
    private final String instructions;
    private final String imageUri;
    private final String allergens;
    private String tools;

    public Recipe(String name, String ingredients, String instructions, String imageUri, String allergens) {
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageUri = imageUri;
        this.allergens = allergens;
        this.tools = "";
    }

    // Konstruktor mit Tools
    public Recipe(String name, String ingredients, String instructions, String imageUri, String allergens, String tools) {
        this.name = name;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.imageUri = imageUri;
        this.allergens = allergens;
        this.tools = tools;
    }


    public String getName() {
        return name;
    }

    public String getIngredients() {
        return ingredients;
    }

    public String getInstructions() {
        return instructions;
    }

    public String getImageUri() {
        return imageUri;
    }

    public String getAllergens() {
        return allergens;
    }

    public String getTools() { // Neu: Getter für Tools
        return tools;
    }
    public void setTools(String tools) { // Neu: Setter für Tools (falls später änderbar)
        this.tools = tools;
    }
}
