package com.netply.zero.status;

public class StatusStringBuilder {
    private StringBuilder stringBuilder = new StringBuilder();


    public StatusStringBuilder(String header) {
        stringBuilder.append(header);
    }

    public void appendStatus(String description, boolean status) {
        stringBuilder.append("\n");
        stringBuilder.append(description).append(": ").append(getFriendlyStatus(status));
    }

    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    private String getFriendlyStatus(boolean status) {
        return status ? "UP" : "DOWN";
    }
}
