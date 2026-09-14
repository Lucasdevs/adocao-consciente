package br.edu.unifacisa.adocao;

import java.util.*;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

interface Users extends JpaRepository<AppUser,Long> {
    Optional<AppUser> findByEmail(String email);
    boolean existsByEmail(String email);
}
interface Animals extends JpaRepository<Animal,Long> {
    List<Animal> findByOwnerIdOrderByIdDesc(Long id);
    List<Animal> findByStatusOrderByIdDesc(AnimalStatus status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Animal a where a.id = :id")
    Optional<Animal> lockById(@Param("id") Long id);
}
interface Requests extends JpaRepository<AdoptionRequest,Long> {
    List<AdoptionRequest> findByAdopterIdOrderByCreatedAtDesc(Long id);
    List<AdoptionRequest> findByAnimalOwnerIdOrderByCreatedAtDesc(Long id);
    List<AdoptionRequest> findByAnimalId(Long id);
    boolean existsByAnimalIdAndAdopterIdAndStatusIn(Long animalId, Long adopterId, Collection<RequestStatus> statuses);
}
