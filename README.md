# Personal Menu Planner

A Spring Boot service that plans personal or family menus using OptaPlanner for optimization. The service maintains a list of favorite dishes and generates menu proposals based on dinner slots while ensuring ingredient variety and no repetition.

## Features

- **Menu Optimization**: Uses OptaPlanner to generate optimal meal plans
- **Ingredient Variety**: Prevents ingredient repetition within configurable timeframes
- **Cuisine Diversity**: Ensures variety across different cuisine types
- **Google Calendar Integration**: OAuth2 authentication and calendar event access
- **REST API**: Full REST interface for menu management
- **Zero Database**: In-memory solution with no external database dependencies

## Technology Stack

- **Backend**: Java 25+, Spring Boot 3.x
- **Build Tool**: Maven
- **Optimization**: OptaPlanner
- **Testing**: JUnit 5, Mockito

## Quick Start

### Prerequisites

- Java 25 or higher
- Maven 3.6+

### Installation

1. Clone the repository:
```bash
git clone <repository-url>
cd personal-menu-planner/backend
```

2. Configure Google OAuth2 credentials:
   - Create a Google OAuth2 client in the Google Cloud Console
   - Add your credentials to the `.env` file:
   ```
   export GOOGLE_CLIENT_ID="your-google-client-id"
   export GOOGLE_CLIENT_SECRET="your-google-client-secret"
   ```

3. Build the project:
```bash
mvn clean install
```

4. Run the application:
```bash
# Using the provided script (recommended)
./run-with-env.sh

# Or manually:
source .env && mvn spring-boot:run
```

The service will start on `http://localhost:8080`

### Google OAuth2 Setup

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select existing one
3. Enable Google Calendar API
4. Create OAuth2 credentials:
   - Go to APIs & Services → Credentials
   - Click "Create Credentials" → "OAuth 2.0 Client IDs"
   - Select "Web application"
   - Add authorized redirect URI: `http://localhost:8080/login/oauth2/code/google`
5. Copy the Client ID and Client Secret to your `.env` file

## API Endpoints

### Menu Planning
- `POST /api/menu/plan` - Generate a new menu plan
- `GET /api/menu/current` - Get current menu plan
- `GET /api/menu/history` - Get menu planning history

### Dish Management
- `GET /api/dishes` - List all available dishes
- `POST /api/dishes` - Add a new dish
- `PUT /api/dishes/{id}` - Update existing dish
- `DELETE /api/dishes/{id}` - Remove a dish

## Project Structure

```
src/main/java/de/brianp/menu/planner
├── domain/
│   ├── Dish.java          # Meal representation
│   └── MenuPlan.java       # Generated menu plan
├── solver/
│   ├── MenuPlanningSolution.java  # OptaPlanner solution
│   └── MenuConstraintProvider.java # Constraint definitions
├── service/
│   ├── DishService.java    # Dish management logic
│   └── MenuPlanningService.java # Menu planning logic
└── controller/
    └── MenuController.java # REST API endpoints
```

## Configuration

The application can be configured through `application.properties`:

```properties
# Solver configuration
menu.planner.solver.time-spent-limit=5s
menu.planner.variety.days=7
menu.planner.max.dishes.per.day=1
```

## Development

### Running Tests
```bash
mvn test
```

### Building for Production
```bash
mvn clean package -Pprod
```

## How It Works

1. **Input**: User provides a list of favorite dishes with ingredients and cuisine types
2. **Planning**: OptaPlanner assigns dishes to dinner slots while respecting constraints
3. **Output**: Optimized menu plan with maximum variety and no repetition

### Constraints

- No ingredient repetition within 7 days
- Minimum cuisine variety across the week
- User preferences and dietary restrictions
- Seasonal ingredient availability

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.