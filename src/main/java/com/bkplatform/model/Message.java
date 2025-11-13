package com.bkplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity @Table(name="message")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Message {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;
    @ManyToOne(optional=false) @JoinColumn(name="conversation_id")
    private Conversation conversation;
    @ManyToOne(optional=false) @JoinColumn(name="sender_id")
    private User sender;
    @Column(nullable=false, columnDefinition="text")
    private String content;
    @Column(nullable=false)
    private Instant createdAt = Instant.now();
}
