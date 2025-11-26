package com.opwriter.services;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Service
public class ChatGptServiceImp implements ChatGptService {

    @Value("${cohere.api.key:}")
    private String apiKey;

    private static final String API_URL = "https://api.cohere.com/v2/chat";
    private static final MediaType JSON = MediaType.parse("application/json");

    public String getAnswer(String topic, Integer words, String format) {

        OkHttpClient client = new OkHttpClient();
        String prompt = "Write a detailed " + format + " on the topic: " + topic + " with " + words + " words";

        String json = """
        {
          "model": "command-a-03-2025",
          "messages": [
            {
              "role": "user",
              "content": "%s"
            }
          ]
        }
        """.formatted(prompt);

        RequestBody body = RequestBody.create(json, JSON);

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {

            if (!response.isSuccessful()) {
                return "Error: " + response.code() + " " + response.message();
            }

            String bodyString = response.body().string();

            JSONObject root = new JSONObject(bodyString);
            JSONObject message = root.getJSONObject("message");
            JSONArray content = message.getJSONArray("content");
            JSONObject textObj = content.getJSONObject(0);
            String text = textObj.getString("text");

            return text;

        } catch (IOException e) {
            return "Exception: " + e.getMessage();
        }
    }
}
