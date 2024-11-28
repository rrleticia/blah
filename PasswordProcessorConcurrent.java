import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Semaphore;


public class PasswordProcessorConcurrent  {
    
    static Semaphore mutex = new Semaphore(1);
    
    public static void main(String[] args)  throws InterruptedException {
        
        if (args.length < 1) {
            System.out.println("Uso: java PasswordProcessorSerial <caminho_do_diretorio>");
            return;
        }

        String directoryPath = args[0]; // Recebe o caminho como argumento

        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.out.println("Erro: Diretório não encontrado ou inválido.");
            return;
        }
        
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null) {
            System.out.println("Erro ao listar arquivos no diretório.");
            return;
        }

       
        Thread[] threads = new Thread[files.length];
        for (int i = 0; i < files.length; i++){
            Thread myThread = new Thread(new ProcessFile(files[i]), "myThread-ProcessFile");
            threads[i] = myThread;
            myThread.start();
        }

        for (Thread myThread : threads){
            myThread.join();
        }
    }

    private static String rot13(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                result.append((char) (((c - 'a' + 13) % 26) + 'a'));
            } else if (c >= 'A' && c <= 'Z') {
                result.append((char) (((c - 'A' + 13) % 26) + 'A'));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    public static class ProcessFile implements Runnable {

        private final File file;
    
        public ProcessFile(File input)  {
            this.file = input;
        }
    
        @Override
        public void run() {
            System.out.println("Processing file: " + file.getName());
            List<String> obfuscatedLines = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(this.file))) {
              String line;
              while ((line = reader.readLine()) != null) {
                  obfuscatedLines.add(rot13(line)); // Adiciona a linha ofuscada à lista
                }
            } catch (IOException e) {
                System.out.println("Erro ao ler o arquivo " + this.file.getName() + ": " + e.getMessage());
               return;
            }  
       

            System.out.println("aqui");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
              for (String obfuscatedLine : obfuscatedLines) {
                writer.write(obfuscatedLine);
                writer.newLine();
                }
            } catch (IOException e) {
                System.out.println("Erro ao escrever no arquivo " + this.file.getName() + ": " + e.getMessage());
            }
        }
    
    }
}