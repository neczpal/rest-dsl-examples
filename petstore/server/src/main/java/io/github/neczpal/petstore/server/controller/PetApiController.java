package io.github.neczpal.petstore.server.controller;

import io.github.neczpal.petstore.server.*;
import io.github.neczpal.petstore.server.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class PetApiController implements PetApi {

    private final PetRepository petRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;

    public PetApiController(PetRepository petRepository, CategoryRepository categoryRepository, TagRepository tagRepository) {
        this.petRepository = petRepository;
        this.categoryRepository = categoryRepository;
        this.tagRepository = tagRepository;
    }

    private PetEntity toEntity(Pet pet) {
        if (pet == null) return null;
        PetEntity entity = new PetEntity();
        entity.setId(pet.id());
        entity.setName(pet.name());
        entity.setStatus(pet.status());
        if (pet.photoUrls() != null) {
            entity.setPhotoUrls(pet.photoUrls());
        }
        if (pet.category() != null) {
            // Find managed entity or create a new one if it doesn't exist
            CategoryEntity categoryEntity = null;
            if (pet.category().id() != null) {
                 categoryEntity = categoryRepository.findById(pet.category().id()).orElse(null);
            }
            if (categoryEntity == null) {
                 categoryEntity = new CategoryEntity();
                 categoryEntity.setName(pet.category().name());
                 categoryEntity = categoryRepository.save(categoryEntity);
            }
            entity.setCategory(categoryEntity);
        }
        if (pet.tags() != null) {
            entity.setTags(pet.tags().stream().map(t -> {
                 TagEntity tagEntity = null;
                 if (t.id() != null) {
                      tagEntity = tagRepository.findById(t.id()).orElse(null);
                 }
                 if (tagEntity == null) {
                      tagEntity = new TagEntity();
                      tagEntity.setName(t.name());
                      tagEntity = tagRepository.save(tagEntity);
                 }
                 return tagEntity;
            }).collect(Collectors.toList()));
        }
        return entity;
    }

    private Pet toDto(PetEntity entity) {
        if (entity == null) return null;
        return new Pet(
                entity.getId(),
                entity.getName(),
                entity.getCategory() == null ? null : new Category(entity.getCategory().getId(), entity.getCategory().getName()),
                entity.getPhotoUrls(),
                entity.getTags() == null ? null : entity.getTags().stream().map(t -> new Tag(t.getId(), t.getName())).collect(Collectors.toList()),
                entity.getStatus()
        );
    }

    @Override
    public ResponseEntity<Pet> updatePet(Pet body) {
        log.info("Attempting to update pet with ID: {}", body.id());
        if (body.id() == null || !petRepository.existsById(body.id())) {
            log.warn("Pet with ID {} not found for update.", body.id());
            return ResponseEntity.notFound().build();
        }
        PetEntity saved = petRepository.save(toEntity(body));
        log.info("Successfully updated pet with ID: {}", saved.getId());
        return ResponseEntity.ok(toDto(saved));
    }

    @Override
    public ResponseEntity<Pet> addPet(Pet body) {
        log.info("Attempting to add a new pet with name: {}", body.name());
        PetEntity saved = petRepository.save(toEntity(body));
        log.info("Successfully added new pet with ID: {}", saved.getId());
        return ResponseEntity.ok(toDto(saved));
    }

    @Override
    public ResponseEntity<List<Pet>> findPetsByStatus(String status) {
        log.info("Searching for pets with status: {}", status);
        List<Pet> pets = petRepository.findByStatus(status).stream().map(this::toDto).collect(Collectors.toList());
        log.info("Found {} pets with status: {}", pets.size(), status);
        return ResponseEntity.ok(pets);
    }

    @Override
    public ResponseEntity<List<Pet>> findPetsByTags(List<String> tags) {
        log.info("Searching for pets with tags: {}", tags);
        List<Pet> pets = petRepository.findByTagsNameIn(tags).stream().map(this::toDto).collect(Collectors.toList());
        log.info("Found {} pets with tags: {}", pets.size(), tags);
        return ResponseEntity.ok(pets);
    }

    @Override
    public ResponseEntity<Pet> getPetById(Integer petId) {
        log.info("Searching for pet with ID: {}", petId);
        return petRepository.findById(petId)
                .map(entity -> {
                    log.info("Found pet with ID: {}", petId);
                    return ResponseEntity.ok(toDto(entity));
                })
                .orElseGet(() -> {
                    log.warn("Pet with ID {} not found.", petId);
                    return ResponseEntity.notFound().build();
                });
    }

    @Override
    public ResponseEntity<Pet> updatePetWithForm(Integer petId, String name, String status) {
        log.info("Updating pet with form data for ID: {}", petId);
        return petRepository.findById(petId).map(entity -> {
            entity.setName(name);
            entity.setStatus(status);
            PetEntity saved = petRepository.save(entity);
            log.info("Successfully updated pet with ID: {}", saved.getId());
            return ResponseEntity.ok(toDto(saved));
        }).orElseGet(() -> {
            log.warn("Pet with ID {} not found for form update.", petId);
            return ResponseEntity.notFound().build();
        });
    }

    @Override
    public ResponseEntity<Void> deletePet(Integer petId) {
        log.info("Attempting to delete pet with ID: {}", petId);
        if (petRepository.existsById(petId)) {
            petRepository.deleteById(petId);
            log.info("Successfully deleted pet with ID: {}", petId);
            return ResponseEntity.ok().build();
        }
        log.warn("Pet with ID {} not found for deletion.", petId);
        return ResponseEntity.notFound().build();
    }

    @Override
    public ResponseEntity<ApiResponse> uploadFile(Integer petId, String additionalMetadata, String body) {
        log.info("Uploading file for pet ID: {}", petId);
        return ResponseEntity.ok(new ApiResponse(200, "success", "Uploaded to pet " + petId));
    }
}
