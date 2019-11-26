package org.bwebserver.http.protocol;

public enum HttpVersion {
    HTTP1_0,
    HTTP1_1,
    HTTP2_0,
    UNSUPPORTED;

    public static HttpVersion getHttpVersionByRequestName(String name){
        String labelName = name.replace("/", "").replace(".", "_");
        return HttpVersion.valueOf(labelName);
    }
}
