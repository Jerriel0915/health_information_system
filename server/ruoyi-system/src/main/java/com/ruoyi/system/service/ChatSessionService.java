package com.ruoyi.system.service;

import com.ruoyi.system.domain.ChatMessage;
import com.ruoyi.system.domain.ChatSession;
import com.ruoyi.system.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ChatSessionService {

    private final ChatMessageMapper chatMessageMapper;

    public ChatSessionService(ChatMessageMapper chatMessageMapper) {
        this.chatMessageMapper = chatMessageMapper;
    }

    public List<ChatSession> getSessionList(Long userId) {
        return chatMessageMapper.selectSessionList(userId);
    }

    public ChatSession getSessionById(String id) {
        return chatMessageMapper.selectSessionById(id);
    }

    public String createSession(Long userId) {
        String sessionId = "chat_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setUserId(userId);
        session.setTitle("新对话");
        chatMessageMapper.insertSession(session);
        return sessionId;
    }

    @Transactional
    public void deleteSession(String sessionId) {
        chatMessageMapper.deleteMessagesBySession(sessionId);
        chatMessageMapper.deleteSession(sessionId);
    }

    public void saveMessage(String sessionId, String role, String content) {
        ChatMessage msg = new ChatMessage();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        chatMessageMapper.insertMessage(msg);

        // 自动命名：取第一条用户消息的前15个字
        if ("user".equals(role)) {
            ChatSession session = chatMessageMapper.selectSessionById(sessionId);
            if (session != null && ("新对话".equals(session.getTitle()) || session.getTitle() == null || session.getTitle().isEmpty())) {
                String title = content.length() > 15 ? content.substring(0, 15) + "..." : content;
                session.setTitle(title);
                chatMessageMapper.updateSession(session);
            }
        }
    }

    public List<ChatMessage> getHistory(String sessionId) {
        return chatMessageMapper.selectRecentMessages(sessionId);
    }

    public void updateSessionTitle(String sessionId, String title) {
        ChatSession session = new ChatSession();
        session.setId(sessionId);
        session.setTitle(title);
        chatMessageMapper.updateSession(session);
    }
}
