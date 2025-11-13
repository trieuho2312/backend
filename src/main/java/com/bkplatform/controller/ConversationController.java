package com.bkplatform.controller;

import com.bkplatform.model.Conversation;
import com.bkplatform.model.Message;
import com.bkplatform.model.User;
import com.bkplatform.repository.ConversationRepository;
import com.bkplatform.repository.MessageRepository;
import com.bkplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController @RequestMapping("/api/conversations")
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

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<Message>> messages(@PathVariable Long id) {
        Conversation c = conversationRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(messageRepository.findByConversationOrderByCreatedAtAsc(c));
    }

    @PostMapping("/{otherUserId}/messages")
    public ResponseEntity<Message> send(@AuthenticationPrincipal UserDetails principal,
                                        @PathVariable Long otherUserId,
                                        @RequestBody String content) {
        User me = userRepository.findByUsername(principal.getUsername()).orElseThrow();
        User other = userRepository.findById(otherUserId).orElseThrow();
        Conversation c = conversationRepository.findByUser1AndUser2(me, other)
                .orElse(conversationRepository.findByUser1AndUser2(other, me).orElseGet(() -> {
                    Conversation nc = Conversation.builder().user1(me).user2(other).build();
                    return conversationRepository.save(nc);
                }));
        Message m = Message.builder().conversation(c).sender(me).content(content).build();
        return ResponseEntity.ok(messageRepository.save(m));
    }
}
