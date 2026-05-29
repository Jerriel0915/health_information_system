package com.ruoyi.system.mapper;

import com.ruoyi.system.domain.ChatMessage;
import com.ruoyi.system.domain.ChatSession;
import java.util.List;

public interface ChatMessageMapper {
    int insertMessage(ChatMessage msg);
    List<ChatMessage> selectRecentMessages(String sessionId);
    List<ChatSession> selectSessionList(Long userId);
    ChatSession selectSessionById(String id);
    int insertSession(ChatSession session);
    int updateSession(ChatSession session);
    int deleteSession(String sessionId);
    int deleteMessagesBySession(String sessionId);
}
