package org.abdrafikov.groupbuy.service;

import lombok.RequiredArgsConstructor;
import org.abdrafikov.groupbuy.dto.WorkspaceDto;
import org.abdrafikov.groupbuy.dto.WorkspaceForm;
import org.abdrafikov.groupbuy.dto.WorkspaceJoinForm;
import org.abdrafikov.groupbuy.exception.AccessDeniedException;
import org.abdrafikov.groupbuy.exception.ResourceNotFoundException;
import org.abdrafikov.groupbuy.model.User;
import org.abdrafikov.groupbuy.model.Workspace;
import org.abdrafikov.groupbuy.model.WorkspaceMember;
import org.abdrafikov.groupbuy.model.choices.GlobalRoleName;
import org.abdrafikov.groupbuy.model.choices.WorkspaceRole;
import org.abdrafikov.groupbuy.repository.UserRepository;
import org.abdrafikov.groupbuy.repository.WorkspaceMemberRepository;
import org.abdrafikov.groupbuy.repository.WorkspaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;

    @Transactional(readOnly = true)
    public List<WorkspaceDto> getAllForCurrentUser() {
        Long currentUserId = currentUserService.getCurrentUserId();
        return workspaceRepository.findDistinctByMembersUserIdOrderByCreatedAtDesc(currentUserId).stream()
                .map(workspace -> toDto(workspace, currentUserId))
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkspaceDto getById(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Workspace workspace = getAccessibleWorkspace(id, currentUserId);
        return toDto(workspace, currentUserId);
    }

    @Transactional
    public WorkspaceDto create(WorkspaceForm form) {
        User currentUser = getCurrentUserEntity();

        Workspace workspace = new Workspace();
        applyForm(workspace, form);
        workspace.setOwner(currentUser);
        workspace.setJoinToken(UUID.randomUUID().toString());
        Workspace savedWorkspace = workspaceRepository.save(workspace);

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(savedWorkspace);
        member.setUser(currentUser);
        member.setRole(WorkspaceRole.SPACE_ADMIN);
        workspaceMemberRepository.save(member);

        return toDto(savedWorkspace, currentUser.getId());
    }

    @Transactional
    public WorkspaceDto joinByToken(WorkspaceJoinForm form) {
        Long currentUserId = currentUserService.getCurrentUserId();
        User currentUser = getCurrentUserEntity();

        Workspace workspace = workspaceRepository.findByJoinToken(form.getToken().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Workspace с таким токеном не найден"));

        if (!workspace.isActive()) {
            throw new AccessDeniedException("Нельзя вступить в неактивный workspace");
        }

        if (workspace.getOwner().getId().equals(currentUserId)) {
            return toDto(workspace, currentUserId);
        }

        if (workspaceMemberRepository.findByWorkspaceIdAndUserId(workspace.getId(), currentUserId).isPresent()) {
            throw new AccessDeniedException("Вы уже состоите в этом workspace");
        }

        WorkspaceMember member = new WorkspaceMember();
        member.setWorkspace(workspace);
        member.setUser(currentUser);
        member.setRole(WorkspaceRole.SPACE_MEMBER);
        member.setInvitedBy(workspace.getOwner());
        workspaceMemberRepository.save(member);

        return toDto(workspace, currentUserId);
    }

    @Transactional
    public void leaveWorkspace(Long workspaceId) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Workspace workspace = getAccessibleWorkspace(workspaceId, currentUserId);

        if (workspace.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Владелец workspace не может покинуть его, не удалив пространство");
        }

        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("Вы не состоите в этом workspace"));

        workspaceMemberRepository.delete(membership);
    }

    @Transactional
    public WorkspaceDto update(Long id, WorkspaceForm form) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Workspace workspace = getAccessibleWorkspace(id, currentUserId);
        ensureWorkspaceAdmin(workspace.getId(), currentUserId);

        applyForm(workspace, form);
        return toDto(workspace, currentUserId);
    }

    @Transactional
    public void delete(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Workspace workspace = getAccessibleWorkspace(id, currentUserId);
        if (!workspace.getOwner().getId().equals(currentUserId) && !isGlobalAdmin()) {
            throw new AccessDeniedException("Удалить workspace может только владелец или глобальный администратор");
        }
        workspaceRepository.delete(workspace);
    }

    @Transactional(readOnly = true)
    public WorkspaceForm getForm(Long id) {
        Long currentUserId = currentUserService.getCurrentUserId();
        Workspace workspace = getAccessibleWorkspace(id, currentUserId);
        ensureWorkspaceAdmin(workspace.getId(), currentUserId);

        WorkspaceForm form = new WorkspaceForm();
        form.setName(workspace.getName());
        form.setDescription(workspace.getDescription());
        form.setActive(workspace.isActive());
        return form;
    }

    @Transactional(readOnly = true)
    public List<WorkspaceDto> getWorkspaceOptions() {
        return getAllForCurrentUser();
    }

    @Transactional(readOnly = true)
    public Workspace getAccessibleWorkspace(Long workspaceId, Long currentUserId) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace не найден"));
        if (isGlobalAdmin() || workspace.getOwner().getId().equals(currentUserId)) {
            return workspace;
        }
        workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("Нет доступа к workspace"));
        return workspace;
    }

    @Transactional(readOnly = true)
    public void ensureWorkspaceAdmin(Long workspaceId, Long currentUserId) {
        if (isGlobalAdmin()) {
            return;
        }
        WorkspaceMember membership = workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, currentUserId)
                .orElseThrow(() -> new AccessDeniedException("Нет доступа к workspace"));
        if (membership.getRole() != WorkspaceRole.SPACE_ADMIN) {
            throw new AccessDeniedException("Недостаточно прав для изменения workspace");
        }
    }

    @Transactional(readOnly = true)
    public boolean isWorkspaceAdmin(Long workspaceId, Long currentUserId) {
        if (isGlobalAdmin()) {
            return true;
        }
        return workspaceMemberRepository.findByWorkspaceIdAndUserId(workspaceId, currentUserId)
                .map(member -> member.getRole() == WorkspaceRole.SPACE_ADMIN)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean isGlobalAdmin() {
        return currentUserService.getCurrentUser().getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals(GlobalRoleName.ROLE_ADMIN.name()));
    }

    private User getCurrentUserEntity() {
        return userRepository.findById(currentUserService.getCurrentUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Текущий пользователь не найден"));
    }

    private WorkspaceDto toDto(Workspace workspace, Long currentUserId) {
        boolean isOwner = workspace.getOwner().getId().equals(currentUserId);
        return WorkspaceDto.builder()
                .id(workspace.getId())
                .name(workspace.getName())
                .description(workspace.getDescription())
                .ownerDisplayName(workspace.getOwner().getDisplayName())
                .joinToken(workspace.getJoinToken())
                .active(workspace.isActive())
                .currentUserOwner(isOwner)
                .currentUserAdmin(isOwner || isWorkspaceAdmin(workspace.getId(), currentUserId))
                .canLeave(!isOwner)
                .build();
    }

    private void applyForm(Workspace workspace, WorkspaceForm form) {
        workspace.setName(form.getName());
        workspace.setDescription(form.getDescription());
        workspace.setActive(form.isActive());
    }
}
