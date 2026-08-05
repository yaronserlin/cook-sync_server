package com.cooksync_server.config;

import com.cooksync_server.entities.*;
import com.cooksync_server.entities.DescriptionBlock;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Data Seeder component for seeding database records under the seed active profile.
 *
 * @author Yaron Serlin
 * @version 1.0
 * @since 02/08/2026
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

    private static final String[] DESCRIPTION_IMAGE_URLS = {
            "https://images.unsplash.com/photo-1512058564366-c9e0d1b4f512?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1523986371872-9d3ba2e2f5b2?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1490645935967-10de6ba17061?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1506354666786-959d6d497f1a?auto=format&fit=crop&w=1200&q=80"
    };

    private static final String[] INSTRUCTION_IMAGE_URLS = {
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1518770660439-4636190af475?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1473093295043-cdd812d0e601?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1498575207490-42125e0e5881?auto=format&fit=crop&w=1200&q=80",
            "https://images.unsplash.com/photo-1506089676908-3592f7389d4d?auto=format&fit=crop&w=1200&q=80"
    };

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
                "personal_instruction_notes", "tags", "recipe_tags", "recipe_images",
                "description_blocks"
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
                Tag.builder().name("spicy").build(),
                Tag.builder().name("gluten free").build(),
                Tag.builder().name("high protein").build(),
                Tag.builder().name("comfort food").build(),
                Tag.builder().name("vegetarian").build()
        ));
    }

    private List<User> seedUsers() {
        logger.info(">>> Seeding users...");
        return userRepository.saveAll(List.of(
                User.builder().firstName("Yaron").lastName("Serlin").email("yaron@gmail.com")
                        .passwordHash(passwordEncoder.encode("123456aA!")).isAdmin(true)
                        .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                        .build(),
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
                        .build(),
                User.builder().firstName("Ari").lastName("Levy").email("ari@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Noa").lastName("Aviv").email("noa@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Lior").lastName("Ben").email("lior@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Dana").lastName("Mor").email("dana@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Guy").lastName("Eldar").email("guy@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Eden").lastName("Nir").email("eden@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Yossi").lastName("Amit").email("yossi@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Neta").lastName("Carmi").email("neta@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Tamar").lastName("Barak").email("tamar@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Omer").lastName("Gal").email("omer@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Moran").lastName("Sofer").email("moran@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Roni").lastName("Tzur").email("roni@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Shira").lastName("Tal").email("shira@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Itay").lastName("Peretz").email("itay@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Or").lastName("Levi").email("or@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Nir").lastName("Dayan").email("nir@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Tal").lastName("Eden").email("tal@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Hadar").lastName("Barzilai").email("hadar@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Liat").lastName("Ashkenazi").email("liat@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Yael").lastName("Rosen").email("yael@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Eyal").lastName("Shapira").email("eyal@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Orly").lastName("Ben-Ami").email("orly@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Eitan").lastName("Galili").email("eitan@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Sigal").lastName("Golan").email("sigal@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Yarden").lastName("Baron").email("yarden@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Gal").lastName("Eisen").email("gal@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Mika").lastName("Tamar").email("mika@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Amit").lastName("Klein").email("amit@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Roni").lastName("Tamar").email("roni.tamar@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Noa").lastName("Erez").email("noa.erez@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Itai").lastName("Regev").email("itai@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Nava").lastName("Shapira").email("nava@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Rafael").lastName("Azar").email("rafael@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Talia").lastName("Mor").email("talia@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Yonatan").lastName("Katz").email("yonatan@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Lina").lastName("Yaari").email("lina@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Dvir").lastName("Nadav").email("dvir@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Michal").lastName("Keren").email("michal@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Ziv").lastName("Tal").email("ziv@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Shai").lastName("Ben-David").email("shai@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Oran").lastName("Shlomo").email("oran@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Rina").lastName("Cohen").email("rina@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Elad").lastName("Hadar").email("elad@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Galit").lastName("Levi").email("galit@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Eran").lastName("Amit").email("eran@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Maya").lastName("Einav").email("maya.einav@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Efrat").lastName("Avraham").email("efrat@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Omri").lastName("Shemesh").email("omri@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Liel").lastName("Eliav").email("liel@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Zara").lastName("Mendel").email("zara@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Oded").lastName("Yosef").email("oded@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Tzvi").lastName("Maimon").email("tzvi@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Noya").lastName("Giladi").email("noya@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Gili").lastName("Nissan").email("gili@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Ely").lastName("Oz").email("ely@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1544723795-3fb6469f5b39?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Rina").lastName("Hadad").email("rina.hadad@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Asaf").lastName("Paz").email("asaf@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Adva").lastName("Ravid").email("adva@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Aviv").lastName("Gazit").email("aviv@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1504593811423-6dd665756598?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Miri").lastName("Azoulay").email("miri@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80")
                        .build(),
                User.builder().firstName("Dean").lastName("Dahan").email("dean@cooksync.com")
                        .passwordHash(passwordEncoder.encode("Password123!")).isAdmin(false)
                        .avatarUrl("https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=400&q=80")
                        .build()
        ));
    }

    private List<Recipe> seedRecipes(List<User> users, List<Unit> units, List<Tag> tags) {
        logger.info(">>> Seeding recipes, ingredients, instructions, and tags...");

        User chef = users.stream().filter(User::isAdmin).findFirst().orElse(users.get(1));
        User regularUser = users.stream().filter(user -> !user.isAdmin()).findFirst().orElse(users.get(1));

        List<Ingredient> saladIngredients = List.of(
                createIngredient("Lettuce", BigDecimal.valueOf(1), units.get(0), null),
                createIngredient("Tomato", BigDecimal.valueOf(2), units.get(9), null),
                createIngredient("Cucumber", BigDecimal.valueOf(1), units.get(9), null)
        );
        Recipe salad = createRecipe(
                "Simple Vegan Salad",
                "A fresh and quick healthy salad that works for lunch or dinner.",
                Recipe.Difficulty.EASY,
                10,
                0,
                2,
                chef,
                List.of(tags.get(0), tags.get(1), tags.get(2), tags.get(13)),
                saladIngredients,
                List.of(
                        createInstruction(1, "Wash and chop the vegetables.", false, null,
                                List.of(saladIngredients.get(0), saladIngredients.get(1), saladIngredients.get(2))),
                        createInstruction(2, "Toss with olive oil and lemon juice.", false, null, List.of())
                )
        );

        List<Ingredient> pastaIngredients = List.of(
                createIngredient("Pasta", BigDecimal.valueOf(250), units.get(3), null),
                createIngredient("Tomato sauce", BigDecimal.valueOf(2), units.get(0), null),
                createIngredient("Garlic", BigDecimal.valueOf(2), units.get(8), null)
        );
        Recipe pasta = createRecipe(
                "Creamy Tomato Pasta",
                "A comforting pasta dish with a silky tomato sauce and herbs.",
                Recipe.Difficulty.MEDIUM,
                15,
                20,
                4,
                regularUser,
                List.of(tags.get(4), tags.get(8), tags.get(1), tags.get(13)),
                pastaIngredients,
                List.of(
                        createInstruction(1, "Boil the pasta until al dente.", false, null,
                                List.of(pastaIngredients.get(0))),
                        createInstruction(2, "Simmer the sauce with garlic and herbs.", false, null,
                                List.of(pastaIngredients.get(1), pastaIngredients.get(2))),
                        createInstruction(3, "Combine and serve warm.", false, null, List.of())
                )
        );

        List<Ingredient> pancakeIngredients = List.of(
                createIngredient("Banana", BigDecimal.valueOf(2), units.get(9), null),
                createIngredient("Flour", BigDecimal.valueOf(200), units.get(3), null),
                createIngredient("Egg", BigDecimal.valueOf(2), units.get(9), null)
        );
        Recipe pancakes = createRecipe(
                "Fluffy Banana Pancakes",
                "Soft pancakes with ripe bananas and a hint of cinnamon.",
                Recipe.Difficulty.MEDIUM,
                10,
                15,
                3,
                regularUser,
                List.of(tags.get(5), tags.get(1), tags.get(3), tags.get(13)),
                pancakeIngredients,
                List.of(
                        createInstruction(1, "Mash the bananas until smooth.", false, null,
                                List.of(pancakeIngredients.get(0))),
                        createInstruction(2, "Mix in flour and eggs to make a batter.", false, null,
                                List.of(pancakeIngredients.get(1), pancakeIngredients.get(2))),
                        createInstruction(3, "Cook each side until golden.", true, 180, List.of())
                )
        );

        List<Ingredient> bowlIngredients = List.of(
                createIngredient("Chickpeas", BigDecimal.valueOf(400), units.get(3), null),
                createIngredient("Rice", BigDecimal.valueOf(1), units.get(0), null),
                createIngredient("Avocado", BigDecimal.valueOf(1), units.get(9), null)
        );
        Recipe bowl = createRecipe(
                "Spicy Chickpea Bowl",
                "A bold bowl filled with roasted chickpeas, rice, and avocado.",
                Recipe.Difficulty.EASY,
                15,
                20,
                2,
                users.get(3),
                List.of(tags.get(0), tags.get(9), tags.get(7), tags.get(13)),
                bowlIngredients,
                List.of(
                        createInstruction(1, "Roast the chickpeas with spices.", true, 600,
                                List.of(bowlIngredients.get(0))),
                        createInstruction(2, "Cook the rice and slice the avocado.", false, null,
                                List.of(bowlIngredients.get(1), bowlIngredients.get(2))),
                        createInstruction(3, "Assemble everything in a bowl.", false, null, List.of())
                )
        );

        List<Ingredient> soupIngredients = List.of(
                createIngredient("Lentils", BigDecimal.valueOf(300), units.get(3), null),
                createIngredient("Carrot", BigDecimal.valueOf(2), units.get(9), null),
                createIngredient("Celery", BigDecimal.valueOf(2), units.get(9), null)
        );
        Recipe soup = createRecipe(
                "Golden Lentil Soup",
                "A cozy, nutritious soup with carrots, celery, and lentils.",
                Recipe.Difficulty.EASY,
                15,
                35,
                4,
                users.get(4),
                List.of(tags.get(2), tags.get(4), tags.get(8), tags.get(13)),
                soupIngredients,
                List.of(
                        createInstruction(1, "Saute the vegetables until softened.", false, null,
                                List.of(soupIngredients.get(1), soupIngredients.get(2))),
                        createInstruction(2, "Add lentils and broth, then simmer.", true, 1800,
                                List.of(soupIngredients.get(0))),
                        createInstruction(3, "Blend partly for a thicker texture.", false, null, List.of())
                )
        );

        List<Ingredient> dessertIngredients = List.of(
                createIngredient("Yogurt", BigDecimal.valueOf(250), units.get(3), null),
                createIngredient("Mixed berries", BigDecimal.valueOf(150), units.get(3), null),
                createIngredient("Granola", BigDecimal.valueOf(100), units.get(3), null)
        );
        Recipe dessert = createRecipe(
                "Berry Yogurt Parfait",
                "A simple dessert with layered yogurt, berries, and granola.",
                Recipe.Difficulty.EASY,
                5,
                0,
                2,
                users.get(5),
                List.of(tags.get(5), tags.get(2), tags.get(1), tags.get(13)),
                dessertIngredients,
                List.of(
                        createInstruction(1, "Layer yogurt, berries, and granola in glasses.", false, null,
                                List.of(dessertIngredients.get(0), dessertIngredients.get(1), dessertIngredients.get(2))),
                        createInstruction(2, "Chill briefly before serving.", false, null, List.of())
                )
        );

        List<Recipe> baseRecipes = List.of(salad, pasta, pancakes, bowl, soup, dessert);
        List<Recipe> allRecipes = new ArrayList<>(baseRecipes);
        allRecipes.addAll(generateAdditionalRecipes(100, users, units, tags, baseRecipes.size() + 1));
        return recipeRepository.saveAll(allRecipes);
    }

    private List<Recipe> generateAdditionalRecipes(int count, List<User> users, List<Unit> units, List<Tag> tags,
                                                   int startingIndex) {
        User chef = users.stream().filter(User::isAdmin).findFirst().orElse(users.get(0));
        List<Recipe> generated = new ArrayList<>(count);
        String[] dishTypes = {"Stew", "Salad", "Pasta", "Bowl", "Wrap", "Soup", "Casserole", "Stir Fry", "Skewers", "Taco"};
        String[] descriptors = {"Garden", "Autumn", "Sunrise", "Harvest", "Spicy", "Creamy", "Golden", "Fresh", "Tangy", "Cozy"};
        String[] ingredientBases = {"Carrot", "Potato", "Pea", "Bean", "Mushroom", "Zucchini", "Spinach", "Bell pepper", "Corn", "Beet"};
        String[] instructionTexts = {
                "Prepare the ingredients and chop them as needed.",
                "Cook the mixture with oil and seasonings until tender.",
                "Serve warm and enjoy."
        };

        for (int i = 0; i < count; i++) {
            int index = startingIndex + i;
            String title = String.format("%s %s %d", descriptors[i % descriptors.length], dishTypes[i % dishTypes.length], index);
            String description = String.format("A %s recipe with simple ingredients and satisfying flavors.", descriptors[i % descriptors.length].toLowerCase());
            Recipe.Difficulty difficulty = Recipe.Difficulty.values()[i % Recipe.Difficulty.values().length];
            int prepTime = 5 + (i % 5) * 5;
            int cookTime = ((i + 1) % 4) * 10;
            int servings = 2 + (i % 4);
            User creator = (i % 2 == 0) ? chef : users.get((i % users.size()));

            List<Tag> recipeTags = List.of(
                    tags.get(i % tags.size()),
                    tags.get((i + 1) % tags.size()),
                    tags.get((i + 2) % tags.size())
            );

            List<Ingredient> recipeIngredients = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                String ingredientName = String.format("%s %s", ingredientBases[(i + j) % ingredientBases.length],
                        j == 0 ? "mix" : j == 1 ? "blend" : "piece");
                BigDecimal quantity = BigDecimal.valueOf(1 + (j * 100) + (i % 5) * 10);
                Unit unit = units.get((i + j) % units.size());
                recipeIngredients.add(createIngredient(ingredientName, quantity, unit, null));
            }

            List<Instruction> recipeInstructions = new ArrayList<>();
            for (int j = 0; j < 3; j++) {
                List<Ingredient> linkedIngredients = new ArrayList<>();
                if (j == 0) {
                    linkedIngredients.add(recipeIngredients.get(0));
                } else if (j == 1) {
                    linkedIngredients.add(recipeIngredients.get(1));
                    linkedIngredients.add(recipeIngredients.get(2));
                }
                Integer timeSeconds = (j == 1 && cookTime > 0) ? cookTime * 60 : null;
                boolean hasTimer = timeSeconds != null;
                recipeInstructions.add(createInstruction(j + 1, instructionTexts[j], hasTimer, timeSeconds, linkedIngredients));
            }

            Recipe recipe = createRecipe(title, description, difficulty, prepTime, cookTime, servings,
                    creator, recipeTags, recipeIngredients, recipeInstructions);
            generated.add(recipe);
        }

        return generated;
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

        recipe.setTags(new LinkedHashSet<>(tags));

        Set<Ingredient> ingredientSet = new LinkedHashSet<>();
        for (Ingredient ingredient : ingredients) {
            ingredient.setRecipe(recipe);
            ingredientSet.add(ingredient);
        }
        recipe.setIngredients(ingredientSet);

        Set<Instruction> instructionSet = new LinkedHashSet<>();
        for (Instruction instruction : instructions) {
            instruction.setRecipe(recipe);
            instructionSet.add(instruction);
        }
        recipe.setInstructions(instructionSet);

        List<DescriptionBlock> blocks = buildDescriptionBlocks(recipe, description);
        if (!blocks.isEmpty()) {
            int insertIndex = ThreadLocalRandom.current().nextInt(blocks.size() + 1);
            blocks.add(insertIndex, DescriptionBlock.builder()
                    .recipe(recipe)
                    .type(DescriptionBlock.BlockType.IMAGE)
                    .imageUrl(pickRandomDescriptionImageUrl())
                    .caption("Recipe image")
                    .build());
        }

        assignBlockSortOrders(blocks);
        recipe.setDescriptionBlocks(blocks);
        assignRandomInstructionImage(instructions);

        return recipe;
    }

    private void assignRandomInstructionImage(List<Instruction> instructions) {
        if (instructions == null || instructions.isEmpty()) {
            return;
        }
        int selected = ThreadLocalRandom.current().nextInt(instructions.size());
        Instruction instruction = instructions.get(selected);
        instruction.setImageUrl(pickRandomInstructionImageUrl());
    }

    private void assignBlockSortOrders(List<DescriptionBlock> blocks) {
        for (int i = 0; i < blocks.size(); i++) {
            blocks.get(i).setSortOrder(i);
        }
    }

    private String pickRandomDescriptionImageUrl() {
        return DESCRIPTION_IMAGE_URLS[ThreadLocalRandom.current().nextInt(DESCRIPTION_IMAGE_URLS.length)];
    }

    private String pickRandomInstructionImageUrl() {
        return INSTRUCTION_IMAGE_URLS[ThreadLocalRandom.current().nextInt(INSTRUCTION_IMAGE_URLS.length)];
    }

    private List<DescriptionBlock> buildDescriptionBlocks(Recipe recipe, String description) {
        List<DescriptionBlock> blocks = new ArrayList<>();
        if (description != null && !description.isBlank()) {
            String[] lines = description.split("\\. ");
            StringBuilder paragraph = new StringBuilder();
            for (int i = 0; i < Math.max(lines.length, 5); i++) {
                String nextLine = (i < lines.length) ? lines[i] : "Enjoy this flavorful recipe for every occasion.";
                paragraph.append(nextLine.trim());
                if (!nextLine.endsWith(".")) {
                    paragraph.append(".");
                }
                if (i < 4) {
                    paragraph.append(" ");
                }
                if ((i + 1) % 2 == 0 || i == 4) {
                    blocks.add(DescriptionBlock.builder()
                            .recipe(recipe)
                            .type(DescriptionBlock.BlockType.TEXT)
                            .text(paragraph.toString())
                            .build());
                    paragraph.setLength(0);
                }
            }
        }
        return blocks;
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
        reviews.addAll(buildStandardReviews(recipes, users));
        reviews.addAll(buildReportedReviews(recipes, users));
        reviews.addAll(buildBulkComments(recipes, users));

        reviewRepository.saveAll(reviews);
        recalculateRecipeStats(recipes, reviews);
        recipeRepository.saveAll(recipes);
    }

    private List<Review> buildStandardReviews(List<Recipe> recipes, List<User> users) {
        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            User primaryReviewer = users.get((i % (users.size() - 1)) + 1);
            reviews.add(Review.builder()
                    .recipe(recipe)
                    .user(primaryReviewer)
                    .rating(BigDecimal.valueOf(4.5 + (i % 3) * 0.5))
                    .title("Great recipe")
                    .comment("This was easy to follow and turned out really well.")
                    .build());

            User secondaryReviewer = users.get(((i + 2) % (users.size() - 1)) + 1);
            reviews.add(Review.builder()
                    .recipe(recipe)
                    .user(secondaryReviewer)
                    .rating(BigDecimal.valueOf(4.0 + (i % 2) * 0.5))
                    .title("Tasty and simple")
                    .comment("I enjoyed this version and will make it again.")
                    .build());
        }
        return reviews;
    }

    private List<Review> buildReportedReviews(List<Recipe> recipes, List<User> users) {
        List<Review> reviews = new ArrayList<>();
        if (recipes.size() < 4) {
            return reviews;
        }
        reviews.add(Review.builder()
                .recipe(recipes.get(0))
                .user(users.get(users.size() - 1))
                .rating(BigDecimal.valueOf(1.0))
                .title("Not related")
                .comment("Buy cheap kitchenware at this link, best prices anywhere, click now.")
                .reported(true)
                .reportReason(Review.ReportReason.SPAM)
                .reportedAt(LocalDateTime.now())
                .build());
        reviews.add(Review.builder()
                .recipe(recipes.get(1))
                .user(users.get(users.size() - 2))
                .rating(BigDecimal.valueOf(1.0))
                .title("Rude")
                .comment("Whoever wrote this should not be allowed near an oven, absolute rubbish.")
                .reported(true)
                .reportReason(Review.ReportReason.ABUSE)
                .reportedAt(LocalDateTime.now())
                .build());
        reviews.add(Review.builder()
                .recipe(recipes.get(2))
                .user(users.get(users.size() - 3))
                .rating(BigDecimal.valueOf(1.5))
                .title("Off topic")
                .comment("This content does not belong in a recipe review and should be removed.")
                .reported(true)
                .reportReason(Review.ReportReason.SPAM)
                .reportedAt(LocalDateTime.now())
                .build());
        reviews.add(Review.builder()
                .recipe(recipes.get(3))
                .user(users.get(users.size() - 4))
                .rating(BigDecimal.valueOf(1.0))
                .title("Inappropriate")
                .comment("This review is inappropriate and unrelated to the recipe instructions.")
                .reported(true)
                .reportReason(Review.ReportReason.ABUSE)
                .reportedAt(LocalDateTime.now())
                .build());
        return reviews;
    }

    private List<Review> buildBulkComments(List<Recipe> recipes, List<User> users) {
        String[] sampleComments = {
                "Loved the balance of flavors, will make again!",
                "Turned out great but I added a bit more salt than the recipe called for.",
                "Easy to follow and perfect for a weeknight dinner.",
                "Family enjoyed it — next time I'll double the sauce.",
                "Recipe is good, I swapped one ingredient and it still worked well.",
                "A little spicy for my taste, but still delicious.",
                "Quick to prepare and packed with flavor.",
                "Great texture, though I baked it a few minutes longer.",
                "Perfect comfort food — will save this one.",
                "Simple, fresh, and satisfying. Recommended!"
        };

        List<Review> reviews = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            Recipe recipe = recipes.get(i);
            for (int c = 0; c < 50; c++) {
                User commenter = users.get((i + c) % users.size());
                double randRating = Math.round(ThreadLocalRandom.current().nextDouble(1.0, 5.0) * 10.0) / 10.0;
                String commentText = String.format("%s (Comment %d for %s) — %s",
                        sampleComments[c % sampleComments.length], c + 1, recipe.getTitle(), commenter.getFirstName());
                reviews.add(Review.builder()
                        .recipe(recipe)
                        .user(commenter)
                        .rating(BigDecimal.valueOf(randRating))
                        .title("Comment")
                        .comment(commentText)
                        .build());
            }
        }
        return reviews;
    }

    private void recalculateRecipeStats(List<Recipe> recipes, List<Review> reviews) {
        Map<String, List<Review>> reviewsByRecipeId = reviews.stream()
                .collect(Collectors.groupingBy(review -> review.getRecipe().getId()));

        for (Recipe recipe : recipes) {
            List<Review> recipeReviews = reviewsByRecipeId.getOrDefault(recipe.getId(), List.of());
            recipe.setReviewCount(recipeReviews.size());
            recipe.setAverageRating(recipeReviews.isEmpty() ? null
                    : recipeReviews.stream().mapToDouble(r -> r.getRating().doubleValue()).average().orElse(0.0));
        }
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
