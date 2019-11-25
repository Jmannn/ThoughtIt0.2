package com.example.thoughtit02;

import java.util.Date;

public class Thought {

    private Long dateInMS;
    private String thought;
    private Type type;
    private String uri;

    public Thought(Long time, String thought, Type type, String uri){
        this.dateInMS = time;
        this.thought = thought;
        this.type = type;
        this.uri = uri;
    }
    public Thought(Date time, String thought, Type type, String uri){
        this(time.getTime(),thought,type,uri);
    }

    public Long getDateInMS() {
        return dateInMS;
    }

    public Date getDate() {
        return new Date(dateInMS);
    }

    public String getThoughtText() {
        return thought;
    }

    public Type getType() {
        return type;
    }

    public String getUri() {
        return uri;
    }
    public void setThought(String thought) {
        this.thought = thought;
    }
}
