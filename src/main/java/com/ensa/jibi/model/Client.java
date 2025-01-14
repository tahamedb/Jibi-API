package com.ensa.jibi.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@Table
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstname;

    @Column(nullable = false)
    private String lastname;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    @Enumerated(EnumType.ORDINAL)
    private AccountType accountType;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private boolean requiresPasswordChange = true;  // Indique si le client doit changer son mot de passe

    @Column(nullable = false)
    private String cin;

    @ManyToOne
    @JoinColumn(name = "agent_id")
    private Agent agent;



}
