package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysRoleDeptMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysRoleMenuMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;

@ExtendWith(MockitoExtension.class)
class SysRoleServiceImplTest {

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysRoleMenuMapper roleMenuMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private SysRoleDeptMapper roleDeptMapper;

    @InjectMocks
    private SysRoleServiceImpl roleService;

    @Test
    void selectRoleById_shouldReturnRole() {
        SysRole role = new SysRole();
        role.setRoleId(1L);
        when(roleMapper.selectRoleById(1L)).thenReturn(role);

        SysRole result = roleService.selectRoleById(1L);

        assertThat(result.getRoleId()).isEqualTo(1L);
    }

    @Test
    void selectRoleById_shouldReturnNullWhenNotFound() {
        when(roleMapper.selectRoleById(999L)).thenReturn(null);

        SysRole result = roleService.selectRoleById(999L);

        assertThat(result).isNull();
    }

    @Test
    void checkRoleNameUnique_shouldReturnUniqueWhenNew() {
        SysRole role = new SysRole();
        when(roleMapper.checkRoleNameUnique(any())).thenReturn(null);

        boolean result = roleService.checkRoleNameUnique(role);

        assertThat(result).isTrue();
    }

    @Test
    void checkRoleNameUnique_shouldReturnNotUniqueWhenNameExists() {
        SysRole role = new SysRole();
        role.setRoleId(1L);
        role.setRoleName("existing");
        SysRole existing = new SysRole();
        existing.setRoleId(2L);
        when(roleMapper.checkRoleNameUnique("existing")).thenReturn(existing);

        boolean result = roleService.checkRoleNameUnique(role);

        assertThat(result).isFalse();
    }

    @Test
    void checkRoleKeyUnique_shouldReturnUniqueWhenNew() {
        SysRole role = new SysRole();
        when(roleMapper.checkRoleKeyUnique(any())).thenReturn(null);

        boolean result = roleService.checkRoleKeyUnique(role);

        assertThat(result).isTrue();
    }

    @Test
    void checkRoleAllowed_shouldNotThrowForNonAdminRole() {
        SysRole role = new SysRole();
        role.setRoleId(2L);

        roleService.checkRoleAllowed(role);
    }

    @Test
    void checkRoleAllowed_shouldThrowForAdminRole() {
        SysRole role = new SysRole();
        role.setRoleId(1L);
        role.setRoleKey("admin");

        assertThatThrownBy(() -> roleService.checkRoleAllowed(role))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不允许操作超级管理员角色");
    }

    @Test
    void countUserRoleByRoleId_shouldReturnCount() {
        when(userRoleMapper.countUserRoleByRoleId(1L)).thenReturn(5);

        int result = roleService.countUserRoleByRoleId(1L);

        assertThat(result).isEqualTo(5);
    }

    @Test
    void updateRoleStatus_shouldReturnAffectedRows() {
        SysRole role = new SysRole();
        when(roleMapper.updateRole(any(SysRole.class))).thenReturn(1);

        int result = roleService.updateRoleStatus(role);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void deleteAuthUser_shouldDeleteUserRole() {
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(100L);
        userRole.setRoleId(1L);
        when(userRoleMapper.deleteUserRoleInfo(any(SysUserRole.class))).thenReturn(1);

        int result = roleService.deleteAuthUser(userRole);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void deleteAuthUsers_shouldBatchDeleteUserRoles() {
        Long[] userIds = {100L, 101L};
        when(userRoleMapper.deleteUserRoleInfos(1L, userIds)).thenReturn(2);

        int result = roleService.deleteAuthUsers(1L, userIds);

        assertThat(result).isEqualTo(2);
    }

    @Test
    void insertAuthUsers_shouldBatchInsertUserRoles() {
        Long[] userIds = {100L, 101L};
        when(userRoleMapper.batchUserRole(any())).thenReturn(2);

        int result = roleService.insertAuthUsers(1L, userIds);

        assertThat(result).isEqualTo(2);
    }

    @Test
    void selectRolePermissionByUserId_shouldReturnPermSet() {
        SysRole role = new SysRole();
        role.setRoleKey("admin");
        when(roleMapper.selectRolePermissionByUserId(100L)).thenReturn(List.of(role));

        Set<String> result = roleService.selectRolePermissionByUserId(100L);

        assertThat(result).contains("admin");
    }

    @Test
    void deleteRoleById_shouldDeleteRoleAndAssociations() {
        when(roleMapper.deleteRoleById(1L)).thenReturn(1);

        int result = roleService.deleteRoleById(1L);

        assertThat(result).isEqualTo(1);
        verify(roleMenuMapper).deleteRoleMenuByRoleId(1L);
        verify(roleDeptMapper).deleteRoleDeptByRoleId(1L);
    }

    @Test
    void insertRole_shouldInsertAndCreateMenuAssociations() {
        SysRole role = new SysRole();
        role.setRoleId(1L);
        role.setMenuIds(new Long[]{1L, 2L});
        when(roleMapper.insertRole(any(SysRole.class))).thenReturn(1);
        when(roleMenuMapper.batchRoleMenu(any())).thenReturn(2);

        int result = roleService.insertRole(role);

        assertThat(result).isEqualTo(2);
    }

    @Test
    void insertRole_shouldReturnDefaultWhenNoMenuIds() {
        SysRole role = new SysRole();
        role.setRoleId(1L);
        role.setMenuIds(new Long[0]);
        when(roleMapper.insertRole(any(SysRole.class))).thenReturn(1);

        int result = roleService.insertRole(role);

        assertThat(result).isEqualTo(1);
    }
}
