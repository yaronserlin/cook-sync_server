package com.cooksync_server.config;

import com.cooksync_server.entities.*;
import com.cooksync_server.repositories.*;
import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Data Seeder that runs only when the 'seed' profile is active.
 * Clears all existing data and seeds the database with initial configurations, users, and sample recipes.
 */
@Component
@Profile("seed")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TagRepository tagRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    private Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Override
    @Transactional
    public void run(String... args) {
        logger.info(">>> Starting Database Reset and Seeding...");
        
        clearDatabase();
        seedUnits();
        seedTags();
        seedUsers();
        seedSampleRecipe();
        
        logger.info(">>> Database Reset and Seeding completed successfully.");
    }

    private void clearDatabase() {
        logger.info(">>> Clearing existing database data...");
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        
        String[] tables = {
            "users", "recipes", "units", "ingredients", "instructions", 
            "instruction_ingredients", "reviews", "favorite_recipes", 
            "personal_instruction_notes", "tags", "recipe_tags", "recipe_images"
        };
        
        for (String table : tables) {
            jdbcTemplate.execute("TRUNCATE TABLE " + table);
        }
        
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    private void seedUnits() {
        logger.info(">>> Seeding units...");
        unitRepository.saveAll(List.of(
            Unit.builder().name("Cup").code("cup").build(),
            Unit.builder().name("Tablespoon").code("tbsp").build(),
            Unit.builder().name("Teaspoon").code("tsp").build(),
            Unit.builder().name("Gram").code("g").build()
        ));
    }

    private void seedTags() {
        logger.info(">>> Seeding tags...");
        tagRepository.saveAll(List.of(
            Tag.builder().name("vegan").build(),
            Tag.builder().name("quick").build(),
            Tag.builder().name("healthy").build()
        ));
    }

    private void seedUsers() {
        logger.info(">>> Seeding users...");
        userRepository.saveAll(List.of(
            User.builder().name("Admin User").email("admin@cooksync.com")
                .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(true).build(),
            User.builder().name("Chef John").email("chef@cooksync.com")
                .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false).build()
        ));
    }

    private void seedSampleRecipe() {
        logger.info(">>> Seeding sample recipe...");
        User chef = userRepository.findByEmail("chef@cooksync.com").orElseThrow();
        
        Recipe recipe = Recipe.builder()
                .title("Simple Vegan Salad")
                .description("A fresh and quick healthy salad.")
                .difficulty(Recipe.Difficulty.EASY)
                .prepTimeMinutes(10)
                .cookTimeMinutes(0)
                .servings(2)
                .createdBy(chef)
                .build();

        Ingredient ing = Ingredient.builder()
                .name("Lettuce")
                .quantity(BigDecimal.valueOf(1))
                .unit(unitRepository.findAll().get(0))
                .recipe(recipe)
                .build();

        Instruction inst = Instruction.builder()
                .stepNumber(1)
                .description("Wash and chop the lettuce.")
                .hasTimer(false)
                .recipe(recipe)
                .build();

        recipe.setIngredients(java.util.Set.of(ing));
        recipe.setInstructions(List.of(inst));
        
        recipeRepository.save(recipe);
    }
}