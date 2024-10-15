package com.practical.myblog.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    // This tells Hibernate that the Post entity is the owner of the relationship
    @ManyToMany(mappedBy = "tags")
    private Set<Post> posts = new HashSet<>();
}
