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

import com.ruoyi.system.domain.MedicalInstitution;
import com.ruoyi.system.mapper.MedicalInstitutionMapper;

@ExtendWith(MockitoExtension.class)
class MedicalInstitutionServiceImplTest {

    @Mock
    private MedicalInstitutionMapper medicalInstitutionMapper;

    @InjectMocks
    private MedicalInstitutionServiceImpl medicalInstitutionService;

    @Test
    void selectMedicalInstitutionById_shouldReturnMedicalInstitution() {
        MedicalInstitution institution = new MedicalInstitution();
        institution.setId(1L);
        when(medicalInstitutionMapper.selectMedicalInstitutionById(1L)).thenReturn(institution);

        MedicalInstitution result = medicalInstitutionService.selectMedicalInstitutionById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(medicalInstitutionMapper).selectMedicalInstitutionById(1L);
    }

    @Test
    void selectMedicalInstitutionById_shouldReturnNullWhenNotFound() {
        when(medicalInstitutionMapper.selectMedicalInstitutionById(999L)).thenReturn(null);

        MedicalInstitution result = medicalInstitutionService.selectMedicalInstitutionById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectMedicalInstitutionList_shouldReturnList() {
        MedicalInstitution institution = new MedicalInstitution();
        institution.setId(1L);
        List<MedicalInstitution> expected = Collections.singletonList(institution);
        when(medicalInstitutionMapper.selectMedicalInstitutionList(any(MedicalInstitution.class)))
                .thenReturn(expected);

        List<MedicalInstitution> result = medicalInstitutionService
                .selectMedicalInstitutionList(new MedicalInstitution());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void selectMedicalInstitutionList_shouldReturnEmptyList() {
        when(medicalInstitutionMapper.selectMedicalInstitutionList(any(MedicalInstitution.class)))
                .thenReturn(Collections.emptyList());

        List<MedicalInstitution> result = medicalInstitutionService
                .selectMedicalInstitutionList(new MedicalInstitution());

        assertThat(result).isEmpty();
    }

    @Test
    void insertMedicalInstitution_shouldReturnAffectedRows() {
        MedicalInstitution institution = new MedicalInstitution();
        when(medicalInstitutionMapper.insertMedicalInstitution(any(MedicalInstitution.class))).thenReturn(1);

        int result = medicalInstitutionService.insertMedicalInstitution(institution);

        assertThat(result).isEqualTo(1);
        verify(medicalInstitutionMapper).insertMedicalInstitution(institution);
    }

    @Test
    void updateMedicalInstitution_shouldReturnAffectedRows() {
        MedicalInstitution institution = new MedicalInstitution();
        institution.setId(1L);
        when(medicalInstitutionMapper.updateMedicalInstitution(any(MedicalInstitution.class))).thenReturn(1);

        int result = medicalInstitutionService.updateMedicalInstitution(institution);

        assertThat(result).isEqualTo(1);
        verify(medicalInstitutionMapper).updateMedicalInstitution(institution);
    }

    @Test
    void deleteMedicalInstitutionByIds_shouldReturnAffectedRows() {
        Long[] ids = {1L, 2L, 3L};
        when(medicalInstitutionMapper.deleteMedicalInstitutionByIds(ids)).thenReturn(3);

        int result = medicalInstitutionService.deleteMedicalInstitutionByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(medicalInstitutionMapper).deleteMedicalInstitutionByIds(ids);
    }

    @Test
    void deleteMedicalInstitutionById_shouldReturnAffectedRows() {
        when(medicalInstitutionMapper.deleteMedicalInstitutionById(1L)).thenReturn(1);

        int result = medicalInstitutionService.deleteMedicalInstitutionById(1L);

        assertThat(result).isEqualTo(1);
        verify(medicalInstitutionMapper).deleteMedicalInstitutionById(1L);
    }

    @Test
    void deleteMedicalInstitutionById_shouldReturnZeroWhenNotFound() {
        when(medicalInstitutionMapper.deleteMedicalInstitutionById(999L)).thenReturn(0);

        int result = medicalInstitutionService.deleteMedicalInstitutionById(999L);

        assertThat(result).isEqualTo(0);
        verify(medicalInstitutionMapper).deleteMedicalInstitutionById(999L);
    }
}
