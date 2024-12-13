import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MusicDatabaseGUI extends JFrame {
    private final MusicDatabaseManager dbManager;
    private JTable table;
    private DefaultTableModel tableModel;

    public MusicDatabaseGUI() {
        this.dbManager = new MusicDatabaseManager("music_database.json");
        initUI();
        loadTable();
    }

    private void initUI() {
        setTitle("Music Database");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel topPanel = new JPanel();
        JButton btnCreate = new JButton("Create Database");
        JButton btnAdd = new JButton("Add Record");
        JButton btnSearch = new JButton("Search");
        JButton btnBackup = new JButton("Backup");
        JButton btnRestore = new JButton("Restore");
        JButton btnClear = new JButton("Clear Database");
        JButton btnDeleteKey = new JButton("Delete by ID");
        JButton btnDeleteField = new JButton("Delete by Field");
        JButton btnShowAll = new JButton("Show All");
        JButton btnDeleteDB = new JButton("Delete Database");
        JButton btnExport = new JButton("Export to Excel");

        topPanel.add(btnCreate);
        topPanel.add(btnAdd);
        topPanel.add(btnSearch);
        topPanel.add(btnBackup);
        topPanel.add(btnRestore);
        topPanel.add(btnClear);
        topPanel.add(btnDeleteKey);
        topPanel.add(btnDeleteField);
        topPanel.add(btnShowAll);
        topPanel.add(btnDeleteDB);
        topPanel.add(btnExport);

        add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"ID", "Title", "Artist", "Album", "Genre", "Duration"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        btnCreate.addActionListener(e -> {
            try {
                dbManager.createDatabase();
                JOptionPane.showMessageDialog(this, "Database created successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error creating database!");
            }
        });

        btnAdd.addActionListener(e -> {
            try {
                String id = JOptionPane.showInputDialog("Enter ID:");
                String title = JOptionPane.showInputDialog("Enter Title:");
                String artist = JOptionPane.showInputDialog("Enter Artist:");
                String album = JOptionPane.showInputDialog("Enter Album:");
                String genre = JOptionPane.showInputDialog("Enter Genre:");
                String duration = JOptionPane.showInputDialog("Enter Duration (seconds):");

                MusicRecord record = new MusicRecord(
                        Integer.parseInt(id), title, artist, album, genre, Integer.parseInt(duration)
                );
                dbManager.addRecord(record);
                loadTable();
                JOptionPane.showMessageDialog(this, "Record added successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error adding record!");
            }
        });

        btnSearch.addActionListener(e -> {
            String fieldName = JOptionPane.showInputDialog("Enter field name (id/title/artist/album/genre/duration):");
            String value = JOptionPane.showInputDialog("Enter value:");
            try {
                List<MusicRecord> results = dbManager.searchByField(fieldName, value);
                if (results.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "No records found!");
                } else {
                    loadTable(results);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error searching records!");
            }
        });

        btnBackup.addActionListener(e -> {
            String backupPath = JOptionPane.showInputDialog("Enter backup file path:");
            try {
                dbManager.backupDatabase(backupPath);
                JOptionPane.showMessageDialog(this, "Backup created successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error creating backup!");
            }
        });

        btnRestore.addActionListener(e -> {
            String backupPath = JOptionPane.showInputDialog("Enter backup file path:");
            try {
                dbManager.restoreDatabase(backupPath);
                loadTable();
                JOptionPane.showMessageDialog(this, "Database restored successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error restoring database!");
            }
        });

        btnClear.addActionListener(e -> {
            try {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear the database?", "Confirm Clear", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    dbManager.clearDatabase();
                    loadTable();
                    JOptionPane.showMessageDialog(this, "Database cleared!");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error clearing database!");
            }
        });

        btnDeleteKey.addActionListener(e -> {
            try {
                String id = JOptionPane.showInputDialog("Enter ID to delete:");
                dbManager.deleteRecordByKey(Integer.parseInt(id));
                loadTable();
                JOptionPane.showMessageDialog(this, "Record deleted successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting record!");
            }
        });

        btnDeleteField.addActionListener(e -> {
            try {
                String fieldName = JOptionPane.showInputDialog("Enter field name (title/artist/album/genre):");
                String value = JOptionPane.showInputDialog("Enter value to delete:");
                dbManager.deleteRecordByField(fieldName, value);
                loadTable();
                JOptionPane.showMessageDialog(this, "Record(s) deleted successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error deleting record(s)!");
            }
        });

        btnDeleteDB.addActionListener(e -> {
            try {
                int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete the database file?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    Files.deleteIfExists(Paths.get("music_database.json"));
                    tableModel.setRowCount(0);
                    JOptionPane.showMessageDialog(this, "Database file deleted successfully!");
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error deleting database file!");
            }
        });

        btnExport.addActionListener(e -> {
            String filePath = JOptionPane.showInputDialog("Enter the file path to save the Excel file:");
            if (filePath != null && !filePath.trim().isEmpty()) {
                try (Workbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Music Database");
                    Row headerRow = sheet.createRow(0);
                    for (int i = 0; i < tableModel.getColumnCount(); i++) {
                        Cell cell = headerRow.createCell(i);
                        cell.setCellValue(tableModel.getColumnName(i));
                    }

                    List<MusicRecord> records = dbManager.openDatabase();
                    for (int i = 0; i < records.size(); i++) {
                        MusicRecord record = records.get(i);
                        Row row = sheet.createRow(i + 1);

                        row.createCell(0).setCellValue(record.getId());
                        row.createCell(1).setCellValue(record.getTitle());
                        row.createCell(2).setCellValue(record.getArtist());
                        row.createCell(3).setCellValue(record.getAlbum());
                        row.createCell(4).setCellValue(record.getGenre());
                        row.createCell(5).setCellValue(record.getDuration());
                    }

                    try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                        workbook.write(fileOut);
                    }

                    JOptionPane.showMessageDialog(this, "Database exported to Excel successfully!");
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Error exporting to Excel!");
                }
            }
        });

        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                Object newValue = tableModel.getValueAt(row, column);

                try {
                    List<MusicRecord> records = dbManager.openDatabase();
                    MusicRecord record = records.get(row);

                    switch (column) {
                        case 0 -> { 
                            try {
                                int newId = Integer.parseInt(newValue.toString());
                                if (records.stream().anyMatch(r -> r.getId() == newId)) {
                                    JOptionPane.showMessageDialog(this, "ID уже существует. Изменение отклонено.");
                                    tableModel.removeTableModelListener((TableModelListener) this);
                                    tableModel.setValueAt(record.getId(), row, column); 
                                    tableModel.addTableModelListener((TableModelListener) this);
                                } else {
                                    record.setId(newId);
                                }
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(this, "ID должен быть числом. Изменение отклонено.");
                                tableModel.removeTableModelListener((TableModelListener) this);
                                tableModel.setValueAt(record.getId(), row, column); 
                                tableModel.addTableModelListener((TableModelListener) this);
                            }
                        }

                        case 1 -> record.setTitle((String) newValue);
                        case 2 -> record.setArtist((String) newValue);
                        case 3 -> record.setAlbum((String) newValue);
                        case 4 -> record.setGenre((String) newValue);
                        case 5 -> record.setDuration((Integer) newValue);
                    }
                    dbManager.saveDatabase(records);
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Ошибка сохранения изменений: " + ex.getMessage());
                }
            }
        });

        btnShowAll.addActionListener(e -> loadTable());
    }

    private void loadTable() {
        try {
            List<MusicRecord> records = dbManager.openDatabase();
            loadTable(records);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error loading database!");
        }
    }

    private void loadTable(List<MusicRecord> records) {
        tableModel.setRowCount(0);
        for (MusicRecord record : records) {
            tableModel.addRow(new Object[]{
                    record.getId(), record.getTitle(), record.getArtist(),
                    record.getAlbum(), record.getGenre(), record.getDuration()
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MusicDatabaseGUI().setVisible(true));
    }
}
