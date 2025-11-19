package de.brianp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Recipe {

    @EqualsAndHashCode.Include
    private String id;
    private String name;
    private int prepTimeMinutes;
    private String difficulty;
    private String cuisine;

    @Override
    public String toString() {
        return name;
    }
}
