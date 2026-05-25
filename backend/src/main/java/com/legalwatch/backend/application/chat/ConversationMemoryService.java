package com.legalwatch.backend.application.chat;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ConversationMemoryService {

    private static final int MAX_TURNS = 5;
    private static final int MAX_ANSWER_CONTEXT_LENGTH = 500;

    private final Map<String, Deque<ConversationTurn>> conversations = new ConcurrentHashMap<>();

    public String resolveConversationId(String requestedId) {
        if (requestedId == null || requestedId.isBlank()) {
            return "conv_" + UUID.randomUUID();
        }
        String conversationId = requestedId.trim();
        if (conversationId.length() > 100 || !conversationId.matches("[A-Za-z0-9_-]+")) {
            throw new IllegalArgumentException("conversationId is invalid");
        }
        return conversationId;
    }

    public String contextFor(String conversationId) {
        Deque<ConversationTurn> turns = conversations.get(conversationId);
        if (turns == null || turns.isEmpty()) {
            return "";
        }
        StringBuilder context = new StringBuilder("此前同一会话内容：\n");
        synchronized (turns) {
            for (ConversationTurn turn : turns) {
                context.append("问题：").append(turn.question()).append("\n")
                        .append("回答摘要：").append(turn.answerSummary()).append("\n");
            }
        }
        return context.toString();
    }

    public void remember(String conversationId, String question, String answer) {
        Deque<ConversationTurn> turns = conversations.computeIfAbsent(conversationId, ignored -> new ArrayDeque<>());
        String answerSummary = answer.length() <= MAX_ANSWER_CONTEXT_LENGTH
                ? answer
                : answer.substring(0, MAX_ANSWER_CONTEXT_LENGTH);
        synchronized (turns) {
            turns.addLast(new ConversationTurn(question, answerSummary));
            while (turns.size() > MAX_TURNS) {
                turns.removeFirst();
            }
        }
    }

    record ConversationTurn(String question, String answerSummary) {
    }
}
