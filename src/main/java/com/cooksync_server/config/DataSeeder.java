package com.cooksync_server.config;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cooksync_server.entities.Ingredient;
import com.cooksync_server.entities.Instruction;
import com.cooksync_server.entities.Recipe;
import com.cooksync_server.entities.Tag;
import com.cooksync_server.entities.Unit;
import com.cooksync_server.entities.User;
import com.cooksync_server.repositories.IngredientRepository;
import com.cooksync_server.repositories.InstructionRepository;
import com.cooksync_server.repositories.RecipeRepository;
import com.cooksync_server.repositories.TagRepository;
import com.cooksync_server.repositories.UnitRepository;
import com.cooksync_server.repositories.UserRepository;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RecipeRepository recipeRepository;
    private final TagRepository tagRepository;
    private final UnitRepository unitRepository;
    private final IngredientRepository ingredientRepository;
    private final InstructionRepository instructionRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, RecipeRepository recipeRepository,
            TagRepository tagRepository, UnitRepository unitRepository,
            IngredientRepository ingredientRepository, InstructionRepository instructionRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.recipeRepository = recipeRepository;
        this.tagRepository = tagRepository;
        this.unitRepository = unitRepository;
        this.ingredientRepository = ingredientRepository;
        this.instructionRepository = instructionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // בדיקה: אם כבר יש משתמשים במסד הנתונים, אל תריץ את הזריעה מחדש
        if (userRepository.count() > 0) {
            System.out.println("🌱 Database already seeded. Skipping seeder...");
            return;
        }

        System.out.println("🌱 Seeding database with initial data...");

        // 1. יצירת יחידות מידה (Units)
        Unit grams = unitRepository.save(Unit.builder().code("g").name("Grams").build());
        Unit cups = unitRepository.save(Unit.builder().code("cup").name("Cups").build());
        Unit tbsp = unitRepository.save(Unit.builder().code("tbsp").name("Tablespoon").build());

        // 2. יצירת תגיות
        Tag vegan = tagRepository.save(Tag.builder().name("Vegan").recipes(new HashSet<>()).build());
        Tag dessert = tagRepository.save(Tag.builder().name("Dessert").recipes(new HashSet<>()).build());
        Tag quick = tagRepository.save(Tag.builder().name("Quick").recipes(new HashSet<>()).build());

        // 3. יצירת משתמשים
        User adminUser = userRepository.save(User.builder()
                .name("Yaron Serlin")
                .email("yaron@cooksync.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .isAdmin(true)
                .build());

        User pastryChef = userRepository.save(User.builder()
                .name("Gaya")
                .email("gaya@cooksync.com")
                .passwordHash(passwordEncoder.encode("user123"))
                .isAdmin(false)
                .build());

        // 4. יצירת מתכון לדוגמה
        Recipe recipe = Recipe.builder()
                .createdBy(pastryChef)
                .title("Vegan Chocolate Fudge Cake")
                .description("A rich, moist, and completely plant-based chocolate dessert.")
                .difficulty(Recipe.Difficulty.MEDIUM)
                .prepTimeMinutes(20)
                .cookTimeMinutes(45)
                .servings(8)
                .reviewCount(0)
                .build();
        Recipe savedRecipe = recipeRepository.save(recipe);

        // --- Additional sample recipe: Quick Avocado Toast ---
        Recipe quickRecipe = Recipe.builder()
                .createdBy(adminUser)
                .title("Quick Avocado Toast")
                .description("Simple and healthy avocado toast for breakfast.")
                .difficulty(Recipe.Difficulty.EASY)
                .prepTimeMinutes(5)
                .cookTimeMinutes(0)
                .servings(1)
                .reviewCount(0)
                .build();
        Recipe savedQuick = recipeRepository.save(quickRecipe);

        ingredientRepository.saveAll(List.of(
                Ingredient.builder().recipe(savedQuick).name("Bread slice").quantity(new BigDecimal("1")).unit(cups).build(),
                Ingredient.builder().recipe(savedQuick).name("Avocado").quantity(new BigDecimal("0.5")).unit(grams).build(),
                Ingredient.builder().recipe(savedQuick).name("Olive oil").quantity(new BigDecimal("1")).unit(tbsp).build()
        ));

        instructionRepository.saveAll(List.of(
                Instruction.builder().recipe(savedQuick).stepNumber(1).description("Toast the bread.").hasTimer(false).build(),
                Instruction.builder().recipe(savedQuick).stepNumber(2).description("Mash avocado, season, and spread on toast.").hasTimer(false).build()
        ));

        // link quick tag to the quick recipe
        quick.getRecipes().add(savedQuick);
        tagRepository.save(quick);

        // --- Additional sample recipe: Healthy Green Smoothie ---
        Recipe smoothie = Recipe.builder()
                .createdBy(pastryChef)
                .title("Healthy Green Smoothie")
                .description("A refreshing smoothie packed with greens and fruit.")
                .difficulty(Recipe.Difficulty.EASY)
                .prepTimeMinutes(5)
                .cookTimeMinutes(0)
                .servings(2)
                .reviewCount(0)
                .build();
        Recipe savedSmoothie = recipeRepository.save(smoothie);

        ingredientRepository.saveAll(List.of(
                Ingredient.builder().recipe(savedSmoothie).name("Spinach").quantity(new BigDecimal("100")).unit(grams).build(),
                Ingredient.builder().recipe(savedSmoothie).name("Banana").quantity(new BigDecimal("1")).unit(cups).build(),
                Ingredient.builder().recipe(savedSmoothie).name("Almond milk").quantity(new BigDecimal("250")).unit(grams).build()
        ));

        instructionRepository.saveAll(List.of(
                Instruction.builder().recipe(savedSmoothie).stepNumber(1).description("Combine all ingredients in a blender and blend until smooth.").hasTimer(false).build()
        ));

        // link healthy tag (create if not present) and breakfast tag
        Tag healthy = tagRepository.save(Tag.builder().name("Healthy").recipes(new HashSet<>()).build());
        Tag breakfast = tagRepository.save(Tag.builder().name("Breakfast").recipes(new HashSet<>()).build());
        healthy.getRecipes().add(savedSmoothie);
        breakfast.getRecipes().add(savedQuick);
        tagRepository.saveAll(List.of(healthy, breakfast));

        // 5. הוספת מצרכים
        ingredientRepository.saveAll(List.of(
                Ingredient.builder().recipe(savedRecipe).name("Flour").quantity(new BigDecimal("2.5")).unit(cups).build(),
                Ingredient.builder().recipe(savedRecipe).name("Cocoa Powder").quantity(new BigDecimal("0.75")).unit(cups).build(),
                Ingredient.builder().recipe(savedRecipe).name("Plant-based Milk").quantity(new BigDecimal("400")).unit(grams).build()
        ));

        // 6. הוספת שלבי הכנה
        instructionRepository.saveAll(List.of(
                Instruction.builder().recipe(savedRecipe).stepNumber(1).description("Preheat oven to 180°C and line a baking pan.").hasTimer(false).build(),
                Instruction.builder().recipe(savedRecipe).stepNumber(2).description("Mix all dry ingredients in a large bowl.").hasTimer(false).build(),
                Instruction.builder().recipe(savedRecipe).stepNumber(3).description("Bake until a toothpick comes out clean.").hasTimer(true).timeSeconds(2700).build()
        ));

        // 7. קישור התגיות למתכון (מכיוון שזה ManyToMany, צריך להוסיף את המתכון לרשימה של התגית)
        vegan.getRecipes().add(savedRecipe);
        dessert.getRecipes().add(savedRecipe);
        tagRepository.saveAll(List.of(vegan, dessert));

        System.out.println("✅ Database seeding completed successfully!");
    }
}
