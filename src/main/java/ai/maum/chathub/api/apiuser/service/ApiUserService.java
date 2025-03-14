package ai.maum.chathub.api.apiuser.service;

import ai.maum.chathub.api.apiuser.model.ApiUser;
import ai.maum.chathub.api.apiuser.mapper.ApiUserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiUserService  implements UserDetailsService {
    private final ApiUserMapper apiUserMapper;
    public ApiUser getApiUser(String vendorId) {
        return apiUserMapper.getApiUserById(vendorId);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ApiUser apiUser = apiUserMapper.getApiUserById(username);
        return new User(apiUser.getVendorId(), apiUser.getApiKey(), new ArrayList<>());
    }
}
