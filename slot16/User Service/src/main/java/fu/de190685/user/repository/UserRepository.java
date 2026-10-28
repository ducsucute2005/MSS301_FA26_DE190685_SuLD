package fu.de190685.user.repository;

import fu.de190685.user.entity.Role;
import fu.de190685.user.entity.User;
import fu.de190685.user.entity.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

	boolean existsByUsername(String username);

	boolean existsByUsernameAndUserIdNot(String username, Long userId);

	Page<User> findByRole(Role role, Pageable pageable);

	Page<User> findByStatus(UserStatus status, Pageable pageable);

	Page<User> findByRoleAndStatus(Role role, UserStatus status, Pageable pageable);
}
