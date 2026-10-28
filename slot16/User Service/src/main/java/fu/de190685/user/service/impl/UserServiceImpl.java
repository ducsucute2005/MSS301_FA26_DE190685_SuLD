package fu.de190685.user.service.impl;

import fu.de190685.user.common.ApiException;
import fu.de190685.user.dto.PageDTO;
import fu.de190685.user.dto.UserDTO;
import fu.de190685.user.entity.Role;
import fu.de190685.user.entity.User;
import fu.de190685.user.entity.UserStatus;
import fu.de190685.user.repository.UserRepository;
import fu.de190685.user.service.UserService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;

	public UserServiceImpl(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	@Override
	public UserDTO create(UserDTO userDTO) {
		validateRequiredFields(userDTO);
		if (userRepository.existsByUsername(userDTO.getUsername())) {
			throw new ApiException(HttpStatus.valueOf(226), 3, "Duplicate username");
		}

		User user = new User();
		apply(user, userDTO);
		if (userDTO.getStatus() == null) {
			user.setStatus(UserStatus.ACTIVE);
		}
		return toDTO(userRepository.save(user));
	}

	@Override
	public UserDTO update(Long userId, UserDTO userDTO) {
		User user = findUser(userId);
		if (userDTO.getUsername() != null
				&& userRepository.existsByUsernameAndUserIdNot(userDTO.getUsername(), userId)) {
			throw new ApiException(HttpStatus.valueOf(226), 3, "Duplicate username");
		}

		apply(user, userDTO);
		validateRequiredFields(toDTO(user));
		return toDTO(userRepository.save(user));
	}

	@Override
	@Transactional(readOnly = true)
	public UserDTO getById(Long userId) {
		return toDTO(findUser(userId));
	}

	@Override
	public void deactivate(Long userId) {
		User user = findUser(userId);
		user.setStatus(UserStatus.DEACTIVATED);
		userRepository.save(user);
	}

	@Override
	@Transactional(readOnly = true)
	public PageDTO getUsers(int page, int size, Role role, UserStatus status) {
		PageRequest pageRequest = PageRequest.of(page, size);
		Page<User> users;
		if (role != null && status != null) {
			users = userRepository.findByRoleAndStatus(role, status, pageRequest);
		} else if (role != null) {
			users = userRepository.findByRole(role, pageRequest);
		} else if (status != null) {
			users = userRepository.findByStatus(status, pageRequest);
		} else {
			users = userRepository.findAll(pageRequest);
		}

		List<UserDTO> content = users.getContent().stream().map(this::toDTO).toList();
		return new PageDTO(size, page, users.getTotalPages(), users.getTotalElements(),
				users.isFirst(), users.isLast(), content);
	}

	private User findUser(Long userId) {
		return userRepository.findById(userId)
				.orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, 4, "User not found"));
	}

	private void validateRequiredFields(UserDTO userDTO) {
		if (userDTO.getUsername() == null || userDTO.getUsername().isBlank()
				|| userDTO.getEmail() == null || userDTO.getEmail().isBlank()
				|| userDTO.getRole() == null || userDTO.getRatePerPost() == null
				|| userDTO.getMaxDailyPosts() == null || userDTO.getLevel() == null) {
			throw new ApiException(HttpStatus.NOT_ACCEPTABLE, 2, "Validation failed: required fields are missing");
		}
	}

	private void apply(User user, UserDTO userDTO) {
		if (userDTO.getUsername() != null) {
			user.setUsername(userDTO.getUsername());
		}
		if (userDTO.getEmail() != null) {
			user.setEmail(userDTO.getEmail());
		}
		if (userDTO.getRole() != null) {
			user.setRole(Role.valueOf(userDTO.getRole()));
		}
		if (userDTO.getRatePerPost() != null) {
			user.setRatePerPost(userDTO.getRatePerPost());
		}
		if (userDTO.getMaxDailyPosts() != null) {
			user.setMaxDailyPosts(userDTO.getMaxDailyPosts());
		}
		if (userDTO.getLevel() != null) {
			user.setLevel(userDTO.getLevel());
		}
		if (userDTO.getStatus() != null) {
			user.setStatus(UserStatus.valueOf(userDTO.getStatus()));
		}
	}

	private UserDTO toDTO(User user) {
		UserDTO userDTO = new UserDTO();
		userDTO.setUserId(user.getUserId());
		userDTO.setUsername(user.getUsername());
		userDTO.setEmail(user.getEmail());
		userDTO.setRole(user.getRole() == null ? null : user.getRole().name());
		userDTO.setRatePerPost(user.getRatePerPost());
		userDTO.setMaxDailyPosts(user.getMaxDailyPosts());
		userDTO.setLevel(user.getLevel());
		userDTO.setStatus(user.getStatus() == null ? null : user.getStatus().name());
		return userDTO;
	}
}
