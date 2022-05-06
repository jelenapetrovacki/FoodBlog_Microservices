package se.magnus.api.core.comment;
import java.util.Date;

public class Comment {
    private final int mealId;
    private final int commentId;
    private final String author;
    private final String subject;
    private final String content;
    private final Date dateTime;
    private final String serviceAddress;

    public Comment() {
        mealId = 0;
        commentId = 0;
        author = null;
        subject = null;
        content = null;
        dateTime = null;
        serviceAddress = null;
    }

    public Comment(int mealId, int commentId, String author, String subject, String content, Date dateTime, String serviceAddress) {
        this.mealId = mealId;
        this.commentId = commentId;
        this.author = author;
        this.subject = subject;
        this.content = content;
        this.dateTime = dateTime;
        this.serviceAddress = serviceAddress;
    }

    public int getMealId() {
        return mealId;
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

    public String getContent() {
        return content;
    }
    
    public Date getDateTime() {
        return dateTime;
    }
    
    public String getServiceAddress() {
        return serviceAddress;
    }
}