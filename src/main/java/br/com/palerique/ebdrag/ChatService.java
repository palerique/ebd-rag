package br.com.palerique.ebdrag;

import static br.com.palerique.ebdrag.Console.startConversationWith;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ChatService {

  private final Assistant assistant;

  public void startConsoleChat() {
    try {
      startConversationWith(assistant);
    } catch (Exception e) {
      log.error("Erro ao iniciar o chat", e);
    }
  }
}
