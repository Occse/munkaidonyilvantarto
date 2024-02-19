package com.niev.munkaidonyilvantartoalkalmazas;

import java.util.HashMap;
import java.util.Objects;

public class HourData {
    private String lunchStart;
    private String lunchEnd;
    private String workDay;
    private String worker;
    private String workStart;
    private String workEnd;
    private Object workedHours;

    public HourData(HashMap<String, Object> map) {
        this.lunchStart = String.valueOf(map.get("lunchStart"));
        this.lunchEnd = String.valueOf(map.get("lunchEnd"));
        this.workDay = String.valueOf(map.get("workDay"));
        this.worker = String.valueOf(map.get("worker"));
        this.workStart = String.valueOf(map.get("workStart"));
        this.workEnd = String.valueOf(map.get("workEnd"));
        this.workedHours = map.get("workedHours");
    }

    public HourData(String lunchStart, String lunchEnd, String workDay, String worker, String workStart, String workEnd, long workedHours) {
        this.lunchStart = lunchStart;
        this.lunchEnd = lunchEnd;
        this.workDay = workDay;
        this.worker = worker;
        this.workStart = workStart;
        this.workEnd = workEnd;
        this.workedHours = workedHours;
    }

    public String getLunchStart() {
        return lunchStart;
    }

    public void setLunchStart(String lunchStart) {
        this.lunchStart = lunchStart;
    }

    public String getLunchEnd() {
        return lunchEnd;
    }

    public void setLunchEnd(String lunchEnd) {
        this.lunchEnd = lunchEnd;
    }

    public String getWorkDay(String type) {
        switch (type) {
            case "full":
                return workDay;
            case "year":
                return workDay.split("/")[0];
                case "month":
                return workDay.split("/")[1];
                case "day":
                return workDay.split("/")[2];
        }
        return workDay;
    }

    public void setWorkDay(String workDay) {
        this.workDay = workDay;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    public String getWorkStart() {
        return workStart;
    }

    public void setWorkStart(String workStart) {
        this.workStart = workStart;
    }

    public String getWorkEnd() {
        return workEnd;
    }

    public void setWorkEnd(String workEnd) {
        this.workEnd = workEnd;
    }

    public Object getWorkedHours() {
        return workedHours;
    }

    public void setWorkedHours(Object workedHours) {
        this.workedHours = workedHours;
    }
}
