package com.example.collegeadmission;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiCounsellorActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TextView tvBack;
    private TextInputEditText etMessage;
    private MaterialButton btnSend;
    private ProgressBar progressBar;

    private final ArrayList<ChatMessage> chatList = new ArrayList<>();
    private ChatAdapter adapter;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    // 🔥 PUT YOUR API KEY HERE
    private static final String GEMINI_API_KEY = "AIzaSyBK8B-Vf38_i83IeC2O4D_wi7jsBwFVpEY";

    // ✅ UPDATED WORKING URL
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key="
                    + GEMINI_API_KEY;

    // ================= MODEL =================
    public static class ChatMessage {
        public String text;
        public boolean isUser;

        public ChatMessage(String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
        }
    }

    // ================= ON CREATE =================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_counsellor);

        recyclerView = findViewById(R.id.recyclerView);
        tvBack = findViewById(R.id.tvBack);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        progressBar = findViewById(R.id.progressBar);

        adapter = new ChatAdapter(chatList);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setStackFromEnd(true);

        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);

        addBotMessage("Hi! I am your AI College Counsellor 😊");

        tvBack.setOnClickListener(v -> finish());
        btnSend.setOnClickListener(v -> sendMessage());
    }

    // ================= SEND MESSAGE =================
    private void sendMessage() {
        if (etMessage.getText() == null) return;

        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        addUserMessage(text);
        etMessage.setText("");

        showLoading(true);
        askGemini(text);
    }

    // ================= CHAT METHODS =================
    private void addUserMessage(String text) {
        chatList.add(new ChatMessage(text, true));
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(chatList.size() - 1);
    }

    private void addBotMessage(String text) {
        chatList.add(new ChatMessage(text, false));
        adapter.notifyDataSetChanged();
        recyclerView.scrollToPosition(chatList.size() - 1);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSend.setEnabled(!show);
    }

    // ================= API CALL =================
    private void askGemini(String question) {
        try {
            JSONObject part = new JSONObject();
            part.put("text", question);

            JSONArray parts = new JSONArray();
            parts.put(part);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject body = new JSONObject();
            body.put("contents", contents);

            RequestBody requestBody = RequestBody.create(
                    body.toString(),
                    MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(GEMINI_URL)
                    .addHeader("Content-Type", "application/json")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> {
                        showLoading(false);
                        addBotMessage("❌ Network error: " + e.getMessage());
                    });
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    String res = response.body() != null ? response.body().string() : "";

                    Log.d("API_RESPONSE", res);

                    runOnUiThread(() -> {
                        showLoading(false);

                        try {
                            JSONObject json = new JSONObject(res);

                            if (json.has("candidates")) {
                                JSONArray candidates = json.getJSONArray("candidates");

                                String reply = candidates
                                        .getJSONObject(0)
                                        .getJSONObject("content")
                                        .getJSONArray("parts")
                                        .getJSONObject(0)
                                        .getString("text");

                                addBotMessage(reply);

                            } else if (json.has("error")) {
                                String error = json.getJSONObject("error").getString("message");
                                addBotMessage("❌ API Error: " + error);
                            } else {
                                addBotMessage("⚠️ Unexpected response");
                            }

                        } catch (Exception e) {
                            addBotMessage("❌ Parsing error");
                        }
                    });
                }
            });

        } catch (Exception e) {
            showLoading(false);
            addBotMessage("❌ Something went wrong");
        }
    }

    // ================= ADAPTER =================
    static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MsgVH> {

        ArrayList<ChatMessage> list;

        ChatAdapter(ArrayList<ChatMessage> list) {
            this.list = list;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        @Override
        public MsgVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            int layout = list.get(viewType).isUser
                    ? R.layout.item_chat_user
                    : R.layout.item_chat_bot;

            View view = LayoutInflater.from(parent.getContext())
                    .inflate(layout, parent, false);

            return new MsgVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MsgVH holder, int position) {
            holder.tvMsg.setText(list.get(position).text);
        }

        @Override
        public int getItemCount() {
            return list.size();
        }

        static class MsgVH extends RecyclerView.ViewHolder {
            TextView tvMsg;

            MsgVH(View itemView) {
                super(itemView);
                tvMsg = itemView.findViewById(R.id.tvMsg);
            }
        }
    }
}