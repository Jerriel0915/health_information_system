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

import com.ruoyi.system.domain.MedicalService;
import com.ruoyi.system.mapper.MedicalServiceMapper;

@ExtendWith(MockitoExtension.class)
class MedicalServiceServiceImplTest {

    @Mock
    private MedicalServiceMapper medicalServiceMapper;

    @InjectMocks
    private MedicalServiceServiceImpl medicalServiceService;

    @Test
    void selectMedicalServiceById_shouldReturnMedicalService() {
        MedicalService service = new MedicalService();
        service.setId(1L);
        when(medicalServiceMapper.selectMedicalServiceById(1L)).thenReturn(service);

        MedicalService result = medicalServiceService.selectMedicalServiceById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(medicalServiceMapper).selectMedicalServiceById(1L);
    }

    @Test
    void selectMedicalServiceById_shouldReturnNullWhenNotFound() {
        when(medicalServiceMapper.selectMedicalServiceById(999L)).thenReturn(null);

        MedicalService result = medicalServiceService.selectMedicalServiceById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectMedicalServiceList_shouldReturnList() {
        MedicalService service = new MedicalService();
        service.setId(1L);
        List<MedicalService> expected = Collections.singletonList(service);
        when(medicalServiceMapper.selectMedicalServiceList(any(MedicalService.class))).thenReturn(expected);

        List<MedicalService> result = medicalServiceService.selectMedicalServiceList(new MedicalService());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void selectMedicalServiceList_shouldReturnEmptyList() {
        when(medicalServiceMapper.selectMedicalServiceList(any(MedicalService.class)))
                .thenReturn(Collections.emptyList());

        List<MedicalService> result = medicalServiceService.selectMedicalServiceList(new MedicalService());

        assertThat(result).isEmpty();
    }

    @Test
    void insertMedicalService_shouldReturnAffectedRows() {
        MedicalService service = new MedicalService();
        when(medicalServiceMapper.insertMedicalService(any(MedicalService.class))).thenReturn(1);

        int result = medicalServiceService.insertMedicalService(service);

        assertThat(result).isEqualTo(1);
        verify(medicalServiceMapper).insertMedicalService(service);
    }

    @Test
    void updateMedicalService_shouldReturnAffectedRows() {
        MedicalService service = new MedicalService();
        service.setId(1L);
        when(medicalServiceMapper.updateMedicalService(any(MedicalService.class))).thenReturn(1);

        int result = medicalServiceService.updateMedicalService(service);

        assertThat(result).isEqualTo(1);
        verify(medicalServiceMapper).updateMedicalService(service);
    }

    @Test
    void deleteMedicalServiceByIds_shouldReturnAffectedRows() {
        Long[] ids = {1L, 2L, 3L};
        when(medicalServiceMapper.deleteMedicalServiceByIds(ids)).thenReturn(3);

        int result = medicalServiceService.deleteMedicalServiceByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(medicalServiceMapper).deleteMedicalServiceByIds(ids);
    }

    @Test
    void deleteMedicalServiceById_shouldReturnAffectedRows() {
        when(medicalServiceMapper.deleteMedicalServiceById(1L)).thenReturn(1);

        int result = medicalServiceService.deleteMedicalServiceById(1L);

        assertThat(result).isEqualTo(1);
        verify(medicalServiceMapper).deleteMedicalServiceById(1L);
    }

    @Test
    void deleteMedicalServiceById_shouldReturnZeroWhenNotFound() {
        when(medicalServiceMapper.deleteMedicalServiceById(999L)).thenReturn(0);

        int result = medicalServiceService.deleteMedicalServiceById(999L);

        assertThat(result).isEqualTo(0);
        verify(medicalServiceMapper).deleteMedicalServiceById(999L);
    }
}
