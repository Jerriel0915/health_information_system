package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysPostMapper;
import com.ruoyi.system.mapper.SysRoleMapper;
import com.ruoyi.system.mapper.SysUserMapper;
import com.ruoyi.system.mapper.SysUserPostMapper;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysDeptService;

@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest {

    @Mock
    private SysUserMapper userMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @Mock
    private SysPostMapper postMapper;

    @Mock
    private SysUserRoleMapper userRoleMapper;

    @Mock
    private SysUserPostMapper userPostMapper;

    @Mock
    private ISysConfigService configService;

    @Mock
    private ISysDeptService deptService;

    @InjectMocks
    private SysUserServiceImpl userService;

    @Test
    void selectUserById_shouldReturnUser() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        when(userMapper.selectUserById(1L)).thenReturn(user);

        SysUser result = userService.selectUserById(1L);

        assertThat(result.getUserId()).isEqualTo(1L);
    }

    @Test
    void selectUserById_shouldReturnNullWhenNotFound() {
        when(userMapper.selectUserById(999L)).thenReturn(null);

        SysUser result = userService.selectUserById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectUserByUserName_shouldReturnUser() {
        SysUser user = new SysUser();
        user.setUserName("admin");
        when(userMapper.selectUserByUserName("admin")).thenReturn(user);

        SysUser result = userService.selectUserByUserName("admin");

        assertThat(result.getUserName()).isEqualTo("admin");
    }

    @Test
    void selectUserList_shouldReturnList() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        when(userMapper.selectUserList(any(SysUser.class)))
                .thenReturn(Collections.singletonList(user));

        List<SysUser> result = userService.selectUserList(new SysUser());

        assertThat(result).hasSize(1);
    }

    @Test
    void selectUserList_shouldReturnEmptyList() {
        when(userMapper.selectUserList(any(SysUser.class))).thenReturn(Collections.emptyList());

        List<SysUser> result = userService.selectUserList(new SysUser());

        assertThat(result).isEmpty();
    }

    @Test
    void checkUserNameUnique_shouldReturnUniqueWhenNew() {
        SysUser user = new SysUser();
        when(userMapper.checkUserNameUnique(any())).thenReturn(null);

        boolean result = userService.checkUserNameUnique(user);

        assertThat(result).isTrue();
    }

    @Test
    void checkUserNameUnique_shouldReturnNotUniqueWhenNameExists() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setUserName("existing");
        SysUser existing = new SysUser();
        existing.setUserId(2L);
        when(userMapper.checkUserNameUnique("existing")).thenReturn(existing);

        boolean result = userService.checkUserNameUnique(user);

        assertThat(result).isFalse();
    }

    @Test
    void checkPhoneUnique_shouldReturnUniqueWhenNew() {
        SysUser user = new SysUser();
        when(userMapper.checkPhoneUnique(any())).thenReturn(null);

        boolean result = userService.checkPhoneUnique(user);

        assertThat(result).isTrue();
    }

    @Test
    void checkPhoneUnique_shouldReturnNotUniqueWhenPhoneExists() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setPhonenumber("13800138000");
        SysUser existing = new SysUser();
        existing.setUserId(2L);
        when(userMapper.checkPhoneUnique("13800138000")).thenReturn(existing);

        boolean result = userService.checkPhoneUnique(user);

        assertThat(result).isFalse();
    }

    @Test
    void checkEmailUnique_shouldReturnUniqueWhenNew() {
        SysUser user = new SysUser();
        when(userMapper.checkEmailUnique(any())).thenReturn(null);

        boolean result = userService.checkEmailUnique(user);

        assertThat(result).isTrue();
    }

    @Test
    void checkUserAllowed_shouldNotThrowForNonAdmin() {
        SysUser user = new SysUser();
        user.setUserId(2L);

        userService.checkUserAllowed(user);
    }

    @Test
    void checkUserAllowed_shouldThrowForAdmin() {
        SysUser user = new SysUser();
        user.setUserId(1L);

        assertThatThrownBy(() -> userService.checkUserAllowed(user))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("不允许操作超级管理员用户");
    }

    @Test
    void updateUserStatus_shouldReturnAffectedRows() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setStatus("1");
        when(userMapper.updateUserStatus(1L, "1")).thenReturn(1);

        int result = userService.updateUserStatus(user);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void updateUserProfile_shouldReturnAffectedRows() {
        SysUser user = new SysUser();
        when(userMapper.updateUser(any(SysUser.class))).thenReturn(1);

        int result = userService.updateUserProfile(user);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void updateUserAvatar_shouldReturnTrueWhenSuccess() {
        when(userMapper.updateUserAvatar(1L, "/avatar.png")).thenReturn(1);

        boolean result = userService.updateUserAvatar(1L, "/avatar.png");

        assertThat(result).isTrue();
    }

    @Test
    void updateUserAvatar_shouldReturnFalseWhenFailed() {
        when(userMapper.updateUserAvatar(1L, "/avatar.png")).thenReturn(0);

        boolean result = userService.updateUserAvatar(1L, "/avatar.png");

        assertThat(result).isFalse();
    }

    @Test
    void resetPwd_shouldReturnAffectedRows() {
        SysUser user = new SysUser();
        user.setUserId(1L);
        user.setPassword("encrypted");
        when(userMapper.resetUserPwd(1L, "encrypted")).thenReturn(1);

        int result = userService.resetPwd(user);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void resetUserPwd_shouldReturnAffectedRows() {
        when(userMapper.resetUserPwd(1L, "newPassword")).thenReturn(1);

        int result = userService.resetUserPwd(1L, "newPassword");

        assertThat(result).isEqualTo(1);
    }

    @Test
    void registerUser_shouldReturnTrueWhenSuccess() {
        when(userMapper.insertUser(any(SysUser.class))).thenReturn(1);

        boolean result = userService.registerUser(new SysUser());

        assertThat(result).isTrue();
    }

    @Test
    void selectUserRoleGroup_shouldReturnEmptyWhenNoRoles() {
        when(roleMapper.selectRolesByUserName("test")).thenReturn(Collections.emptyList());

        String result = userService.selectUserRoleGroup("test");

        assertThat(result).isEmpty();
    }

    @Test
    void selectUserPostGroup_shouldReturnEmptyWhenNoPosts() {
        when(postMapper.selectPostsByUserName("test")).thenReturn(Collections.emptyList());

        String result = userService.selectUserPostGroup("test");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteUserById_shouldDeleteAndRemoveAssociations() {
        when(userMapper.deleteUserById(1L)).thenReturn(1);

        int result = userService.deleteUserById(1L);

        assertThat(result).isEqualTo(1);
        verify(userRoleMapper).deleteUserRoleByUserId(1L);
        verify(userPostMapper).deleteUserPostByUserId(1L);
    }
}
