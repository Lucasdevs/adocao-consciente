package br.edu.unifacisa.adocao;

import jakarta.validation.constraints.*;
import java.time.Instant;

record RegisterInput(@NotBlank @Size(max=100) String name,
 @NotBlank @Email @Size(max=254) String email,
 @NotBlank @Size(min=10,max=64) String password, @NotNull Role role) {
 @Override public String toString() { return "RegisterInput[credentials omitted]"; }
}
record LoginInput(@NotBlank @Email String email, @NotBlank String password) {
 @Override public String toString() { return "LoginInput[credentials omitted]"; }
}
record UserView(Long id, String name, String email, Role role) {
 static UserView of(AppUser u) { return new UserView(u.id,u.name,u.email,u.role); }
}
record AnimalInput(@NotBlank @Size(max=80) String name, @NotNull Species species,
 @NotNull AnimalSize size, @Min(0) @Max(480) int ageMonths,
 @NotBlank @Size(max=100) String city, @NotBlank @Size(max=3000) String description,
 @NotNull @Size(max=2000) String care,
 @NotNull @Size(max=1000) @Pattern(regexp="^$|^https://[^\\s]+$|^/images/(dog|cat)\\.jpg$",message="Informe uma URL HTTPS ou deixe em branco") String photoUrl,
 boolean vaccinated, boolean neutered) {}
record AnimalView(Long id,String name,Species species,AnimalSize size,int ageMonths,String city,
 String description,String care,String photoUrl,boolean vaccinated,boolean neutered,AnimalStatus status,
 Long ownerId,String ownerName) {
 static AnimalView of(Animal a) { return new AnimalView(a.id,a.name,a.species,a.size,a.ageMonths,a.city,
 a.description,a.care,a.photoUrl,a.vaccinated,a.neutered,a.status,a.owner.id,a.owner.name); }
}
record RequestInput(@NotBlank @Size(max=1000) String housing,
 @NotBlank @Size(max=2000) String routine, @NotBlank @Size(max=2000) String experience,
 @AssertTrue(message="Confirme o compromisso com os cuidados") boolean commitment) {}
record DecisionInput(@NotNull RequestStatus status, @NotNull @Size(max=1000) String note) {}
record RequestView(Long id,AnimalView animal,UserView adopter,String housing,String routine,
 String experience,RequestStatus status,String decisionNote,Instant createdAt) {
 static RequestView of(AdoptionRequest r) { return new RequestView(r.id,AnimalView.of(r.animal),
 UserView.of(r.adopter),r.housing,r.routine,r.experience,r.status,r.decisionNote,r.createdAt); }
}
