package com.progringer.crossword.service.impl;

import com.progringer.crossword.exception.DictionaryFileException;
import com.progringer.crossword.model.Crossword;
import com.progringer.crossword.model.Dictionary;
import com.progringer.crossword.model.Notion;
import com.progringer.crossword.service.FileService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FileServiceImpl implements FileService {
    Path crosswordFiles = Path.of("src","main","resources","static","crosswords");
    Path dictionariesFiles = Path.of("src","main","resources","static","dictionaries");

    @Override
    public void saveCrosswordToFile(Crossword crossword) throws IOException {
        Path path = crosswordFiles.resolve(filenameFromName(crossword.getName(), ".kros"));
        if(!Files.exists(path))
            path = Files.createFile(path);
        try(ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(path))){
            out.writeObject(crossword);
        }
    }

    @Override
    public void saveDictionaryToFile(Dictionary dictionary) throws IOException {
        Path path = dictionariesFiles.resolve(filenameFromName(dictionary.getName(), ".dict"));
        if(!Files.exists(path))
            Files.createFile(path);
        try (PrintWriter out = new PrintWriter(Files.newBufferedWriter(path, Charset.forName("windows-1251")))){
            out.println(dictionary.getName());
            for(Notion notion:dictionary.getWords()){
                out.println(notion.getWord()+" "+notion.getDefinition());
            }
        }
    }

    @Override
    public Crossword browseCrosswordFromFile(String name) throws IOException, ClassNotFoundException {
        Path path = crosswordFiles.resolve(filenameFromName(name, ".kros"));
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))){
            return (Crossword) in.readObject();
        }
    }

    @Override
    public Dictionary browseDictionaryFromFile(String name) throws IOException {
        Path path = dictionariesFiles.resolve(filenameFromName(name, ".dict"));
        Dictionary dictionary = new Dictionary(name);
        try (Stream<String> lines = Files.lines(path, Charset.forName("windows-1251"))){
            lines.skip(1).map(line->line.split("\\s",2)).forEach(x->dictionary.addNotion(new Notion(x[0], x[1])));
            return dictionary;
        }
    }
    @Override
    public Dictionary parseFileToDictionary(MultipartFile file) {
        Dictionary dictionary = new Dictionary();
        List<Notion> notions = new ArrayList<>();
        try {
            new BufferedReader(new InputStreamReader(file.getInputStream(), Charset.forName("windows-1251"))).lines().filter(line->line.matches("[\\p{InCyrillic}]+\\s.+")).map(line->line.split("\\s",2)).forEach(x->notions.add(new Notion(x[0], x[1])));
        }
        catch (Exception e){
            throw new DictionaryFileException();
        }
        if (notions.size()>0) {
            dictionary.setWords(notions);
            return dictionary;
        }
        return null;
    }
    @Override
    public Crossword browseCrosswordFromFile(Path path) throws IOException, ClassNotFoundException {
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(path))){
            return (Crossword) in.readObject();
        }
    }

    private String filenameFromName(String name, String exp){
        return name.replaceAll("[^[a-zA-Z0-9а-яА-ЯёЁ]]", "_")+exp;
    }

}
