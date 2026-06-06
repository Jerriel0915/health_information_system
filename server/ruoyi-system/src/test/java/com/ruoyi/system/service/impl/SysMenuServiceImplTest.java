package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.system.domain.vo.RouterVo;
import com.ruoyi.system.mapper.SysMenuMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysRoleMenuMapper;

@ExtendWith(MockitoExtension.class)
class SysMenuServiceImplTest {

    @Mock
    private SysMenuMapper menuMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysRoleMenuMapper roleMenuMapper;

    @InjectMocks
    private SysMenuServiceImpl menuService;

    @Test
    void selectMenuById_shouldReturnMenu() {
        SysMenu menu = new SysMenu();
        menu.setMenuId(1L);
        when(menuMapper.selectMenuById(1L)).thenReturn(menu);

        SysMenu result = menuService.selectMenuById(1L);

        assertThat(result.getMenuId()).isEqualTo(1L);
    }

    @Test
    void selectMenuById_shouldReturnNullWhenNotFound() {
        when(menuMapper.selectMenuById(999L)).thenReturn(null);

        SysMenu result = menuService.selectMenuById(999L);

        assertThat(result).isNull();
    }

    @Test
    void hasChildByMenuId_shouldReturnTrueWhenChildrenExist() {
        when(menuMapper.hasChildByMenuId(1L)).thenReturn(3);

        boolean result = menuService.hasChildByMenuId(1L);

        assertThat(result).isTrue();
    }

    @Test
    void hasChildByMenuId_shouldReturnFalseWhenNoChildren() {
        when(menuMapper.hasChildByMenuId(1L)).thenReturn(0);

        boolean result = menuService.hasChildByMenuId(1L);

        assertThat(result).isFalse();
    }

    @Test
    void checkMenuExistRole_shouldReturnTrueWhenRoleExists() {
        when(roleMenuMapper.checkMenuExistRole(1L)).thenReturn(2);

        boolean result = menuService.checkMenuExistRole(1L);

        assertThat(result).isTrue();
    }

    @Test
    void checkMenuExistRole_shouldReturnFalseWhenNoRole() {
        when(roleMenuMapper.checkMenuExistRole(1L)).thenReturn(0);

        boolean result = menuService.checkMenuExistRole(1L);

        assertThat(result).isFalse();
    }

