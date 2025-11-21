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

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class MenuPlanController {

    private final MenuPlanSolver menuPlanSolver;
    private final AtomicLong problemIdCounter = new AtomicLong(0);

    /**
     * Get current menu plan (datastar compatible)
     */
    @GetMapping("/current")
    public void getCurrentMenuPlan(HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        
        // Mock current menu plan
        String menuHtml = """
            <div style="background: white; padding: 20px; border-radius: 8px; margin-top: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                <h3 style="color: #333; margin-bottom: 15px;">🍽️ Current Menu Plan</h3>
                <div style="display: grid; gap: 10px;">
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #4285f4; border-radius: 4px;">
                        <strong>Monday:</strong> Spaghetti Carbonara
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #34a853; border-radius: 4px;">
                        <strong>Tuesday:</strong> Grilled Chicken Salad
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #fbbc04; border-radius: 4px;">
                        <strong>Wednesday:</strong> Vegetable Stir Fry
                    </div>
                </div>
                <p style="margin-top: 15px; color: #666; font-size: 14px;">
                    Last updated: %s
                </p>
            </div>
            """.formatted(LocalDate.now().minusDays(1));
        
        sendSseEvent(response, "datastar-merge-fragments", 
            "{\"fragments\":[{\"selector\":\"#menu-display\",\"html\":\"" + escapeJson(menuHtml) + "\"}]}");
    }

    /**
     * Generate new menu plan (datastar compatible)
     */
    @PostMapping("/plan")
    public void generateMenuPlan(@RequestBody(required = false) Map<String, Object> request, HttpServletResponse response) throws IOException {
        response.setContentType("text/event-stream");
        
        // Send loading state
        sendSseEvent(response, "datastar-merge-signals", "{\"loading\": true}");
        
        // Simulate processing time
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Generate new menu plan
        String menuHtml = """
            <div style="background: white; padding: 20px; border-radius: 8px; margin-top: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
                <h3 style="color: #333; margin-bottom: 15px;">🍽️ New Menu Plan Generated</h3>
                <div style="display: grid; gap: 10px;">
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #4285f4; border-radius: 4px;">
                        <strong>Monday:</strong> Beef Tacos
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #34a853; border-radius: 4px;">
                        <strong>Tuesday:</strong> Homemade Pizza
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #fbbc04; border-radius: 4px;">
                        <strong>Wednesday:</strong> Grilled Salmon
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #ea4335; border-radius: 4px;">
                        <strong>Thursday:</strong> Chicken Curry
                    </div>
                    <div style="padding: 10px; background: #f8f9fa; border-left: 4px solid #9333ea; border-radius: 4px;">
                        <strong>Friday:</strong> Pasta Primavera
                    </div>
                </div>
                <p style="margin-top: 15px; color: #666; font-size: 14px;">
                    Generated on %s with OptaPlanner optimization
                </p>
            </div>
            """.formatted(LocalDate.now());
        
        sendSseEvent(response, "datastar-merge-fragments", 
            "{\"fragments\":[{\"selector\":\"#menu-display\",\"html\":\"" + escapeJson(menuHtml) + "\"}]}");
        
        // Clear loading state
        sendSseEvent(response, "datastar-merge-signals", "{\"loading\": false}");
    }

    private void sendSseEvent(HttpServletResponse response, String event, String data) throws IOException {
        response.getWriter().write("event: " + event + "\n");
        response.getWriter().write("data: " + data + "\n\n");
        response.getWriter().flush();
    }

    private String escapeJson(String str) {
        return str.replace("\\", "\\\\")
                 .replace("\"", "\\\"")
                 .replace("\n", "\\n")
                 .replace("\r", "\\r")
                 .replace("\t", "\\t");
    }

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
                .map(itemRequest -> {
                    MenuItem menuItem = new MenuItem();
                    menuItem.setId(itemRequest.getId());
                    menuItem.setDay(itemRequest.getDay());
                    menuItem.setMealType(itemRequest.getMealType());
                    return menuItem;
                })
                .toList();

        // Convert recipes
        List<Recipe> recipes = request.getAvailableRecipes().stream()
                .map(recipeRequest -> new Recipe(recipeRequest.getId(), recipeRequest.getName(),
                        recipeRequest.getPrepTimeMinutes(), recipeRequest.getDifficulty(), recipeRequest.getCuisine()))
                .toList();

        return new MenuPlan(menuItems, recipes, null);
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
