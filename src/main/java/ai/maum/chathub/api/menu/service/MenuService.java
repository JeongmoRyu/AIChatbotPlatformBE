package ai.maum.chathub.api.menu.service;

import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.menu.dto.MenuRes;
import ai.maum.chathub.api.menu.mapper.MenuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {
    private final MenuMapper menuMapper;

    public BaseResponse<List<MenuRes>> getMenuList(Long userKey, String menuType) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userKey", userKey);
        params.put("menuType", menuType);
        return BaseResponse.success(menuMapper.selectMenuList(params));
    }
}
