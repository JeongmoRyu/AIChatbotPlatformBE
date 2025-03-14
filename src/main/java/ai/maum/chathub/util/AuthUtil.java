package ai.maum.chathub.util;

import ai.maum.chathub.api.member.dto.MemberDetail;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthUtil {
    public static boolean checkAuthorize(MemberDetail myInfo, MemberDetail targetInfo, boolean bAcceptSameAuth) {

        try {

            int myAuth = getAuthCount(myInfo);
            int targetAuth = getAuthCount(targetInfo);

            if(myAuth ==3) // 수퍼 어드민은 무조건 가능
                return true;

            if(myAuth == 2 && myAuth >= targetAuth) // 어드민은 수퍼 어드민 빼고 가능
                return true;

            if ((myAuth < targetAuth) || (myAuth == targetAuth && !bAcceptSameAuth)) // 에디터는 bAccpetSameAuth q 반영하여 판단
                return false;
            else
                return true;
        } catch (Exception e) {
            log.error("getAuthCount error: {}", e.getMessage());
            return false;
        }
    }

    public static int getAuthCount(MemberDetail memberInfo) {
        if(memberInfo.getIsSuperAdmin())
            return 3;
        else if(memberInfo.getIsAdmin())
            return 2;
        else if(memberInfo.getIsEditor())
            return 1;
        else
            return 0;
    }

}
