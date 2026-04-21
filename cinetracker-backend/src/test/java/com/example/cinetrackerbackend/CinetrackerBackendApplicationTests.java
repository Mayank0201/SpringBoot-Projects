package com.example.cinetrackerbackend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "spring.datasource.url=jdbc:h2:mem:cinetracker_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.cache.type=none",
    "tmdb.api.key=test_key",
    "jwt.secret=test_secret_key_for_testing_only_minimum_32_characters_long",
    "spring.mail.host=smtp.gmail.com",
    "spring.mail.port=587",
    "spring.mail.username=test@test.com",
    "spring.mail.password=test_password"
})
class CinetrackerBackendApplicationTests {

    @Test
    void contextLoads() {
    }

}
