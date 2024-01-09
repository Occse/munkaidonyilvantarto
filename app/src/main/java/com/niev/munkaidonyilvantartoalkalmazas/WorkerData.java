package com.niev.munkaidonyilvantartoalkalmazas;

import java.util.HashMap;

public class WorkerData {
    private String id;
    private String userName;
    private String email;
    private String userAdo;
    private String userDegree;
    private String userBirthDate;
    private String userId;
    private String userLakcim;
    private String userTAJ;
    private String userMunkakor;

    public WorkerData() {
    }

    public WorkerData(HashMap<String, String> map) {
        this.id = map.get("id");
        this.userName = map.get("userName");
        this.email = map.get("email");
        this.userAdo = map.get("userAdo");
        this.userDegree = map.get("userDegree");
        this.userBirthDate = map.get("userBirthDate");
        this.userId = map.get("userId");
        this.userLakcim = map.get("userLakcim");
        this.userTAJ = map.get("userTAJ");
        this.userMunkakor = map.get("userMunkakor");
    }

    public WorkerData(String id, String userName, String email, String userAdo, String userDegree, String userBirthDate, String userId, String userLakcim, String userTAJ, String userMunkakor) {
        this.id = id;
        this.userName = userName;
        this.email = email;
        this.userAdo = userAdo;
        this.userDegree = userDegree;
        this.userBirthDate = userBirthDate;
        this.userId = userId;
        this.userLakcim = userLakcim;
        this.userTAJ = userTAJ;
        this.userMunkakor = userMunkakor;
    }

    public String getUserMunkakor() {
        return userMunkakor;
    }

    public void setUserMunkakor(String userMunkakor) {
        this.userMunkakor = userMunkakor;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserAdo() {
        return userAdo;
    }

    public void setUserAdo(String userAdo) {
        this.userAdo = userAdo;
    }

    public String getUserDegree() {
        return userDegree;
    }

    public void setUserDegree(String userDegree) {
        this.userDegree = userDegree;
    }

    public String getUserBirthDate() {
        return userBirthDate;
    }

    public void setUserBirthDate(String userBirthDate) {
        this.userBirthDate = userBirthDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserLakcim() {
        return userLakcim;
    }

    public void setUserLakcim(String userLakcim) {
        this.userLakcim = userLakcim;
    }

    public String getUserTAJ() {
        return userTAJ;
    }

    public void setUserTAJ(String userTAJ) {
        this.userTAJ = userTAJ;
    }

    public String getId() {
        return id;
    }
}
