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

import com.ruoyi.system.domain.MedicalCost;
import com.ruoyi.system.mapper.MedicalCostMapper;

@ExtendWith(MockitoExtension.class)
class MedicalCostServiceImplTest {

    @Mock
    private MedicalCostMapper medicalCostMapper;

    @InjectMocks
    private MedicalCostServiceImpl medicalCostService;

    @Test
    void selectMedicalCostById_shouldReturnMedicalCost() {
        MedicalCost cost = new MedicalCost();
        cost.setId(1L);
        when(medicalCostMapper.selectMedicalCostById(1L)).thenReturn(cost);

        MedicalCost result = medicalCostService.selectMedicalCostById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(medicalCostMapper).selectMedicalCostById(1L);
    }

    @Test
    void selectMedicalCostById_shouldReturnNullWhenNotFound() {
        when(medicalCostMapper.selectMedicalCostById(999L)).thenReturn(null);

        MedicalCost result = medicalCostService.selectMedicalCostById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectMedicalCostList_shouldReturnList() {
        MedicalCost cost = new MedicalCost();
        cost.setId(1L);
        List<MedicalCost> expected = Collections.singletonList(cost);
        when(medicalCostMapper.selectMedicalCostList(any(MedicalCost.class))).thenReturn(expected);

        List<MedicalCost> result = medicalCostService.selectMedicalCostList(new MedicalCost());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void selectMedicalCostList_shouldReturnEmptyList() {
        when(medicalCostMapper.selectMedicalCostList(any(MedicalCost.class)))
                .thenReturn(Collections.emptyList());

        List<MedicalCost> result = medicalCostService.selectMedicalCostList(new MedicalCost());

        assertThat(result).isEmpty();
    }

    @Test
    void insertMedicalCost_shouldReturnAffectedRows() {
        MedicalCost cost = new MedicalCost();
        when(medicalCostMapper.insertMedicalCost(any(MedicalCost.class))).thenReturn(1);

        int result = medicalCostService.insertMedicalCost(cost);

        assertThat(result).isEqualTo(1);
        verify(medicalCostMapper).insertMedicalCost(cost);
    }

    @Test
    void updateMedicalCost_shouldReturnAffectedRows() {
        MedicalCost cost = new MedicalCost();
        cost.setId(1L);
        when(medicalCostMapper.updateMedicalCost(any(MedicalCost.class))).thenReturn(1);

        int result = medicalCostService.updateMedicalCost(cost);

        assertThat(result).isEqualTo(1);
        verify(medicalCostMapper).updateMedicalCost(cost);
    }

    @Test
    void deleteMedicalCostByIds_shouldReturnAffectedRows() {
        Long[] ids = {1L, 2L, 3L};
        when(medicalCostMapper.deleteMedicalCostByIds(ids)).thenReturn(3);

        int result = medicalCostService.deleteMedicalCostByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(medicalCostMapper).deleteMedicalCostByIds(ids);
    }

    @Test
    void deleteMedicalCostById_shouldReturnAffectedRows() {
        when(medicalCostMapper.deleteMedicalCostById(1L)).thenReturn(1);

        int result = medicalCostService.deleteMedicalCostById(1L);

        assertThat(result).isEqualTo(1);
        verify(medicalCostMapper).deleteMedicalCostById(1L);
    }

    @Test
    void deleteMedicalCostById_shouldReturnZeroWhenNotFound() {
        when(medicalCostMapper.deleteMedicalCostById(999L)).thenReturn(0);

        int result = medicalCostService.deleteMedicalCostById(999L);

        assertThat(result).isEqualTo(0);
        verify(medicalCostMapper).deleteMedicalCostById(999L);
    }
}
