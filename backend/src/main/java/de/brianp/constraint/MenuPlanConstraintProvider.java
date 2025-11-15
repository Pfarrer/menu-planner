package de.brianp.constraint;

import de.brianp.domain.MenuItem;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.api.score.stream.Constraint;
import org.optaplanner.core.api.score.stream.ConstraintFactory;
import org.optaplanner.core.api.score.stream.ConstraintProvider;

public class MenuPlanConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory constraintFactory) {
        return new Constraint[]{
                // No duplicate recipes on the same day
                duplicateRecipeOnSameDay(constraintFactory),
                // Prefer shorter prep times (soft score)
                minimizePrepTime(constraintFactory)
        };
    }

    private Constraint duplicateRecipeOnSameDay(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(MenuItem.class)
                .groupBy(MenuItem::getDay, MenuItem::getRecipe)
                .filter((day, recipe) -> recipe != null)
                .penalize("Duplicate recipe on same day", HardSoftScore.ONE_HARD, 
                        (day, recipe) -> 1);
    }

    private Constraint minimizePrepTime(ConstraintFactory constraintFactory) {
        return constraintFactory
                .forEach(MenuItem.class)
                .penalize("Minimize prep time", HardSoftScore.ONE_SOFT, 
                        menuItem -> menuItem.getRecipe() != null ? menuItem.getRecipe().getPrepTimeMinutes() : 0);
    }
}