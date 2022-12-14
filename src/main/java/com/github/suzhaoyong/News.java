package com.github.suzhaoyong;

import java.math.BigInteger;
import java.time.Instant;

public class News {
    private BigInteger id;
    private String title;
    private String content;
    private String url;
    private Instant createdAt;
    private Instant modifiedAt;

    public News() {
    }

    public News(String title, String content, String url) {
        this.title = title;
        this.content = content;
        this.url = url;
    }

    public News(News old) {
        this.id = old.id;
        this.title = old.title;
        this.content = old.content;
        this.url = old.url;
        this.createdAt = old.createdAt;
        this.modifiedAt = old.modifiedAt;
    }

    public BigInteger getId() {
        return id;
    }

    public void setId(BigInteger id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getModifiedAt() {
        return modifiedAt;
    }

    public void setModifiedAt(Instant modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
