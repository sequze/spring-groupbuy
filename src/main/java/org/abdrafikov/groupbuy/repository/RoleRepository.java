package org.abdrafikov.groupbuy.repository;
import org.abdrafikov.groupbuy.model.Role;
import org.abdrafikov.groupbuy.model.choises.GlobalRoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(GlobalRoleName name);
}