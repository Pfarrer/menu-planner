package de.brianp.config;

import de.brianp.domain.MenuPlan;
import de.brianp.solver.MenuPlanSolver;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.CompletableFuture;

@TestConfiguration
public class MenuPlannerTestConfiguration {

    @Bean
    @Primary
    public MenuPlanSolver mockMenuPlanSolver() {
        return new MenuPlanSolver(null) {
            @Override
            public CompletableFuture<MenuPlan> solve(Long problemId, MenuPlan problem) {
                // Mock implementation - returns empty menu plan for tests
                return CompletableFuture.completedFuture(new MenuPlan(null, null));
            }
        };
    }
}
