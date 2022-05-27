package se.magnus.api.core.comment;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import se.magnus.api.core.ingredient.Ingredient;

import java.util.Date;
import java.util.List;

public interface CommentService {

    /**
     * Sample usage: curl $HOST:$PORT/comment?mealId=1
     *
     * @param mealId
     * @return
     */
    @GetMapping(
        value    = "/comment",
        produces = "application/json")
    List<Comment> getComments(@RequestParam(value = "mealId", required = true) int mealId);
   
    /**
     * Sample usage:
     *
     * curl -X POST $HOST:$PORT/comment \
     *   -H "Content-Type: application/json" --data \
     *   '{"mealId":123,"commentId":456,"author":"author","subject":"subj","content":"content", "dateTime":null}'
     *
     * @param body
     * @return
     */
    @PostMapping(
        value    = "/comment",
        consumes = "application/json",
        produces = "application/json")
    Comment createComment(@RequestBody Comment body);
    
    /**
     * Sample usage:
     *
     * curl -X DELETE $HOST:$PORT/comment?mealId=1
     *
     * @param commentId
     */
    @DeleteMapping(value = "/comment")
    void deleteComments(@RequestParam(value = "mealId", required = true)  int mealId);
}