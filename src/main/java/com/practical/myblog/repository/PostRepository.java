package com.practical.myblog.repository;

import com.practical.myblog.model.Post;
import com.practical.myblog.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    // JPQL
    // SELECT p.* FROM post p, post_tag pt, tag t WHERE p.id = pt.post_id AND t.id = pt.tag_id AND t.name = :tagName
    // JOIN p.tags t  >> means it's using the junction table
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.name = :tagName")
    List<Post> findAllPostsByTagName(@Param("tagName") String tagName);

    @Query("SELECT COUNT(t) > 0 FROM Post p JOIN p.tags t WHERE p.id = :postId AND t.name = :tagName")
    boolean existsTagForPost(@Param("postId") Long postId, @Param("tagName") String tagName);

    @Query("SELECT t FROM Post p JOIN p.tags t WHERE p.id = :postId")
    Set<Tag> tagsByPost(@Param("postId") Long postId);
}
