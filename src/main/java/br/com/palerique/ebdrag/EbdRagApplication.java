package br.com.palerique.ebdrag;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@RequiredArgsConstructor
public class EbdRagApplication {

  private final ChatService chatService;

  public static void main(String[] args) {
    SpringApplication.run(EbdRagApplication.class, args);
  }

  @Bean
  CommandLineRunner runner() {
    return _ -> chatService.startConsoleChat();
  }
}
