package ai.maum.chathub.util;

import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.meta.SecurityMeta;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

public class JwtUtil {
    private JwtUtil() {}

    /**
     * 사용자 정보가 저장된 JWT 토큰을 발행한다.
     * @param memberDetail 사용자 정보
     * @param roles 사용자에게 부여된 권한 목록
     * @return 토큰
     * @author baekgol
     */
    public static String generateToken(MemberDetail memberDetail, Collection<? extends GrantedAuthority> roles) {
        return Jwts.builder()
                .setClaims(Jwts.claims(Map.of(
                        "email", memberDetail.getUsername(),
                        "userId", String.valueOf(memberDetail.getUserId()),
                        "name", memberDetail.getName(),
                        "userKey", String.valueOf(memberDetail.getUserKey()),
                        "roles", roles.stream()
                                .map(GrantedAuthority::getAuthority)
                                .collect(Collectors.toList()),
//                        "companyId", "MAUMAI"                                                   //임시코드. 챗플레이 토큰값
                        "companyId", "64d1e8806cbe7f7652779cbc"   //임시코드. 챗플레이 토큰값
                )))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + SecurityMeta.TOKEN_EXPIRE_TIME))
                .signWith(Keys.hmacShaKeyFor(SecurityMeta.TOKEN_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    public static String generateToken(MemberDetail memberDetail) {
        Collection<GrantedAuthority> roleList = new ArrayList<>();

        String roles = memberDetail.getRoles();
        if(roles != null && !roles.isEmpty()) {
            String[] roleArray = roles.split("\\|");
            if(roleArray != null && roleArray.length > 0) {
                for(String role : roleArray) {
                    roleList.add(new SimpleGrantedAuthority(role));
                }
            }
        }

        if(roleList.isEmpty()) {
            roleList.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return generateToken(memberDetail, roleList);
    }

    /**
     * 사용자 정보가 저장된 JWT 토큰을 발행한다.
     * 기존 토큰 데이터에 권한을 추가할 때 사용한다.
     * @param token 토큰
     * @param roles 사용자에게 부여된 권한 목록
     * @return 토큰
     * @author baekgol
     */
    public static String addRoles(String token, Collection<? extends GrantedAuthority> roles) {
        Claims claims = parse(token);
        claims.put("roles", roles.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        return Jwts.builder()
                .setClaims(claims)
                .signWith(Keys.hmacShaKeyFor(SecurityMeta.TOKEN_SECRET_KEY.getBytes()), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * JWT 토큰에 저장된 정보와 DB에 저장된 정보를 비교하여 일치하는지 확인한다.
     * @param token 토큰
     * @return 검증 통과 유무
     * @author baekgol
     */
    public static boolean validateToken(String token) {
        Claims info = parse(token);
        // 정보 검증 로직
        return !StringUtil.valueOf(info.get("userId")).equals("");
    }

    /**
     * JWT 토큰에서 사용자 ID를 불러온다.
     * @param token 토큰
     * @return 사용자 ID
     * @author baekgol
     */
    // FIXME mongodb to mariadb
    public static String getEmail(String token) {
        return new String((String)parse(token).get("email"));
    }
    public static String getUserId(String token) {
        return new String((String)parse(token).get("userId"));
    }
    public static String getName(String token) {
        return new String((String)parse(token).get("name"));
    }
    public static String getUserKey(String token) { return new String((String)parse(token).get("userKey"));}
    public static String getCompanyId(String token) { return new String((String)parse(token).get("companyId"));}

    /**
     * JWT 토큰에서 권한 목록을 불러온다.
     * @param token 토큰
     * @return 권한 목록
     * @author baekgol
     */
    @SuppressWarnings("unchecked")
    public static List<GrantedAuthority> getRoles(String token) {
        return ((List<String>)parse(token).get("roles")).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    /**
     * JWT 토큰에 저장된 정보를 불러온다.
     * @param token 토큰
     * @return 클레임(토큰 내부 저장 정보)
     * @author baekgol
     */
    private static Claims parse(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SecurityMeta.TOKEN_SECRET_KEY.getBytes())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
