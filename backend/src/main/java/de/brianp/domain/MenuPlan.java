package de.brianp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningScore;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.ProblemFactCollectionProperty;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

import java.util.List;

@PlanningSolution
@Data
@NoArgsConstructor
@AllArgsConstructor
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
}
