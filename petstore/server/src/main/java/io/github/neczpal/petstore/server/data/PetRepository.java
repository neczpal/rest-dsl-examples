package io.github.neczpal.petstore.server.data;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<PetEntity, Integer> {
    List<PetEntity> findByStatus(String status);
    List<PetEntity> findByTagsNameIn(List<String> tags);
}
