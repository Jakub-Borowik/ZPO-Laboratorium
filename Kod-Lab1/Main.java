import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) {
        // 1. Ustawienia początkowe
        int liczbaProducentow = 1;
        int liczbaKonsumentow = 2;
        String folderDoPrzeszukania = "Kod-Lab1/Files";

        // 2. Tworzymy kolejke blokującą - pojemność wynosi 4
        BlockingQueue<Optional<Path>> kolejka = new LinkedBlockingQueue<>(4);
        
        // 3. Korzystamy z pliku WordStatistics aby przeskanować podane pliki .txt
        WordStatistics statystyki = new WordStatistics();
        
        // NOWOŚĆ: Bezpieczna tablica (sejf), do której Konsumenci będą wrzucać wyniki
        ConcurrentHashMap<String, Map<String, Long>> globalneWyniki = new ConcurrentHashMap<>();

        // --- DEFINICJA PRODUCENTA ---
        Runnable producent = () -> {
            Path dir = Paths.get(folderDoPrzeszukania);

            try {
                // Przeszukujemy foldery
                Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                        if (path.toString().endsWith(".txt")) {
                            System.out.println("PRODUCENT: Found and put a file in the queue: " + path.getFileName());
                            
                            try {
                                kolejka.put(Optional.ofNullable(path)); 
                            } catch (InterruptedException e) {
                                // W wypadku przerwania wątku podczas czekania, szukanie plików zostaje zatrzymane
                                Thread.currentThread().interrupt(); 
                                return FileVisitResult.TERMINATE; 
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

                // Po zakończeniu wyszukiwania plików wysyłane są Poison pills dla wszystkich (obu) konsumentów
                for (int i = 0; i < liczbaKonsumentow; i++) {
                    kolejka.put(Optional.empty()); // Poison pill jako Optional.empty
                }

            } catch (IOException e) {
                System.out.println("PRODUCENT: ERROR");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("PRODUCENT: INTERRUPTED");
            }
        };

        // --- DEFINICJA KONSUMENTA ---
        Runnable konsument = () -> {
            // Pobieram nazwe konsumenta aby na koncu potwierdzic dzialanie wieowątkowości
            String nazwaKonsumenta = Thread.currentThread().getName();

            while (true) {
                try {
                    Optional<Path> optPath = kolejka.take(); 

                    if (optPath.isPresent()) {
                        Path plikDoPoliczenia = optPath.get();
                        String nazwaPliku = plikDoPoliczenia.getFileName().toString();
                        
                        // Zmieniłem na 100 słów, aby miara kosinusowa miała więcej danych i była precyzyjniejsza
                        Map<String, Long> wynik = statystyki.getLinkedCountedWords(plikDoPoliczenia, 25);
                        
                        // Przedstawia wielowatkowosc przez wskazanie ze rozni konsumenci zajmuja sie roznymi plikami naraz
                        System.out.println(nazwaKonsumenta + " | " + nazwaPliku + " -> " + wynik);
                        
                        // Zapisanei wyników do późniejszego porównania
                        globalneWyniki.put(nazwaPliku, wynik);
                        
                    } else {
                        // Sytuacja w której zostanie pobrana Poison pill - zakończenie procesu
                        break; 
                    }
                } catch (InterruptedException e) {
                    System.out.println("KONSUMENT: ERROR");
                    break;
                }
            }
        };

        // 4. Włączenie programu za pomocą wątków
        System.out.println("--- FILE SEARCH HAS STARTED ---");
        ExecutorService executor = Executors.newFixedThreadPool(liczbaProducentow + liczbaKonsumentow);
        
        executor.submit(producent); // Uruchamiamy 1 producenta
        
        for (int i = 0; i < liczbaKonsumentow; i++) {
            executor.submit(konsument); // Uruchamiamy resztę konsumentów
        }

        // Po zakończeniu całego działania wątki są wyłączane
        executor.shutdown(); 
        
        // Zmuszenie programu głównego do poczekania na zakonczenie pracy przez Konsumentow
        try {
            executor.awaitTermination(5, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // --- PODOBIEŃSTWO COSINUSWE ---
        System.out.println("\n--- COSINE SIMILARITY ---");

        // Zmienie kluczy (nazwy plików) z mapy na zwykłą listę
        List<String> nazwyPlikow = new ArrayList<>(globalneWyniki.keySet());

        String plikBazowy = null;
        try (Scanner scanner = new Scanner(System.in)) {
            while (plikBazowy == null) {
                System.out.print("Primary File Name (with extension):");
                String input = scanner.nextLine().trim();
                if (globalneWyniki.containsKey(input)) {
                    plikBazowy = input;
                } else {
                    System.out.print("File not found, ones available: " + nazwyPlikow);
                }
            }
        }

        Map<String, Long> mapaBazowa = globalneWyniki.get(plikBazowy);

        for (String plik : nazwyPlikow) {
            if (plik.equals(plikBazowy)) {
                continue;
                // Jeśli plik istnieje zostaje on porównany nową funkcją w WordStatistics z resztą plików
            }
            Map<String, Long> mapa = globalneWyniki.get(plik);
            double podobienstwo = statystyki.calculateCosineSimilarity(mapaBazowa, mapa);
            System.out.printf("Similarity between [%s] and [%s] equals: %.2f%%\n", plikBazowy, plik, podobienstwo * 100);
        }
    }

}
