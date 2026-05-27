-- 菜单 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('???????', '3', '1', 'service', 'system/service/index', 1, 0, 'C', '0', '0', 'system:service:list', '#', 'admin', sysdate(), '', null, '???????菜单');

-- 按钮父菜单ID
SELECT @parentId := LAST_INSERT_ID();

-- 按钮 SQL
insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('???????查询', @parentId, '1',  '#', '', 1, 0, 'F', '0', '0', 'system:service:query',        '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('???????新增', @parentId, '2',  '#', '', 1, 0, 'F', '0', '0', 'system:service:add',          '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('???????修改', @parentId, '3',  '#', '', 1, 0, 'F', '0', '0', 'system:service:edit',         '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('???????删除', @parentId, '4',  '#', '', 1, 0, 'F', '0', '0', 'system:service:remove',       '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values('???????导出', @parentId, '5',  '#', '', 1, 0, 'F', '0', '0', 'system:service:export',       '#', 'admin', sysdate(), '', null, '');