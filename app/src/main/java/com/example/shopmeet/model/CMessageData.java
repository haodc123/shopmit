package com.example.shopmeet.model;

import java.io.Serializable;

public class CMessageData implements Serializable {
    public static final String CHAT_CONTENT_TYPE_MSG = "message";
    public static final String CHAT_CONTENT_TYPE_IMG = "image";
    public static final String CHAT_CONTENT_TYPE_CALL = "call";
    String id, message, createdAt, local_conv_id;
    CSender sender;
    String contentType;
    int isDisplay = 1;
 
    public CMessageData() {
    }
 
    public CMessageData(String id, String message, String createdAt, CSender sender, String contentType, int isDisplay, String local_conv_id) {
        this.id = id;
        this.message = message;
        this.createdAt = createdAt;
        this.sender = sender;
        this.contentType = contentType;
        this.isDisplay = isDisplay;
        this.local_conv_id = local_conv_id;
    }

    public String getLocal_conv_id() {
        return local_conv_id;
    }

    public void setLocal_conv_id(String local_conv_id) {
        this.local_conv_id = local_conv_id;
    }

    public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getId() {
        return id;
    }
 
    public void setId(String id) {
        this.id = id;
    }
 
    public String getMessage() {
        return message;
    }
 
    public void setMessage(String message) {
        this.message = message;
    }
 
    public String getCreatedAt() {
        return createdAt;
    }
 
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
 
    public CSender getSender() {
        return sender;
    }
 
    public void setSender(CSender s) {
        this.sender = s;
    }

    public int getIsDisplay() {
        return isDisplay;
    }

    public void setIsDisplay(int isDisplay) {
        this.isDisplay = isDisplay;
    }
}