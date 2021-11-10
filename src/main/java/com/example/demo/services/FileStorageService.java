package com.example.demo.services;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class FileStorageService {
    static final Path path = FileSystems.getDefault().getPath("").toAbsolutePath();

    private final Path root = Path.of(Paths.get(path.toString(), "src", "main", "resources", "imgs").toString());
    public void init() {
        try {
            System.out.println("/////////////////###########################3333-**************"+root);

            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize folder for upload!");
        }
    }
    public String save(MultipartFile file) {
        try {
            String uniqueID = UUID.randomUUID().toString();
            Files.copy(file.getInputStream(), this.root.resolve(uniqueID+file.getOriginalFilename()));
            return uniqueID+file.getOriginalFilename();
        } catch (Exception e) {
            throw new RuntimeException("Could not store the file. Error: " + e.getMessage());
        }
    }

    public String save(String base64String){

        String uniqueID = UUID.randomUUID().toString();
        
        String[] strings = base64String.split(",");
        String extension;
        switch (strings[0]) {//check image's extension
            case "data:image/jpeg;base64":
                extension = "jpeg";
                break;
            case "data:image/png;base64":
                extension = "png";
                break;
            default://should write cases for more images types
                extension = "jpg";
                break;
        }
        //convert base64 string to binary data
        byte[] data = DatatypeConverter.parseBase64Binary(strings.length>1?strings[1]:strings[0]);
        String path = this.root.resolve(uniqueID) +"."+ extension;
        File file = new File(path);
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file))) {
            outputStream.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return uniqueID+"."+extension;
    }

    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }



    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Could not load the files!");
        }
    }

    @Async
    public void asyncSearchInCSV(String[] querySplit, BufferedReader reader, String ...headers) throws IOException {
        CSVParser parser = CSVFormat.Builder
                .create()
                .setQuote(null)
                .setDelimiter(',')
                .setHeader(headers)
                .build()
                .parse(reader);
        System.out.println("b thread"+Thread.currentThread().getName());
        System.out.println(Thread.activeCount());
        Iterable<CSVRecord> records = parser;
        System.out.println("size is");
        for (CSVRecord line : records) {
        }
    }

    public String[] getHeadersFromCsv(CSVRecord csvRecord){
        String headers[]=new String[csvRecord.size()];
        int count=0;
        csvRecord.stream().forEach(x->headers[count] = x);
        return headers;
    }
    public String getRootPath(){
        return Path.of(Paths.get(path.toString(), "src", "main", "resources").toString()).toString();

    }


}
