package com.bkplatform.repository;
import com.bkplatform.model.Conversation;
import com.bkplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    Optional<Conversation> findByUser1AndUser2(User u1, User u2);
    List<Conversation> findByUser1OrUser2(User u1, User u2);
}
