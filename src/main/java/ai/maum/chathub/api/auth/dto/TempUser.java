package ai.maum.chathub.api.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TempUser {


    private String marketing_agreement_type = null;
    private String join_type = "US0101";
    private String company_id = null;
    private String expiredDate = "";
    private String mobile = "01000000000";
    private Boolean is_company_admin = false;
    private String user_type = "US0204";
    private String company_registration_number = null;
    private Boolean exist_user_gpt_info = false;
    private String company_name = null;
    private String status = "UNSUBSCRIBED";
    private String userId;
    private String access_token;
    private String refresh_token;
    private String name;
    private String email;

    public TempUser() {
        generateUser("id1");
    }

    public TempUser(String name) {
        generateUser(name);
    }

    private void generateUser(String name) {
        switch (name) {
            case ("id2"):
                TempUser(
                        "65af23c75451204292a3fae5",
                        "key",
                        "key",
                        "id",
                        "id@mail.com"
                );
                break;
            default:
                TempUser(
                    "65af23c75451204292a3fae5",
                    "key",
                    "key",
                    "id",
                    "id@mail.com"
                );
                break;
        }
    }

    public void TempUser(String userId, String access_token, String refresh_token, String name, String email) {
        this.userId = userId;
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.name = name;
        this.email = email;
    }


}
