package com.example.cakesmenagement.security;

import com.example.cakesmenagement.JWT.JwtFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // 🔹 התוספת: הגדרת היררכיית הרשאות
    // הפונקציה הזו יוצרת כלל מערכתי ש-ADMIN הוא גם USER
    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl();
        roleHierarchy.setHierarchy("ROLE_ADMIN > ROLE_USER");
        return roleHierarchy;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable()) // ביטול CSRF
                .authorizeHttpRequests(auth -> auth
                        // פתוח לכולם
                        .requestMatchers(
                                "/api/cakes/all",
                                "/api/cakes/search",
                                "/api/categories/all",
                                "/api/categories/category/**",
                                "/api/users/register",
                                "/auth/**"
                        ).permitAll()

                        // הרשאות ADMIN בלבד (משתמש רגיל לא יוכל להיכנס לכאן)
                        // ✅ תוקן: כוכבית אחת באמצע מותרת, כוכבית כפולה מותרת רק בסוף
                        .requestMatchers("/api/*/admin/**").hasRole("ADMIN")

                        // הרשאות USER
                        // (בזכות ה-RoleHierarchy שהוספנו למעלה, גם ADMIN יוכל להיכנס לכאן בלי בעיה!)
                        .requestMatchers(
                                "/api/users/**",
                                "/api/orders/add",
                                "/api/cakes/recommend"
                        ).hasRole("USER")

                        // כל השאר דורש התחברות (טוקן חוקי כלשהו)
                        .anyRequest().authenticated()
                )
                // מוסיפים את JwtFilter לפני UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 🔹 PasswordEncoder Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}