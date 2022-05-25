package se.magnus.microservices.core.comment.persistence;

import java.util.Date;

import javax.persistence.*;

@Entity
@Table(name = "comments", indexes = { @Index(name = "comments_unique_idx", unique = true, columnList = "mealId,commentId") })
public class CommentEntity {

	@Id @GeneratedValue
    private int id;

    @Version
    private int version;

    private int mealId;
    private int commentId;
    private String author;
    private String subject;
    private String content;
    private Date dateTime;
    
	public CommentEntity() {
	}
	
	public CommentEntity(int mealId, int commentId, String author, String subject, String content,
			Date dateTime) {
		this.mealId = mealId;
		this.commentId = commentId;
		this.author = author;
		this.subject = subject;
		this.content = content;
		this.dateTime = dateTime;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getMealId() {
		return mealId;
	}

	public void setMealId(int mealId) {
		this.mealId = mealId;
	}

	public int getCommentId() {
		return commentId;
	}

	public void setCommentId(int commentId) {
		this.commentId = commentId;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}


    
}
