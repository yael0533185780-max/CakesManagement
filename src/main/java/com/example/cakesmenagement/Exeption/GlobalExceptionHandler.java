package com.example.cakesmenagement.Exeption;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // הפונקציה הזו תופעל אוטומטית בכל פעם שיש בעיה מול מסד הנתונים (למשל השרת נפל)
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<String> handleDatabaseExceptions(DataAccessException ex) {
        // כאן גם נהוג להדפיס ללוג את השגיאה האמיתית למפתח
        // log.error("Database error occurred", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("אירעה שגיאה פנימית בעת הגישה לנתונים. אנא נסה שנית מאוחר יותר.");
    }

    // רשת ביטחון כללית - תופסת כל שגיאה אחרת שלא הוגדרה מראש
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneralExceptions(Exception ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("אירעה שגיאה בלתי צפויה.");
    }
}
