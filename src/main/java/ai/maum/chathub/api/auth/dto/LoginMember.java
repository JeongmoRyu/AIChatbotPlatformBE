package ai.maum.chathub.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginMember {
    private String marketingAgreementType = null;
    private String joinType = "US0101";
    private String companyId = null;
    private String expiredDate = "";
    private String mobile = "01000000000";
    private Boolean isCompanyEditor = false;
    private Boolean isCompanyAdmin = false;
    private Boolean isCompanySuperAdmin = false;
    private String user_type = "US0204";
    private String companyRegistrationNumber = null;
    private Boolean existUserGptInfo = false;
    private String companyName = null;
    private String Status = "UNSUBSCRIBED";
    private String userId;
    private String accessToken;
    private String refreshToken;
    private String name;
    private String email;
    private Long defaultChatbotId;
    private Long userKey;


}
