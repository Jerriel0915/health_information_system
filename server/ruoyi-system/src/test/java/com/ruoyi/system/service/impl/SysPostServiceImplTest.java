package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.SysPost;
import com.ruoyi.system.mapper.SysPostMapper;
import com.ruoyi.system.mapper.SysUserPostMapper;

@ExtendWith(MockitoExtension.class)
class SysPostServiceImplTest {

    @Mock
    private SysPostMapper postMapper;

    @Mock
    private SysUserPostMapper userPostMapper;

    @InjectMocks
    private SysPostServiceImpl postService;

    @Test
    void selectPostList_shouldReturnList() {
        SysPost post = new SysPost();
        post.setPostId(1L);
        when(postMapper.selectPostList(any(SysPost.class)))
                .thenReturn(Collections.singletonList(post));

        List<SysPost> result = postService.selectPostList(new SysPost());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPostId()).isEqualTo(1L);
    }

    @Test
    void selectPostList_shouldReturnEmptyList() {
        when(postMapper.selectPostList(any(SysPost.class))).thenReturn(Collections.emptyList());

        List<SysPost> result = postService.selectPostList(new SysPost());

        assertThat(result).isEmpty();
    }

    @Test
    void selectPostAll_shouldReturnAll() {
        SysPost post = new SysPost();
        post.setPostId(1L);
        when(postMapper.selectPostAll()).thenReturn(Collections.singletonList(post));

        List<SysPost> result = postService.selectPostAll();

        assertThat(result).hasSize(1);
    }

    @Test
    void selectPostById_shouldReturnPost() {
        SysPost post = new SysPost();
        post.setPostId(1L);
        when(postMapper.selectPostById(1L)).thenReturn(post);

        SysPost result = postService.selectPostById(1L);

        assertThat(result.getPostId()).isEqualTo(1L);
    }

    @Test
    void selectPostById_shouldReturnNullWhenNotFound() {
        when(postMapper.selectPostById(999L)).thenReturn(null);

        SysPost result = postService.selectPostById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectPostListByUserId_shouldReturnPostIds() {
        List<Long> expected = List.of(1L, 2L);
        when(postMapper.selectPostListByUserId(100L)).thenReturn(expected);

        List<Long> result = postService.selectPostListByUserId(100L);

        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    void checkPostNameUnique_shouldReturnUniqueWhenNewPost() {
        SysPost post = new SysPost();
        when(postMapper.checkPostNameUnique(any())).thenReturn(null);

        boolean result = postService.checkPostNameUnique(post);

        assertThat(result).isTrue();
    }

    @Test
    void checkPostNameUnique_shouldReturnNotUniqueWhenNameExists() {
        SysPost post = new SysPost();
        post.setPostId(1L);
        post.setPostName("existing");
        SysPost existing = new SysPost();
        existing.setPostId(2L);
        when(postMapper.checkPostNameUnique("existing")).thenReturn(existing);

        boolean result = postService.checkPostNameUnique(post);

        assertThat(result).isFalse();
    }

    @Test
    void checkPostCodeUnique_shouldReturnUniqueWhenNewPost() {
        SysPost post = new SysPost();
        when(postMapper.checkPostCodeUnique(any())).thenReturn(null);

        boolean result = postService.checkPostCodeUnique(post);

        assertThat(result).isTrue();
    }

    @Test
    void checkPostCodeUnique_shouldReturnNotUniqueWhenCodeExists() {
        SysPost post = new SysPost();
        post.setPostId(1L);
        post.setPostCode("existing");
        SysPost existing = new SysPost();
        existing.setPostId(2L);
        when(postMapper.checkPostCodeUnique("existing")).thenReturn(existing);

        boolean result = postService.checkPostCodeUnique(post);

        assertThat(result).isFalse();
    }

    @Test
    void countUserPostById_shouldReturnCount() {
        when(userPostMapper.countUserPostById(1L)).thenReturn(3);

        int result = postService.countUserPostById(1L);

        assertThat(result).isEqualTo(3);
    }

    @Test
    void insertPost_shouldReturnAffectedRows() {
        SysPost post = new SysPost();
        when(postMapper.insertPost(any(SysPost.class))).thenReturn(1);

        int result = postService.insertPost(post);

        assertThat(result).isEqualTo(1);
        verify(postMapper).insertPost(post);
    }

    @Test
    void updatePost_shouldReturnAffectedRows() {
        SysPost post = new SysPost();
        post.setPostId(1L);
        when(postMapper.updatePost(any(SysPost.class))).thenReturn(1);

        int result = postService.updatePost(post);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void deletePostById_shouldReturnAffectedRows() {
        when(postMapper.deletePostById(1L)).thenReturn(1);

        int result = postService.deletePostById(1L);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void deletePostByIds_shouldDeleteWhenNoUsersAssigned() {
        Long[] ids = {1L, 2L};
        SysPost post = new SysPost();
        post.setPostId(1L);
        post.setPostName("test");
        when(postMapper.selectPostById(1L)).thenReturn(post);
        when(postMapper.selectPostById(2L)).thenReturn(post);
        when(userPostMapper.countUserPostById(1L)).thenReturn(0);
        when(userPostMapper.countUserPostById(2L)).thenReturn(0);
        when(postMapper.deletePostByIds(ids)).thenReturn(2);

        int result = postService.deletePostByIds(ids);

        assertThat(result).isEqualTo(2);
    }

    @Test
    void deletePostByIds_shouldThrowWhenUsersAssigned() {
        Long[] ids = {1L};
        SysPost post = new SysPost();
        post.setPostId(1L);
        post.setPostName("admin");
        when(postMapper.selectPostById(1L)).thenReturn(post);
        when(userPostMapper.countUserPostById(1L)).thenReturn(5);

        assertThatThrownBy(() -> postService.deletePostByIds(ids))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("已分配,不能删除");
    }
}
