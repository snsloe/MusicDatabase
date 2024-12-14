import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class MusicDatabaseManager {
    private final File databaseFile;
    private final Map<Integer, MusicRecord> recordById = new HashMap<>();
    private final Map<String, TreeMap<String, List<MusicRecord>>> indexedFields = new HashMap<>();

    public MusicDatabaseManager(String filePath) {
        this.databaseFile = new File(filePath);
    }

    public void createDatabase() throws IOException {
        if (!databaseFile.exists()) {
            databaseFile.createNewFile();
            saveDatabase(new ArrayList<>());
        }
        rebuildIndexes();
    }

    public List<MusicRecord> openDatabase() throws IOException {
        if (!databaseFile.exists()) {
            throw new FileNotFoundException("Database file not found!");
        }
        try (FileReader reader = new FileReader(databaseFile)) {
            List<MusicRecord> records = new Gson().fromJson(reader, new TypeToken<List<MusicRecord>>() {}.getType());
            if (records == null) {
                records = new ArrayList<>();
            }
            rebuildIndexes(records);
            return records;
        }
    }

    public void saveDatabase(List<MusicRecord> records) throws IOException {
        try (FileWriter writer = new FileWriter(databaseFile)) {
            new Gson().toJson(records, writer);
        }
        rebuildIndexes(records);
    }

    public void addRecord(MusicRecord record) throws IOException {
        if (recordById.containsKey(record.getId())) {
            throw new IllegalArgumentException("Record with the same ID already exists!");
        }
        recordById.put(record.getId(), record);
        indexRecord(record);
        ArrayList<MusicRecord> newBase = new ArrayList<>(recordById.values());
        saveDatabase(newBase);
    }

    public void deleteRecordByKey(int id) throws IOException {
        MusicRecord record = recordById.remove(id);
        if (record != null) {
            deindexRecord(record);
            ArrayList<MusicRecord> newBase = new ArrayList<>(recordById.values());
            saveDatabase(newBase);
        }
    }

    public void deleteRecordByField(String fieldName, String value) throws IOException {
        TreeMap<String, List<MusicRecord>> fieldIndex = indexedFields.get(fieldName);
        if (fieldIndex != null) {
            List<MusicRecord> recordsToDelete = new ArrayList<>(fieldIndex.getOrDefault(value, new ArrayList<>()));
            if (!recordsToDelete.isEmpty()) {
                for (MusicRecord record : recordsToDelete) {
                    recordById.remove(record.getId());
                    deindexRecord(record);
                }
                ArrayList<MusicRecord> newBase = new ArrayList<>(recordById.values());
                saveDatabase(newBase);
            } else {
                throw new IllegalArgumentException("No records found for deletion!");
            }
        } else {
            throw new IllegalArgumentException("Field name not indexed!");
        }
    }

    public List<MusicRecord> searchByField(String fieldName, String value) {
        TreeMap<String, List<MusicRecord>> fieldIndex = indexedFields.get(fieldName);
        if (fieldIndex != null) {
            return fieldIndex.getOrDefault(value, new ArrayList<>());
        }
        return Collections.emptyList();
    }

    public void backupDatabase(String backupPath) throws IOException {
        Files.copy(databaseFile.toPath(), Paths.get(backupPath), StandardCopyOption.REPLACE_EXISTING);
    }

    public void restoreDatabase(String backupPath) throws IOException {
        Files.copy(Paths.get(backupPath), databaseFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        rebuildIndexes();
    }

    public void clearDatabase() throws IOException {
        recordById.clear();
        indexedFields.clear();
        saveDatabase(new ArrayList<>());
    }

    private void rebuildIndexes() throws IOException {
        rebuildIndexes(openDatabase());
    }

    private void rebuildIndexes(List<MusicRecord> records) {
        recordById.clear();
        indexedFields.clear();
        for (MusicRecord record : records) {
            recordById.put(record.getId(), record);
            indexRecord(record);
        }
    }

    private void indexRecord(MusicRecord record) {
        indexField("id", String.valueOf(record.getId()), record);
        indexField("duration", String.valueOf(record.getDuration()), record);
        indexField("title", record.getTitle(), record);
        indexField("artist", record.getArtist(), record);
        indexField("album", record.getAlbum(), record);
        indexField("genre", record.getGenre(), record);
    }

    private void deindexRecord(MusicRecord record) {
        deindexField("id", String.valueOf(record.getId()), record);
        deindexField("duration", String.valueOf(record.getDuration()), record);
        deindexField("title", record.getTitle(), record);
        deindexField("artist", record.getArtist(), record);
        deindexField("album", record.getAlbum(), record);
        deindexField("genre", record.getGenre(), record);
    }

    private void indexField(String fieldName, String value, MusicRecord record) {
        indexedFields.putIfAbsent(fieldName, new TreeMap<>());
        indexedFields.get(fieldName).computeIfAbsent(value, k -> new ArrayList<>()).add(record);
    }

    private void deindexField(String fieldName, String value, MusicRecord record) {
        TreeMap<String, List<MusicRecord>> fieldIndex = indexedFields.get(fieldName);
        if (fieldIndex != null) {
            List<MusicRecord> records = fieldIndex.get(value);
            if (records != null) {
                records.remove(record);
                if (records.isEmpty()) {
                    fieldIndex.remove(value);
                }
            }
        }
    }
}
