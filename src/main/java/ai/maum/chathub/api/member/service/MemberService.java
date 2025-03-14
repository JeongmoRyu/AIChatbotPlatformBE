package ai.maum.chathub.api.member.service;

import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberMapper memberMapper;

    public MemberDetail findMemberByUserId(String userId) {
        return memberMapper.findMemberByUsername(userId);
    }

    public MemberDetail findMemberByUserKey(Long userKey) {
        return memberMapper.findMemberByUserKey(userKey);
    }

    public List<MemberDetail> findMemberByName(String name) {
        return memberMapper.findMemberByName(name);
    }

    public MemberDetail loadUserByVendorIdAndVendorType(String vendorUserKey, String vendorType) {
        return memberMapper.findMemberByVendorUserKeyAndVendorType(vendorUserKey, vendorType);
    }

    public MemberDetail findMemberByVendorUserIdAndVendorType(String vendorUserId, String vendorType) {
        return memberMapper.findMemberByVendorUserIdAndVendorType(vendorUserId, vendorType);
    }

    public int updateMemberBotId(String userKey, String botId) {
        Map<String,Object> param = new HashMap<String,Object>();
        param.put("userKey", Long.valueOf(userKey));
        param.put("vendorUserKey", botId);
        return memberMapper.updateMemberBotId(param);
    }
}
