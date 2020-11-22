import java.io.*;
import java.net.URI;
import java.util.Date;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Storage {

    // FUTURE DEVELOPMENT IDEAS
    // each thread could use their own file to write onto (no need for sync)
    // at the end of the process each file will be closed
    // and they will be merged into one common file

    private BufferedWriter bufferedWriter;

    public Storage(String filename) throws IOException {
        File fout = new File(filename);
        FileOutputStream fos = new FileOutputStream(fout);
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(fos));
    }

    public void closeWriter() throws IOException {
        this.bufferedWriter.close();
    }

    public synchronized void insertCrawlResult(URI uri, String content) {
        try {
            System.out.println(uri + " -> " + content);
            bufferedWriter.write(convertToCSV(uri.toString(), (new Date()).toString(), content));
            bufferedWriter.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String escapeSpecialCharacters(String data) {
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }

    private static String convertToCSV(String... data) {
        return Stream.of(data)
                .map(Storage::escapeSpecialCharacters)
                .collect(Collectors.joining(","));
    }

}
