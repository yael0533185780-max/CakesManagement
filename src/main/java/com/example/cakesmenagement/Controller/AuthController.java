package com.example.cakesmenagement.Controller;

import com.example.cakesmenagement.Dto.RegisterRequest;
import com.example.cakesmenagement.Entities.Users;
import com.example.cakesmenagement.Service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private ClientService clientService;

    // 🔹 הרשמה - קורא לסרוויס בצורה נקייה
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) { // שמנו פה את ה-DTO
        try {
            Users registeredUser = clientService.register(request);
            return ResponseEntity.ok("נרשמת בהצלחה!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Users loginRequest) {
        try {
            String token = clientService.loginAndGetToken(loginRequest.getEmail(), loginRequest.getPassword());
            return ResponseEntity.ok(token);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}