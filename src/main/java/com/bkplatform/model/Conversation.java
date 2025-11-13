package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name="conversation", uniqueConstraints = @UniqueConstraint(columnNames = {"user1_id","user2_id"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Conversation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long conversationId;
    @ManyToOne(optional=false) @JoinColumn(name="user1_id")
    private User user1;
    @ManyToOne(optional=false) @JoinColumn(name="user2_id")
    private User user2;
    @Column(nullable=false)
    private Instant createdDate = Instant.now();
}
