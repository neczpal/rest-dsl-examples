package io.github.neczpal.petstore.server.data;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Data
public class PetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String name;

    @ManyToOne(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private CategoryEntity category;

    @ElementCollection
    private List<String> photoUrls;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<TagEntity> tags;

    private String status;
}
