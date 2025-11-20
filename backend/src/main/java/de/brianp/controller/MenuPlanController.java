package de.brianp.controller;

import de.brianp.domain.MenuPlan;
import de.brianp.domain.MenuItem;
import de.brianp.domain.Recipe;
import de.brianp.solver.MenuPlanSolver;
import de.brianp.service.MenuCalendarService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/menu-plan")
@RequiredArgsConstructor
public class MenuPlanController {

    private final MenuPlanSolver menuPlanSolver;
    private final AtomicLong problemIdCounter = new AtomicLong(0);

    /**
     * Solve a menu planning problem
     */
    @PostMapping("/solve")
    public CompletableFuture<ResponseEntity<MenuPlanResponse>> solveMenuPlan(@RequestBody MenuPlanRequest request,
            @RequestParam(defaultValue = "false") boolean syncToCalendar,
            @AuthenticationPrincipal OAuth2AuthenticationToken authentication) {

        try {
            // Create menu plan from request
            MenuPlan menuPlan = createMenuPlanFromRequest(request);

            // Solve the problem
            Long problemId = problemIdCounter.incrementAndGet();
            return menuPlanSolver.solveAndSync(problemId, menuPlan, syncToCalendar, authentication)
                    .thenApply(result -> {
                        MenuPlanResponse response = new MenuPlanResponse(result.getMenuPlan(),
                                result.getCalendarSyncResult());
                        return ResponseEntity.ok(response);
                    }).exceptionally(throwable -> {
                        return ResponseEntity.<MenuPlanResponse> internalServerError().body(null);
                    });

        } catch (Exception e) {
            return CompletableFuture.completedFuture(ResponseEntity.<MenuPlanResponse> badRequest().body(null));
        }
    }

    /**
     * Get calendar events that might conflict with menu planning
     */
    @GetMapping("/conflicts")
    public ResponseEntity<?> getConflictingEvents(@RequestParam String startDate, @RequestParam String endDate) {

        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            // This would need to be injected or created
            // For now, return empty list
            return ResponseEntity.ok(List.of());

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid date format: " + e.getMessage());
        }
    }

    /**
     * Create MenuPlan from request
     */
    private MenuPlan createMenuPlanFromRequest(MenuPlanRequest request) {
        // Convert menu items
        List<MenuItem> menuItems = request.getMenuItems().stream()
                .map(itemRequest -> new MenuItem(itemRequest.getId(), itemRequest.getDay(), itemRequest.getMealType()))
                .toList();

        // Convert recipes
        List<Recipe> recipes = request.getAvailableRecipes().stream()
                .map(recipeRequest -> new Recipe(recipeRequest.getId(), recipeRequest.getName(),
                        recipeRequest.getPrepTimeMinutes(), recipeRequest.getDifficulty(), recipeRequest.getCuisine()))
                .toList();

        return new MenuPlan(menuItems, recipes);
    }

    // Request and Response DTOs
    @Data
    public static class MenuPlanRequest {
        private List<MenuItemRequest> menuItems;
        private List<RecipeRequest> availableRecipes;
    }

    @Data
    public static class MenuItemRequest {
        private String id;
        private String day;
        private String mealType;
    }

    @Data
    public static class RecipeRequest {
        private String id;
        private String name;
        private int prepTimeMinutes;
        private String difficulty;
        private String cuisine;
    }

    @Data
    @AllArgsConstructor
    public static class MenuPlanResponse {
        private MenuPlan menuPlan;
        private MenuCalendarService.MenuCalendarSyncResult calendarSyncResult;
    }
}
