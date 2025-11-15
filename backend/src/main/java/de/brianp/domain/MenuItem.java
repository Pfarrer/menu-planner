package de.brianp.domain;

import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class MenuItem {

    private String id;
    private String day;
    private String mealType;
    
    @PlanningVariable(valueRangeProviderRefs = "recipeRange")
    private Recipe recipe;

    public MenuItem() {
    }

    public MenuItem(String id, String day, String mealType) {
        this.id = id;
        this.day = day;
        this.mealType = mealType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getMealType() {
        return mealType;
    }

    public void setMealType(String mealType) {
        this.mealType = mealType;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}