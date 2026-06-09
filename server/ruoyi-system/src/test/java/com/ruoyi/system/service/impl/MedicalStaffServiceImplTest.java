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

import com.ruoyi.system.domain.MedicalStaff;
import com.ruoyi.system.mapper.MedicalStaffMapper;

@ExtendWith(MockitoExtension.class)
class MedicalStaffServiceImplTest {

    @Mock
    private MedicalStaffMapper medicalStaffMapper;

    @InjectMocks
    private MedicalStaffServiceImpl medicalStaffService;

    @Test
    void selectMedicalStaffById_shouldReturnMedicalStaff() {
        MedicalStaff staff = new MedicalStaff();
        staff.setId(1L);
        when(medicalStaffMapper.selectMedicalStaffById(1L)).thenReturn(staff);

        MedicalStaff result = medicalStaffService.selectMedicalStaffById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(medicalStaffMapper).selectMedicalStaffById(1L);
    }

    @Test
    void selectMedicalStaffById_shouldReturnNullWhenNotFound() {
        when(medicalStaffMapper.selectMedicalStaffById(999L)).thenReturn(null);

        MedicalStaff result = medicalStaffService.selectMedicalStaffById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectMedicalStaffList_shouldReturnList() {
        MedicalStaff staff = new MedicalStaff();
        staff.setId(1L);
        List<MedicalStaff> expected = Collections.singletonList(staff);
        when(medicalStaffMapper.selectMedicalStaffList(any(MedicalStaff.class))).thenReturn(expected);

        List<MedicalStaff> result = medicalStaffService.selectMedicalStaffList(new MedicalStaff());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void selectMedicalStaffList_shouldReturnEmptyList() {
        when(medicalStaffMapper.selectMedicalStaffList(any(MedicalStaff.class)))
                .thenReturn(Collections.emptyList());

        List<MedicalStaff> result = medicalStaffService.selectMedicalStaffList(new MedicalStaff());

        assertThat(result).isEmpty();
    }

    @Test
    void insertMedicalStaff_shouldReturnAffectedRows() {
        MedicalStaff staff = new MedicalStaff();
        when(medicalStaffMapper.insertMedicalStaff(any(MedicalStaff.class))).thenReturn(1);

        int result = medicalStaffService.insertMedicalStaff(staff);

        assertThat(result).isEqualTo(1);
        verify(medicalStaffMapper).insertMedicalStaff(staff);
    }

    @Test
    void updateMedicalStaff_shouldReturnAffectedRows() {
        MedicalStaff staff = new MedicalStaff();
        staff.setId(1L);
        when(medicalStaffMapper.updateMedicalStaff(any(MedicalStaff.class))).thenReturn(1);

        int result = medicalStaffService.updateMedicalStaff(staff);

        assertThat(result).isEqualTo(1);
        verify(medicalStaffMapper).updateMedicalStaff(staff);
    }

    @Test
    void deleteMedicalStaffByIds_shouldReturnAffectedRows() {
        Long[] ids = {1L, 2L, 3L};
        when(medicalStaffMapper.deleteMedicalStaffByIds(ids)).thenReturn(3);

        int result = medicalStaffService.deleteMedicalStaffByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(medicalStaffMapper).deleteMedicalStaffByIds(ids);
    }

    @Test
    void deleteMedicalStaffById_shouldReturnAffectedRows() {
        when(medicalStaffMapper.deleteMedicalStaffById(1L)).thenReturn(1);

        int result = medicalStaffService.deleteMedicalStaffById(1L);

        assertThat(result).isEqualTo(1);
        verify(medicalStaffMapper).deleteMedicalStaffById(1L);
    }

    @Test
    void deleteMedicalStaffById_shouldReturnZeroWhenNotFound() {
        when(medicalStaffMapper.deleteMedicalStaffById(999L)).thenReturn(0);

        int result = medicalStaffService.deleteMedicalStaffById(999L);

        assertThat(result).isEqualTo(0);
        verify(medicalStaffMapper).deleteMedicalStaffById(999L);
    }
}
