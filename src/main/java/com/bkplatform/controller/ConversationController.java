package com.bkplatform.controller;

import com.bkplatform.model.Conversation;
import com.bkplatform.model.Message;
import com.bkplatform.model.User;
import com.bkplatform.repository.ConversationRepository;
import com.bkplatform.repository.MessageRepository;
import com.bkplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversations")
@RequiredArgsConstructor
public class ConversationController {
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Conversation>> mine(@AuthenticationPrincipal UserDetails principal) {
        User me = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(conversationRepository.findByUser1OrUser2(me, me));
    }

    /**
     * ✅ FIX: Thêm authorization check - chỉ user trong conversation mới xem được messages
     */
    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> messages(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long id) {

        User me = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        Conversation c = conversationRepository.findById(id).orElseThrow();

        // ✅ Kiểm tra user có quyền xem conversation này không
        if (!c.getUser1().equals(me) && !c.getUser2().equals(me)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(messageRepository.findByConversationOrderByCreatedAtAsc(c));
    }

    /**
     * ✅ FIX: Validate content không rỗng
     */
    @PostMapping("/{otherUserId}/messages")
    public ResponseEntity<?> send(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable Long otherUserId,
            @RequestBody String content) {

        // ✅ Validate content
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Message content cannot be empty");
        }

        User me = userRepository.findByUsername(principal.getUsername()).orElseThrow();

        // ✅ Validate không thể gửi tin nhắn cho chính mình
        if (me.getUserId().equals(otherUserId)) {
            return ResponseEntity.badRequest().body("Cannot send message to yourself");
        }

        User other = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // ✅ Tìm hoặc tạo conversation
        Conversation c = conversationRepository.findByUser1AndUser2(me, other)
                .orElse(conversationRepository.findByUser1AndUser2(other, me)
                        .orElseGet(() -> {
                            Conversation nc = Conversation.builder()
                                    .user1(me)
                                    .user2(other)
                                    .build();
                            return conversationRepository.save(nc);
                        }));

        Message m = Message.builder()
                .conversation(c)
                .sender(me)
                .content(content.trim())
                .build();

        return ResponseEntity.ok(messageRepository.save(m));
    }
}