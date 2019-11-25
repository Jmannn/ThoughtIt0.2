package com.example.thoughtit02;

import java.util.Date;

public class Thought {

    private Long dateInMS;
    private String thought;
    private Type type;
    private String uri;

    private Thought(Long time, String thought, Type type, String uri){
        this.dateInMS = time;
        this.thought = thought;
        this.type = type;
        this.uri = uri;
    }
    Thought(Date time, String thought, Type type, String uri){
        this(time.getTime(),thought,type,uri);
    }
    Thought(Long time, String thought, String typeStr, String uri){
        this.dateInMS = time;
        this.thought = thought;
        this.type = stringToType(typeStr);
        this.uri = uri;
    }
    private Type stringToType(String typeStr){
        switch (typeStr){
            case "Text":
                return Type.TEXT;
            case "Audio":
                return Type.AUDIO;
            case "Picture":
                return Type.PICTURE;
        }
        return Type.TEXT;
    }

    Long getDateInMS() {
        return dateInMS;
    }

    public Date getDate() {
        return new Date(dateInMS);
    }

    String getThoughtText() {
        return thought;
    }

    Type getType() {
        return type;
    }

    String getUri() {
        return uri;
    }
    void setThought(String thought) {
        this.thought = thought;
    }
}
