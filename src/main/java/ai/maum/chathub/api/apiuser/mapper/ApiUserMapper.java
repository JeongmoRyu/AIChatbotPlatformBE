package ai.maum.chathub.api.apiuser.mapper;

import ai.maum.chathub.api.apiuser.model.ApiUser;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ApiUserMapper {
    public ApiUser getApiUserById(String vendorId);
}