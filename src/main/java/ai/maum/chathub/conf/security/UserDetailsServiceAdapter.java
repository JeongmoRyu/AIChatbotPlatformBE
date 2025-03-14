package ai.maum.chathub.conf.security;

import ai.maum.chathub.api.role.RoleEntity;
import ai.maum.chathub.api.role.RoleRepository;
import ai.maum.chathub.api.user.UserEntity;
import ai.maum.chathub.api.user.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 사용자 인증 시 사용자 정보를 조회하는 서비스
 * @author baekgol
 */
public class UserDetailsServiceAdapter implements UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    UserDetailsServiceAdapter(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByAccount(username);

        if(user == null) return null;

        // FIXME mongodb to maria
        //List<GrantedAuthority> roles = roleRepository.findByIdIn(user.getRoles()).stream()
        List<GrantedAuthority> roles = roleRepository.findById(user.getRoles()).stream()
                .map(RoleEntity::getName)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return UserInfo.builder()
                .username(user.getId().toString())
                .password(user.getPassword())
                .authorities(roles)
                .build();
    }
}
