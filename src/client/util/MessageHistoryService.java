/**
 * 消息历史存储服务
 * 负责在客户端本地保存和加载聊天记录
 */
package client.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import common.model.entity.Message;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.*;
import java.util.*;

public class MessageHistoryService {
    private static final String HISTORY_DIR = "chat_history";
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .create();
    
    /**
     * 保存消息列表到本地文件
     * @param userId 当前用户ID
     * @param chatKey 聊天对象键（-1为群聊，其他为用户ID）
     * @param messages 消息列表
     */
    public static void saveMessages(long userId, Long chatKey, List<Message> messages) {
        try {
            // 创建用户专属目录
            Path userDir = Paths.get(HISTORY_DIR, String.valueOf(userId));
            Files.createDirectories(userDir);
            
            // 确定文件名
            String filename = chatKey == -1L ? "group_chat.json" : "private_" + chatKey + ".json";
            Path filePath = userDir.resolve(filename);
            
            // 序列化并保存
            String json = gson.toJson(messages);
            Files.write(filePath, json.getBytes("UTF-8"), 
                StandardOpenOption.CREATE, 
                StandardOpenOption.TRUNCATE_EXISTING);
            
            System.out.println("✓ 已保存聊天记录: " + filename + " (" + messages.size() + " 条消息)");
            
        } catch (IOException e) {
            System.err.println("✗ 保存消息失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从本地文件加载消息列表
     * @param userId 当前用户ID
     * @param chatKey 聊天对象键（-1为群聊，其他为用户ID）
     * @return 消息列表，如果文件不存在则返回空列表
     */
    public static List<Message> loadMessages(long userId, Long chatKey) {
        try {
            Path userDir = Paths.get(HISTORY_DIR, String.valueOf(userId));
            String filename = chatKey == -1L ? "group_chat.json" : "private_" + chatKey + ".json";
            Path filePath = userDir.resolve(filename);
            
            if (!Files.exists(filePath)) {
                return new ArrayList<>();
            }
            
            String json = new String(Files.readAllBytes(filePath), "UTF-8");
            Type listType = new TypeToken<ArrayList<Message>>(){}.getType();
            List<Message> messages = gson.fromJson(json, listType);
            
            System.out.println("✓ 已加载聊天记录: " + filename + " (" + messages.size() + " 条消息)");
            return messages != null ? messages : new ArrayList<>();
            
        } catch (IOException e) {
            System.err.println("✗ 加载消息失败: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    /**
     * 加载用户的所有聊天记录
     * @param userId 当前用户ID
     * @return Map<聊天键, 消息列表>
     */
    public static Map<Long, List<Message>> loadAllMessages(long userId) {
        Map<Long, List<Message>> allMessages = new HashMap<>();
        
        try {
            Path userDir = Paths.get(HISTORY_DIR, String.valueOf(userId));
            if (!Files.exists(userDir)) {
                System.out.println("ℹ 用户 " + userId + " 暂无历史记录");
                return allMessages;
            }
            
            // 遍历用户目录下的所有聊天记录文件
            Files.list(userDir)
                .filter(file -> file.toString().endsWith(".json"))
                .forEach(file -> {
                    String filename = file.getFileName().toString();
                    
                    if (filename.equals("group_chat.json")) {
                        // 加载群聊记录
                        List<Message> messages = loadMessages(userId, -1L);
                        if (!messages.isEmpty()) {
                            allMessages.put(-1L, messages);
                        }
                    } else if (filename.startsWith("private_") && filename.endsWith(".json")) {
                        // 加载私聊记录
                        String idStr = filename.substring(8, filename.length() - 5);
                        try {
                            long chatKey = Long.parseLong(idStr);
                            List<Message> messages = loadMessages(userId, chatKey);
                            if (!messages.isEmpty()) {
                                allMessages.put(chatKey, messages);
                            }
                        } catch (NumberFormatException e) {
                            System.err.println("✗ 无效的文件名: " + filename);
                        }
                    }
                });
            
            System.out.println("✓ 已加载所有聊天记录，共 " + allMessages.size() + " 个对话");
            
        } catch (IOException e) {
            System.err.println("✗ 加载所有消息失败: " + e.getMessage());
        }
        
        return allMessages;
    }
    
    /**
     * 清空指定聊天的历史记录
     * @param userId 当前用户ID
     * @param chatKey 聊天对象键
     */
    public static void clearMessages(long userId, Long chatKey) {
        try {
            Path userDir = Paths.get(HISTORY_DIR, String.valueOf(userId));
            String filename = chatKey == -1L ? "group_chat.json" : "private_" + chatKey + ".json";
            Path filePath = userDir.resolve(filename);
            
            if (Files.deleteIfExists(filePath)) {
                System.out.println("✓ 已清空聊天记录: " + filename);
            }
        } catch (IOException e) {
            System.err.println("✗ 清空消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 清空用户的所有聊天记录
     * @param userId 当前用户ID
     */
    public static void clearAllMessages(long userId) {
        try {
            Path userDir = Paths.get(HISTORY_DIR, String.valueOf(userId));
            if (Files.exists(userDir)) {
                Files.walk(userDir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            System.err.println("✗ 删除文件失败: " + path);
                        }
                    });
                System.out.println("✓ 已清空所有聊天记录");
            }
        } catch (IOException e) {
            System.err.println("✗ 清空所有消息失败: " + e.getMessage());
        }
    }
    
    /**
     * 获取用户的聊天历史统计信息
     * @param userId 当前用户ID
     * @return 统计信息字符串
     */
    public static String getHistoryStats(long userId) {
        try {
            Path userDir = Paths.get(HISTORY_DIR, String.valueOf(userId));
            if (!Files.exists(userDir)) {
                return "暂无聊天记录";
            }
            
            long fileCount = Files.list(userDir)
                .filter(file -> file.toString().endsWith(".json"))
                .count();
            
            long totalSize = Files.walk(userDir)
                .filter(Files::isRegularFile)
                .mapToLong(path -> {
                    try {
                        return Files.size(path);
                    } catch (IOException e) {
                        return 0;
                    }
                })
                .sum();
            
            return String.format("共 %d 个对话，占用 %.2f KB", fileCount, totalSize / 1024.0);
            
        } catch (IOException e) {
            return "统计失败";
        }
    }
}


