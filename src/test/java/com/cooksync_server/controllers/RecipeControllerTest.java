// package com.cooksync_server.controllers;

// import com.cooksync_server.config.JwtAuthenticationFilter;
// import com.cooksync_server.config.JwtUtil;
// import com.cooksync_server.services.RecipeService;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.ComponentScan;
// import org.springframework.context.annotation.FilterType;
// import org.springframework.http.MediaType;
// import org.springframework.security.test.context.support.WithMockUser;
// import org.springframework.test.web.servlet.MockMvc;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(controllers = RecipeController.class, 
//             excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {JwtAuthenticationFilter.class, JwtUtil.class}))
// public class RecipeControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @MockBean
//     private RecipeService recipeService;

//     @Test
//     void getAllRecipes_ShouldReturn200() throws Exception {
//         mockMvc.perform(get("/api/recipes/public")
//                 .contentType(MediaType.APPLICATION_JSON))
//                 .andExpect(status().isOk());
//     }

//     @Test
//     @WithMockUser // מדמה משתמש מחובר לצורך בדיקת נתיבים מאובטחים
//     void createRecipe_WithoutData_ShouldReturn400() throws Exception {
//         mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/recipes")
//                 .contentType(MediaType.APPLICATION_JSON)
//                 .content("{}")) // שליחת גוף ריק כדי לבדוק Validation
//                 .andExpect(status().isBadRequest());
//     }
// }