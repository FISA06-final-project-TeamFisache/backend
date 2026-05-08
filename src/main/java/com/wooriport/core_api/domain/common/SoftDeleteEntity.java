package com.wooriport.core_api.domain.common;


import jakarta.persistence.Column;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public abstract class SoftDeleteEntity extends BaseEntity {

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public void delete() {
        this.deletedAt = LocalDateTime.now();
    }
}
