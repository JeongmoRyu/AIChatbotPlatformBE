package ai.maum.chathub.api.member.service;

import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
//public class MemberDetailService implements UserDetailsService {
public class MemberDetailService  {
    private final MemberMapper memberMapper;

    /*
    @Override
    public UserDetails loadUserByUsername(String userKey) throws UsernameNotFoundException {

        log.debug("loadUserByUsername!!!-" + userKey);

        MemberDetail member = null;
        try {
            member = memberMapper.findMemberByUserKey(Long.valueOf(userKey));
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return member;
    }
    */
    public MemberDetail findMemberByUserKey(String userKey) {
        return memberMapper.findMemberByUserKey(Long.valueOf(userKey));
    }
}
