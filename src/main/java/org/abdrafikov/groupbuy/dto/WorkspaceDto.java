package org.abdrafikov.groupbuy.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WorkspaceDto {

    private final Long id;
    private final String name;
    private final String description;
    private final String ownerDisplayName;
    private final String joinToken;
    private final boolean active;
    private final boolean currentUserAdmin;
    private final boolean currentUserOwner;
}
