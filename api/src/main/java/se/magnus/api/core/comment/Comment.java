package se.magnus.api.core.comment;
import java.util.Date;

public class Comment {
    private int mealId;
    private int commentId;
    private String author;
    private String subject;
    private String content;
    private Date dateTime;
    private String serviceAddress;

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

	public void setMealId(int mealId) {
		this.mealId = mealId;
	}

	public void setCommentId(int commentId) {
		this.commentId = commentId;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
}