    @Test
    void insertMenu_shouldReturnAffectedRows() {
        SysMenu menu = new SysMenu();
        when(menuMapper.insertMenu(any(SysMenu.class))).thenReturn(1);

        int result = menuService.insertMenu(menu);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void updateMenu_shouldReturnAffectedRows() {
        SysMenu menu = new SysMenu();
        menu.setMenuId(1L);
        when(menuMapper.updateMenu(any(SysMenu.class))).thenReturn(1);

        int result = menuService.updateMenu(menu);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void deleteMenuById_shouldReturnAffectedRows() {
        when(menuMapper.deleteMenuById(1L)).thenReturn(1);

        int result = menuService.deleteMenuById(1L);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void checkMenuNameUnique_shouldReturnUniqueWhenNew() {
        SysMenu menu = new SysMenu();
        when(menuMapper.checkMenuNameUnique(any(), any())).thenReturn(null);

        boolean result = menuService.checkMenuNameUnique(menu);

        assertThat(result).isTrue();
    }

    @Test
    void checkMenuNameUnique_shouldReturnNotUniqueWhenNameExists() {
        SysMenu menu = new SysMenu();
        menu.setMenuId(1L);
        menu.setMenuName("existing");
        SysMenu existing = new SysMenu();
        existing.setMenuId(2L);
        when(menuMapper.checkMenuNameUnique("existing", null)).thenReturn(existing);

        boolean result = menuService.checkMenuNameUnique(menu);

        assertThat(result).isFalse();
    }

    @Test
    void selectMenuListByRoleId_shouldReturnMenuIds() {
        SysRole role = new SysRole();
        role.setMenuCheckStrictly(false);
        when(roleMapper.selectRoleById(1L)).thenReturn(role);
        when(menuMapper.selectMenuListByRoleId(1L, false)).thenReturn(List.of(1L, 2L));

        List<Long> result = menuService.selectMenuListByRoleId(1L);

        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    void selectMenuPermsByUserId_shouldReturnPermSet() {
        when(menuMapper.selectMenuPermsByUserId(100L)).thenReturn(List.of("system:user:list", "system:role:list"));

        Set<String> result = menuService.selectMenuPermsByUserId(100L);

        assertThat(result).contains("system:user:list", "system:role:list");
    }

    @Test
    void selectMenuPermsByUserId_shouldSplitCommaDelimitedPerms() {
        when(menuMapper.selectMenuPermsByUserId(100L))
                .thenReturn(List.of("system:user:list,system:user:add"));

        Set<String> result = menuService.selectMenuPermsByUserId(100L);

        assertThat(result).contains("system:user:list", "system:user:add");
    }

    @Test
    void selectMenuPermsByUserId_shouldHandleEmptyPerms() {
        when(menuMapper.selectMenuPermsByUserId(100L)).thenReturn(List.of(""));

        Set<String> result = menuService.selectMenuPermsByUserId(100L);

        assertThat(result).isEmpty();
    }

    @Test
    void buildMenuTree_shouldBuildTreeStructure() {
        SysMenu parent = buildMenu(1L, 0L, "parent");
        SysMenu child = buildMenu(2L, 1L, "child");
        List<SysMenu> menus = List.of(parent, child);

        List<SysMenu> result = menuService.buildMenuTree(menus);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChildren()).hasSize(1);
    }

    @Test
    void buildMenuTreeSelect_shouldConvertToTreeSelect() {
        SysMenu menu = buildMenu(1L, 0L, "test");
        List<SysMenu> menus = List.of(menu);

        List<TreeSelect> result = menuService.buildMenuTreeSelect(menus);

        assertThat(result).hasSize(1);
    }

    @Test
    void buildMenus_shouldBuildRouterList() {
        SysMenu menu = buildMenu(1L, 0L, "System");
        menu.setMenuType(UserConstants.TYPE_DIR);
        menu.setIcon("system");
        menu.setPath("system");
        menu.setVisible("0");
        menu.setIsCache("0");
        menu.setIsFrame(UserConstants.NO_FRAME);
        menu.setQuery("");
        List<SysMenu> menus = List.of(menu);

        List<RouterVo> result = menuService.buildMenus(menus);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("System");
        assertThat(result.get(0).getPath()).isEqualTo("/system");
    }

    @Test
    void isMenuFrame_shouldReturnTrueForTopLevelMenuWithoutFrame() {
        SysMenu menu = new SysMenu();
        menu.setParentId(0L);
        menu.setMenuType(UserConstants.TYPE_MENU);
        menu.setIsFrame(UserConstants.NO_FRAME);

        boolean result = menuService.isMenuFrame(menu);

        assertThat(result).isTrue();
    }

    @Test
    void isMenuFrame_shouldReturnFalseForDirectory() {
        SysMenu menu = new SysMenu();
        menu.setParentId(0L);
        menu.setMenuType(UserConstants.TYPE_DIR);
        menu.setIsFrame(UserConstants.NO_FRAME);

        boolean result = menuService.isMenuFrame(menu);

        assertThat(result).isFalse();
    }

    @Test
    void isParentView_shouldReturnTrueForSubDirectory() {
        SysMenu menu = new SysMenu();
        menu.setParentId(1L);
        menu.setMenuType(UserConstants.TYPE_DIR);

        boolean result = menuService.isParentView(menu);

        assertThat(result).isTrue();
    }

    @Test
    void isInnerLink_shouldReturnTrueForHttpPathWithoutFrame() {
        SysMenu menu = new SysMenu();
        menu.setIsFrame(UserConstants.NO_FRAME);
        menu.setPath("https://example.com");

        boolean result = menuService.isInnerLink(menu);

        assertThat(result).isTrue();
    }

    private SysMenu buildMenu(Long id, Long parentId, String name) {
        SysMenu menu = new SysMenu();
        menu.setMenuId(id);
        menu.setParentId(parentId);
        menu.setMenuName(name);
        menu.setMenuType(UserConstants.TYPE_DIR);
        menu.setVisible("0");
        menu.setIsCache("0");
        menu.setIsFrame(UserConstants.NO_FRAME);
        menu.setPath(name.toLowerCase());
        menu.setChildren(new ArrayList<>());
        return menu;
    }
}
