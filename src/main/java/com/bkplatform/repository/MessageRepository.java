package com.bkplatform.repository;
import com.bkplatform.model.Message;
import com.bkplatform.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderByCreatedAtAsc(Conversation c);
}
