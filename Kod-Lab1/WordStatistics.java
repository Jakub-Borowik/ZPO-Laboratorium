import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.Set;

public class WordStatistics {

    public Map<String, Long> getLinkedCountedWords(Path path, int wordsLimit) {
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return reader.lines() 
                .map(String::toLowerCase) // Zmiana na małe litery
                .map(line -> line.split("\\s+")) // Cięcie wyrazów po spacji
                .flatMap(Arrays::stream) // Konwersja z strumienia tablic na strumień słów
                .map(word -> word.replaceAll("[^a-z0-9ąęćłńóśźż]", "")) // Usunięcie znaków specjalnych
                .filter(word -> word.matches("[a-z0-9ąęćłńóśźż]{3,}")) // Odrzucenie słów gdzie ilość liter<3
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting())) // Zliczanie i grupowanie słów
                .entrySet().stream() // Konwersja na strumień aby posortować
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder())) // Sortowanie malejąco
                .limit(wordsLimit) // Limitowanie wyniku
                .collect(Collectors.toMap( // Zapakowanie wyniku do HashMap
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (k, v) -> { throw new IllegalStateException(String.format("ERROR -  Key duplicate %s.", k)); },
                    LinkedHashMap::new
                ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

/* 
    // --- METODA TESTOWA ---
    public static void main(String[] args) {
        WordStatistics stats = new WordStatistics();
        
        // Wskazujemy na plik text
        Path testPath = Paths.get("OdaDoMlodosci");
        
        try {
            // Wywołanie metody
            Map<String, Long> wynik = stats.getLinkedCountedWords(testPath, 10);
            
            // Wypisanie wyniku
            System.out.println("Result: " + wynik);
            
        } catch (Exception e) {
            System.out.println("ERROR - Check teh file name.");
            e.printStackTrace();
        } 
    } */

    // --- METODA PODOBIEŃSTWA COSINUSOWEGO ---
    public double calculateCosineSimilarity(Map<String, Long> text1, Map<String, Long> text2) {
        // 1. Zebranie wszystkich słów do jednego HashSetu
        Set<String> allWords = new HashSet<>();
        allWords.addAll(text1.keySet());
        allWords.addAll(text2.keySet());

        double dotProduct = 0.0; // Licznik (iloczyn skalarny)
        double normA = 0.0;      // Mianownik część 1 (długość wektora A)
        double normB = 0.0;      // Mianownik część 2 (długość wektora B)

        // 2. Przejscie przez wzór dla każdego słowa
        for (String word : allWords) {
            long countInText1 = text1.getOrDefault(word, 0L);
            long countInText2 = text2.getOrDefault(word, 0L);

            dotProduct += (countInText1 * countInText2);
            normA += Math.pow(countInText1, 2);
            normB += Math.pow(countInText2, 2);
        }

        // Zabezpieczenie przed dzieleniem przez zero
        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        // 3. Obliczenie ze wzoru
        return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}