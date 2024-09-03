package br.com.palerique.ebdrag;

/**
 * The Assistant interface provides a method for responding to queries. Implementations of this
 * interface should provide logic for generating answers based on the input query.
 */
public interface Assistant {
  String answer(String query);
}
