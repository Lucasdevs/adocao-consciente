package br.edu.unifacisa.adocao;

import jakarta.persistence.*;
import java.time.Instant;

enum Role { ADOTANTE, RESPONSAVEL }
enum Species { CAO, GATO }
enum AnimalSize { PEQUENO, MEDIO, GRANDE }
enum AnimalStatus { DISPONIVEL, EM_PROCESSO, ADOTADO }
enum RequestStatus { PENDENTE, APROVADA, RECUSADA, CANCELADA, CONCLUIDA }

@Entity @Table(name="app_users")
class AppUser {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(nullable=false, length=100) String name;
    @Column(nullable=false, unique=true, length=254) String email;
    @Column(name="password_hash", nullable=false, length=100) String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) Role role;
}

@Entity @Table(name="animals")
class Animal {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne(optional=false) @JoinColumn(name="owner_id") AppUser owner;
    @Column(nullable=false, length=80) String name;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=10) Species species;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=10) AnimalSize size;
    @Column(nullable=false) int ageMonths;
    @Column(nullable=false, length=100) String city;
    @Column(nullable=false, length=3000) String description;
    @Column(nullable=false, length=2000) String care;
    @Column(nullable=false, length=1000) String photoUrl;
    @Column(nullable=false) boolean vaccinated;
    @Column(nullable=false) boolean neutered;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) AnimalStatus status;
}

@Entity @Table(name="adoption_requests")
class AdoptionRequest {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @ManyToOne(optional=false) @JoinColumn(name="animal_id") Animal animal;
    @ManyToOne(optional=false) @JoinColumn(name="adopter_id") AppUser adopter;
    @Column(nullable=false, length=1000) String housing;
    @Column(nullable=false, length=2000) String routine;
    @Column(nullable=false, length=2000) String experience;
    @Column(nullable=false) boolean commitment;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) RequestStatus status;
    @Column(nullable=false, length=1000) String decisionNote;
    @Column(nullable=false) Instant createdAt;
}
