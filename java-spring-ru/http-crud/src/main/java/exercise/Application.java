package exercise;

import exercise.model.Post;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@SpringBootApplication
@RestController
public class Application {
    // Хранилище добавленных постов
    private List<Post> posts = Data.getPosts();

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    // BEGIN
    @GetMapping("/posts") // список всех постов
    public List<Post> getAll() {
        return posts.stream()
                .toList();
    }

    @GetMapping("/posts/{id}")
    public Optional<Post> getOne(@PathVariable String id) {
        return posts.stream()
                .filter(post -> post.getId().equals(id))
                .findFirst();
    }

    @PostMapping("/posts")
    public Post create(@RequestBody Post post) {
        posts.add(post);
        return post;
    }

    @PutMapping("/posts/{id}")
    public Post update(@PathVariable String id, @RequestBody Post updatedPost) {
        var optionalPost = posts.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
        if (optionalPost.isPresent()) {
            var post = optionalPost.get();
            post.setId(updatedPost.getId());
            post.setTitle(updatedPost.getTitle());
            post.setBody(updatedPost.getBody());
        }
        return updatedPost;
    }

    @DeleteMapping("/posts/{id}")
    public void delete(@PathVariable String id) {
        posts.removeIf(p -> p.getId().equals(id));
    }
    // END
}
