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

import com.ruoyi.system.domain.MedicalBed;
import com.ruoyi.system.mapper.MedicalBedMapper;

@ExtendWith(MockitoExtension.class)
class MedicalBedServiceImplTest {

    @Mock
    private MedicalBedMapper medicalBedMapper;

    @InjectMocks
    private MedicalBedServiceImpl medicalBedService;

    @Test
    void selectMedicalBedById_shouldReturnMedicalBed() {
        MedicalBed bed = new MedicalBed();
        bed.setId(1L);
        when(medicalBedMapper.selectMedicalBedById(1L)).thenReturn(bed);

        MedicalBed result = medicalBedService.selectMedicalBedById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(medicalBedMapper).selectMedicalBedById(1L);
    }

    @Test
    void selectMedicalBedById_shouldReturnNullWhenNotFound() {
        when(medicalBedMapper.selectMedicalBedById(999L)).thenReturn(null);

        MedicalBed result = medicalBedService.selectMedicalBedById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectMedicalBedList_shouldReturnList() {
        MedicalBed bed = new MedicalBed();
        bed.setId(1L);
        List<MedicalBed> expected = Collections.singletonList(bed);
        when(medicalBedMapper.selectMedicalBedList(any(MedicalBed.class))).thenReturn(expected);

        List<MedicalBed> result = medicalBedService.selectMedicalBedList(new MedicalBed());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void selectMedicalBedList_shouldReturnEmptyList() {
        when(medicalBedMapper.selectMedicalBedList(any(MedicalBed.class)))
                .thenReturn(Collections.emptyList());

        List<MedicalBed> result = medicalBedService.selectMedicalBedList(new MedicalBed());

        assertThat(result).isEmpty();
    }

    @Test
    void insertMedicalBed_shouldReturnAffectedRows() {
        MedicalBed bed = new MedicalBed();
        when(medicalBedMapper.insertMedicalBed(any(MedicalBed.class))).thenReturn(1);

        int result = medicalBedService.insertMedicalBed(bed);

        assertThat(result).isEqualTo(1);
        verify(medicalBedMapper).insertMedicalBed(bed);
    }

    @Test
    void updateMedicalBed_shouldReturnAffectedRows() {
        MedicalBed bed = new MedicalBed();
        bed.setId(1L);
        when(medicalBedMapper.updateMedicalBed(any(MedicalBed.class))).thenReturn(1);

        int result = medicalBedService.updateMedicalBed(bed);

        assertThat(result).isEqualTo(1);
        verify(medicalBedMapper).updateMedicalBed(bed);
    }

    @Test
    void deleteMedicalBedByIds_shouldReturnAffectedRows() {
        Long[] ids = {1L, 2L, 3L};
        when(medicalBedMapper.deleteMedicalBedByIds(ids)).thenReturn(3);

        int result = medicalBedService.deleteMedicalBedByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(medicalBedMapper).deleteMedicalBedByIds(ids);
    }

    @Test
    void deleteMedicalBedById_shouldReturnAffectedRows() {
        when(medicalBedMapper.deleteMedicalBedById(1L)).thenReturn(1);

        int result = medicalBedService.deleteMedicalBedById(1L);

        assertThat(result).isEqualTo(1);
        verify(medicalBedMapper).deleteMedicalBedById(1L);
    }

    @Test
    void deleteMedicalBedById_shouldReturnZeroWhenNotFound() {
        when(medicalBedMapper.deleteMedicalBedById(999L)).thenReturn(0);

        int result = medicalBedService.deleteMedicalBedById(999L);

        assertThat(result).isEqualTo(0);
        verify(medicalBedMapper).deleteMedicalBedById(999L);
    }
}
