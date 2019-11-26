package org.bwebserver.content;

public class ContentInfo {
    private final String name;
    private final byte[] contentBytes;
    private boolean hasValue = false;

    public ContentInfo(String name, byte[] contentBytes){
       this.name = name;
       this.contentBytes = contentBytes;
    }

    public static ContentInfo createEmpty(){
        return new ContentInfo(null, null);
    }

    public boolean getHasValue(){
        return hasValue;
    }
    public void setHasValue(boolean value){
        hasValue = value;
    }
    public String getName(){
        return name;
    }
    public byte[] getContentBytes(){
        return contentBytes;
    }
}
