package de.brianp.domain;

import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@PlanningSolution
public class MenuPlan {

    @PlanningEntityCollectionProperty
    private List<MenuItem> menuItems;
    
    @ProblemFactCollectionProperty
    private List<Recipe> availableRecipes;
    
    @ValueRangeProvider(id = "recipeRange")
    public List<Recipe> getRecipeRange() {
        return availableRecipes;
    }
    
    @PlanningScore
    private HardSoftScore score;

    public MenuPlan() {
    }

    public MenuPlan(List<MenuItem> menuItems, List<Recipe> availableRecipes) {
        this.menuItems = menuItems;
        this.availableRecipes = availableRecipes;
    }

    public List<MenuItem> getMenuItems() {
        return menuItems;
    }

    public void setMenuItems(List<MenuItem> menuItems) {
        this.menuItems = menuItems;
    }

    public List<Recipe> getAvailableRecipes() {
        return availableRecipes;
    }

    public void setAvailableRecipes(List<Recipe> availableRecipes) {
        this.availableRecipes = availableRecipes;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }
}