package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.mapper.SysNoticeReadMapper;

@ExtendWith(MockitoExtension.class)
class SysNoticeReadServiceImplTest {

    @Mock
    private SysNoticeReadMapper noticeReadMapper;

    @InjectMocks
    private SysNoticeReadServiceImpl noticeReadService;

    @Test
    void markRead_shouldInsertRecord() {
        noticeReadService.markRead(1L, 100L);

        verify(noticeReadMapper).insertNoticeRead(any());
    }

    @Test
    void selectUnreadCount_shouldReturnCount() {
        when(noticeReadMapper.selectUnreadCount(100L)).thenReturn(5);

        int result = noticeReadService.selectUnreadCount(100L);

        assertThat(result).isEqualTo(5);
        verify(noticeReadMapper).selectUnreadCount(100L);
    }

    @Test
    void selectNoticeListWithReadStatus_shouldReturnList() {
        SysNotice notice = new SysNotice();
        notice.setNoticeId(1L);
        List<SysNotice> expected = Collections.singletonList(notice);
        when(noticeReadMapper.selectNoticeListWithReadStatus(100L, 10)).thenReturn(expected);

        List<SysNotice> result = noticeReadService.selectNoticeListWithReadStatus(100L, 10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNoticeId()).isEqualTo(1L);
    }

    @Test
    void selectNoticeListWithReadStatus_shouldReturnEmptyList() {
        when(noticeReadMapper.selectNoticeListWithReadStatus(anyLong(), anyInt()))
                .thenReturn(Collections.emptyList());

        List<SysNotice> result = noticeReadService.selectNoticeListWithReadStatus(1L, 5);

        assertThat(result).isEmpty();
    }

    @Test
    void markReadBatch_shouldInsertBatch() {
        Long[] noticeIds = {1L, 2L, 3L};

        noticeReadService.markReadBatch(100L, noticeIds);

        verify(noticeReadMapper).insertNoticeReadBatch(100L, noticeIds);
    }

    @Test
    void markReadBatch_shouldNotInsertWhenArrayIsNull() {
        noticeReadService.markReadBatch(100L, null);

        verify(noticeReadMapper, never()).insertNoticeReadBatch(anyLong(), any());
    }

    @Test
    void markReadBatch_shouldNotInsertWhenArrayIsEmpty() {
        noticeReadService.markReadBatch(100L, new Long[0]);

        verify(noticeReadMapper, never()).insertNoticeReadBatch(anyLong(), any());
    }

    @Test
    void selectReadUsersByNoticeId_shouldReturnList() {
        List<Map<String, Object>> expected = Collections.singletonList(Collections.singletonMap("userName", "admin"));
        when(noticeReadMapper.selectReadUsersByNoticeId(1L, "")).thenReturn(expected);

        List<Map<String, Object>> result = noticeReadService.selectReadUsersByNoticeId(1L, "");

        assertThat(result).hasSize(1);
    }

    @Test
    void deleteByNoticeIds_shouldDelegateToMapper() {
        Long[] noticeIds = {1L, 2L};

        noticeReadService.deleteByNoticeIds(noticeIds);

        verify(noticeReadMapper).deleteByNoticeIds(noticeIds);
    }
}
