package com.ruoyi.system.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.TreeSelect;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysRoleMapper;

@ExtendWith(MockitoExtension.class)
class SysDeptServiceImplTest {

    @Mock
    private SysDeptMapper deptMapper;

    @Mock
    private SysRoleMapper roleMapper;

    @InjectMocks
    private SysDeptServiceImpl deptService;

    @Test
    void selectDeptById_shouldReturnDept() {
        SysDept dept = new SysDept();
        dept.setDeptId(1L);
        when(deptMapper.selectDeptById(1L)).thenReturn(dept);

        SysDept result = deptService.selectDeptById(1L);

        assertThat(result.getDeptId()).isEqualTo(1L);
    }

    @Test
    void selectDeptById_shouldReturnNullWhenNotFound() {
        when(deptMapper.selectDeptById(999L)).thenReturn(null);

        SysDept result = deptService.selectDeptById(999L);

        assertThat(result).isNull();
    }

    @Test
    void selectNormalChildrenDeptById_shouldReturnCount() {
        when(deptMapper.selectNormalChildrenDeptById(1L)).thenReturn(5);

        int result = deptService.selectNormalChildrenDeptById(1L);

        assertThat(result).isEqualTo(5);
    }

    @Test
    void hasChildByDeptId_shouldReturnTrueWhenChildrenExist() {
        when(deptMapper.hasChildByDeptId(1L)).thenReturn(3);

        boolean result = deptService.hasChildByDeptId(1L);

        assertThat(result).isTrue();
    }

    @Test
    void hasChildByDeptId_shouldReturnFalseWhenNoChildren() {
        when(deptMapper.hasChildByDeptId(1L)).thenReturn(0);

        boolean result = deptService.hasChildByDeptId(1L);

        assertThat(result).isFalse();
    }

    @Test
    void checkDeptExistUser_shouldReturnTrueWhenUsersExist() {
        when(deptMapper.checkDeptExistUser(1L)).thenReturn(2);

        boolean result = deptService.checkDeptExistUser(1L);

        assertThat(result).isTrue();
    }

    @Test
    void checkDeptExistUser_shouldReturnFalseWhenNoUsers() {
        when(deptMapper.checkDeptExistUser(1L)).thenReturn(0);

        boolean result = deptService.checkDeptExistUser(1L);

        assertThat(result).isFalse();
    }

    @Test
    void checkDeptNameUnique_shouldReturnUniqueWhenNew() {
        SysDept dept = new SysDept();
        when(deptMapper.checkDeptNameUnique(any(), any())).thenReturn(null);

        boolean result = deptService.checkDeptNameUnique(dept);

        assertThat(result).isTrue();
    }

    @Test
    void checkDeptNameUnique_shouldReturnNotUniqueWhenNameExists() {
        SysDept dept = new SysDept();
        dept.setDeptId(1L);
        dept.setDeptName("existing");
        SysDept existing = new SysDept();
        existing.setDeptId(2L);
        when(deptMapper.checkDeptNameUnique("existing", null)).thenReturn(existing);

        boolean result = deptService.checkDeptNameUnique(dept);

        assertThat(result).isFalse();
    }

    @Test
    void insertDept_shouldThrowWhenParentDisabled() {
        SysDept dept = new SysDept();
        dept.setParentId(1L);
        SysDept parent = new SysDept();
        parent.setStatus("1");
        when(deptMapper.selectDeptById(1L)).thenReturn(parent);

        assertThatThrownBy(() -> deptService.insertDept(dept))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("部门停用");
    }

    @Test
    void insertDept_shouldInsertWhenParentNormal() {
        SysDept dept = new SysDept();
        dept.setParentId(1L);
        SysDept parent = new SysDept();
        parent.setStatus(UserConstants.DEPT_NORMAL);
        parent.setAncestors("0,100");
        when(deptMapper.selectDeptById(1L)).thenReturn(parent);
        when(deptMapper.insertDept(any(SysDept.class))).thenReturn(1);

        int result = deptService.insertDept(dept);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void deleteDeptById_shouldReturnAffectedRows() {
        when(deptMapper.deleteDeptById(1L)).thenReturn(1);

        int result = deptService.deleteDeptById(1L);

        assertThat(result).isEqualTo(1);
    }

    @Test
    void selectDeptListByRoleId_shouldReturnDeptIds() {
        SysRole role = new SysRole();
        role.setDeptCheckStrictly(false);
        when(roleMapper.selectRoleById(1L)).thenReturn(role);
        when(deptMapper.selectDeptListByRoleId(1L, false)).thenReturn(List.of(1L, 2L));

        List<Long> result = deptService.selectDeptListByRoleId(1L);

        assertThat(result).containsExactly(1L, 2L);
    }

    @Test
    void buildDeptTree_shouldBuildTreeStructure() {
        SysDept parent = buildDept(1L, 0L, "parent");
        SysDept child = buildDept(2L, 1L, "child");
        List<SysDept> depts = List.of(parent, child);

        List<SysDept> result = deptService.buildDeptTree(depts);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChildren()).hasSize(1);
        assertThat(result.get(0).getChildren().get(0).getDeptId()).isEqualTo(2L);
    }

    @Test
    void buildDeptTree_shouldReturnOriginalListWhenEmptyTree() {
        List<SysDept> depts = List.of(buildDept(1L, 1L, "orphan"));

        List<SysDept> result = deptService.buildDeptTree(depts);

        assertThat(result).hasSize(1);
    }

    @Test
    void buildDeptTreeSelect_shouldConvertToTreeSelect() {
        SysDept dept = buildDept(1L, 0L, "test");
        List<SysDept> depts = List.of(dept);

        List<TreeSelect> result = deptService.buildDeptTreeSelect(depts);

        assertThat(result).hasSize(1);
    }

    @Test
    void buildDeptTreeSelect_shouldReturnEmptyForEmptyInput() {
        List<TreeSelect> result = deptService.buildDeptTreeSelect(Collections.emptyList());

        assertThat(result).isEmpty();
    }

    private SysDept buildDept(Long id, Long parentId, String name) {
        SysDept dept = new SysDept();
        dept.setDeptId(id);
        dept.setParentId(parentId);
        dept.setDeptName(name);
        dept.setChildren(new ArrayList<>());
        return dept;
    }
}
