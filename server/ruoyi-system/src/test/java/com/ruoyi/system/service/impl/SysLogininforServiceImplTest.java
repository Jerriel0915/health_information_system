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

import com.ruoyi.system.domain.SysLogininfor;
import com.ruoyi.system.mapper.SysLogininforMapper;

@ExtendWith(MockitoExtension.class)
class SysLogininforServiceImplTest {

    @Mock
    private SysLogininforMapper logininforMapper;

    @InjectMocks
    private SysLogininforServiceImpl logininforService;

    @Test
    void insertLogininfor_shouldDelegateToMapper() {
        SysLogininfor logininfor = new SysLogininfor();

        logininforService.insertLogininfor(logininfor);

        verify(logininforMapper).insertLogininfor(logininfor);
    }

    @Test
    void selectLogininforList_shouldReturnList() {
        SysLogininfor info = new SysLogininfor();
        info.setInfoId(1L);
        List<SysLogininfor> expected = Collections.singletonList(info);
        when(logininforMapper.selectLogininforList(any(SysLogininfor.class))).thenReturn(expected);

        List<SysLogininfor> result = logininforService.selectLogininforList(new SysLogininfor());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getInfoId()).isEqualTo(1L);
    }

    @Test
    void selectLogininforList_shouldReturnEmptyList() {
        when(logininforMapper.selectLogininforList(any(SysLogininfor.class)))
                .thenReturn(Collections.emptyList());

        List<SysLogininfor> result = logininforService.selectLogininforList(new SysLogininfor());

        assertThat(result).isEmpty();
    }

    @Test
    void deleteLogininforByIds_shouldReturnAffectedRows() {
        Long[] ids = {1L, 2L, 3L};
        when(logininforMapper.deleteLogininforByIds(ids)).thenReturn(3);

        int result = logininforService.deleteLogininforByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(logininforMapper).deleteLogininforByIds(ids);
    }

    @Test
    void deleteLogininforByIds_shouldReturnZeroWhenNoneDeleted() {
        Long[] ids = {999L};
        when(logininforMapper.deleteLogininforByIds(ids)).thenReturn(0);

        int result = logininforService.deleteLogininforByIds(ids);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void cleanLogininfor_shouldDelegateToMapper() {
        logininforService.cleanLogininfor();

        verify(logininforMapper).cleanLogininfor();
    }
}
