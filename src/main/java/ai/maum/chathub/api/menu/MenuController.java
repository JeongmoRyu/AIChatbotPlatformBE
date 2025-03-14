package ai.maum.chathub.api.menu;

import ai.maum.chathub.api.common.BaseResponse;
import ai.maum.chathub.api.member.dto.MemberDetail;
import ai.maum.chathub.api.menu.dto.MenuRes;
import ai.maum.chathub.api.menu.service.MenuService;
import ai.maum.chathub.meta.ResponseMeta;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name="메뉴관리", description="메뉴관리API")
public class MenuController {
    private final MenuService menuService;
    @GetMapping("/menu/list/{type}")
    public BaseResponse<List<MenuRes>> getMenuList(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberDetail user,
            @PathVariable(name = "type", required = false) @Parameter(name = "type") String menuType
    ) {

        if(menuType == null || menuType.isEmpty())
            return BaseResponse.failure(null, ResponseMeta.PARAM_WRONG);

        return menuService.getMenuList(user.getUserKey(), menuType);
    }

}
