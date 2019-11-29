package org.bwebserver.http;

import org.bwebserver.http.client.Policy;

import java.io.*;
import java.net.Socket;
import java.util.Dictionary;
import java.util.Hashtable;

public class HttpResponse {
    private BufferedOutputStream out;
    private Hashtable headers;

    private static Dictionary status = new Hashtable();
    static {
        status.put(100, "Continue");
        status.put(200, "OK");
        status.put(204, "No Content");
        status.put(206, "Partial Content");
        status.put(301, "Moved Permanently");
        status.put(302, "Found");
        status.put(304, "Not Modified");
        status.put(307, "Temporary Redirect");
        status.put(400, "Bad Request");
        status.put(401, "Unauthorized");
        status.put(403, "Forbidden");
        status.put(404, "Not Found");
        status.put(405, "Method Not Allowed");
        status.put(408, "Request Timeout");
        status.put(412, "Precondition Failed");
        status.put(413, "Request Entity Too Large");
        status.put(414, "Request-URI Too Large");
        status.put(416, "Requested Range Not Satisfiable");
        status.put(417, "Expectation Failed");
        status.put(500, "Internal Server Error");
        status.put(501, "Not Implemented");
        status.put(502, "Bad Gateway");
        status.put(503, "Service Unavailable");
        status.put(504, "Gateway Time-out");
    }

    private int responseCode;

    private HttpResponse()  {

    }

    public static HttpResponse create(OutputStream outStream){
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.out = new BufferedOutputStream(outStream);
        httpResponse.headers = new Hashtable();
        return httpResponse;
    }

    /**
     * Sends Service Unavailable status code to client when server is busy
     * @param socket - current connection
     * @throws IOException - when writing to the response stream
     */
    public static void sendBusy(Socket socket) throws IOException {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.out = new BufferedOutputStream(socket.getOutputStream());
        httpResponse.headers = new Hashtable();
        httpResponse.responseCode = 503;
        httpResponse.writeBody("");
        HttpContext.closeConnection(socket);
    }

    /**
     * Sends error status to client
     * @param socket - current connection
     * @throws IOException - when writing to the response stream
     */
    public static void sendError(Socket socket, int code) throws IOException {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.responseCode = code;
        httpResponse.out = new BufferedOutputStream(socket.getOutputStream());
        httpResponse.headers = new Hashtable();
        Policy.applyBeforeErrorPolicies(httpResponse);
        httpResponse.writeBody("");
        HttpContext.closeConnection(socket);
    }

    public void addHeader(String name, String value){
        headers.put(name, value);
    }

    public void writeBody(String text) throws IOException {
        writeBody(text.getBytes());
    }

    /**
     * Sends body to client
     * @param body - body to be sent to client
     */
    public void writeBody(byte[] body) throws IOException {
        addHeader("Content-Length", String.valueOf(body.length));
        sendHeaders();
        out.write(body);
        out.flush();
    }

    /**
     * Sends header information to client
     */
    private void sendHeaders() throws IOException {
        if (headers.get("Date") != null)
            headers.put("Date", String.format("%dns", System.nanoTime()));
        headers.put("Server", "BWebServerHTTP/1.0");
        out.write(String.format("HTTP/1.1 %d %s\r\n", responseCode, status.get(responseCode)).getBytes());
        for ( Object key : headers.keySet() ) {
            out.write(String.format("%s: %s\r\n", key, headers.get(key.toString())).getBytes());
        }
        out.write("\r\n".getBytes());
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int code) {
        responseCode = code;
    }
}