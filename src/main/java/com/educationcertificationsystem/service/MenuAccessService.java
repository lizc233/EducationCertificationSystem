package com.educationcertificationsystem.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.educationcertificationsystem.auth.RoleConstants;
import com.educationcertificationsystem.model.entity.SysMenu;
import com.educationcertificationsystem.model.entity.SysRole;
import com.educationcertificationsystem.model.entity.SysRoleMenu;
import com.educationcertificationsystem.role.service.SysMenuService;
import com.educationcertificationsystem.role.service.SysRoleMenuService;
import com.educationcertificationsystem.role.service.SysRoleService;
import com.educationcertificationsystem.vo.auth.MenuNodeVO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MenuAccessService {

    private final SysMenuService menuService;

    private final SysRoleService roleService;

    private final SysRoleMenuService roleMenuService;

    public MenuAccessService(
        SysMenuService menuService,
        SysRoleService roleService,
        SysRoleMenuService roleMenuService
    ) {
        this.menuService = menuService;
        this.roleService = roleService;
        this.roleMenuService = roleMenuService;
    }

    public List<String> getAccessiblePaths(Set<String> roleCodes) {
        ensureSeedData();
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        return getMenusByRoleCodes(roleCodes).stream()
            .map(SysMenu::getRoutePath)
            .filter(StringUtils::hasText)
            .distinct()
            .sorted()
            .toList();
    }

    public List<String> getPermissionCodes(Set<String> roleCodes) {
        ensureSeedData();
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        return getMenusByRoleCodes(roleCodes).stream()
            .map(SysMenu::getPermissionCode)
            .filter(StringUtils::hasText)
            .distinct()
            .sorted()
            .toList();
    }

    public List<MenuNodeVO> getAccessibleMenuTree(Set<String> roleCodes) {
        ensureSeedData();
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        Set<Long> allowedIds = getMenusByRoleCodes(roleCodes).stream()
            .map(SysMenu::getId)
            .collect(Collectors.toSet());
        return buildTree(filterMenusForTree(allowedIds));
    }

    public List<MenuNodeVO> getFullMenuTree() {
        ensureSeedData();
        List<SysMenu> menus = menuService.list(new LambdaQueryWrapper<SysMenu>()
            .eq(SysMenu::getIsDeleted, 0)
            .eq(SysMenu::getStatus, 1)
            .orderByAsc(SysMenu::getSortNo)
            .orderByAsc(SysMenu::getId));
        return buildTree(menus);
    }

    public void replaceRoleMenus(Long roleId, Collection<Long> menuIds) {
        ensureSeedData();
        List<SysRoleMenu> existing = roleMenuService.list(new LambdaQueryWrapper<SysRoleMenu>()
            .eq(SysRoleMenu::getRoleId, roleId)
            .eq(SysRoleMenu::getIsDeleted, 0));
        LocalDateTime now = LocalDateTime.now();
        existing.forEach(item -> {
            item.setIsDeleted(1);
            item.setUpdatedAt(now);
        });
        if (!existing.isEmpty()) {
            roleMenuService.updateBatchById(existing);
        }

        List<Long> normalizedIds = normalizeMenuIds(menuIds);
        if (normalizedIds.isEmpty()) {
            return;
        }

        List<SysRoleMenu> relations = normalizedIds.stream()
            .map(menuId -> {
                SysRoleMenu relation = new SysRoleMenu();
                relation.setRoleId(roleId);
                relation.setMenuId(menuId);
                relation.setCreatedAt(now);
                relation.setUpdatedAt(now);
                relation.setIsDeleted(0);
                return relation;
            })
            .toList();
        roleMenuService.saveBatch(relations);
    }

    public List<Long> getAssignedMenuIds(Long roleId) {
        ensureSeedData();
        return roleMenuService.list(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
                .eq(SysRoleMenu::getIsDeleted, 0))
            .stream()
            .map(SysRoleMenu::getMenuId)
            .distinct()
            .toList();
    }

    private List<SysMenu> getMenusByRoleCodes(Set<String> roleCodes) {
        List<Long> roleIds = roleService.list(new LambdaQueryWrapper<SysRole>()
                .in(SysRole::getRoleCode, roleCodes)
                .eq(SysRole::getIsDeleted, 0)
                .eq(SysRole::getStatus, 1))
            .stream()
            .map(SysRole::getId)
            .toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        List<Long> menuIds = roleMenuService.list(new LambdaQueryWrapper<SysRoleMenu>()
                .in(SysRoleMenu::getRoleId, roleIds)
                .eq(SysRoleMenu::getIsDeleted, 0))
            .stream()
            .map(SysRoleMenu::getMenuId)
            .distinct()
            .toList();
        if (menuIds.isEmpty()) {
            return List.of();
        }
        return menuService.listByIds(menuIds).stream()
            .filter(menu -> menu.getIsDeleted() == 0 && menu.getStatus() == 1)
            .sorted(Comparator.comparing(SysMenu::getSortNo, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(SysMenu::getId))
            .toList();
    }

    private List<SysMenu> filterMenusForTree(Set<Long> allowedIds) {
        List<SysMenu> allMenus = menuService.list(new LambdaQueryWrapper<SysMenu>()
            .eq(SysMenu::getIsDeleted, 0)
            .eq(SysMenu::getStatus, 1)
            .orderByAsc(SysMenu::getSortNo)
            .orderByAsc(SysMenu::getId));
        Map<Long, SysMenu> byId = allMenus.stream().collect(Collectors.toMap(SysMenu::getId, item -> item));
        Set<Long> includedIds = new LinkedHashSet<>();
        for (Long id : allowedIds) {
            Long cursor = id;
            while (cursor != null && cursor > 0 && includedIds.add(cursor)) {
                cursor = byId.get(cursor) != null ? byId.get(cursor).getParentId() : null;
            }
        }
        return allMenus.stream().filter(menu -> includedIds.contains(menu.getId())).toList();
    }

    private List<MenuNodeVO> buildTree(List<SysMenu> menus) {
        Map<Long, MenuNodeVO> nodeMap = new LinkedHashMap<>();
        for (SysMenu menu : menus) {
            MenuNodeVO node = new MenuNodeVO();
            node.setId(menu.getId());
            node.setParentId(menu.getParentId());
            node.setMenuType(menu.getMenuType());
            node.setMenuName(menu.getMenuName());
            node.setRoutePath(menu.getRoutePath());
            node.setPermissionCode(menu.getPermissionCode());
            node.setVisible(menu.getVisible());
            node.setSortNo(menu.getSortNo());
            nodeMap.put(node.getId(), node);
        }

        List<MenuNodeVO> roots = new ArrayList<>();
        for (MenuNodeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() == 0 || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        }
        sortNodes(roots);
        return roots;
    }

    private void sortNodes(List<MenuNodeVO> nodes) {
        nodes.sort(Comparator.comparing(MenuNodeVO::getSortNo, Comparator.nullsLast(Integer::compareTo))
            .thenComparing(MenuNodeVO::getId));
        nodes.forEach(node -> sortNodes(node.getChildren()));
    }

    private List<Long> normalizeMenuIds(Collection<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) {
            return List.of();
        }
        Set<Long> existingIds = menuService.list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getIsDeleted, 0)
                .eq(SysMenu::getStatus, 1))
            .stream()
            .map(SysMenu::getId)
            .collect(Collectors.toSet());
        return menuIds.stream()
            .filter(existingIds::contains)
            .distinct()
            .toList();
    }

    private synchronized void ensureSeedData() {
        if (menuService.count(new LambdaQueryWrapper<SysMenu>().eq(SysMenu::getIsDeleted, 0)) == 0) {
            seedMenus();
        }
        syncMenus();
        seedRoleMenus(loadMenuByPath());
    }

    private void seedMenus() {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Long> parentIds = new HashMap<>();
        for (MenuSeed seed : MENU_SEEDS) {
            SysMenu menu = new SysMenu();
            menu.setParentId(seed.parentKey == null ? null : parentIds.get(seed.parentKey));
            menu.setMenuType(seed.menuType);
            menu.setMenuName(seed.menuName);
            menu.setRoutePath(seed.routePath);
            menu.setComponentPath(seed.componentPath);
            menu.setPermissionCode(seed.permissionCode);
            menu.setIcon(seed.icon);
            menu.setSortNo(seed.sortNo);
            menu.setVisible(seed.visible ? 1 : 0);
            menu.setStatus(1);
            menu.setCreatedAt(now);
            menu.setUpdatedAt(now);
            menu.setIsDeleted(0);
            menuService.save(menu);
            parentIds.put(seed.key, menu.getId());
        }
    }

    private void syncMenus() {
        List<SysMenu> menus = menuService.list(new LambdaQueryWrapper<SysMenu>()
            .eq(SysMenu::getIsDeleted, 0)
            .orderByAsc(SysMenu::getSortNo)
            .orderByAsc(SysMenu::getId));
        Map<String, SysMenu> menuByRoute = menus.stream()
            .filter(menu -> StringUtils.hasText(menu.getRoutePath()))
            .collect(Collectors.toMap(SysMenu::getRoutePath, item -> item, (left, right) -> left));
        Map<String, SysMenu> catalogByName = menus.stream()
            .filter(menu -> !StringUtils.hasText(menu.getRoutePath()))
            .collect(Collectors.toMap(SysMenu::getMenuName, item -> item, (left, right) -> left));
        Map<String, Long> resolvedSeedIds = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        for (MenuSeed seed : MENU_SEEDS) {
            SysMenu menu = StringUtils.hasText(seed.routePath)
                ? menuByRoute.get(seed.routePath)
                : catalogByName.get(seed.menuName);
            if (menu == null) {
                menu = new SysMenu();
                menu.setParentId(seed.parentKey == null ? null : resolvedSeedIds.get(seed.parentKey));
                menu.setMenuType(seed.menuType);
                menu.setMenuName(seed.menuName);
                menu.setRoutePath(seed.routePath);
                menu.setComponentPath(seed.componentPath);
                menu.setPermissionCode(seed.permissionCode);
                menu.setIcon(seed.icon);
                menu.setSortNo(seed.sortNo);
                menu.setVisible(seed.visible ? 1 : 0);
                menu.setStatus(1);
                menu.setCreatedAt(now);
                menu.setUpdatedAt(now);
                menu.setIsDeleted(0);
                menuService.save(menu);
            }
            resolvedSeedIds.put(seed.key, menu.getId());
        }
    }

    private void seedRoleMenus(Map<String, SysMenu> menuByPath) {
        LocalDateTime now = LocalDateTime.now();
        Map<String, Long> roleIdMap = roleService.list(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getIsDeleted, 0)
                .eq(SysRole::getStatus, 1))
            .stream()
            .collect(Collectors.toMap(SysRole::getRoleCode, SysRole::getId));
        Set<String> existingRelationKeys = roleMenuService.list(new LambdaQueryWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getIsDeleted, 0))
            .stream()
            .map(item -> item.getRoleId() + "-" + item.getMenuId())
            .collect(Collectors.toSet());
        List<SysRoleMenu> relations = new ArrayList<>();
        for (MenuSeed seed : MENU_SEEDS) {
            if (!StringUtils.hasText(seed.routePath)) {
                continue;
            }
            SysMenu menu = menuByPath.get(seed.routePath);
            if (menu == null) {
                continue;
            }
            for (String roleCode : seed.defaultRoles) {
                Long roleId = roleIdMap.get(roleCode);
                if (roleId == null) {
                    continue;
                }
                String relationKey = roleId + "-" + menu.getId();
                if (!existingRelationKeys.add(relationKey)) {
                    continue;
                }
                SysRoleMenu relation = new SysRoleMenu();
                relation.setRoleId(roleId);
                relation.setMenuId(menu.getId());
                relation.setCreatedAt(now);
                relation.setUpdatedAt(now);
                relation.setIsDeleted(0);
                relations.add(relation);
            }
        }
        if (!relations.isEmpty()) {
            roleMenuService.saveBatch(relations);
        }
    }

    private Map<String, SysMenu> loadMenuByPath() {
        return menuService.list(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getIsDeleted, 0))
            .stream()
            .filter(menu -> StringUtils.hasText(menu.getRoutePath()))
            .collect(Collectors.toMap(SysMenu::getRoutePath, item -> item, (left, right) -> left));
    }

    private static final class MenuSeed {

        private final String key;
        private final String parentKey;
        private final String menuType;
        private final String menuName;
        private final String routePath;
        private final String componentPath;
        private final String permissionCode;
        private final String icon;
        private final int sortNo;
        private final boolean visible;
        private final List<String> defaultRoles;

        private MenuSeed(
            String key,
            String parentKey,
            String menuType,
            String menuName,
            String routePath,
            int sortNo,
            boolean visible,
            String... defaultRoles
        ) {
            this.key = key;
            this.parentKey = parentKey;
            this.menuType = menuType;
            this.menuName = menuName;
            this.routePath = StringUtils.hasText(routePath) ? routePath : null;
            this.componentPath = null;
            this.permissionCode = StringUtils.hasText(routePath) ? "route:" + routePath : null;
            this.icon = null;
            this.sortNo = sortNo;
            this.visible = visible;
            this.defaultRoles = Arrays.asList(defaultRoles);
        }
    }

    private static final List<MenuSeed> MENU_SEEDS = List.of(
        new MenuSeed("work", null, "CATALOG", "Workbench", "", 10, true),
        new MenuSeed("dashboard", "work", "MENU", "Dashboard", "/dashboard", 11, true, RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER, RoleConstants.STUDENT),
        new MenuSeed("messages", "work", "MENU", "Messages", "/messages", 12, false, RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER, RoleConstants.STUDENT),
        new MenuSeed("profile", "work", "MENU", "Profile", "/profile", 13, false, RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER, RoleConstants.STUDENT),
        new MenuSeed("ai", "work", "MENU", "AI Assistant", "/ai", 14, true, RoleConstants.SUPER_ADMIN),

        new MenuSeed("core", null, "CATALOG", "Core Admin", "", 20, true),
        new MenuSeed("organization", "core", "MENU", "Organization", "/organization", 21, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("organization-bindings", "core", "MENU", "Organization Bindings", "/organization/bindings", 22, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("params", "core", "MENU", "System Params", "/params", 23, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("dicts", "core", "MENU", "Dictionaries", "/dicts", 24, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("logs", "core", "MENU", "Operation Logs", "/logs", 25, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("announcements", "core", "MENU", "Announcements", "/announcements", 26, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("users", "core", "MENU", "Users", "/users", 27, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("user-colleges", "core", "MENU", "Colleges", "/users/colleges", 28, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("user-majors", "core", "MENU", "Majors", "/users/majors", 29, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("user-grades", "core", "MENU", "Grades", "/users/grades", 30, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("user-classes", "core", "MENU", "Classes", "/users/classes", 31, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("user-teacher-bindings", "core", "MENU", "Teacher Bindings", "/users/teacher-bindings", 32, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("user-student-bindings", "core", "MENU", "Student Bindings", "/users/student-bindings", 33, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("roles", "core", "MENU", "Roles", "/roles", 34, true, RoleConstants.SUPER_ADMIN),

        new MenuSeed("program", null, "CATALOG", "Program", "", 30, true),
        new MenuSeed("program-list", "program", "MENU", "Program Versions", "/program", 31, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("program-goals", "program", "MENU", "Program Goals", "/program/goals", 32, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("program-courses", "program", "MENU", "Program Courses", "/program/courses", 33, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("courses", "program", "MENU", "Courses", "/courses", 34, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("course-goals", "program", "MENU", "Course Goals", "/courses/goals", 35, true, RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER),

        new MenuSeed("evaluation", null, "CATALOG", "Evaluation", "", 40, true),
        new MenuSeed("course-teaching", "evaluation", "MENU", "Course Teaching", "/courses/teaching", 41, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("teaching", "evaluation", "MENU", "Teaching Tasks", "/teaching", 42, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("course-resources", "evaluation", "MENU", "Course Resources", "/courses/resources", 43, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("evaluation-materials", "evaluation", "MENU", "Evidence Materials", "/evaluation/materials", 44, true, RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER),
        new MenuSeed("evaluation-scores", "evaluation", "MENU", "Evaluation Scores", "/evaluation/scores", 45, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("achievement-model", "evaluation", "MENU", "Achievement Model", "/achievement/model", 46, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("achievement-course", "evaluation", "MENU", "Course Achievement", "/achievement/course", 47, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("achievement-graduate", "evaluation", "MENU", "Graduate Achievement", "/achievement/graduate", 48, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("achievement-dashboard", "evaluation", "MENU", "Achievement Dashboard", "/achievement/dashboard", 49, true, RoleConstants.SUPER_ADMIN),

        new MenuSeed("selection", null, "CATALOG", "Selection And Scores", "", 50, true),
        new MenuSeed("course-selection-management", "selection", "MENU", "Course Selection Management", "/course-selection-management", 51, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("score-audit", "selection", "MENU", "Score Audit", "/score-audit", 52, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("score-input", "selection", "MENU", "Score Input", "/score-input", 53, true, RoleConstants.TEACHER),
        new MenuSeed("my-scores", "selection", "MENU", "My Scores", "/my-scores", 54, true, RoleConstants.STUDENT),

        new MenuSeed("survey-group", null, "CATALOG", "Survey And Improvement", "", 60, true),
        new MenuSeed("survey", "survey-group", "MENU", "Survey Designer", "/survey", 61, true, RoleConstants.SUPER_ADMIN),
        new MenuSeed("survey-fill", "survey-group", "MENU", "Survey Fill", "/survey/fill", 62, true, RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER, RoleConstants.STUDENT),
        new MenuSeed("improve", "survey-group", "MENU", "Improve Plan", "/improve", 63, true, RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER),
        new MenuSeed("report", "survey-group", "MENU", "Report", "/report", 64, true, RoleConstants.SUPER_ADMIN, RoleConstants.TEACHER),

        new MenuSeed("teacher-work", null, "CATALOG", "Teacher Workspace", "", 70, true),
        new MenuSeed("my-teaching", "teacher-work", "MENU", "My Teaching", "/my-teaching", 71, true, RoleConstants.TEACHER),
        new MenuSeed("my-courses", "teacher-work", "MENU", "My Courses", "/my-courses", 72, true, RoleConstants.TEACHER, RoleConstants.STUDENT),
        new MenuSeed("my-schedule", "teacher-work", "MENU", "My Schedule", "/my-schedule", 73, true, RoleConstants.TEACHER, RoleConstants.STUDENT),
        new MenuSeed("course-students", "teacher-work", "MENU", "Course Students", "/course-students", 74, true, RoleConstants.TEACHER),
        new MenuSeed("course-announcements", "teacher-work", "MENU", "Course Announcements", "/course-announcements", 75, true, RoleConstants.TEACHER),
        new MenuSeed("teaching-feedback", "teacher-work", "MENU", "Teaching Feedback", "/teaching-feedback", 76, true, RoleConstants.TEACHER),

        new MenuSeed("student-work", null, "CATALOG", "Student Workspace", "", 80, true),
        new MenuSeed("course-selection", "student-work", "MENU", "Course Selection", "/course-selection", 81, true, RoleConstants.STUDENT),
        new MenuSeed("academic-progress", "student-work", "MENU", "Academic Progress", "/academic-progress", 82, true, RoleConstants.STUDENT),
        new MenuSeed("my-achievement", "student-work", "MENU", "My Achievement", "/my-achievement", 83, true, RoleConstants.STUDENT),
        new MenuSeed("course-evaluate", "student-work", "MENU", "Course Evaluate", "/course-evaluate", 84, true, RoleConstants.STUDENT),
        new MenuSeed("course-announcements-view", "student-work", "MENU", "Course Announcement Feed", "/course-announcements-view", 85, false, RoleConstants.STUDENT)
    );
}
