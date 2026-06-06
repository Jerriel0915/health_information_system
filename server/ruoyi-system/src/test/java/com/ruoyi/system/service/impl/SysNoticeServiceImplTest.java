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

import com.ruoyi.system.domain.SysNotice;
import com.ruoyi.system.mapper.SysNoticeMapper;

@ExtendWith(MockitoExtension.class)
class SysNoticeServiceImplTest {

    @Mock
    private SysNoticeMapper noticeMapper;

    @InjectMocks
    private SysNoticeServiceImpl noticeService;

    @Test
    void selectNoticeById_shouldReturnNotice() {
        SysNotice notice = new SysNotice();
        notice.setNoticeId(1L);
        when(noticeMapper.selectNoticeById(1L)).thenReturn(notice);

        SysNotice result = noticeService.selectNoticeById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getNoticeId()).isEqualTo(1L);
        verify(noticeMapper).selectNoticeById(1L);
    }

    @Test
    void selectNoticeById_shouldReturnNullWhenNotFound() {
        when(noticeMapper.selectNoticeById(999L)).thenReturn(null);

        SysNotice result = noticeService.selectNoticeById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectNoticeList_shouldReturnList() {
        SysNotice notice = new SysNotice();
        notice.setNoticeId(1L);
        List<SysNotice> expected = Collections.singletonList(notice);
        when(noticeMapper.selectNoticeList(any(SysNotice.class))).thenReturn(expected);

        List<SysNotice> result = noticeService.selectNoticeList(new SysNotice());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNoticeId()).isEqualTo(1L);
    }

    @Test
    void selectNoticeList_shouldReturnEmptyList() {
        when(noticeMapper.selectNoticeList(any(SysNotice.class))).thenReturn(Collections.emptyList());

        List<SysNotice> result = noticeService.selectNoticeList(new SysNotice());

        assertThat(result).isEmpty();
    }

    @Test
    void insertNotice_shouldReturnAffectedRows() {
        SysNotice notice = new SysNotice();
        when(noticeMapper.insertNotice(any(SysNotice.class))).thenReturn(1);

        int result = noticeService.insertNotice(notice);

        assertThat(result).isEqualTo(1);
        verify(noticeMapper).insertNotice(notice);
    }

    @Test
    void updateNotice_shouldReturnAffectedRows() {
        SysNotice notice = new SysNotice();
        notice.setNoticeId(1L);
        when(noticeMapper.updateNotice(any(SysNotice.class))).thenReturn(1);

        int result = noticeService.updateNotice(notice);

        assertThat(result).isEqualTo(1);
        verify(noticeMapper).updateNotice(notice);
    }

    @Test
    void deleteNoticeById_shouldReturnAffectedRows() {
        when(noticeMapper.deleteNoticeById(1L)).thenReturn(1);

        int result = noticeService.deleteNoticeById(1L);

        assertThat(result).isEqualTo(1);
        verify(noticeMapper).deleteNoticeById(1L);
    }

    @Test
    void deleteNoticeById_shouldReturnZeroWhenNotFound() {
        when(noticeMapper.deleteNoticeById(999L)).thenReturn(0);

        int result = noticeService.deleteNoticeById(999L);

        assertThat(result).isEqualTo(0);
    }

    @Test
    void deleteNoticeByIds_shouldReturnAffectedRows() {
        Long[] ids = {1L, 2L, 3L};
        when(noticeMapper.deleteNoticeByIds(ids)).thenReturn(3);

        int result = noticeService.deleteNoticeByIds(ids);

        assertThat(result).isEqualTo(3);
        verify(noticeMapper).deleteNoticeByIds(ids);
    }
}
