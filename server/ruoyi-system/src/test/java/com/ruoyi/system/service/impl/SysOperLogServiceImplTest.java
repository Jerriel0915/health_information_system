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

import com.ruoyi.system.domain.SysOperLog;
import com.ruoyi.system.mapper.SysOperLogMapper;

@ExtendWith(MockitoExtension.class)
class SysOperLogServiceImplTest {

    @Mock
    private SysOperLogMapper operLogMapper;

    @InjectMocks
    private SysOperLogServiceImpl operLogService;

    @Test
    void insertOperlog_shouldDelegateToMapper() {
        SysOperLog operLog = new SysOperLog();
        operLog.setOperId(1L);

        operLogService.insertOperlog(operLog);

        verify(operLogMapper).insertOperlog(operLog);
    }

    @Test
    void selectOperLogList_shouldReturnList() {
        SysOperLog operLog = new SysOperLog();
        operLog.setOperId(1L);
        List<SysOperLog> expected = Collections.singletonList(operLog);
        when(operLogMapper.selectOperLogList(any(SysOperLog.class))).thenReturn(expected);

        List<SysOperLog> result = operLogService.selectOperLogList(new SysOperLog());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getOperId()).isEqualTo(1L);
    }

    @Test
    void selectOperLogList_shouldReturnEmptyList() {
        when(operLogMapper.selectOperLogList(any(SysOperLog.class))).thenReturn(Collections.emptyList());

        List<SysOperLog> result = operLogService.selectOperLogList(new SysOperLog());

        assertThat(result).isEmpty();
    }

    @Test
    void selectOperLogById_shouldReturnOperLog() {
        SysOperLog operLog = new SysOperLog();
        operLog.setOperId(1L);
        when(operLogMapper.selectOperLogById(1L)).thenReturn(operLog);

        SysOperLog result = operLogService.selectOperLogById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getOperId()).isEqualTo(1L);
    }

    @Test
    void selectOperLogById_shouldReturnNullWhenNotFound() {
        when(operLogMapper.selectOperLogById(999L)).thenReturn(null);

        SysOperLog result = operLogService.selectOperLogById(999L);

        assertThat(result).isNull();
    }

    @Test
    void deleteOperLogByIds_shouldReturnAffectedRows() {
        Long[] ids = {1L, 2L, 3L};
        when(operLogMapper.deleteOperLogByIds(ids)).thenReturn(3);

        int result = operLogService.deleteOperLogByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(operLogMapper).deleteOperLogByIds(ids);
    }

    @Test
    void deleteOperLogByIds_shouldReturnZeroWhenNoneDeleted() {
        Long[] ids = {999L};
        when(operLogMapper.deleteOperLogByIds(ids)).thenReturn(0);

        int result = operLogService.deleteOperLogByIds(ids);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void cleanOperLog_shouldDelegateToMapper() {
        operLogService.cleanOperLog();

        verify(operLogMapper).cleanOperLog();
    }
}
