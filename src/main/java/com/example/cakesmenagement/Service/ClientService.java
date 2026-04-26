package com.example.cakesmenagement.Service;

import com.example.cakesmenagement.Dto.RegisterRequest;
import com.example.cakesmenagement.Entities.*;
import com.example.cakesmenagement.JWT.JwtUtil;
import com.example.cakesmenagement.Repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.cakesmenagement.Entities.Orders.OrderStatus.PAID;

@Service

public class ClientService {
    @Autowired
    private UsersRepo usersRepo;
    @Autowired
    private OrdersRepo orderRepo;
    @Autowired
    private CategoriesRepo categoryRepo;
    @Autowired
    private CakesRepo cakeRepo;
    @Autowired
    private PaymentsRepo paymentsRepo;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public Users register(RegisterRequest request) {
        if (usersRepo.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        Users u = new Users();
        u.setName(request.getName());
        u.setEmail(request.getEmail());
        u.setPhoneNumber(request.getPhoneNumber());
        u.setPassword(passwordEncoder.encode(request.getPassword()));
        u.setRole("ROLE_USER");
        return usersRepo.save(u);
    }
    @Autowired
    private JwtUtil jwtUtil;


    // הפונקציה החדשה ב-ClientService:
    public String loginAndGetToken(String email, String password) {
        // 1. מחפשים משתמש
        Users user = usersRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("משתמש לא קיים"));

        // 2. בודקים סיסמה
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("סיסמה שגויה");
        }

        // 3. מייצרים ומחזירים את הטוקן
        return jwtUtil.generateToken(user.getEmail(), user.getRole());
    }
    public void updateUser(int id,Users u1) {
        Users user = usersRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("id not exist"));
        user.setName(u1.getName());
        user.setEmail(u1.getEmail());
        user.setPhoneNumber(u1.getPhoneNumber());
        user.setPassword(u1.getPassword());
        user.setRole(u1.getRole());
        usersRepo.save(user);
    }
    public List<OrderItem> getCart(int id) {
        Optional<Users> user = usersRepo.findById(id);
        if (!user.isPresent()) {
            throw new RuntimeException("id not exist");
        }
        return user.get().getCakesInCart();
    }
    public List<String> addRecommendation(int cakeId, String text) {
        // 1. מציאת העוגה מה-Repository
        Cakes cake = cakeRepo.findById(cakeId)
                .orElseThrow(() -> new RuntimeException("עוגה לא נמצאה"));

        // 2. הוספת הטקסט החדש לרשימת ההמלצות (ElementCollection)
        cake.getRecommendation().add(text);

        // 3. שמירה מחדש של האובייקט - המערכת תעדכן את טבלת העזר לבד
        cakeRepo.save(cake);

        // 4. החזרת הרשימה המעודכנת לצורך תצוגה מיידית ב-React
        return cake.getRecommendation();
    }
    public List<OrderItem> addToCart(Cakes c1, int userId) {
//        לטפל בהרשאות אם המשתמש לא מחובר להעביר להרשמה
        Optional<Users> user = usersRepo.findById(userId);
        if (!user.isPresent()) {
            throw new RuntimeException("id not exist");
        }
        OrderItem existingOrderItem = user.get().getCakesInCart().stream()
                .filter(item -> item.getCake().getId()==c1.getId())
                .findFirst()
                .orElse(null);
        if (existingOrderItem != null) {
            existingOrderItem.setQuantity(existingOrderItem.getQuantity() + 1);
        }
        else {
            OrderItem newItem = new OrderItem();
            newItem.setCake(c1);
            newItem.setQuantity(1);
            user.get().getCakesInCart().add(newItem);
            usersRepo.save(user.get());
        }
        return user.get().getCakesInCart();
    }
    public List<OrderItem> removeFromCart(int cakeId, int userId) {
        // 1. שליפת המשתמש ובדיקה שהוא קיים
        Users user = usersRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. חיפוש הפריט הספציפי בעגלה לפי ה-ID של העוגה
        OrderItem existingOrderItem = user.getCakesInCart().stream()
                .filter(item -> item.getCake().getId() == cakeId) // וודאי שכאן זה getCode() או getId() לפי הישות Cakes
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Cake not found in cart"));
        if (existingOrderItem.getQuantity() > 1) {
            existingOrderItem.setQuantity(existingOrderItem.getQuantity() - 1);
        } else {
            user.getCakesInCart().remove(existingOrderItem);
        }
        usersRepo.save(user);

        return user.getCakesInCart();
    }
    public void addOrder(Orders o) {
        if(o.getStatus()!=PAID){
            throw new RuntimeException("you didn't pay");
        }
        if (orderRepo.existsById(o.getOrderCode()))
            throw new RuntimeException("id exist");
        Users user = o.getUser();
        user.getUserOrders().add(o);
        user.getCakesInCart().clear();
        orderRepo.save(o);
        usersRepo.save(user);
    }
    public Optional<Orders> getOrdersById(int userId) {
        if (!usersRepo.existsById(userId))
            throw new RuntimeException("id not exist");
       Optional<Orders> order1= orderRepo.findById(userId);
        return order1;
    }
    public List<Cakes> getCakesByCategory(int categoryCode) {
        return cakeRepo.findByCategory_CategoryCode(categoryCode);
    }
    public List<Cakes> getAllCakes() {
        List<Cakes> lCakes= cakeRepo.findAll();
        return lCakes;
    }
    public List<Cakes> getCakesByName(String name) {
        List<Cakes> lCakes= cakeRepo.findByName(name);
        return lCakes;
    }

    public Payments addPayment(Payments payment) {
        if (paymentsRepo.existsById(payment.getId())) {
            throw new RuntimeException("Payment ID already exists");
        }
        Payments savedPayment = paymentsRepo.save(payment);
        Orders order = savedPayment.getOrder();
        if (order != null) {
            order.setStatus(PAID);
            orderRepo.save(order);
        } else {
            throw new RuntimeException("Payment must be linked to an existing order");
        }
        return savedPayment;
    }
    public Payments getPaymentById(int id) {
        return paymentsRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }
    public List<Categories> getAllCategories() {
        return categoryRepo.findAll();
    }


    public Categories findByName(String name) {
        Categories category = categoryRepo.findByName(name);
        if (category == null) {
            return null;
        }
        return category;
    }
}
