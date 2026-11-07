package com.shopflow.user.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "user_tbl")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    @Column(unique = true, nullable = false, length = 100)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(unique = true, nullable = false, length = 150)
    private String email;
    @Column(name = "phone", length = 20)
    private String phone;
    private LocalDate dob;
    @Column(name = "gender", length = 10)
    private String gender;

    @ManyToMany
    @JoinTable(
            name = "user_tbl",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn("role_id")
    )
    private Set<Role> roles;
}
