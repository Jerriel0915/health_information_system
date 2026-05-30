package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.domain.SysUserOnline;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysUserOnlineServiceImplTest {

    @Mock
    private LoginUser loginUser;

    @Mock
    private SysUser sysUser;

    @Mock
    private SysDept sysDept;

    @InjectMocks
    private SysUserOnlineServiceImpl userOnlineService;

    private void setupLoginUser() {
        when(loginUser.getToken()).thenReturn("token-abc");
        when(loginUser.getUsername()).thenReturn("admin");
        when(loginUser.getIpaddr()).thenReturn("127.0.0.1");
        when(loginUser.getLoginLocation()).thenReturn("内网IP");
        when(loginUser.getBrowser()).thenReturn("Chrome");
        when(loginUser.getOs()).thenReturn("Windows");
        when(loginUser.getLoginTime()).thenReturn(1000L);
        when(loginUser.getUser()).thenReturn(sysUser);
        when(sysUser.getDept()).thenReturn(sysDept);
        when(sysDept.getDeptName()).thenReturn("研发部");
    }

    @Test
    void selectOnlineByIpaddr_shouldReturnUserWhenIpMatches() {
        setupLoginUser();

        SysUserOnline result = userOnlineService.selectOnlineByIpaddr("127.0.0.1", loginUser);

        assertThat(result).isNotNull();
        assertThat(result.getTokenId()).isEqualTo("token-abc");
        assertThat(result.getUserName()).isEqualTo("admin");
    }

    @Test
    void selectOnlineByIpaddr_shouldReturnNullWhenIpMismatches() {
        when(loginUser.getIpaddr()).thenReturn("10.0.0.1");

        SysUserOnline result = userOnlineService.selectOnlineByIpaddr("192.168.1.1", loginUser);

        assertThat(result).isNull();
    }

    @Test
    void selectOnlineByUserName_shouldReturnUserWhenNameMatches() {
        setupLoginUser();

        SysUserOnline result = userOnlineService.selectOnlineByUserName("admin", loginUser);

        assertThat(result).isNotNull();
        assertThat(result.getUserName()).isEqualTo("admin");
    }

    @Test
    void selectOnlineByUserName_shouldReturnNullWhenNameMismatches() {
        when(loginUser.getUsername()).thenReturn("other");

        SysUserOnline result = userOnlineService.selectOnlineByUserName("admin", loginUser);

        assertThat(result).isNull();
    }

    @Test
    void selectOnlineByInfo_shouldReturnUserWhenBothMatch() {
        setupLoginUser();

        SysUserOnline result = userOnlineService.selectOnlineByInfo("127.0.0.1", "admin", loginUser);

        assertThat(result).isNotNull();
    }

    @Test
    void selectOnlineByInfo_shouldReturnNullWhenIpMismatches() {
        setupLoginUser();
        when(loginUser.getIpaddr()).thenReturn("10.0.0.1");

        SysUserOnline result = userOnlineService.selectOnlineByInfo("192.168.1.1", "admin", loginUser);

        assertThat(result).isNull();
    }

    @Test
    void loginUserToUserOnline_shouldConvertCorrectly() {
        setupLoginUser();

        SysUserOnline result = userOnlineService.loginUserToUserOnline(loginUser);

        assertThat(result).isNotNull();
        assertThat(result.getTokenId()).isEqualTo("token-abc");
        assertThat(result.getUserName()).isEqualTo("admin");
        assertThat(result.getIpaddr()).isEqualTo("127.0.0.1");
        assertThat(result.getLoginLocation()).isEqualTo("内网IP");
        assertThat(result.getBrowser()).isEqualTo("Chrome");
        assertThat(result.getOs()).isEqualTo("Windows");
        assertThat(result.getLoginTime()).isEqualTo(1000L);
        assertThat(result.getDeptName()).isEqualTo("研发部");
    }

    @Test
    void loginUserToUserOnline_shouldReturnNullWhenUserIsNull() {
        SysUserOnline result = userOnlineService.loginUserToUserOnline(null);

        assertThat(result).isNull();
    }

    @Test
    void loginUserToUserOnline_shouldReturnNullWhenSysUserIsNull() {
        when(loginUser.getUser()).thenReturn(null);

        SysUserOnline result = userOnlineService.loginUserToUserOnline(loginUser);

        assertThat(result).isNull();
    }

    @Test
    void loginUserToUserOnline_shouldNotSetDeptNameWhenDeptIsNull() {
        setupLoginUser();
        when(sysUser.getDept()).thenReturn(null);

        SysUserOnline result = userOnlineService.loginUserToUserOnline(loginUser);

        assertThat(result.getDeptName()).isNull();
    }
}
