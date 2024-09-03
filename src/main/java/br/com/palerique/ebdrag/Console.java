package br.com.palerique.ebdrag;

import java.time.Duration;
import java.time.Instant;
import java.util.Scanner;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Console {
  static final String welcomeMessageEng =
      """
            Welcome to the chat with the assistant. You can start the conversation by typing a message.
            Type 'exit' or 'bye' to finish the conversation.
            """;
  static final String welcomeMessageEsp =
      """
            Bienvenido al chat con el asistente. Puedes iniciar la conversación escribiendo un mensaje.
            Escribe 'exit' o 'bye' para finalizar la conversación.
            """;

  private Console() {
    throw new IllegalStateException("Instantiation not allowed");
  }

  public static void startConversationWith(Assistant assistant) {
    Logger log = LoggerFactory.getLogger(Assistant.class);
    log.info(welcomeMessageEng);
    try (Scanner scanner = new Scanner(System.in)) {
      while (true) {
        log.info("==================================================");
        log.info("User: ");
        String userQuery = scanner.nextLine();
        log.info("==================================================");

        if ("exit".equalsIgnoreCase(userQuery) || "bye".equalsIgnoreCase(userQuery)) {
          break;
        }

        Instant startTime = Instant.now(); // Start measuring time
        String agentAnswer = assistant.answer(userQuery);
        Instant endTime = Instant.now(); // End measuring time

        log.info("==================================================");
        log.info("Assistant: " + agentAnswer);
        log.info("Elapsed Time: " + calculateElapsedTimeFormatted(startTime, endTime));
      }
    }
  }

  @NotNull
  private static String calculateElapsedTimeFormatted(Instant startTime, Instant endTime) {
    // Calculate elapsed time
    Duration elapsedTime = Duration.between(startTime, endTime);

    // Format elapsed time as hh:mm:ss.mmm
    long hours = elapsedTime.toHours();
    long minutes = elapsedTime.toMinutes() % 60;
    long seconds = elapsedTime.getSeconds() % 60;
    long millis = elapsedTime.toMillis() % 1000;

    return String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);
  }
}
