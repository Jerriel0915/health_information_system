package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import com.ruoyi.common.core.domain.entity.SysDictData;
import com.ruoyi.common.core.domain.entity.SysDictType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.system.mapper.SysDictDataMapper;
import com.ruoyi.system.mapper.SysDictTypeMapper;

@ExtendWith(MockitoExtension.class)
class SysDictTypeServiceImplTest {

    @Mock
    private SysDictTypeMapper dictTypeMapper;

    @Mock
    private SysDictDataMapper dictDataMapper;

    @InjectMocks
    private SysDictTypeServiceImpl dictTypeService;

    // ---- select queries ----

    @Test
    void selectDictTypeList_shouldReturnList() {
        SysDictType dictType = new SysDictType();
        dictType.setDictId(1L);
        when(dictTypeMapper.selectDictTypeList(any(SysDictType.class)))
                .thenReturn(Collections.singletonList(dictType));

        List<SysDictType> result = dictTypeService.selectDictTypeList(new SysDictType());

        assertThat(result).hasSize(1);
    }

    @Test
    void selectDictTypeAll_shouldReturnAll() {
        SysDictType dictType = new SysDictType();
        dictType.setDictId(1L);
        when(dictTypeMapper.selectDictTypeAll()).thenReturn(Collections.singletonList(dictType));

        List<SysDictType> result = dictTypeService.selectDictTypeAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void selectDictTypeById_shouldReturnDictType() {
        SysDictType dictType = new SysDictType();
        dictType.setDictId(1L);
        when(dictTypeMapper.selectDictTypeById(1L)).thenReturn(dictType);

        SysDictType result = dictTypeService.selectDictTypeById(1L);

        assertThat(result.getDictId()).isEqualTo(1L);
    }

    @Test
    void selectDictTypeByType_shouldReturnDictType() {
        SysDictType dictType = new SysDictType();
        dictType.setDictType("sys_user_sex");
        when(dictTypeMapper.selectDictTypeByType("sys_user_sex")).thenReturn(dictType);

        SysDictType result = dictTypeService.selectDictTypeByType("sys_user_sex");

        assertThat(result.getDictType()).isEqualTo("sys_user_sex");
    }

    // ---- selectDictDataByType ----

    @Test
    void selectDictDataByType_shouldReturnFromCacheWhenPresent() {
        SysDictData data = new SysDictData();
        data.setDictCode(1L);
        List<SysDictData> cached = Collections.singletonList(data);

        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            dictUtils.when(() -> DictUtils.getDictCache("sys_user_sex")).thenReturn(cached);

            List<SysDictData> result = dictTypeService.selectDictDataByType("sys_user_sex");

            assertThat(result).hasSize(1);
            dictUtils.verify(() -> DictUtils.getDictCache("sys_user_sex"));
        }
    }

    @Test
    void selectDictDataByType_shouldFallbackToDbWhenCacheEmpty() {
        SysDictData data = new SysDictData();
        data.setDictCode(1L);
        List<SysDictData> dbResult = Collections.singletonList(data);

        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            dictUtils.when(() -> DictUtils.getDictCache("sys_user_sex")).thenReturn(null);
            when(dictDataMapper.selectDictDataByType("sys_user_sex")).thenReturn(dbResult);

            List<SysDictData> result = dictTypeService.selectDictDataByType("sys_user_sex");

            assertThat(result).hasSize(1);
            dictUtils.verify(() -> DictUtils.setDictCache("sys_user_sex", dbResult));
        }
    }

    @Test
    void selectDictDataByType_shouldReturnNullWhenNotFound() {
        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            dictUtils.when(() -> DictUtils.getDictCache("unknown")).thenReturn(null);
            when(dictDataMapper.selectDictDataByType("unknown")).thenReturn(null);

            List<SysDictData> result = dictTypeService.selectDictDataByType("unknown");

            assertThat(result).isNull();
        }
    }

    // ---- uniqueness check ----

    @Test
    void checkDictTypeUnique_shouldReturnUniqueWhenNew() {
        SysDictType dict = new SysDictType();
        when(dictTypeMapper.checkDictTypeUnique(any())).thenReturn(null);

        boolean result = dictTypeService.checkDictTypeUnique(dict);

        assertThat(result).isTrue();
    }

    @Test
    void checkDictTypeUnique_shouldReturnNotUniqueWhenTypeExists() {
        SysDictType dict = new SysDictType();
        dict.setDictId(1L);
        dict.setDictType("existing");
        SysDictType existing = new SysDictType();
        existing.setDictId(2L);
        when(dictTypeMapper.checkDictTypeUnique("existing")).thenReturn(existing);

        boolean result = dictTypeService.checkDictTypeUnique(dict);

        assertThat(result).isFalse();
    }

    // ---- insert ----

    @Test
    void insertDictType_shouldInsertAndSetCache() {
        SysDictType dict = new SysDictType();
        dict.setDictType("new_type");
        when(dictTypeMapper.insertDictType(any(SysDictType.class))).thenReturn(1);

        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            int result = dictTypeService.insertDictType(dict);

            assertThat(result).isEqualTo(1);
            dictUtils.verify(() -> DictUtils.setDictCache("new_type", null));
        }
    }

    // ---- delete ----

    @Test
    void deleteDictTypeByIds_shouldDeleteWhenNoDataAssigned() {
        Long[] ids = {1L};
        SysDictType dict = new SysDictType();
        dict.setDictId(1L);
        dict.setDictType("test_type");
        dict.setDictName("测试");
        when(dictTypeMapper.selectDictTypeById(1L)).thenReturn(dict);
        when(dictDataMapper.countDictDataByType("test_type")).thenReturn(0);

        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            dictTypeService.deleteDictTypeByIds(ids);

            verify(dictTypeMapper).deleteDictTypeById(1L);
            dictUtils.verify(() -> DictUtils.removeDictCache("test_type"));
        }
    }

    @Test
    void deleteDictTypeByIds_shouldThrowWhenDataAssigned() {
        Long[] ids = {1L};
        SysDictType dict = new SysDictType();
        dict.setDictId(1L);
        dict.setDictType("test_type");
        dict.setDictName("测试");
        when(dictTypeMapper.selectDictTypeById(1L)).thenReturn(dict);
        when(dictDataMapper.countDictDataByType("test_type")).thenReturn(5);

        assertThatThrownBy(() -> dictTypeService.deleteDictTypeByIds(ids))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已分配,不能删除");
    }

    // ---- cache operations ----

    @Test
    void clearDictCache_shouldClearCache() {
        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            dictTypeService.clearDictCache();

            dictUtils.verify(DictUtils::clearDictCache);
        }
    }

    @Test
    void resetDictCache_shouldClearAndReload() {
        SysDictData data = new SysDictData();
        data.setDictType("test_type");
        data.setDictSort(1L);
        when(dictDataMapper.selectDictDataList(any(SysDictData.class)))
                .thenReturn(Collections.singletonList(data));

        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            dictTypeService.resetDictCache();

            dictUtils.verify(DictUtils::clearDictCache);
            dictUtils.verify(() -> DictUtils.setDictCache(anyString(), any()));
        }
    }
}
