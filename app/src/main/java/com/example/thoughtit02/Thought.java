package com.example.thoughtit02;

import java.io.InvalidObjectException;
import java.util.Date;
/* This class represents a row in a database table
 * which is described as a thought.
 * @author Johnny Mann
 */
public class Thought {
    /* Represents a date as number of milliseconds since 1970.*/
    private Long dateInMS;
    /* The thought as a string. */
    private String thought;
    /* The type of the thought, whether it contains images or audio also. */
    private Type type;
    /* The location of the file in android storage. */
    private String uri;
    /* A constructor which accepts thought as date in milliseconds. */
    Thought(Long time, String thought, Type type, String uri) {
        this.dateInMS = time;
        this.thought = thought;
        this.type = type;
        this.uri = uri;
        if (time < 0 || time > Utilities.getToday()||
        Utilities.checkBlank(thought)){
            throw new IllegalArgumentException("Time must be ms post 1970");
        } else if (Type.TEXT != type && Utilities.checkBlank(uri)){
            throw new IllegalArgumentException("Uri must be provided for non text type.");
        }
    }
    /* A constructor which accepts date as a Date object. */
    Thought(Date time, String thought, Type type, String uri){
        this(time.getTime(),thought,type,uri);
    }
    /* A constructor which accepts date in ms and type as a string representation. */
    Thought(Long time, String thought, String typeStr, String uri) {
        this.dateInMS = time;
        this.thought = thought;
        this.type = stringToType(typeStr);
        this.uri = uri;
        if (time < 0 || time > Utilities.getToday() ||
        Utilities.checkBlank(new String[]{thought, typeStr})){
            throw new IllegalArgumentException("Time must be ms post 1970");
        } else if (Type.TEXT != type && Utilities.checkBlank(uri)){
            throw new IllegalArgumentException("Uri must be provided for non text type.");
        }
    }

    /* Converts a string representation of a type
     * to the correct enum.
     * @param typeStr - The string representation of the date.
     * @return The correct enum.
     */
    private Type stringToType(String typeStr){
        switch (typeStr){
            case "Text":
                return Type.TEXT;
            case "Audio":
                return Type.AUDIO;
            case "Picture":
                return Type.PICTURE;
            default:
                throw new IllegalArgumentException("The text type does not match an enumerated type.");
        }
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
