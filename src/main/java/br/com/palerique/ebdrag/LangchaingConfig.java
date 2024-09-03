package br.com.palerique.ebdrag;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocumentsRecursively;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.DocumentSplitter;
import dev.langchain4j.data.document.parser.apache.tika.ApacheTikaDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.allminilml6v2q.AllMiniLmL6V2QuantizedEmbeddingModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.rag.DefaultRetrievalAugmentor;
import dev.langchain4j.rag.RetrievalAugmentor;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.rag.content.retriever.WebSearchContentRetriever;
import dev.langchain4j.rag.query.router.DefaultQueryRouter;
import dev.langchain4j.rag.query.router.QueryRouter;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.web.search.WebSearchEngine;
import dev.langchain4j.web.search.tavily.TavilyWebSearchEngine;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
@Slf4j
public class LangchaingConfig {

  private final String systemMessage =
      """
      Você é um assistente de chat para questões sobre a Bíblia e teologia, com base nos documentos fornecidos.
      As respostas devem ser em português brasileiro. Se houver uma pergunta para a qual você não sabe a resposta,
      informe "Não tenho essa informação no momento".
      Por favor, responda em frases completas e com precisão.
      """;

  @Value("${ollama.model.name}")
  private String modelName;

  @Value("${ollama.host.url}")
  private String baseUrl;

  @Bean
  ChatLanguageModel chatLanguageModel() {
    return OllamaChatModel.builder()
        .modelName(modelName)
        .baseUrl(baseUrl)
        .timeout(Duration.ofMinutes(5))
        .build();
  }

  @Bean
  EmbeddingModel embeddingModel() {
    return new AllMiniLmL6V2QuantizedEmbeddingModel();
  }

  @Bean
  EmbeddingStore<TextSegment> embeddingStore(
      ResourceLoader resourceLoader, EmbeddingModel embeddingModel) {
    //    return new InMemoryEmbeddingStore<>();
    // Normally, you would already have your embedding store filled with your data.
    // However, for the purpose of this demonstration, we will:

    // 1. Create an in-memory embedding store
    EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

    // 2. Load an example document ("Miles of Smiles" terms of use)
    //    Resource resource =
    // resourceLoader.getResource("classpath:miles-of-smiles-terms-of-use.txt");
    //    Document document = loadDocument(resource.getFile().toPath(), new TextDocumentParser());
    List<Document> documents = loadMultipleDocumentsRecursively(resourceLoader);
    //    EmbeddingStoreIngestor.ingest(documents, embeddingStore);

    // 3. Split the document into segments 100 tokens each
    // 4. Convert segments into embeddings
    // 5. Store embeddings into embedding store
    // All this can be done manually, but we will use EmbeddingStoreIngestor to automate this:
    //    DocumentSplitter documentSplitter =
    //        DocumentSplitters.recursive(100, 0, new OpenAiTokenizer("gpt-3.5-turbo"));
    DocumentSplitter documentSplitter = new DocumentBySentenceSplitter(300, 0);
    EmbeddingStoreIngestor ingestor =
        EmbeddingStoreIngestor.builder()
            .documentSplitter(documentSplitter)
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build();
    ingestor.ingest(documents);

    return embeddingStore;
  }

  @Bean
  EmbeddingStoreIngestor embeddingStoreIngestor(
      EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {
    return EmbeddingStoreIngestor.builder()
        .documentSplitter(DocumentSplitters.recursive(300, 0))
        .embeddingStore(embeddingStore)
        .embeddingModel(embeddingModel)
        .build();
  }

  @Bean
  RetrievalAugmentor retrievalAugmentor(
      EmbeddingStore<TextSegment> embeddingStore, EmbeddingModel embeddingModel) {

    //    return EmbeddingStoreContentRetriever.from(embeddingStore);

    // You will need to adjust these parameters to find the optimal setting, which will depend on
    // two main factors:
    // - The nature of your data
    // - The embedding model you are using
    int maxResults = 5;
    double minScore = 0.7;

    final var embeddingStoreContentRetriever =
        EmbeddingStoreContentRetriever.builder()
            .embeddingStore(embeddingStore)
            .embeddingModel(embeddingModel)
            .maxResults(maxResults)
            .minScore(minScore)
            .build();

    // Create web search content retriever.
    WebSearchEngine webSearchEngine =
        TavilyWebSearchEngine.builder().apiKey("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").build();

    ContentRetriever webSearchContentRetriever =
        WebSearchContentRetriever.builder().webSearchEngine(webSearchEngine).maxResults(3).build();

    // Create a query router that routes queries to both retrievers.
    QueryRouter queryRouter =
        new DefaultQueryRouter(embeddingStoreContentRetriever, webSearchContentRetriever);

    return DefaultRetrievalAugmentor.builder().queryRouter(queryRouter).build();
  }

  private List<Document> loadMultipleDocumentsRecursively(ResourceLoader resourceLoader) {
    Path directoryPath = toPath("documents/", resourceLoader);
    log.info("Carregando múltiplos documentos recursivamente de: {}", directoryPath);
    return loadDocumentsRecursively(directoryPath, new ApacheTikaDocumentParser());
  }

  private Path toPath(String fileName, ResourceLoader resourceLoader) {
    try {
      Resource resource = resourceLoader.getResource("classpath:%s".formatted(fileName));
      return resource.getFile().toPath();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Bean
  Assistant getAssistant(
      ChatLanguageModel chatLanguageModel, RetrievalAugmentor retrievalAugmentor) {
    return AiServices.builder(Assistant.class)
        .systemMessageProvider(_ -> systemMessage)
        .chatLanguageModel(chatLanguageModel)
        .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
        .retrievalAugmentor(retrievalAugmentor)
        .build();
  }
}
