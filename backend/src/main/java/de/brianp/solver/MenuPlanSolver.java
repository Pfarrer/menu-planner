package de.brianp.solver;

import de.brianp.domain.MenuPlan;
import de.brianp.service.MenuCalendarService;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class MenuPlanSolver {

    private final SolverManager<MenuPlan, Long> solverManager;
    private MenuCalendarService menuCalendarService;

    public MenuPlanSolver(SolverManager<MenuPlan, Long> solverManager) {
        this.solverManager = solverManager;
    }

    @Autowired(required = false)
    public void setMenuCalendarService(MenuCalendarService menuCalendarService) {
        this.menuCalendarService = menuCalendarService;
    }

    public CompletableFuture<MenuPlan> solve(Long problemId, MenuPlan problem) {
        try {
            return CompletableFuture.completedFuture(solverManager.solve(problemId, problem).getFinalBestSolution());
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Solve and optionally sync to calendar
     */
    public CompletableFuture<MenuPlanSolveResult> solveAndSync(Long problemId, MenuPlan problem, boolean syncToCalendar,
            OAuth2AuthenticationToken authentication) {
        return solve(problemId, problem).thenApply(solution -> {
            MenuCalendarService.MenuCalendarSyncResult syncResult = null;

            if (syncToCalendar && menuCalendarService != null) {
                syncResult = menuCalendarService.syncMenuToCalendar(solution, authentication);
            }

            return new MenuPlanSolveResult(solution, syncResult);
        });
    }

    /**
     * Result class for solve operations
     */
    @lombok.Data
    @lombok.AllArgsConstructor
    public static class MenuPlanSolveResult {
        private final MenuPlan menuPlan;
        private final MenuCalendarService.MenuCalendarSyncResult calendarSyncResult;
    }
}
