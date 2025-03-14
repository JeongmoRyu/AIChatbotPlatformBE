package ai.maum.chathub.api.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TempUser {
    //        "marketing_agreement_type": null,
//                "join_type": "US0101",
//                "company_id": null,
//                "expiredDate": "",
//                "mobile": "01000000000",
//                "userId": "65af23c75451204292a3fae5",
//                "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NWFmMjNjNzU0NTEyMDQyOTJhM2ZhZTUiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImNoYXRwbGF5dGVzdDJAbWF1bS5haSIsImVtYWlsIjoiY2hhdHBsYXl0ZXN0MkBtYXVtLmFpIiwibmFtZSI6ImNoYXRwbGF5dGVzdDIiLCJzdGF0dXMiOiJVTlNVQlNDUklCRUQiLCJleHBpcmVkRGF0ZSI6IiIsImlhdCI6MTcxMTUyNzA3MiwiZXhwIjoxNzExNjEzNDcyfQ.upc-HPwnKNd1D6sU52NtXWY1acfnfs7lvEAR26OCxtk",
//                "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NWFmMjNjNzU0NTEyMDQyOTJhM2ZhZTUiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImNoYXRwbGF5dGVzdDJAbWF1bS5haSIsImVtYWlsIjoiY2hhdHBsYXl0ZXN0MkBtYXVtLmFpIiwibmFtZSI6ImNoYXRwbGF5dGVzdDIiLCJzdGF0dXMiOiJVTlNVQlNDUklCRUQiLCJleHBpcmVkRGF0ZSI6IiIsImlhdCI6MTcxMTUyNzA3MiwiZXhwIjoxNzE0MTE5MDcyfQ.NKoqS52cWmNbNp9PfuvLfMbJuaDPN9TinT2o09Xd9M0",
//                "is_company_admin": false,
//                "user_type": "US0204",
//                "company_registration_number": null,
//                "exist_user_gpt_info": false,
//                "company_name": null,
//                "name": "chatplaytest2",
//                "email": "chatplaytest2@maum.ai",
//                "status": "UNSUBSCRIBED"


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
        generateUser("chatplaytest1");
    }

    public TempUser(String name) {
        generateUser(name);
    }

    private void generateUser(String name) {
        switch (name) {
            case ("chatplaytest2"):
                TempUser(
                        "65af23c75451204292a3fae5",
                        "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NWFmMjNjNzU0NTEyMDQyOTJhM2ZhZTUiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImNoYXRwbGF5dGVzdDJAbWF1bS5haSIsImVtYWlsIjoiY2hhdHBsYXl0ZXN0MkBtYXVtLmFpIiwibmFtZSI6ImNoYXRwbGF5dGVzdDIiLCJzdGF0dXMiOiJVTlNVQlNDUklCRUQiLCJleHBpcmVkRGF0ZSI6IiIsImlhdCI6MTcxMjkwMDg2MCwiZXhwIjoxNzEyOTg3MjYwfQ.f1fSwlAVo49dFQ7qV-uHRJcFUiFpNtNbzGo74wxnqdM",
                        "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NWFmMjNjNzU0NTEyMDQyOTJhM2ZhZTUiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImNoYXRwbGF5dGVzdDJAbWF1bS5haSIsImVtYWlsIjoiY2hhdHBsYXl0ZXN0MkBtYXVtLmFpIiwibmFtZSI6ImNoYXRwbGF5dGVzdDIiLCJzdGF0dXMiOiJVTlNVQlNDUklCRUQiLCJleHBpcmVkRGF0ZSI6IiIsImlhdCI6MTcxMjkwMDg2MCwiZXhwIjoxNzE1NDkyODYwfQ.6R6vYguu6kwVhtI9LKqEbSOn16hpmM1_r6UsbRoGxt4",
                        "chatplaytest2",
                        "chatplaytest2@maum.ai"
                );
                break;
            default:
                TempUser(
                        "65af23b35451204292a3fad9",
                        "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NWFmMjNiMzU0NTEyMDQyOTJhM2ZhZDkiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImNoYXRwbGF5dGVzdDFAbWF1bS5haSIsImVtYWlsIjoiY2hhdHBsYXl0ZXN0MUBtYXVtLmFpIiwibmFtZSI6ImNoYXRwbGF5dGVzdDEiLCJzdGF0dXMiOiJVTlNVQlNDUklCRUQiLCJleHBpcmVkRGF0ZSI6IiIsImlhdCI6MTcxMjkwMDkxMCwiZXhwIjoxNzEyOTg3MzEwfQ.Ug4y0a3DgiIGOQoC71C3AWHdlmkTzrbbH4LyqiqpgDE",
                        "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NWFmMjNiMzU0NTEyMDQyOTJhM2ZhZDkiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImNoYXRwbGF5dGVzdDFAbWF1bS5haSIsImVtYWlsIjoiY2hhdHBsYXl0ZXN0MUBtYXVtLmFpIiwibmFtZSI6ImNoYXRwbGF5dGVzdDEiLCJzdGF0dXMiOiJVTlNVQlNDUklCRUQiLCJleHBpcmVkRGF0ZSI6IiIsImlhdCI6MTcxMjkwMDkxMCwiZXhwIjoxNzE1NDkyOTEwfQ.0p6yirb7c7mPf13r0e-LQZYklj51c-GO_Dk0y9tSfSQ",
                        "chatplaytest1",
                        "chatplaytest1@maum.ai"
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


//    {
//        "result": "ok",
//            "data": {
//        "marketing_agreement_type": "US0704",
//                "join_type": "US0103",
//                "company_id": null,
//                "expiredDate": "",
//                "mobile": "001063324347",
//                "userId": "659b556dd6ef631a555b89af",
//                "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NTliNTU2ZGQ2ZWY2MzFhNTU1Yjg5YWYiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImtvbWFzaW40QGdtYWlsLmNvbSIsImVtYWlsIjoia29tYXNpbjRAZ21haWwuY29tIiwibmFtZSI6Iuuwleygle2YhCIsInN0YXR1cyI6IkZSRUVfRVhQSVJFRCIsImV4cGlyZWREYXRlIjoiIiwiaWF0IjoxNzE0MDM2MzM3LCJleHAiOjE3MTQxMjI3Mzd9.A3d_rHaNKl9z12UchxTEmzW96QHdbe4YXta5d8P1CGY",
//                "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NTliNTU2ZGQ2ZWY2MzFhNTU1Yjg5YWYiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImtvbWFzaW40QGdtYWlsLmNvbSIsImVtYWlsIjoia29tYXNpbjRAZ21haWwuY29tIiwibmFtZSI6Iuuwleygle2YhCIsInN0YXR1cyI6IkZSRUVfRVhQSVJFRCIsImV4cGlyZWREYXRlIjoiIiwiaWF0IjoxNzE0MDM2MzM3LCJleHAiOjE3MTY2MjgzMzd9.YP2WLUflIQbLrWlM2ujRpo2fzRFF33QacRlz_xzg7g8",
//                "is_company_admin": false,
//                "user_type": "US0204",
//                "company_registration_number": null,
//                "exist_user_gpt_info": false,
//                "company_name": null,
//                "name": "박정현",
//                "email": "komasin4@gmail.com",
//                "status": "FREE_EXPIRED"
//    },
//        "message": "SUCCESS"
//    }

//    {
//        "result": "ok",
//            "data": {
//        "marketing_agreement_type": null,
//                "join_type": "US0101",
//                "company_id": null,
//                "expiredDate": "",
//                "mobile": "01000000000",
//                "userId": "65af23b35451204292a3fad9",
//                "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NWFmMjNiMzU0NTEyMDQyOTJhM2ZhZDkiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImNoYXRwbGF5dGVzdDFAbWF1bS5haSIsImVtYWlsIjoiY2hhdHBsYXl0ZXN0MUBtYXVtLmFpIiwibmFtZSI6ImNoYXRwbGF5dGVzdDEiLCJzdGF0dXMiOiJVTlNVQlNDUklCRUQiLCJleHBpcmVkRGF0ZSI6IiIsImlhdCI6MTcxMjkwMDkxMCwiZXhwIjoxNzEyOTg3MzEwfQ.Ug4y0a3DgiIGOQoC71C3AWHdlmkTzrbbH4LyqiqpgDE",
//                "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NWFmMjNiMzU0NTEyMDQyOTJhM2ZhZDkiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImNoYXRwbGF5dGVzdDFAbWF1bS5haSIsImVtYWlsIjoiY2hhdHBsYXl0ZXN0MUBtYXVtLmFpIiwibmFtZSI6ImNoYXRwbGF5dGVzdDEiLCJzdGF0dXMiOiJVTlNVQlNDUklCRUQiLCJleHBpcmVkRGF0ZSI6IiIsImlhdCI6MTcxMjkwMDkxMCwiZXhwIjoxNzE1NDkyOTEwfQ.0p6yirb7c7mPf13r0e-LQZYklj51c-GO_Dk0y9tSfSQ",
//                "is_company_admin": false,
//                "user_type": "US0204",
//                "company_registration_number": null,
//                "exist_user_gpt_info": false,
//                "company_name": null,
//                "name": "chatplaytest1",
//                "email": "chatplaytest1@maum.ai",
//                "status": "UNSUBSCRIBED"
//    },
//        "message": "SUCCESS"
//    }
//
//
//    {
//        "result": "ok",
//            "data": {
//        "marketing_agreement_type": null,
//                "join_type": "US0101",
//                "company_id": null,
//                "expiredDate": "",
//                "mobile": "01000000000",
//                "userId": "65af23c75451204292a3fae5",
//                "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NWFmMjNjNzU0NTEyMDQyOTJhM2ZhZTUiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImNoYXRwbGF5dGVzdDJAbWF1bS5haSIsImVtYWlsIjoiY2hhdHBsYXl0ZXN0MkBtYXVtLmFpIiwibmFtZSI6ImNoYXRwbGF5dGVzdDIiLCJzdGF0dXMiOiJVTlNVQlNDUklCRUQiLCJleHBpcmVkRGF0ZSI6IiIsImlhdCI6MTcxMjkwMDg2MCwiZXhwIjoxNzEyOTg3MjYwfQ.f1fSwlAVo49dFQ7qV-uHRJcFUiFpNtNbzGo74wxnqdM",
//                "refresh_token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOiI2NWFmMjNjNzU0NTEyMDQyOTJhM2ZhZTUiLCJ1c2VyVHlwZSI6IlVTMDIwNCIsInN1YiI6ImNoYXRwbGF5dGVzdDJAbWF1bS5haSIsImVtYWlsIjoiY2hhdHBsYXl0ZXN0MkBtYXVtLmFpIiwibmFtZSI6ImNoYXRwbGF5dGVzdDIiLCJzdGF0dXMiOiJVTlNVQlNDUklCRUQiLCJleHBpcmVkRGF0ZSI6IiIsImlhdCI6MTcxMjkwMDg2MCwiZXhwIjoxNzE1NDkyODYwfQ.6R6vYguu6kwVhtI9LKqEbSOn16hpmM1_r6UsbRoGxt4",
//                "is_company_admin": false,
//                "user_type": "US0204",
//                "company_registration_number": null,
//                "exist_user_gpt_info": false,
//                "company_name": null,
//                "name": "chatplaytest2",
//                "email": "chatplaytest2@maum.ai",
//                "status": "UNSUBSCRIBED"
//    },
//        "message": "SUCCESS"
//    }

}
