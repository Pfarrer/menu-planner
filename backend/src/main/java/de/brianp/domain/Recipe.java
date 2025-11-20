package de.brianp.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class Recipe {

    @EqualsAndHashCode.Include
    private String id;
    
    @ToString.Include
    private String name;
    private int prepTimeMinutes;
    private String difficulty;
    private String cuisine;
}
