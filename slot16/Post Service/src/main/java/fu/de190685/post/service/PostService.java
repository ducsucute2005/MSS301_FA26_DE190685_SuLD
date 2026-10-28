package fu.de190685.post.service;

import fu.de190685.post.dto.PageDTO;
import fu.de190685.post.dto.PostDTO;
import fu.de190685.post.dto.PostDetailDTO;
import fu.de190685.post.entity.PostStatus;

public interface PostService {

	PostDTO create(PostDTO postDTO);

	PostDTO update(Long postId, PostDTO postDTO);

	PostDetailDTO getById(Long postId);

	void cancel(Long postId);

	PageDTO getPosts(int page, int size, String advertiserName, PostStatus status);
}
