package com.example.clubmanagementsystem.service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface AiClientService {

    String chat(String prompt);

    String chatStream(List<Map<String, String>> messages, Consumer<String> onToken);
}
