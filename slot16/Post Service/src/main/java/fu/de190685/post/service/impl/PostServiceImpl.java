package fu.de190685.post.service.impl;

import feign.FeignException;
import fu.de190685.post.client.UserServiceClient;
import fu.de190685.post.common.ApiException;
import fu.de190685.post.dto.ApiResponse;
import fu.de190685.post.dto.PageDTO;
import fu.de190685.post.dto.PostDTO;
import fu.de190685.post.dto.PostDetailDTO;
import fu.de190685.post.dto.UserDTO;
import fu.de190685.post.entity.Post;
import fu.de190685.post.entity.PostStatus;
import fu.de190685.post.repository.PostRepository;
import fu.de190685.post.service.PostService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PostServiceImpl implements PostService {

	private final PostRepository postRepository;
	private final UserServiceClient userServiceClient;

	public PostServiceImpl(PostRepository postRepository, UserServiceClient userServiceClient) {
		this.postRepository = postRepository;
		this.userServiceClient = userServiceClient;
	}

	@Override
	public PostDTO create(PostDTO postDTO) {
		LocalDate startDate = toLocalDate(postDTO.getStartDate());
		LocalDate endDate = toLocalDate(postDTO.getEndDate());
		validateDates(startDate, endDate);

		UserDTO user = getUser(postDTO.getUserId());
		validateActiveCreator(user);
		validatePostCount(postDTO.getNumberOfPosts(), user);

		Post post = new Post();
		apply(post, postDTO);
		post.setStatus(postDTO.getStatus() == null ? PostStatus.SCHEDULED : PostStatus.valueOf(postDTO.getStatus()));
		post.setTotalCost(calculateCost(user.getRatePerPost(), startDate, endDate));
		return toDTO(postRepository.save(post));
	}

	@Override
	public PostDTO update(Long postId, PostDTO postDTO) {
		Post post = findPost(postId);
		UserDTO user = getUser(post.getUserId());
		validateCreatorData(user);

		if (postDTO.getUserId() != null && !postDTO.getUserId().equals(post.getUserId())) {
			user = getUser(postDTO.getUserId());
			validateActiveCreator(user);
			post.setUserId(user.getUserId());
		}

		apply(post, postDTO);
		validateDates(post.getStartDate(), post.getEndDate());
		validatePostCount(post.getNumberOfPosts(), user);
		post.setTotalCost(calculateCost(user.getRatePerPost(), post.getStartDate(), post.getEndDate()));
		return toDTO(postRepository.save(post));
	}

	@Override
	@Transactional(readOnly = true)
	public PostDetailDTO getById(Long postId) {
		Post post = findPost(postId);
		PostDetailDTO detail = toDetailDTO(post);
		detail.setUser(getUser(post.getUserId()));
		return detail;
	}

	@Override
	public void cancel(Long postId) {
		Post post = findPost(postId);
		post.setStatus(PostStatus.CANCELLED);
		postRepository.save(post);
	}

	@Override
	@Transactional(readOnly = true)
	public PageDTO getPosts(int page, int size, String advertiserName, PostStatus status) {
		Page<Post> posts = postRepository.search(
				advertiserName == null || advertiserName.isBlank() ? null : advertiserName,
				status, PageRequest.of(page, size));
		List<PostDetailDTO> content = posts.getContent().stream().map(post -> {
			PostDetailDTO detail = toDetailDTO(post);
			detail.setUser(getUser(post.getUserId()));
			return detail;
		}).toList();
		return new PageDTO(size, page, posts.getTotalPages(), posts.getTotalElements(),
				posts.isFirst(), posts.isLast(), content);
	}

	private Post findPost(Long postId) {
		return postRepository.findById(postId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, 4, "Post not found"));
	}

	private UserDTO getUser(Long userId) {
		try {
			ApiResponse<UserDTO> response = userServiceClient.getUserById(userId);
			if (response == null || response.getData() == null) {
				throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, 0,
						"User Service returned an invalid response");
			}
			return response.getData();
		} catch (FeignException.NotFound exception) {
			throw new ApiException(HttpStatus.NOT_FOUND, 4, "User not found");
		}
	}

	private void validateActiveCreator(UserDTO user) {
		if (!"ACTIVE".equals(user.getStatus())) {
			throw new ApiException(HttpStatus.BAD_REQUEST, 5, "User is not ACTIVE");
		}
		validateCreatorData(user);
	}

	private void validateCreatorData(UserDTO user) {
		if (user.getRatePerPost() == null || user.getMaxDailyPosts() == null) {
			throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, 0,
					"User Service returned incomplete creator data");
		}
	}

	private void validatePostCount(Integer numberOfPosts, UserDTO user) {
		if (numberOfPosts == null || numberOfPosts < 1 || numberOfPosts > user.getMaxDailyPosts()) {
			throw new ApiException(HttpStatus.NOT_ACCEPTABLE, 2,
					"numberOfPosts must be between 1 and the user's maxDailyPosts");
		}
	}

	private void validateDates(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null || !endDate.isAfter(startDate)) {
			throw new ApiException(HttpStatus.NOT_ACCEPTABLE, 2, "endDate must be after startDate");
		}
	}

	private BigDecimal calculateCost(BigDecimal ratePerPost, LocalDate startDate, LocalDate endDate) {
		long numberOfDays = ChronoUnit.DAYS.between(startDate, endDate);
		return ratePerPost.multiply(BigDecimal.valueOf(numberOfDays));
	}

	private void apply(Post post, PostDTO postDTO) {
		if (postDTO.getAdvertiserName() != null) {
			post.setAdvertiserName(postDTO.getAdvertiserName());
		}
		if (postDTO.getAdvertiserEmail() != null) {
			post.setAdvertiserEmail(postDTO.getAdvertiserEmail());
		}
		if (postDTO.getAdvertiserPhone() != null) {
			post.setAdvertiserPhone(postDTO.getAdvertiserPhone());
		}
		if (postDTO.getStartDate() != null) {
			post.setStartDate(toLocalDate(postDTO.getStartDate()));
		}
		if (postDTO.getEndDate() != null) {
			post.setEndDate(toLocalDate(postDTO.getEndDate()));
		}
		if (postDTO.getNumberOfPosts() != null) {
			post.setNumberOfPosts(postDTO.getNumberOfPosts());
		}
		if (postDTO.getStatus() != null) {
			post.setStatus(PostStatus.valueOf(postDTO.getStatus()));
		}
		if (postDTO.getUserId() != null) {
			post.setUserId(postDTO.getUserId());
		}
	}

	private PostDTO toDTO(Post post) {
		PostDTO postDTO = new PostDTO();
		postDTO.setPostId(post.getPostId());
		postDTO.setAdvertiserName(post.getAdvertiserName());
		postDTO.setAdvertiserEmail(post.getAdvertiserEmail());
		postDTO.setAdvertiserPhone(post.getAdvertiserPhone());
		postDTO.setStartDate(toDate(post.getStartDate()));
		postDTO.setEndDate(toDate(post.getEndDate()));
		postDTO.setNumberOfPosts(post.getNumberOfPosts());
		postDTO.setTotalCost(post.getTotalCost());
		postDTO.setStatus(post.getStatus().name());
		postDTO.setUserId(post.getUserId());
		return postDTO;
	}

	private PostDetailDTO toDetailDTO(Post post) {
		PostDetailDTO detail = new PostDetailDTO();
		detail.setPostId(post.getPostId());
		detail.setAdvertiserName(post.getAdvertiserName());
		detail.setAdvertiserEmail(post.getAdvertiserEmail());
		detail.setAdvertiserPhone(post.getAdvertiserPhone());
		detail.setStartDate(toDate(post.getStartDate()));
		detail.setEndDate(toDate(post.getEndDate()));
		detail.setNumberOfPosts(post.getNumberOfPosts());
		detail.setTotalCost(post.getTotalCost());
		detail.setStatus(post.getStatus().name());
		return detail;
	}

	private LocalDate toLocalDate(Date date) {
		return date == null ? null : Instant.ofEpochMilli(date.getTime()).atZone(ZoneOffset.UTC).toLocalDate();
	}

	private Date toDate(LocalDate date) {
		return date == null ? null : Date.from(date.atStartOfDay().toInstant(ZoneOffset.UTC));
	}
}
