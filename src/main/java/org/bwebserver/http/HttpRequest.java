package org.bwebserver.http;

import org.bwebserver.BWebServer;
import org.bwebserver.http.protocol.HttpMethod;
import org.bwebserver.http.protocol.HttpVersion;
import org.bwebserver.logging.LoggerProvider;
import org.bwebserver.logging.LoggerService;

import java.io.*;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;

public class HttpRequest{

    private HttpMethod method;
    private String path;
    private HttpVersion version;
    private HashMap<String, String> headers;
    private boolean isSupported = true;
    private byte[] body;

    private LoggerService logger = BWebServer.getLoggerService();

    private HttpRequest() {

    }

    public static HttpRequest create(InputStream in) throws IOException {
        HttpRequest req = new HttpRequest();
        req.readRequest(in);
        return req;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public String getPath() {
        return path;
    }

    public HttpVersion getVersion() {
        return  version;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public byte[] getBody() {
        return body;
    }

    public boolean isSupported(){
        return isSupported;
    }

    /**
     * Basic method to read the current request
     * @param in - InputStream from the socket
     */
    private void readRequest(InputStream in) throws IOException {
        headers = new HashMap<>();

        String line = readLine(in);
        // read the request main info (method, protocol, version)
        if (!line.isEmpty()) {
            // first line
            String[] parts = line.split(" ");
            String reqMethod = parts[0];
            path = parts[1];
            String reqVersion = parts[2];
            try {
                method = HttpMethod.valueOf(reqMethod);
            }catch (IllegalArgumentException ex){
                method = HttpMethod.UNSUPPORTED;
            }
            try {
                version = HttpVersion.getHttpVersionByRequestName(reqVersion);
            }catch (IllegalArgumentException ex){
                version = HttpVersion.UNSUPPORTED;
            }

        }

        // read header information
        while (!(line = readLine(in)).isEmpty()) {
            String headerName = line.substring(0, line.indexOf(":")).toLowerCase().trim();
            String headerValue = line.substring(line.indexOf(":") + 1).trim();
            headers.put(headerName, headerValue);
        }

        // if response body is sent, read it
        if (headers.get("content-length") != null){
            int bodyLength = Integer.parseInt(headers.get("content-length"));
            body = new byte[bodyLength];
            BufferedInputStream bin = new BufferedInputStream(in);
            bin.read(body);
        }

        canServerSupportRequest();
    }

    /**
     * Method taken from: https://www.freeutils.net/source/jlhttp/
     * Reads the ISO-8859-1 encoded string starting at the current stream
     * position and ending at the first occurrence of the LF character.
     *
     * @param in the stream from which the line is read
     * @return the read string, excluding the terminating LF character
     *         and (if exists) the CR character immediately preceding it
     * @throws EOFException if the stream end is reached before an LF character is found
     * @throws IOException if an IO error occurs, or the line is longer than 8192 bytes
     * @see #readToken(InputStream, int, String, int)
     */
    private String readLine(InputStream in) throws IOException {
        return readToken(in, '\n', "ISO8859_1", 8192*2*2*2*2*2*2*2*2);
    }

    /**
     * Method taken from https://www.freeutils.net/source/jlhttp/
     * Reads the token starting at the current stream position and ending at
     * the first occurrence of the given delimiter byte, in the given encoding.
     * If LF is specified as the delimiter, a CRLF pair is also treated as one.
     *
     * @param in the stream from which the token is read
     * @param delim the byte value which marks the end of the token,
     *        or -1 if the token ends at the end of the stream
     * @param enc a character-encoding name
     * @param maxLength the maximum length (in bytes) to read
     * @return the read token, excluding the delimiter
     * @throws UnsupportedEncodingException if the encoding is not supported
     * @throws EOFException if the stream end is reached before a delimiter is found
     * @throws IOException if an IO error occurs, or the maximum length
     *         is reached before the token end is reached
     */
    private String readToken(InputStream in, int delim,
                             String enc, int maxLength) throws IOException {
        // note: we avoid using a ByteArrayOutputStream here because it
        // suffers the overhead of synchronization for each byte written
        int b;
        int len = 0; // buffer length
        int count = 0; // number of read bytes
        byte[] buf = null; // optimization - lazy allocation only if necessary
        while ((b = in.read()) != -1 && b != delim) {
            if (count == len) { // expand buffer
                if (count == maxLength)
                    throw new IOException("token too large (" + count + ")");
                len = len > 0 ? 2 * len : 256; // start small, double each expansion
                len = maxLength < len ? maxLength : len;
                byte[] expanded = new byte[len];
                if (buf != null)
                    System.arraycopy(buf, 0, expanded, 0, count);
                buf = expanded;
            }
            buf[count++] = (byte)b;
        }
        if (b < 0 && delim != -1)
            throw new EOFException("unexpected end of stream");
        if (delim == '\n' && count > 0 && buf[count - 1] == '\r')
            count--;
        return count > 0 ? new String(buf, 0, count, enc) : "";
    }

    private void canServerSupportRequest() {
        isSupported = method != HttpMethod.UNSUPPORTED &&
                version != HttpVersion.UNSUPPORTED;
    }
}