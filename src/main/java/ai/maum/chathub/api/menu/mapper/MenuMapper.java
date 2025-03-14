package ai.maum.chathub.api.menu.mapper;

import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.menu.dto.MenuRes;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Mapper
@Repository
public interface MenuMapper {
    public List<MenuRes> selectMenuList(Map param);
}
