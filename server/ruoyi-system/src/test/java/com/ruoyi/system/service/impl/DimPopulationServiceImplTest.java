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

import com.ruoyi.system.domain.DimPopulation;
import com.ruoyi.system.mapper.DimPopulationMapper;

@ExtendWith(MockitoExtension.class)
class DimPopulationServiceImplTest {

    @Mock
    private DimPopulationMapper dimPopulationMapper;

    @InjectMocks
    private DimPopulationServiceImpl dimPopulationService;

    @Test
    void selectDimPopulationById_shouldReturnDimPopulation() {
        DimPopulation population = new DimPopulation();
        population.setId(1L);
        when(dimPopulationMapper.selectDimPopulationById(1L)).thenReturn(population);

        DimPopulation result = dimPopulationService.selectDimPopulationById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(dimPopulationMapper).selectDimPopulationById(1L);
    }

    @Test
    void selectDimPopulationById_shouldReturnNullWhenNotFound() {
        when(dimPopulationMapper.selectDimPopulationById(999L)).thenReturn(null);

        DimPopulation result = dimPopulationService.selectDimPopulationById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectDimPopulationList_shouldReturnList() {
        DimPopulation population = new DimPopulation();
        population.setId(1L);
        List<DimPopulation> expected = Collections.singletonList(population);
        when(dimPopulationMapper.selectDimPopulationList(any(DimPopulation.class))).thenReturn(expected);

        List<DimPopulation> result = dimPopulationService.selectDimPopulationList(new DimPopulation());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void selectDimPopulationList_shouldReturnEmptyList() {
        when(dimPopulationMapper.selectDimPopulationList(any(DimPopulation.class)))
                .thenReturn(Collections.emptyList());

        List<DimPopulation> result = dimPopulationService.selectDimPopulationList(new DimPopulation());

        assertThat(result).isEmpty();
    }

    @Test
    void insertDimPopulation_shouldReturnAffectedRows() {
        DimPopulation population = new DimPopulation();
        when(dimPopulationMapper.insertDimPopulation(any(DimPopulation.class))).thenReturn(1);

        int result = dimPopulationService.insertDimPopulation(population);

        assertThat(result).isEqualTo(1);
        verify(dimPopulationMapper).insertDimPopulation(population);
    }

    @Test
    void updateDimPopulation_shouldReturnAffectedRows() {
        DimPopulation population = new DimPopulation();
        population.setId(1L);
        when(dimPopulationMapper.updateDimPopulation(any(DimPopulation.class))).thenReturn(1);

        int result = dimPopulationService.updateDimPopulation(population);

        assertThat(result).isEqualTo(1);
        verify(dimPopulationMapper).updateDimPopulation(population);
    }

    @Test
    void deleteDimPopulationByIds_shouldReturnAffectedRows() {
        Long[] ids = {1L, 2L, 3L};
        when(dimPopulationMapper.deleteDimPopulationByIds(ids)).thenReturn(3);

        int result = dimPopulationService.deleteDimPopulationByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(dimPopulationMapper).deleteDimPopulationByIds(ids);
    }

    @Test
    void deleteDimPopulationById_shouldReturnAffectedRows() {
        when(dimPopulationMapper.deleteDimPopulationById(1L)).thenReturn(1);

        int result = dimPopulationService.deleteDimPopulationById(1L);

        assertThat(result).isEqualTo(1);
        verify(dimPopulationMapper).deleteDimPopulationById(1L);
    }

    @Test
    void deleteDimPopulationById_shouldReturnZeroWhenNotFound() {
        when(dimPopulationMapper.deleteDimPopulationById(999L)).thenReturn(0);

        int result = dimPopulationService.deleteDimPopulationById(999L);

        assertThat(result).isEqualTo(0);
        verify(dimPopulationMapper).deleteDimPopulationById(999L);
    }
}
