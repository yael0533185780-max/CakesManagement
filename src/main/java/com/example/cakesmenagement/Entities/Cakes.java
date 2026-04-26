package com.example.cakesmenagement.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@EqualsAndHashCode
@Entity
public class Cakes {
    @Id
    private int id;
    private String name;
    private String description;
    private double price; // מחיר עוגה
    private String ingredients; // מרכיבים
    private String imageUrl; // תמונה
    @ElementCollection
    private List<String> recommendation;
    private boolean isActive; // אם העוגה זמינה למכירה
    @ManyToOne
    private  Categories category;
}
