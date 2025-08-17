package com.todotalk.chatbot.model;

public class SubTask {
    private String description;
    private boolean completed;

    public SubTask() {
        this.completed = false;
    }

    public SubTask(String description) {
        this.description = description;
        this.completed = false;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
