package com.pitechitsolutions.essentialtrailerhire;

public class ChatMessage {
    private String branchName;
    private String senderId;
    private String receiverId;
    private String message;
    private Long timestamp;

    public ChatMessage() {
        // Needed for Firebase
    }

    public ChatMessage(String senderId, String message, Long timestamp) {
        this.senderId = senderId;
        this.receiverId = null; // or you can assign some default value other than null
        this.message = message;
        this.timestamp = timestamp;
    }


    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }
}
