package de.brianp.solver;

import de.brianp.domain.MenuPlan;
import de.brianp.service.MenuCalendarService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
public class MenuPlanSolver {

    private final SolverManager<MenuPlan, Long> solverManager;
    private MenuCalendarService menuCalendarService;

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
    @Data
    @AllArgsConstructor
    public static class MenuPlanSolveResult {
        private MenuPlan menuPlan;
        private MenuCalendarService.MenuCalendarSyncResult calendarSyncResult;
    }
}
