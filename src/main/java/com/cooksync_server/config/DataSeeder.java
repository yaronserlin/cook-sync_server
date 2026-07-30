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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Data Seeder that runs only when the 'seed' profile is active. Clears all
 * existing data and seeds the database with realistic, connected sample data.
 */
@Component
@Profile("seed")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final TagRepository tagRepository;
    private final UnitRepository unitRepository;
    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final ReviewRepository reviewRepository;
    private final FavoriteRecipeRepository favoriteRecipeRepository;
    private final PersonalInstructionNoteRepository personalInstructionNoteRepository;
    private final RecipeImageRepository recipeImageRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    private final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Override
    @Transactional
    public void run(String... args) {
        logger.info(">>> Starting database reset and seeding...");

        clearDatabase();
        List<Unit> units = seedUnits();
        List<Tag> tags = seedTags();
        List<User> users = seedUsers();
        List<Recipe> recipes = seedRecipes(users, units, tags);
        seedReviews(recipes, users);
        seedFavorites(recipes, users);
        seedPersonalNotes(recipes, users);
        seedRecipeImages(recipes);

        logger.info(">>> Database reset and seeding completed successfully.");
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

    private List<Unit> seedUnits() {
        logger.info(">>> Seeding units...");
        return unitRepository.saveAll(List.of(
                Unit.builder().name("Cup").code("cup").build(),
                Unit.builder().name("Tablespoon").code("tbsp").build(),
                Unit.builder().name("Teaspoon").code("tsp").build(),
                Unit.builder().name("Gram").code("g").build(),
                Unit.builder().name("Kilogram").code("kg").build(),
                Unit.builder().name("Milliliter").code("ml").build(),
                Unit.builder().name("Liter").code("l").build(),
                Unit.builder().name("Pinch").code("pinch").build(),
                Unit.builder().name("Clove").code("clove").build(),
                Unit.builder().name("Piece").code("piece").build()
        ));
    }

    private List<Tag> seedTags() {
        logger.info(">>> Seeding tags...");
        return tagRepository.saveAll(List.of(
                Tag.builder().name("vegan").build(),
                Tag.builder().name("quick").build(),
                Tag.builder().name("healthy").build(),
                Tag.builder().name("breakfast").build(),
                Tag.builder().name("dinner").build(),
                Tag.builder().name("dessert").build(),
                Tag.builder().name("gluten-free").build(),
                Tag.builder().name("high-protein").build(),
                Tag.builder().name("comfort-food").build(),
                Tag.builder().name("spicy").build()
        ));
    }

    private List<User> seedUsers() {
        logger.info(">>> Seeding users...");
        return userRepository.saveAll(List.of(
                User.builder().firstName("Admin").lastName("User").email("admin@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(true)
                        .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Chef").lastName("John").email("chef@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Maya").lastName("Levi").email("maya@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Noam").lastName("Cohen").email("noam@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Sara").lastName("Green").email("sara@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Eli").lastName("Sharon").email("eli@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                        .build()
        ));
    }

    private List<Recipe> seedRecipes(List<User> users, List<Unit> units, List<Tag> tags) {
        logger.info(">>> Seeding recipes, ingredients, instructions, and tags...");

        User chef = users.stream().filter(User::isAdmin).findFirst().orElse(users.get(1));
        User regularUser = users.stream().filter(user -> !user.isAdmin()).findFirst().orElse(users.get(1));

        Recipe salad = createRecipe(
                "Simple Vegan Salad",
                "A fresh and quick healthy salad that works for lunch or dinner.",
                Recipe.Difficulty.EASY,
                10,
                0,
                2,
                chef,
                List.of(tags.get(0), tags.get(1), tags.get(2)),
                List.of(
                        createIngredient("Lettuce", BigDecimal.valueOf(1), units.get(0), null),
                        createIngredient("Tomato", BigDecimal.valueOf(2), units.get(9), null),
                        createIngredient("Cucumber", BigDecimal.valueOf(1), units.get(9), null)
                ),
                List.of(
                        createInstruction(1, "Wash and chop the vegetables.", false, null, List.of()),
                        createInstruction(2, "Toss with olive oil and lemon juice.", false, null, List.of())
                )
        );

        Recipe pasta = createRecipe(
                "Creamy Tomato Pasta",
                "A comforting pasta dish with a silky tomato sauce and herbs.",
                Recipe.Difficulty.MEDIUM,
                15,
                20,
                4,
                regularUser,
                List.of(tags.get(4), tags.get(8), tags.get(1)),
                List.of(
                        createIngredient("Pasta", BigDecimal.valueOf(250), units.get(3), null),
                        createIngredient("Tomato sauce", BigDecimal.valueOf(2), units.get(0), null),
                        createIngredient("Garlic", BigDecimal.valueOf(2), units.get(8), null)
                ),
                List.of(
                        createInstruction(1, "Boil the pasta until al dente.", false, null, List.of()),
                        createInstruction(2, "Simmer the sauce with garlic and herbs.", false, null, List.of()),
                        createInstruction(3, "Combine and serve warm.", false, null, List.of())
                )
        );

        Recipe pancakes = createRecipe(
                "Fluffy Banana Pancakes",
                "Soft pancakes with ripe bananas and a hint of cinnamon.",
                Recipe.Difficulty.MEDIUM,
                10,
                15,
                3,
                regularUser,
                List.of(tags.get(5), tags.get(1), tags.get(3)),
                List.of(
                        createIngredient("Banana", BigDecimal.valueOf(2), units.get(9), null),
                        createIngredient("Flour", BigDecimal.valueOf(200), units.get(3), null),
                        createIngredient("Egg", BigDecimal.valueOf(2), units.get(9), null)
                ),
                List.of(
                        createInstruction(1, "Mash the bananas until smooth.", false, null, List.of()),
                        createInstruction(2, "Mix in flour and eggs to make a batter.", false, null, List.of()),
                        createInstruction(3, "Cook each side until golden.", true, 180, List.of())
                )
        );

        Recipe bowl = createRecipe(
                "Spicy Chickpea Bowl",
                "A bold bowl filled with roasted chickpeas, rice, and avocado.",
                Recipe.Difficulty.EASY,
                15,
                20,
                2,
                users.get(3),
                List.of(tags.get(0), tags.get(9), tags.get(7)),
                List.of(
                        createIngredient("Chickpeas", BigDecimal.valueOf(400), units.get(3), null),
                        createIngredient("Rice", BigDecimal.valueOf(1), units.get(0), null),
                        createIngredient("Avocado", BigDecimal.valueOf(1), units.get(9), null)
                ),
                List.of(
                        createInstruction(1, "Roast the chickpeas with spices.", true, 600, List.of()),
                        createInstruction(2, "Cook the rice and slice the avocado.", false, null, List.of()),
                        createInstruction(3, "Assemble everything in a bowl.", false, null, List.of())
                )
        );

        Recipe soup = createRecipe(
                "Golden Lentil Soup",
                "A cozy, nutritious soup with carrots, celery, and lentils.",
                Recipe.Difficulty.EASY,
                15,
                35,
                4,
                users.get(4),
                List.of(tags.get(2), tags.get(4), tags.get(8)),
                List.of(
                        createIngredient("Lentils", BigDecimal.valueOf(300), units.get(3), null),
                        createIngredient("Carrot", BigDecimal.valueOf(2), units.get(9), null),
                        createIngredient("Celery", BigDecimal.valueOf(2), units.get(9), null)
                ),
                List.of(
                        createInstruction(1, "Saute the vegetables until softened.", false, null, List.of()),
                        createInstruction(2, "Add lentils and broth, then simmer.", true, 1800, List.of()),
                        createInstruction(3, "Blend partly for a thicker texture.", false, null, List.of())
                )
        );

        Recipe dessert = createRecipe(
                "Berry Yogurt Parfait",
                "A simple dessert with layered yogurt, berries, and granola.",
                Recipe.Difficulty.EASY,
                5,
                0,
                2,
                users.get(5),
                List.of(tags.get(5), tags.get(2), tags.get(1)),
                List.of(
                        createIngredient("Yogurt", BigDecimal.valueOf(250), units.get(3), null),
                        createIngredient("Mixed berries", BigDecimal.valueOf(150), units.get(3), null),
                        createIngredient("Granola", BigDecimal.valueOf(100), units.get(3), null)
                ),
                List.of(
                        createInstruction(1, "Layer yogurt, berries, and granola in glasses.", false, null, List.of()),
                        createInstruction(2, "Chill briefly before serving.", false, null, List.of())
                )
        );

        return recipeRepository.saveAll(List.of(salad, pasta, pancakes, bowl, soup, dessert));
    }

    private Recipe createRecipe(String title, String description, Recipe.Difficulty difficulty, int prepTime,
                                int cookTime, int servings, User creator, List<Tag> tags,
                                List<Ingredient> ingredients, List<Instruction> instructions) {
        Recipe recipe = Recipe.builder()
                .title(title)
                .description(description)
                .difficulty(difficulty)
                .prepTimeMinutes(prepTime)
                .cookTimeMinutes(cookTime)
                .servings(servings)
                .createdBy(creator)
                .build();

        recipe.setTags(new ArrayList<>(tags));

        Set<Ingredient> ingredientSet = new LinkedHashSet<>();
        for (Ingredient ingredient : ingredients) {
            ingredient.setRecipe(recipe);
            ingredientSet.add(ingredient);
        }
        recipe.setIngredients(ingredientSet);

        List<Instruction> instructionList = new ArrayList<>();
        for (Instruction instruction : instructions) {
            instruction.setRecipe(recipe);
            instructionList.add(instruction);
        }
        recipe.setInstructions(instructionList);

        return recipe;
    }

    private Ingredient createIngredient(String name, BigDecimal quantity, Unit unit, Recipe recipe) {
        Ingredient ingredient = Ingredient.builder()
                .name(name)
                .quantity(quantity)
                .unit(unit)
                .recipe(recipe)
                .build();
        return ingredient;
    }

    private Instruction createInstruction(int stepNumber, String description, boolean hasTimer, Integer timeSeconds,
                                         List<Ingredient> linkedIngredients) {
        Instruction instruction = Instruction.builder()
                .stepNumber(stepNumber)
                .description(description)
                .hasTimer(hasTimer)
                .timeSeconds(timeSeconds)
                .ingredients(new LinkedHashSet<>(linkedIngredients))
                .build();
        return instruction;
    }

    private void seedReviews(List<Recipe> recipes, List<User> users) {
        logger.info(">>> Seeding reviews...");
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            User reviewer = users.get((i % (users.size() - 1)) + 1);
            reviews.add(Review.builder()
                    .recipe(recipe)
                    .user(reviewer)
                    .rating(BigDecimal.valueOf(4.5 + (i % 3) * 0.5))
                    .title("Great recipe")
                    .comment("This was easy to follow and turned out really well.")
                    .build());
        }
        reviewRepository.saveAll(reviews);

        for (Recipe recipe : recipes) {
            recipe.setReviewCount((int) reviewRepository.findByRecipeIdOrderByCreatedAtDesc(recipe.getId()).size());
        }
        recipeRepository.saveAll(recipes);
    }

    private void seedFavorites(List<Recipe> recipes, List<User> users) {
        logger.info(">>> Seeding favorites...");
        List<FavoriteRecipe> favorites = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            User user = users.get((i % users.size()));
            favorites.add(FavoriteRecipe.builder()
                    .user(user)
                    .recipe(recipe)
                    .build());
        }
        favoriteRecipeRepository.saveAll(favorites);
    }

    private void seedPersonalNotes(List<Recipe> recipes, List<User> users) {
        logger.info(">>> Seeding personal notes...");
        List<PersonalInstructionNote> notes = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            User user = users.get((i % users.size()));
            notes.add(PersonalInstructionNote.builder()
                    .user(user)
                    .recipe(recipe)
                    .note("Tip: " + recipe.getTitle() + " tastes even better with fresh herbs.")
                    .build());
        }
        personalInstructionNoteRepository.saveAll(notes);
    }

    private void seedRecipeImages(List<Recipe> recipes) {
        logger.info(">>> Seeding recipe images...");
        List<RecipeImage> images = new ArrayList<>();
        String[] primaryUrls = {
                "https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1516100882582-96c3a05fe590?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1547592180-85f173990554?auto=format&fit=crop&w=1200&q=80",
                "https://images.unsplash.com/photo-1488477181946-6428a0291777?auto=format&fit=crop&w=1200&q=80"
        };
        String[][] additionalUrls = {
                {"https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1200&q=80"},
                {"https://images.unsplash.com/photo-1518779578993-ec3579fee39f?auto=format&fit=crop&w=1200&q=80", "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=1200&q=80"},
                {"https://images.unsplash.com/photo-1482049016688-2d3e1b311543?auto=format&fit=crop&w=1200&q=80"},
                {"https://images.unsplash.com/photo-1529042410759-befb1204b468?auto=format&fit=crop&w=1200&q=80", "https://images.unsplash.com/photo-1509440159596-0249088772ff?auto=format&fit=crop&w=1200&q=80"},
                {"https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=1200&q=80"},
                {"https://images.unsplash.com/photo-1488900128323-21503983a07e?auto=format&fit=crop&w=1200&q=80", "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=1200&q=80"}
        };

        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            images.add(RecipeImage.builder()
                    .recipe(recipe)
                    .imageUrl(primaryUrls[i % primaryUrls.length])
                    .isPrimary(true)
                    .build());

            for (String extraUrl : additionalUrls[i % additionalUrls.length]) {
                images.add(RecipeImage.builder()
                        .recipe(recipe)
                        .imageUrl(extraUrl)
                        .isPrimary(false)
                        .build());
            }
        }
        recipeImageRepository.saveAll(images);
    }
}
