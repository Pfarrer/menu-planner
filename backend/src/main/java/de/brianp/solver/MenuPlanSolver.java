package de.brianp.solver;

import de.brianp.domain.MenuPlan;
import org.optaplanner.core.api.solver.SolverManager;
import org.optaplanner.core.api.solver.SolverFactory;
import org.springframework.stereotype.Service;

@Service
public class MenuPlanSolver {

    private final SolverManager<MenuPlan, Long> solverManager;

    public MenuPlanSolver(SolverManager<MenuPlan, Long> solverManager) {
        this.solverManager = solverManager;
    }

    public void solve(Long problemId, MenuPlan problem) {
        solverManager.solve(problemId, problem);
    }
}