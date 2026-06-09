package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ruoyi.system.domain.DimRegion;
import com.ruoyi.system.mapper.DimRegionMapper;

@ExtendWith(MockitoExtension.class)
class DimRegionServiceImplTest {

    @Mock
    private DimRegionMapper dimRegionMapper;

    @InjectMocks
    private DimRegionServiceImpl dimRegionService;

    @Test
    void selectDimRegionById_shouldReturnDimRegion() {
        DimRegion region = new DimRegion();
        region.setId(1L);
        when(dimRegionMapper.selectDimRegionById(1L)).thenReturn(region);

        DimRegion result = dimRegionService.selectDimRegionById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(dimRegionMapper).selectDimRegionById(1L);
    }

    @Test
    void selectDimRegionById_shouldReturnNullWhenNotFound() {
        when(dimRegionMapper.selectDimRegionById(999L)).thenReturn(null);

        DimRegion result = dimRegionService.selectDimRegionById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectDimRegionList_shouldReturnList() {
        DimRegion region = new DimRegion();
        region.setId(1L);
        List<DimRegion> expected = Collections.singletonList(region);
        when(dimRegionMapper.selectDimRegionList(any(DimRegion.class))).thenReturn(expected);

        List<DimRegion> result = dimRegionService.selectDimRegionList(new DimRegion());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void selectDimRegionList_shouldReturnEmptyList() {
        when(dimRegionMapper.selectDimRegionList(any(DimRegion.class)))
                .thenReturn(Collections.emptyList());

        List<DimRegion> result = dimRegionService.selectDimRegionList(new DimRegion());

        assertThat(result).isEmpty();
    }

    @Test
    void insertDimRegion_shouldReturnAffectedRows() {
        DimRegion region = new DimRegion();
        when(dimRegionMapper.insertDimRegion(any(DimRegion.class))).thenReturn(1);

        int result = dimRegionService.insertDimRegion(region);

        assertThat(result).isEqualTo(1);
        verify(dimRegionMapper).insertDimRegion(region);
    }

    @Test
    void updateDimRegion_shouldReturnAffectedRows() {
        DimRegion region = new DimRegion();
        region.setId(1L);
        when(dimRegionMapper.updateDimRegion(any(DimRegion.class))).thenReturn(1);

        int result = dimRegionService.updateDimRegion(region);

        assertThat(result).isEqualTo(1);
        verify(dimRegionMapper).updateDimRegion(region);
    }

    @Test
    void deleteDimRegionByIds_shouldReturnAffectedRows() {
        Long[] ids = {1L, 2L, 3L};
        when(dimRegionMapper.deleteDimRegionByIds(ids)).thenReturn(3);

        int result = dimRegionService.deleteDimRegionByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(dimRegionMapper).deleteDimRegionByIds(ids);
    }

    @Test
    void deleteDimRegionById_shouldReturnAffectedRows() {
        when(dimRegionMapper.deleteDimRegionById(1L)).thenReturn(1);

        int result = dimRegionService.deleteDimRegionById(1L);

        assertThat(result).isEqualTo(1);
        verify(dimRegionMapper).deleteDimRegionById(1L);
    }

    @Test
    void deleteDimRegionById_shouldReturnZeroWhenNotFound() {
        when(dimRegionMapper.deleteDimRegionById(999L)).thenReturn(0);

        int result = dimRegionService.deleteDimRegionById(999L);

        assertThat(result).isEqualTo(0);
        verify(dimRegionMapper).deleteDimRegionById(999L);
    }
}
