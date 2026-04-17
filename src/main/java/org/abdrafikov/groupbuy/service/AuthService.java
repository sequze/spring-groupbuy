package org.abdrafikov.groupbuy.service;

import org.abdrafikov.groupbuy.dto.RegisterRequest;
import org.abdrafikov.groupbuy.model.Role;
import org.abdrafikov.groupbuy.model.User;
import org.abdrafikov.groupbuy.model.choises.GlobalRoleName;
import org.abdrafikov.groupbuy.repository.RoleRepository;
import org.abdrafikov.groupbuy.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(RegisterRequest form) {
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("Пользователь с таким email уже существует");
        }

        Role userRole = roleRepository.findByName(GlobalRoleName.ROLE_USER)
                .orElseThrow(() -> new IllegalStateException("Роль USER не найдена в базе данных"));

        User user = new User();
        user.setEmail(form.getEmail());
        user.setDisplayName(form.getDisplayName());
        user.setPasswordHash(passwordEncoder.encode(form.getPassword()));
        user.setEnabled(true);
        user.getRoles().add(userRole);
        userRepository.save(user);
    }
}