package com.github.zxh.classpy.gui;

import com.github.zxh.classpy.common.FileComponent;
import com.github.zxh.classpy.common.FileHex;
import com.github.zxh.classpy.common.FileParseException;
import com.github.zxh.classpy.common.FileParser;
import com.github.zxh.classpy.common.FileParsers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import javafx.concurrent.Task;

/**
 *
 * @author zxh
 */
public class OpenFileTask extends Task<Object> {

    private final Path file;

    public OpenFileTask(Path file) {
        this.file = file;
    }
    
    @Override
    protected Object call() throws Exception {
        System.out.println("loading " + file + "...");
        
        if (Files.size(file) > 512 * 1024) {
            throw new FileParseException("File is too large!");
        }
        
        String fileType = getExtension(file.toString());
        FileParser parser = FileParsers.getParser(fileType);
        
        byte[] bytes = Files.readAllBytes(file);
        FileComponent fc = parser.parse(bytes);
        fc.setName(file.getFileName().toString());
        FileHex hex = new FileHex(bytes);
        
        System.out.println("finish loading");
        return new Object[] {fc, hex};
    }
    
    private static String getExtension(String fileName) {
        int idxOfDot = fileName.lastIndexOf('.');
        return idxOfDot < 0 ? fileName : fileName.substring(idxOfDot);
    }
    
    public void setOnSucceeded(BiConsumer<FileComponent, FileHex> callback) {
        super.setOnSucceeded(e -> {
            Object[] arr = (Object[]) e.getSource().getValue();
            FileComponent fc = (FileComponent) arr[0];
            FileHex hex = (FileHex) arr[1];
            
            callback.accept(fc, hex);
        });
    }
    
    public void setOnFailed(Consumer<Throwable> callback) {
        super.setOnFailed(e -> {
            Throwable err = e.getSource().getException();
            err.printStackTrace(System.err);
            
            callback.accept(err);
        });
    }
    
    public void startInNewThread() {
        new Thread(this).start();
    }
    
}
