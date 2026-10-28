package fu.de190685.post.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "posts")
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "post_id")
	private Long postId;

	@Column(name = "advertiser_name", nullable = false, length = 100)
	private String advertiserName;

	@Column(name = "advertiser_email", nullable = false, length = 100)
	private String advertiserEmail;

	@Column(name = "advertiser_phone", nullable = false, length = 20)
	private String advertiserPhone;

	@Column(name = "start_date", nullable = false)
	private LocalDate startDate;

	@Column(name = "end_date", nullable = false)
	private LocalDate endDate;

	@Column(name = "number_of_posts", nullable = false)
	private Integer numberOfPosts;

	@Column(name = "total_cost", nullable = false, precision = 18, scale = 2)
	private BigDecimal totalCost;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 15)
	private PostStatus status;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	public Post() {
	}

	public Long getPostId() {
		return postId;
	}

	public void setPostId(Long postId) {
		this.postId = postId;
	}

	public String getAdvertiserName() {
		return advertiserName;
	}

	public void setAdvertiserName(String advertiserName) {
		this.advertiserName = advertiserName;
	}

	public String getAdvertiserEmail() {
		return advertiserEmail;
	}

	public void setAdvertiserEmail(String advertiserEmail) {
		this.advertiserEmail = advertiserEmail;
	}

	public String getAdvertiserPhone() {
		return advertiserPhone;
	}

	public void setAdvertiserPhone(String advertiserPhone) {
		this.advertiserPhone = advertiserPhone;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}

	public Integer getNumberOfPosts() {
		return numberOfPosts;
	}

	public void setNumberOfPosts(Integer numberOfPosts) {
		this.numberOfPosts = numberOfPosts;
	}

	public BigDecimal getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(BigDecimal totalCost) {
		this.totalCost = totalCost;
	}

	public PostStatus getStatus() {
		return status;
	}

	public void setStatus(PostStatus status) {
		this.status = status;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}
}
