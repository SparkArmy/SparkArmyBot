package de.SparkArmy.utils;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RequestUtils {
    public static String BOUNDARY = "8721656041911415653955004498";
    public static HttpClient client = HttpClient.newHttpClient();
    /**
     * This should just help making requests in Java as I learned programming with Python and find requests in Java a bit too much
     */
    public static JSONObject get(String url, JSONObject headers, HttpRequest.BodyPublisher body) {
        return httpRequestToJsonObject(
                prepareRequest(url, headers, body, "GET")
        );
    }

    public static JSONObject get(String url){
        return get(url, new JSONObject(), noParams());
    }

    public static JSONObject get(String url, JSONObject headers){
        return get(url, headers, noParams());
    }

    public static JSONObject get(String url, HttpRequest.BodyPublisher body){
        return get(url, new JSONObject(), body);
    }

    public static JSONObject post(String url, JSONObject headers, HttpRequest.BodyPublisher body){
        return httpRequestToJsonObject(
                prepareRequest(url, headers, body, "POST")
        );
    }

    public static JSONObject post(String url, JSONObject headers){
        return post(url, headers, noParams());
    }

    public static JSONObject post(String url, HttpRequest.BodyPublisher body){
        return post(url, new JSONObject(), body);
    }

    /**
     * End of Python-like implementation
     * */


    public static HttpRequest prepareRequest(String url, JSONObject headers, HttpRequest.BodyPublisher body, String mode){
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url.replace(" ", "%20")))
                .method(mode, body);
        if (!headers.keySet().contains("UserAgent")) {
            headers.put("UserAgent", RandomUserAgent.getRandomUserAgent());
        }
        headers.keys().forEachRemaining(x -> requestBuilder.header(x, headers.getString(x)));
        return requestBuilder.build();
    }

    public static JSONObject httpRequestToJsonObject(HttpRequest request) {
        HttpResponse<String> response = httpRequestToResponse(request);
        return responseToJson(response);
    }

    public static HttpResponse<String> httpRequestToResponse(HttpRequest request) {
        HttpResponse<String> response = null;
        try {
            CompletableFuture<HttpResponse<String>> futureResponse = client.sendAsync(request, HttpResponse.BodyHandlers.ofString());
            response = futureResponse.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    public static JSONObject responseToJson(HttpResponse<String> response) {
        JSONObject data;
        try {
            data = new JSONObject(response.body());
        } catch (Exception e) {
            data = new JSONObject();
            data.put("html", response.body());
        }
        return data;
    }

    public static HttpRequest.BodyPublisher noParams(){
        return HttpRequest.BodyPublishers.noBody();
    }

    public static void refreshHttpClient(){
        client = HttpClient.newHttpClient();
    }

    public static HttpRequest.BodyPublisher toParams(Map<Object, Object> data, String boundary) {
        /*
         * ------------------------------------ Not my Code ------------------------------------
         * I just renamed the method
         *
         * webiste:
         *   https://stackoverflow.com/questions/56481475/how-to-define-multiple-parameters-for-a-post-request-using-java-11-http-client
         * author:
         *   https://stackoverflow.com/users/3523579/mikhail-kholodkov
         * same code but on GitHub: (link from stackoverflow article)
         *   https://github.com/ralscha/blog2019/blob/master/java11httpclient/client/src/main/java/ch/rasc/httpclient/File.java#L69-L91
         *
         * Side note: smart way of doing it
         * */


        // Result request body
        List<byte[]> byteArrays = new ArrayList<>();

        // Separator with boundary
        byte[] separator = ("--" + boundary + "\r\nContent-Disposition: form-data; name=").getBytes(StandardCharsets.UTF_8);

        // Iterating over data parts
        for (Map.Entry<Object, Object> entry : data.entrySet()) {

            // Opening boundary
            byteArrays.add(separator);

            // If value is type of Path (file) append content type with file name and file binaries, otherwise simply append key=value
            if (entry.getValue() instanceof Path path) {//moved path variable from try block to if-statement
                try {
                    String mimeType = Files.probeContentType(path);
                    byteArrays.add(("\"" + entry.getKey() + "\"; filename=\"" + path.getFileName()
                            + "\"\r\nContent-Type: " + mimeType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                    byteArrays.add(Files.readAllBytes(path));
                    byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
                } catch (IOException e) {
                    //This catch block is by me not the original author
                    e.printStackTrace();
                    MainUtil.logger.severe(entry.getValue().toString());
                    throw new RuntimeException("An error accrued whilst trying to make the body for a request");
                }
            } else {
                byteArrays.add(("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue() + "\r\n")
                        .getBytes(StandardCharsets.UTF_8));
            }
        }

        // Closing boundary
        byteArrays.add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));

        // Serializing as byte array
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

}
