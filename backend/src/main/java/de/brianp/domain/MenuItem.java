package de.brianp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.optaplanner.core.api.domain.entity.PlanningEntity;
import org.optaplanner.core.api.domain.variable.PlanningVariable;

@PlanningEntity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    private String id;
    private String day;
    private String mealType;

    @PlanningVariable(valueRangeProviderRefs = "recipeRange")
    private Recipe recipe;
}
