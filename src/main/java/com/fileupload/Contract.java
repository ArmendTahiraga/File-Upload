package com.fileupload;

import java.io.File;
import java.time.LocalDate;

public class Contract {
    private int id;
    private int userId;
    private File file;
    private LocalDate date;

    public Contract(int id, int userId, File file, LocalDate date) {
        this.id = id;
        this.userId = userId;
        this.file = file;
        this.date = date;
    }

    public Contract(int userId, File file, LocalDate date) {
        this.userId = userId;
        this.file = file;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
