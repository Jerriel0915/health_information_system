package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.system.mapper.SysDictDataMapper;

@ExtendWith(MockitoExtension.class)
class SysDictDataServiceImplTest {

    @Mock
    private SysDictDataMapper dictDataMapper;

    @InjectMocks
    private SysDictDataServiceImpl dictDataService;

    @Test
    void selectDictDataList_shouldReturnList() {
        SysDictData data = new SysDictData();
        data.setDictCode(1L);
        when(dictDataMapper.selectDictDataList(any(SysDictData.class)))
                .thenReturn(Collections.singletonList(data));

        List<SysDictData> result = dictDataService.selectDictDataList(new SysDictData());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDictCode()).isEqualTo(1L);
    }

    @Test
    void selectDictDataList_shouldReturnEmptyList() {
        when(dictDataMapper.selectDictDataList(any(SysDictData.class)))
                .thenReturn(Collections.emptyList());

        List<SysDictData> result = dictDataService.selectDictDataList(new SysDictData());

        assertThat(result).isEmpty();
    }

    @Test
    void selectDictLabel_shouldReturnLabel() {
        when(dictDataMapper.selectDictLabel("sys_user_sex", "0")).thenReturn("男");

        String result = dictDataService.selectDictLabel("sys_user_sex", "0");

        assertThat(result).isEqualTo("男");
    }

    @Test
    void selectDictDataById_shouldReturnDictData() {
        SysDictData data = new SysDictData();
        data.setDictCode(1L);
        when(dictDataMapper.selectDictDataById(1L)).thenReturn(data);

        SysDictData result = dictDataService.selectDictDataById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getDictCode()).isEqualTo(1L);
    }

    @Test
    void selectDictDataById_shouldReturnNullWhenNotFound() {
        when(dictDataMapper.selectDictDataById(999L)).thenReturn(null);

        SysDictData result = dictDataService.selectDictDataById(999L);

        assertThat(result).isNull();
    }

    @Test
    void insertDictData_shouldInsertAndUpdateCache() {
        SysDictData data = new SysDictData();
        data.setDictType("sys_user_sex");
        when(dictDataMapper.insertDictData(any(SysDictData.class))).thenReturn(1);
        when(dictDataMapper.selectDictDataByType("sys_user_sex"))
                .thenReturn(Collections.singletonList(data));

        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            int result = dictDataService.insertDictData(data);

            assertThat(result).isEqualTo(1);
            dictUtils.verify(() -> DictUtils.setDictCache("sys_user_sex", Collections.singletonList(data)));
        }
    }

    @Test
    void insertDictData_shouldNotUpdateCacheWhenInsertFails() {
        SysDictData data = new SysDictData();
        data.setDictType("sys_user_sex");
        when(dictDataMapper.insertDictData(any(SysDictData.class))).thenReturn(0);

        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            int result = dictDataService.insertDictData(data);

            assertThat(result).isEqualTo(0);
            dictUtils.verifyNoInteractions();
        }
    }

    @Test
    void updateDictData_shouldUpdateAndRefreshCache() {
        SysDictData data = new SysDictData();
        data.setDictCode(1L);
        data.setDictType("sys_user_sex");
        when(dictDataMapper.updateDictData(any(SysDictData.class))).thenReturn(1);
        when(dictDataMapper.selectDictDataByType("sys_user_sex"))
                .thenReturn(Collections.singletonList(data));

        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            int result = dictDataService.updateDictData(data);

            assertThat(result).isEqualTo(1);
            dictUtils.verify(() -> DictUtils.setDictCache("sys_user_sex", Collections.singletonList(data)));
        }
    }

    @Test
    void deleteDictDataByIds_shouldDeleteAndUpdateCache() {
        Long[] ids = {1L};
        SysDictData data = new SysDictData();
        data.setDictCode(1L);
        data.setDictType("sys_user_sex");
        when(dictDataMapper.selectDictDataById(1L)).thenReturn(data);
        when(dictDataMapper.selectDictDataByType("sys_user_sex"))
                .thenReturn(Collections.singletonList(data));

        try (MockedStatic<DictUtils> dictUtils = mockStatic(DictUtils.class)) {
            dictDataService.deleteDictDataByIds(ids);

            verify(dictDataMapper).deleteDictDataById(1L);
            dictUtils.verify(() -> DictUtils.setDictCache("sys_user_sex", Collections.singletonList(data)));
        }
    }
}
