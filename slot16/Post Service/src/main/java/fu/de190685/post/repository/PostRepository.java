package fu.de190685.post.repository;

import fu.de190685.post.entity.Post;
import fu.de190685.post.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

	@Query("""
			select p from Post p
			where (:advertiserName is null or lower(p.advertiserName) like lower(concat('%', :advertiserName, '%')))
			  and (:status is null or p.status = :status)
			""")
	Page<Post> search(@Param("advertiserName") String advertiserName,
			@Param("status") PostStatus status, Pageable pageable);
}
