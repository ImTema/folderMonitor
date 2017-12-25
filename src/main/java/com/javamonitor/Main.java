package com.javamonitor;

import com.javamonitor.exceptions.StopNowException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args) throws IOException {
        String propsPath=null;
        if(args.length!=0){
            propsPath=args[0];
        }
        initProperties(propsPath);
        processFiles();
    }

    private static void processFiles() {
        ExecutorService ex = Executors.newFixedThreadPool(5);
        int totalCount=0;
        try{
            while(true){
                for(Path file : getAllCsvFiles()){
                    if(file.getFileName().toString().equals("__stopnow__.csv"))
                        throw new StopNowException();
                    Future fut = ex.submit(new ProcessThread(file));
                    totalCount++;
                }
            }
        }
        catch(StopNowException e){
            System.out.println("stop-file '__stopnow__.csv' has been noticed. Program terminating.");
            String[] info = ex.toString().replace(" ", "").split(",");
            int count=Integer.valueOf(info[1].split("=")[1])+Integer.valueOf(info[2].split("=")[1]);
            System.out.println("Waiting to process remaining files ("+count+"/"+totalCount+")");
            ex.shutdown();
        } catch (Exception e) {
            System.out.println("Error occurred: "+e.getCause());
            e.printStackTrace();
        }

        System.out.println(totalCount +" files processed.");
    }

    private static void initProperties(String propsPath) {
        Properties props = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream(propsPath);
            props.load(input);
            Props.INPUT_FOLDER=new File(props.getProperty("inputFolder")).getAbsolutePath();
            Props.OUTPUT_FOLDER=new File(props.getProperty("outputFolder")).getAbsolutePath();
        } catch (Exception e) {
            System.out.println("Can't load properties from " + e.getMessage());
            System.out.println("folders has been set to default:");
            Props.INPUT_FOLDER=System.getProperty("user.dir");
            Props.OUTPUT_FOLDER=System.getProperty("user.dir");;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("INPUT FOLDER:\t"+ Props.INPUT_FOLDER);
        System.out.println("OUTPUT FOLDER:\t"+ Props.OUTPUT_FOLDER);
    }

    private static List<Path> getAllCsvFiles() throws IOException {
        try (Stream<Path> paths = Files.list(Paths.get(Props.INPUT_FOLDER))) {
            return paths.filter(Files::isRegularFile)
                        .filter(t->t.toString().endsWith(".csv"))
                        .filter(t->!t.toFile().getName().startsWith("avg_"))
                        .collect(Collectors.toList())
                    ;
        }catch (Exception e){
            System.out.println("Error getting files: " + e.getCause());
            throw e;
        }
    }


}

