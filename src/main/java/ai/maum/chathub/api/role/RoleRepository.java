package ai.maum.chathub.api.role;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRepository extends JpaRepository<RoleEntity, Integer> {
    List<RoleEntity> findByIdIn(List<Integer> roles);
}
