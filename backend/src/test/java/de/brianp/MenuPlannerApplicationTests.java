package de.brianp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = TestMenuPlannerApplication.class)
@ActiveProfiles("test")
class MenuPlannerApplicationTests {

    @Test
    void contextLoads() {
    }

}
