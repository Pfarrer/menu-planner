# Personal Menu Planner - AI Agents Configuration

## Project Overview
A Spring Boot service that plans personal or family menus using OptaPlanner for optimization. The service maintains a list of favorite dishes and generates menu proposals based on dinner slots while ensuring ingredient variety and no repetition.

## Technology Stack
- **Backend**: Java 25+, Spring Boot 3.x
- **Build Tool**: Maven
- **Optimization**: OptaPlanner
- **Database**: No database
- **Testing**: JUnit 5, Mockito

## Core Components

### 1. Domain Models
- `Dish`: Represents a meal with primary ingredients, cuisine type
- `MenuPlan`: Generated plan with scheduled dishes for dinner slots

### 2. OptaPlanner Integration
- **Planning Entity**: `MenuSlot` - represents a dinner time slot
- **Planning Variable**: `Dish` - assigned to each menu slot
- **Constraints**: 
  - No ingredient repetition within timeframe
  - Cuisine variety across the week

## Development Agents

### Backend Developer
**Focus**: Spring Boot service implementation, OptaPlanner integration
**Tasks**:
- Set up Spring Boot project structure with Maven
- Configure OptaPlanner solver and constraints
- Create REST controllers and service layer

### Optimization Specialist  
**Focus**: OptaPlanner constraint definition and tuning
**Tasks**:
- Define planning entities and variables
- Implement constraint streams for ingredient variety
- Optimize solver configuration for performance
- Create scoring functions for meal variety
- Test different solver parameters

### Frontend Developer (Future)
**Focus**: Web interface for menu management
**Tasks**:
- Create React/Vue.js frontend for dish management
- Build menu visualization and calendar view
- Implement preference management UI
- Add mobile-responsive design
- Integrate with backend APIs

## Getting Started

1. **Initialize Spring Boot Project**:
   ```bash
   mvn archetype:generate -DgroupId=de.brianp -DartifactId=menu-planner -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
   ```

2. **Add Dependencies** (pom.xml):
   - Spring Boot Starter Web
   - OptaPlanner Core

3. **Project Structure**:
   ```
   src/main/java/de/brianp/menu/planner
   ├── domain/
   │   ├── Dish.java
   │   └── MenuPlan.java
   ├── solver/
   │   ├── MenuPlanningSolution.java
   │   └── MenuConstraintProvider.java
   ├── service/
   │   ├── DishService.java
   │   └── MenuPlanningService.java
   └── controller/
       └── MenuController.java
   ```

## Next Steps
1. Set up basic Spring Boot project structure
2. Implement core domain models
3. Configure OptaPlanner integration
4. Create basic REST endpoints
5. Implement constraint definitions