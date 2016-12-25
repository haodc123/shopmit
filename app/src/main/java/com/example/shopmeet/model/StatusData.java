package com.example.shopmeet.model;

import java.io.Serializable;

public class StatusData implements Serializable {
    public static int STATUS_DO_LIKE = 1;
    public static int STATUS_DO_UNLIKE = 0;

    String status_id, staff_id, staff_fname, staff_lname, staff_avatar, status_created, status_modified, status_content;
    int isDisplay = 1;
    int numLike, numComment, isLike;

    public StatusData() {
    }

    public StatusData(String status_id, String staff_id, String staff_fname, String staff_lname,
                      String staff_avatar, String status_created, String status_modified, String status_content, int isDisplay,
                      int numLike, int numComment, int isLike) {
        this.status_id = status_id;
        this.staff_id = staff_id;
        this.staff_fname = staff_fname;
        this.staff_lname = staff_lname;
        this.staff_avatar = staff_avatar;
        this.status_created = status_created;
        this.status_modified = status_modified;
        this.status_content = status_content;
        this.isDisplay = isDisplay;
        this.numLike = numLike;
        this.numComment = numComment;
        this.isLike = isLike;
    }

    public String getStatus_id() {
        return status_id;
    }

    public void setStatus_id(String status_id) {
        this.status_id = status_id;
    }

    public String getStatus_created() {
        return status_created;
    }

    public void setStatus_created(String status_created) {
        this.status_created = status_created;
    }

    public String getStatus_modified() {
        return status_modified;
    }

    public void setStatus_modified(String status_modified) {
        this.status_modified = status_modified;
    }

    public String getStatus_content() {
        return status_content;
    }

    public void setStatus_content(String status_content) {
        this.status_content = status_content;
    }

    public int getNumLike() {
        return numLike;
    }

    public void setNumLike(int numLike) {
        this.numLike = numLike;
    }

    public int getNumComment() {
        return numComment;
    }

    public void setNumComment(int numComment) {
        this.numComment = numComment;
    }

    public int getIsLike() {
        return isLike;
    }

    public void setIsLike(int isLike) {
        this.isLike = isLike;
    }

    public String getStaff_id() {
        return staff_id;
    }

    public void setStaff_id(String staff_id) {
        this.staff_id = staff_id;
    }

    public String getStaff_fname() {
        return staff_fname;
    }

    public void setStaff_fname(String staff_fname) {
        this.staff_fname = staff_fname;
    }

    public String getStaff_lname() {
        return staff_lname;
    }

    public void setStaff_lname(String staff_lname) {
        this.staff_lname = staff_lname;
    }

    public String getStaff_avatar() {
        return staff_avatar;
    }

    public void setStaff_avatar(String staff_avatar) {
        this.staff_avatar = staff_avatar;
    }

    public int getIsDisplay() {
        return isDisplay;
    }

    public void setIsDisplay(int isDisplay) {
        this.isDisplay = isDisplay;
    }
}