package se.magnus.api.composite.meal;

public class CommentSummary {
    private final int commentId;
    private final String author;
    private final String subject;

    public CommentSummary(int commentId, String author, String subject) {
        this.commentId = commentId;
        this.author = author;
        this.subject = subject;
    }

    public int getCommentId() {
        return commentId;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubject() {
        return subject;
    }
}