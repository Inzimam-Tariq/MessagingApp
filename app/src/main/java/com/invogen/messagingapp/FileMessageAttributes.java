package com.invogen.messagingapp;

public class FileMessageAttributes {
    private String fileName;
    private String filePath;
    private float fileSize;

    public FileMessageAttributes() {
    }

    public FileMessageAttributes(String fileName, String filePath, float fileSize) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }


    // Getter Methods

    public String getFileName() {
        return fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public float getFileSize() {
        return fileSize;
    }

    // Setter Methods

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileSize(float fileSize) {
        this.fileSize = fileSize;
    }
}
