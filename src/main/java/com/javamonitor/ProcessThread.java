package com.javamonitor;

import com.opencsv.CSVReader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessThread implements Runnable{
    private Path pathToFile;
    public ProcessThread(Path path){
        File file = path.toFile();
        File newFile = new File( Props.INPUT_FOLDER+"/in_process/"+ file.getName());
        if(!newFile.exists()){
            newFile.getParentFile().mkdirs();
        }
        file.renameTo(newFile);
        this.pathToFile = newFile.toPath();

    }

    public void run() {
        CSVReader reader;
        Map<LocalDate, Map<String, UserData>> data = new HashMap<>();
        try {
            Map<String, UserData> users;
            reader = new CSVReader(new FileReader(pathToFile.toFile()));
            String[] line;
            while ((line  = reader.readNext()) != null) {
                long timestamp = Long.valueOf(line[0])*1000;
                String newUser= line[1];
                String url=line[2];
                long time=Long.valueOf(line[3]);
                long isMoreThanOne=0;
                LocalDateTime newDateTime = convertToDateTime(timestamp).plusSeconds(time);
                LocalDateTime oldDateTime = convertToDateTime(timestamp);
                //check if overdate
                while(newDateTime.toLocalDate().isAfter(oldDateTime.toLocalDate())){
                    long diff = convertToTimestamp(newDateTime)/1000
                            -convertToTimestamp(newDateTime.toLocalDate().atStartOfDay())/1000;
                    users=data.getOrDefault(newDateTime.toLocalDate(), new HashMap<>());
                    users.merge(newUser, new UserData(newUser, url, diff+isMoreThanOne, Long.valueOf(line[0])*1000), (userData, newUserData) -> {
                        userData.sites.merge(url, newUserData.sites.get(url),
                                (val, newVal) -> val + newVal);
                        return userData;
                    });
                    data.put(newDateTime.toLocalDate(), users);
                    time=time-diff-1;
                    newDateTime = convertToDateTime(timestamp).plusSeconds(time);
                    isMoreThanOne=1;
                }
                users=data.getOrDefault(oldDateTime.toLocalDate(), new HashMap<>());
                users.merge(newUser, new UserData(newUser, url, time+isMoreThanOne, Long.valueOf(line[0])*1000), (userData, newUserData) -> {
                    userData.sites.merge(url, newUserData.sites.get(url),
                            (val, newVal) -> val + newVal);
                    return userData;
                });
                data.put(oldDateTime.toLocalDate(), users);
            }
        } catch (IOException e) {
            System.out.println("Error reading csv-file: " + e.getCause());
        }finally{
            try {
                Files.delete(pathToFile.toAbsolutePath());
            } catch (IOException e) {
                System.out.println("Error deleting file: " + e.getCause());
            }
        }
        try{
            File f = new File( Props.OUTPUT_FOLDER+"/avg_"+ pathToFile.getFileName());
            if(!f.exists()){
                f.getParentFile().mkdirs();
                f.createNewFile();
            }
            Files.write(Paths.get(f.getAbsolutePath()), compileStrings(data));
        }catch(IOException e){
            System.out.println("Error writing file: " + e.getCause());
        }
        System.out.println("DONE "+new Date(System.currentTimeMillis()).toString()+"\t"+ pathToFile.getFileName());
    }


    private static List<String> compileStrings(Map<LocalDate, Map<String, UserData>> data) {

        List<String> lines = new ArrayList<>();
        List<LocalDate> sortedData = data.keySet().stream().sorted().collect(Collectors.toList());
        for (LocalDate entry : sortedData)
        {
            Instant instant = entry.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
            Date res = Date.from(instant);
            lines.add(new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH).format(res).toUpperCase());
            Map<String, UserData> users = data.get(entry);
            List<String> sortedUsers = users.keySet().stream().sorted(Comparator.comparingInt(s -> Integer.valueOf(s.subSequence(4, s.length()).toString()))).collect(Collectors.toList());
            for(String user : sortedUsers){
                List<String> sortedUrls = users.get(user).sites.keySet().stream().sorted().collect(Collectors.toList());
                for(String url : sortedUrls){
                    String line = user;
                    line+=","+url+","+users.get(user).sites.get(url);
                    lines.add(line);
                }
            }
        }
        return lines;
    }

    private long convertToTimestamp(LocalDateTime ldt) {
        return Timestamp.valueOf(ldt).getTime();
    }

    private LocalDateTime convertToDateTime(long ts) {
        return new Timestamp(ts).toLocalDateTime();
    }
}
