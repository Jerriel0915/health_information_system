package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;

@ExtendWith(MockitoExtension.class)
class SysConfigServiceImplTest {

    @Mock
    private SysConfigMapper configMapper;

    @Mock
    private RedisCache redisCache;

    @InjectMocks
    private SysConfigServiceImpl configService;

    @Test
    void selectConfigById_shouldReturnConfig() {
        SysConfig config = new SysConfig();
        config.setConfigId(1L);
        when(configMapper.selectConfig(any(SysConfig.class))).thenReturn(config);

        SysConfig result = configService.selectConfigById(1L);

        assertThat(result).isNotNull();
        verify(configMapper).selectConfig(any(SysConfig.class));
    }

    @Test
    void selectConfigList_shouldReturnList() {
        SysConfig config = new SysConfig();
        config.setConfigId(1L);
        when(configMapper.selectConfigList(any(SysConfig.class)))
                .thenReturn(Collections.singletonList(config));

        List<SysConfig> result = configService.selectConfigList(new SysConfig());

        assertThat(result).hasSize(1);
    }

    @Test
    void selectConfigByKey_shouldReturnFromCache() {
        when(redisCache.getCacheObject(CacheConstants.SYS_CONFIG_KEY + "test.key")).thenReturn("cachedValue");

        String result = configService.selectConfigByKey("test.key");

        assertThat(result).isEqualTo("cachedValue");
    }

    @Test
    void selectConfigByKey_shouldFallbackToDbWhenCacheMiss() {
        when(redisCache.getCacheObject(CacheConstants.SYS_CONFIG_KEY + "test.key")).thenReturn(null);
        SysConfig config = new SysConfig();
        config.setConfigKey("test.key");
        config.setConfigValue("dbValue");
        when(configMapper.selectConfig(any(SysConfig.class))).thenReturn(config);

        String result = configService.selectConfigByKey("test.key");

        assertThat(result).isEqualTo("dbValue");
        verify(redisCache).setCacheObject(CacheConstants.SYS_CONFIG_KEY + "test.key", "dbValue");
    }

    @Test
    void selectConfigByKey_shouldReturnEmptyWhenNotFound() {
        when(redisCache.getCacheObject(CacheConstants.SYS_CONFIG_KEY + "missing")).thenReturn(null);
        when(configMapper.selectConfig(any(SysConfig.class))).thenReturn(null);

        String result = configService.selectConfigByKey("missing");

        assertThat(result).isEmpty();
    }

    @Test
    void selectCaptchaEnabled_shouldReturnTrueByDefault() {
        when(redisCache.getCacheObject(CacheConstants.SYS_CONFIG_KEY + "sys.account.captchaEnabled")).thenReturn(null);
        when(configMapper.selectConfig(any(SysConfig.class))).thenReturn(null);

        boolean result = configService.selectCaptchaEnabled();

        assertThat(result).isTrue();
    }

    @Test
    void insertConfig_shouldInsertAndSetCache() {
        SysConfig config = new SysConfig();
        config.setConfigKey("test.key");
        config.setConfigValue("value");
        when(configMapper.insertConfig(any(SysConfig.class))).thenReturn(1);

        int result = configService.insertConfig(config);

        assertThat(result).isEqualTo(1);
        verify(redisCache).setCacheObject(CacheConstants.SYS_CONFIG_KEY + "test.key", "value");
    }

    @Test
    void insertConfig_shouldNotSetCacheWhenInsertFails() {
        SysConfig config = new SysConfig();
        config.setConfigKey("test.key");
        when(configMapper.insertConfig(any(SysConfig.class))).thenReturn(0);

        int result = configService.insertConfig(config);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void updateConfig_shouldUpdateAndUpdateCache() {
        SysConfig config = new SysConfig();
        config.setConfigId(1L);
        config.setConfigKey("new.key");
        config.setConfigValue("newValue");
        SysConfig oldConfig = new SysConfig();
        oldConfig.setConfigKey("new.key");
        when(configMapper.selectConfigById(1L)).thenReturn(oldConfig);
        when(configMapper.updateConfig(any(SysConfig.class))).thenReturn(1);

        int result = configService.updateConfig(config);

        assertThat(result).isEqualTo(1);
        verify(redisCache).setCacheObject(CacheConstants.SYS_CONFIG_KEY + "new.key", "newValue");
    }

    @Test
    void updateConfig_shouldRemoveOldCacheWhenKeyChanged() {
        SysConfig config = new SysConfig();
        config.setConfigId(1L);
        config.setConfigKey("new.key");
        config.setConfigValue("newValue");
        SysConfig oldConfig = new SysConfig();
        oldConfig.setConfigKey("old.key");
        when(configMapper.selectConfigById(1L)).thenReturn(oldConfig);
        when(configMapper.updateConfig(any(SysConfig.class))).thenReturn(1);

        int result = configService.updateConfig(config);

        assertThat(result).isEqualTo(1);
        verify(redisCache).deleteObject(CacheConstants.SYS_CONFIG_KEY + "old.key");
        verify(redisCache).setCacheObject(CacheConstants.SYS_CONFIG_KEY + "new.key", "newValue");
    }

    @Test
    void deleteConfigByIds_shouldThrowWhenDeletingBuiltinConfig() {
        Long[] ids = {1L};
        SysConfig config = new SysConfig();
        config.setConfigKey("builtin");
        config.setConfigType(UserConstants.YES);
        when(configMapper.selectConfig(any(SysConfig.class))).thenReturn(config);

        assertThatThrownBy(() -> configService.deleteConfigByIds(ids))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("内置参数");
    }

    @Test
    void deleteConfigByIds_shouldDeleteAndRemoveCache() {
        Long[] ids = {1L};
        SysConfig config = new SysConfig();
        config.setConfigKey("custom.key");
        config.setConfigType("0");
        when(configMapper.selectConfig(any(SysConfig.class))).thenReturn(config);

        configService.deleteConfigByIds(ids);

        verify(configMapper).deleteConfigById(1L);
        verify(redisCache).deleteObject(CacheConstants.SYS_CONFIG_KEY + "custom.key");
    }

    @Test
    void loadingConfigCache_shouldLoadAllIntoCache() {
        SysConfig config = new SysConfig();
        config.setConfigKey("test.key");
        config.setConfigValue("value");
        when(configMapper.selectConfigList(any(SysConfig.class)))
                .thenReturn(Collections.singletonList(config));

        configService.loadingConfigCache();

        verify(redisCache).setCacheObject(CacheConstants.SYS_CONFIG_KEY + "test.key", "value");
    }

    @Test
    void checkConfigKeyUnique_shouldReturnUniqueWhenNew() {
        SysConfig config = new SysConfig();
        when(configMapper.checkConfigKeyUnique(any())).thenReturn(null);

        boolean result = configService.checkConfigKeyUnique(config);

        assertThat(result).isTrue();
    }

    @Test
    void checkConfigKeyUnique_shouldReturnNotUniqueWhenKeyExists() {
        SysConfig config = new SysConfig();
        config.setConfigId(1L);
        config.setConfigKey("existing");
        SysConfig existing = new SysConfig();
        existing.setConfigId(2L);
        when(configMapper.checkConfigKeyUnique("existing")).thenReturn(existing);

        boolean result = configService.checkConfigKeyUnique(config);

        assertThat(result).isFalse();
    }
}
