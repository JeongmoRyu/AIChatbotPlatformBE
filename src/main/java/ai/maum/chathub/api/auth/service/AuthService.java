package ai.maum.chathub.api.auth.service;

import ai.maum.chathub.api.auth.dto.LoginMember;
import ai.maum.chathub.api.auth.dto.LoginParam;
import ai.maum.chathub.api.auth.dto.res.MemberForMemberList;
import ai.maum.chathub.api.auth.repo.MemberForMemberListRepository;
import ai.maum.chathub.api.chatplay.dto.req.ChatplayReq;
import ai.maum.chathub.api.chatplay.service.ChatplayService;
import ai.maum.chathub.api.common.BaseException;
import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.member.entity.MemberEntity;
import ai.maum.chathub.api.member.entity.MemberOrganizationEntity;
import ai.maum.chathub.api.member.entity.MemberOrganizationId;
import ai.maum.chathub.api.member.entity.OrganizationEntity;
import ai.maum.chathub.api.member.mapper.MemberMapper;
import ai.maum.chathub.api.member.repo.MemberOrganizationRepository;
import ai.maum.chathub.api.member.repo.MemberRepository;
import ai.maum.chathub.api.member.repo.OrganizationRepository;
import ai.maum.chathub.api.member.service.MemberOrganizationService;
import ai.maum.chathub.external.api.n8n.service.N8nService;
import ai.maum.chathub.meta.ResponseMeta;
import ai.maum.chathub.util.CryptoUtil;
import ai.maum.chathub.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {
    private final MemberOrganizationService memberOrganizationService;
    private final N8nService n8nService;
    @Value("${service.inner.chatbot-id}")
    private Long defaultChatbotId;

    @Value("${service.chatplay.enable}")
    Boolean CHATPLAY_ENABLE;

    @Value("${service.n8n.enable}")
    Boolean N8N_ENABLE;

    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final ChatplayService chatplayService;
    private final MemberForMemberListRepository memberListRepository;
    private final MemberRepository memberRepository;
    private final OrganizationRepository organizationRepository;
    private final MemberOrganizationRepository memberOrganizationRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("loadUserByUsername!!!-" + username);

        MemberDetail member = null;
        try {
            member = memberMapper.findMemberByUsername(username);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return member;
    }

    @Transactional
    public MemberDetail createAccount(MemberDetail memberDetail, Boolean bHash, String authorizationHeader, Long organizationId) {

        String hashPwd = bHash? CryptoUtil.encode(memberDetail.getPassword()):memberDetail.getPassword();
        String encPassword = passwordEncoder.encode(hashPwd);
        log.debug("encPassword:" + encPassword);
        memberDetail.setPassword(encPassword);

        if(memberDetail.getIsSuperAdmin() != null && memberDetail.getIsSuperAdmin()) {
            memberDetail.setRoles("ROLE_USER|ROLE_EDITOR|ROLE_ADMIN|ROLE_SUPER_ADMIN");
        } else if (memberDetail.getIsAdmin() != null && memberDetail.getIsAdmin()){
            memberDetail.setRoles("ROLE_USER|ROLE_EDITOR|ROLE_ADMIN");
        } else if (memberDetail.getIsEditor() != null && memberDetail.getIsEditor()){
            memberDetail.setRoles("ROLE_USER|ROLE_EDITOR");
        } else {
            memberDetail.setRoles("ROLE_USER");
        }

        Integer result = memberMapper.insertMember(memberDetail);

        if(result != null && result > 0L) {
            //조직 매핑 정보 insert
//          MemberEntity memberEntity = memberRepository.findMemberEntityByUserKey(memberDetail.getUserKey()).orElse(null);
//          OrganizationEntity organizationEntity = organizationRepository.findById(orgId).orElse(null);
            MemberEntity memberProxy = memberRepository.getReferenceById(memberDetail.getUserKey()); //getReferenceById를 통해 DB를 직접 조회하지 않고 객체만 생성
            OrganizationEntity organizationProxy = organizationRepository.getReferenceById(organizationId);
            MemberOrganizationEntity memberOrganization = new MemberOrganizationEntity();
            memberOrganization.setId(new MemberOrganizationId(memberDetail.getUserKey(), organizationId));
            memberOrganization.setMember(memberProxy);
            memberOrganization.setOrganization(organizationProxy);
            memberOrganizationRepository.save(memberOrganization);

            if (CHATPLAY_ENABLE) {
                List<ChatplayReq> userList = new ArrayList<>();
                ChatplayReq chatPlayUser = new ChatplayReq(memberDetail.getUsername(), hashPwd, memberDetail.getName(), null, null, null, null);
                userList.add(chatPlayUser);
                BaseResponse<Void> chatplayResponse = chatplayService.processUser(userList, HttpMethod.POST, authorizationHeader);
            }

            if(N8N_ENABLE) {
                List<ChatplayReq> userList = new ArrayList<>();
                //n8n은 password hash 로직이 상이하여 일단 plaintext password로 넘긴 후 service 에서 처리
                ChatplayReq chatPlayUser = new ChatplayReq(memberDetail.getUsername(), memberDetail.getPassword(), memberDetail.getName(), null, null, null, null);
                userList.add(chatPlayUser);
                BaseResponse<Void> n8nResponse = n8nService.addAccount(userList);
            }

            return memberDetail;
        } else
            return null;
    }

    public void createAccounts(List<MemberDetail> memberDetail, Boolean bHash, String authorizationHeader, Long organizationId) {
        Integer cnt = 0;
        for (MemberDetail member : memberDetail) {
            MemberDetail result = createAccount(member, bHash, authorizationHeader, organizationId);
            if (result == null) {
                log.error("Failed to create account: {}" ,member.getUsername());
            } else {
                cnt++;
                log.debug("succeed to create account: {}" ,member.getUsername());
            }
        }
        log.debug("succees count : {}", cnt);
    }

    public BaseResponse<Void> createAccountFromFile(List<MultipartFile> files, MemberDetail user) {
        Long organizationId = memberOrganizationService.getMemberOrganizationId(user.getUserKey());
        for (MultipartFile file : files) {
            // 엑셀 파일 확인
            if (!file.getOriginalFilename().endsWith(".xlsx") && !file.getOriginalFilename().endsWith(".xls") ) {
                return BaseResponse.failure("지원하지 않는 확장자 입니다.");
//                throw new IllegalArgumentException("Invalid file format. Only .xlsx files are supported.");
            }

            List<MemberDetail> userList = new ArrayList<>();

            try (InputStream inputStream = file.getInputStream()) {

                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0); // 첫 번째 시트만 처리


                int iStartRow = 2;
                for (int i = iStartRow; i <= sheet.getLastRowNum(); i++) { // 첫 번째 행은 헤더, 두번째 행은 샘플, 세번째 행 부터 처리
                    MemberDetail newMemberDetail = new MemberDetail();
                    Row row = sheet.getRow(i);

                    newMemberDetail.setUsername(getCellValue(row.getCell(0)));
                    newMemberDetail.setPassword(getCellValue(row.getCell(1)));
                    newMemberDetail.setName(getCellValue(row.getCell(2)));

                    switch(String.valueOf(getCellValue(row.getCell(3)))) {
                        case "User":
                            newMemberDetail.setRoles("ROLE_USER");
                            break;
                        case "Editor":
                            newMemberDetail.setRoles("ROLE_USER|ROLE_EDITOR");
                            newMemberDetail.setIsEditor(true);
                            break;
                        case "Admin":
                            newMemberDetail.setRoles("ROLE_USER|ROLE_EDITOR|ROLE_ADMIN");
                            newMemberDetail.setIsAdmin(true);
                            break;
                        case "SuperAdmin":
                            newMemberDetail.setRoles("ROLE_USER|ROLE_EDITOR|ROLE_ADMIN|ROLE_SUPER_ADMIN");
                            newMemberDetail.setIsSuperAdmin(true);
                            break;
                        default:
                    }

                    userList.add(newMemberDetail);

                    log.debug("execute - {} - id: {}, password: {}, name: {}, role: {}", i, newMemberDetail.getUsername(), newMemberDetail.getPassword(), newMemberDetail.getName(), getCellValue(row.getCell(3)));
                }

                // 파일내 중복 체크
                int iRow = iStartRow + 1;
                List<String> errorRows = new ArrayList<>();
                HashMap<String, Integer> idList = new HashMap<>();
                HashSet<String> idSet = new HashSet<>();
                for(MemberDetail account: userList) {
                    log.debug(account.getUsername());
                    idList.put(account.getUsername(), iRow);
                    if(!idSet.add(account.getUsername())) {
                        log.debug("duplicate - {} - id: {}", iRow, account.getUsername());
                        errorRows.add(String.valueOf(iRow));
                    }
                    iRow++;
                }

                if(errorRows.size() > 0) {
                    return BaseResponse.failure(ResponseMeta.DUPLICATE_IN_FILE, "오류위치 : " + String.join(",", errorRows) + " 행");
                }

                // 데이터 유효성 검증
                errorRows.clear();
                iRow = iStartRow + 1;
                for(MemberDetail account: userList) {
                    log.debug(account.getUsername());

                    if(account.getUsername() == null || account.getUsername().isEmpty() || account.getUsername().isBlank() ||
                            account.getPassword() == null || account.getPassword().isEmpty() || account.getPassword().isBlank() ||
                            account.getName() == null || account.getName().isEmpty() || account.getName().isBlank() ||
                            account.getRoles() == null || account.getRoles().isEmpty() || account.getRoles().isBlank()) {
                        errorRows.add(String.valueOf(iRow));

                    }
                    iRow++;
                }

                if(errorRows.size() > 0) {
                    return BaseResponse.failure(ResponseMeta.NO_MANDATORY_VALUE, "오류위치 : " + String.join(",", errorRows) + " 행");
                }

                // 사용중 ID 체크
                List<String> selectedIds = memberMapper.findIdByInList(new ArrayList<>(idSet));
                errorRows.clear();
                if(selectedIds.size() > 0) {
                    for(String id : selectedIds) {
                        errorRows.add(idList.get(id).toString());
                        log.debug("duplicate - id: {}, {}", id, idList.get(id).toString());
                    }
                    Collections.sort(errorRows);
                    return BaseResponse.failure(ResponseMeta.USING_ID_EXIST, "오류위치 : " + String.join(",", errorRows) + " 행");
                }

                createAccounts(userList, true, null, organizationId);

            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        return BaseResponse.success();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf((int) cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    public MemberDetail modifyMyAccount(MemberDetail memberDetail, String authorizationHeader) {
        int result = memberMapper.updateMember(memberDetail);

        if(result > 0) {
            if(CHATPLAY_ENABLE) {
                ChatplayReq user = new ChatplayReq(memberDetail.getUsername(),
                        null,
                        memberDetail.getName() == null || memberDetail.getName().isEmpty() ? null : memberDetail.getName(),
                        null, null, null, null);
                BaseResponse<Void> chatplayResponse = chatplayService.processUser(user, HttpMethod.PATCH, authorizationHeader);
            }
        }

        return memberMapper.findMemberByUserKey(memberDetail.getUserKey());
    }

    public MemberDetail modefiyAccount(MemberDetail memberDetail, Boolean bHash, String authorizationHeader) {

        String hashPwd = "";
        String encPassword = "";

        if(memberDetail.getPassword() == null || memberDetail.getPassword().isBlank()) {
            memberDetail.setPassword("");
        } else {
            hashPwd = bHash?CryptoUtil.encode(memberDetail.getPassword()):memberDetail.getPassword();
            encPassword = passwordEncoder.encode(hashPwd);
            log.debug("encPassword:" + encPassword);
            memberDetail.setPassword(encPassword);
        }

        if(memberDetail.getIsSuperAdmin() != null && memberDetail.getIsSuperAdmin()) {
            memberDetail.setRoles("ROLE_USER|ROLE_EDITOR|ROLE_ADMIN|ROLE_SUPER_ADMIN");
        }
        else if(memberDetail.getIsAdmin() != null && memberDetail.getIsAdmin()) {
            memberDetail.setRoles("ROLE_USER|ROLE_EDITOR|ROLE_ADMIN");
        }
        else if(memberDetail.getIsEditor() != null && memberDetail.getIsEditor()) {
            memberDetail.setRoles("ROLE_USER|ROLE_EDITOR");
        } else {
            memberDetail.setRoles("ROLE_USER");
        }

        Integer result = memberMapper.updateMember(memberDetail);

        if (CHATPLAY_ENABLE) {
            try {
                ChatplayReq user = new ChatplayReq(memberDetail.getUsername(),
                        hashPwd == null || hashPwd.isEmpty() ? null : hashPwd,
                        memberDetail.getName() == null || memberDetail.getName().isEmpty() ? null : memberDetail.getName(),
                        null, null, null, null);
                BaseResponse<Void> chatplayResponse = chatplayService.processUser(user, HttpMethod.PATCH, authorizationHeader);

                if("U003".equals(chatplayResponse.getCode())) { // 계정이 없으면 만들어 준다.
                    List<ChatplayReq> userList = new ArrayList<>();
                    userList.add(user);
                    chatplayResponse = chatplayService.processUser(userList, HttpMethod.POST, authorizationHeader);
                }

            } catch (Exception e) {
                log.error("Chatplay API Error : {}", e.getMessage());
            }
        }

        return memberMapper.findMemberByUserKey(memberDetail.getUserKey());
    }

    public LoginMember authLogin(LoginParam loginParam) {
        LoginMember loginMember = new LoginMember();
        MemberDetail userDetails = (MemberDetail) loadUserByUsername(loginParam.getEmail());

        if(userDetails == null) {
            log.error("LOGIN FAIL : NOT EXIST ID : {}", loginParam.getEmail());
            throw BaseException.of(ResponseMeta.LOGIN_FAIL_NOT_EXIST_ID);
        } else if("N".equals(userDetails.getUseYn())) {
            log.error("LOGIN FAIL : DELETED ID : {}", loginParam.getEmail());
            throw BaseException.of(ResponseMeta.LOGIN_FAIL_NOT_EXIST_ID);
        }

        String param1 = loginParam.getPassword512();
        String param2 = CryptoUtil.encode(param1);
        String param3 = passwordEncoder.encode(param2);
        String param4 = userDetails.getPassword();

        log.debug("param1:" +  param1);
        log.debug("param2:" +  param2);
        log.debug("param3:" +  param3);
        log.debug("param4:" +  param4);

        if(userDetails != null && passwordEncoder.matches(loginParam.getPassword512(), userDetails.getPassword())) {

            String userId = userDetails.getUsername();

            if(CHATPLAY_ENABLE) {
                try {
                    userId = chatplayService.getChatplayId(loginParam.getEmail());
                } catch( Exception e) {
                    log.error("Chatplay API Error : {}", e.getMessage());
                }
            }

            userDetails.setUserId(userId == null || userId.isEmpty()? userDetails.getUsername() : userId);

            log.debug("password-matched!!!" + userDetails.getUsername());

            String token = JwtUtil.generateToken(userDetails);
            loginMember.setAccessToken(token);

            Boolean isEditor = false;
            if(userDetails.getRoles() != null && userDetails.getRoles().matches(".*ROLE_EDITOR.*"))
                isEditor = true;
            loginMember.setIsCompanyEditor(isEditor);
            Boolean isAdmin = false;
            if(userDetails.getRoles() != null && userDetails.getRoles().matches(".*ROLE_ADMIN.*"))
                isAdmin = true;
            loginMember.setIsCompanyAdmin(isAdmin);
            Boolean isSuperAdmin = false;
            if(userDetails.getRoles() != null && userDetails.getRoles().matches(".*ROLE_SUPER_ADMIN.*"))
                isSuperAdmin = true;
            loginMember.setIsCompanySuperAdmin(isSuperAdmin);
            if(userDetails.getDefaultChatbotId() != null)
                loginMember.setDefaultChatbotId(userDetails.getDefaultChatbotId());
            else
                loginMember.setDefaultChatbotId(defaultChatbotId);

            loginMember.setEmail(JwtUtil.getEmail(token));
            loginMember.setUserId(JwtUtil.getUserId(token));
            loginMember.setName(JwtUtil.getName(token));
            loginMember.setUserKey(Long.valueOf(JwtUtil.getUserKey(token)));
            loginMember.setCompanyId(JwtUtil.getCompanyId(token));
        } else {
            log.debug("password-not-matched!!!" + userDetails.getUsername());
        }

        return loginMember;
    }

    public static Specification<MemberForMemberList> rolesNotContaining(List<String> excludedRoles) {
        return (root, query, criteriaBuilder) -> {
            if (excludedRoles == null || excludedRoles.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            // 여러 조건을 "AND NOT LIKE"로 조합
            return criteriaBuilder.and(
                    excludedRoles.stream()
                            .map(role -> criteriaBuilder.notLike(root.get("roles"), "%" + role + "%"))
                            .toArray(Predicate[]::new)
            );
        };
    }

    public static Specification<MemberForMemberList> hasUseYn(String useYn) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("useYn"), useYn);
    }

    public static Specification<MemberForMemberList> usernameContains(String email) {
        return (root, query, criteriaBuilder) ->
                email == null || email.isEmpty()
                        ? criteriaBuilder.conjunction()
                        : criteriaBuilder.like(root.get("username"), "%" + email + "%");
    }

    public static Specification<MemberForMemberList> organizationEquals(Long organizationId) {
        return (root, query, criteriaBuilder) -> {
            if (organizationId == null) {
                return criteriaBuilder.conjunction();
            }
            // 서브쿼리를 사용하여 MemberForMemberList의 userKey가 MemberOrganization의 memberId에 존재하는지 확인
            Subquery<Long> subquery = query.subquery(Long.class);
            Root<MemberOrganizationEntity> subRoot = subquery.from(MemberOrganizationEntity.class);
            subquery.select(subRoot.get("id").get("memberId"))
                    .where(criteriaBuilder.equal(subRoot.get("id").get("organizationId"), organizationId));

            return criteriaBuilder.in(root.get("userKey")).value(subquery);
        };
    }

    /**
     * 사용자의 조직 ID를 조회하는 메서드
     */
    private Long getUserOrganizationId(Long userKey) {
        return memberOrganizationRepository.findFirstByMember_UserKeyOrderByOrganization_IdAsc(userKey)
                .map(mo -> mo.getId().getOrganizationId()) // MemberOrganizationEntity에서 조직 ID 추출
                .orElse(null); // 없을 경우 null 반환
    }

    public Page<MemberForMemberList> getMemberDetail(int page, int size, String email, MemberDetail user) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("userKey").ascending()); // 정렬 기준 설정

        if (email != null && !email.isEmpty()) { // 이메일 검색 조건이 있는 경우

            Specification<MemberForMemberList> specification = Specification.where(hasUseYn("Y"))
                    .and(usernameContains(email));

            if (!user.getIsSuperAdmin()) {
                if (user.getIsAdmin())
                    specification = specification.and(rolesNotContaining(Arrays.asList("ROLE_SUPER_ADMIN")));
                else if (user.getIsEditor())
                    specification = specification.and(rolesNotContaining(Arrays.asList("ROLE_SUPER_ADMIN", "ROLE_ADMIN")));

                // ✅ 조직 필터 적용
                Long userOrganizationId = getUserOrganizationId(user.getUserKey());
                if (userOrganizationId != null) {
                    specification = specification.and(organizationEquals(userOrganizationId));
                }
            }

            return memberListRepository.findAll(specification, pageable);
        } else {
            // 검색 조건이 없는 경우 모든 데이터 반환
            Specification<MemberForMemberList> specification = Specification.where(hasUseYn("Y"));
            if (!user.getIsSuperAdmin()) {
                if (user.getIsAdmin())
                    specification = specification.and(rolesNotContaining(Arrays.asList("ROLE_SUPER_ADMIN")));
                else if (user.getIsEditor())
                    specification = specification.and(rolesNotContaining(Arrays.asList("ROLE_SUPER_ADMIN", "ROLE_ADMIN")));

                // ✅ 조직 필터 적용
                Long userOrganizationId = getUserOrganizationId(user.getUserKey());
                if (userOrganizationId != null) {
                    specification = specification.and(organizationEquals(userOrganizationId));
                }
            }

            Page<MemberForMemberList> result = memberListRepository.findAll(specification, pageable);

            result.getContent().forEach(member -> {
                Optional<String> orgName = memberOrganizationRepository.findFirstOrganizationNameByMemberId(member.getUserKey());
                member.setOrganization(orgName.orElse(null)); // 없으면 null 설정
            });

//            return memberListRepository.findAll(specification, pageable);
            return result;
        }
    }

    public List<MemberDetail> getMemberList() {
        return memberMapper.selectMemberList();
    }

    @Transactional
    public boolean deleteMember(Long userKey, String email, String authorizationHeader) {
        MemberDetail member = memberMapper.findMemberByUserKey(userKey);

        if(member == null)
            throw BaseException.of("없는 계정입니다.");

        int result =  memberMapper.softDeleteMemberByUserKey(userKey);

        if(result > 0) {
            try {
                if (CHATPLAY_ENABLE) {
                    List<String> userList = new ArrayList<>();
                    userList.add(email);
                    BaseResponse<Void> chatplayResponse = chatplayService.processUser(userList, HttpMethod.DELETE, authorizationHeader);
                }
            } catch (Exception e) {
                log.error("chat play call error", e);
            }
            return true;
        }
        else
            throw BaseException.of("삭제에 실패했습니다.");
    }

    public Boolean changePassword(Long userKey, String password, Boolean bHash, String authorizationHeader) {
        String hashPwd = bHash?CryptoUtil.encode(password):password;
        String encPassword = passwordEncoder.encode(hashPwd);

        MemberDetail checkMember = memberMapper.findMemberByUserKey(userKey);

        MemberDetail memberDetail = new MemberDetail();

        memberDetail.setUserKey(userKey);
        memberDetail.setPassword(encPassword);

        int result = memberMapper.updateMember(memberDetail);

        if(result > 0) {
            try {
                if (CHATPLAY_ENABLE) {
                    ChatplayReq user = new ChatplayReq();
                    user.setEmail(checkMember.getUsername());
                    user.setPassword(hashPwd);
                    BaseResponse<Void> chatplayResponse = chatplayService.processUser(user, HttpMethod.PATCH, authorizationHeader);
                    if ("U003".equals(chatplayResponse.getCode())) { //없는 계정일 경우
                        List<ChatplayReq> userList = new ArrayList<>();
                        userList.add(user);
                        chatplayResponse = chatplayService.processUser(userList, HttpMethod.POST, authorizationHeader);
                    }
                }
            } catch (Exception e) {
                log.error("chat play call error", e);
            }
        }

        return result == 1;
    }
}
