package fu.de190685.user.service;

import fu.de190685.user.dto.PageDTO;
import fu.de190685.user.dto.UserDTO;
import fu.de190685.user.entity.Role;
import fu.de190685.user.entity.UserStatus;

public interface UserService {

	UserDTO create(UserDTO userDTO);

	UserDTO update(Long userId, UserDTO userDTO);

	UserDTO getById(Long userId);

	void deactivate(Long userId);

	PageDTO getUsers(int page, int size, Role role, UserStatus status);
}
