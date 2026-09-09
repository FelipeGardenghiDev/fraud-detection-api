package com.felipedev.frauddetection.infrastructure.persistence.entity;

import com.felipedev.frauddetection.domain.model.BlacklistType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "blacklist", indexes = {
        @Index(name = "idx_blacklist_val_type", columnList = "type, value")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BlacklistType type;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false, length = 500)
    private String reason;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}