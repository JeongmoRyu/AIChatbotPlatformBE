package ai.maum.chathub.api.member.mapper;

import ai.maum.chathub.api.member.dto.MemberDetail;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface MemberMapper {
    public MemberDetail findMemberByUserKey(Long userKey);
    public MemberDetail findMemberByVendorUserKeyAndVendorType(String vendorUserKey, String vendorType);
    public MemberDetail findMemberByReceiveIdAndVendorType(String receiveId, String vendorType);
    public MemberDetail findMemberByVendorUserIdAndVendorType(String vendorUserId, String vendorType);
    public int updateMemberBotId(Map<String,Object> param);
    public List<MemberDetail> findMemberByName(String name);
    public int insertMember(MemberDetail memberDetail);
    public int upsertMember(MemberDetail memberDetail);
    public int insertMembers(List<MemberDetail> memberDetails);
    public MemberDetail findMemberByUsername(String username);
    public int updateMember(MemberDetail memberDetail);
    public List<MemberDetail> selectMemberList();
    public int deleteMemberByUserKey(Long userKey);
    public int softDeleteMemberByUserKey(Long userKey);
    public int backupMemberBeforeDelete(Long userKey);
    public List<String> findIdByInList(List<String> idList);
    public Integer getMyRoleLevel(Long userKey);
}
