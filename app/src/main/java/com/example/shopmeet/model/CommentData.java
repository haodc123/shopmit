package com.example.shopmeet.model;

import java.io.Serializable;

public class CommentData implements Serializable {
    String comment_id, staff_id, staff_fname, staff_lname, staff_avatar, comment_created, comment_content, comment_modified;
    int isDisplay = 1;

    public CommentData() {
    }

    public CommentData(String comment_id, String staff_id, String staff_fname, String staff_lname,
                       String staff_avatar, String comment_created, String comment_content, String comment_modified, int isDisplay) {
        this.comment_id = comment_id;
        this.staff_id = staff_id;
        this.staff_fname = staff_fname;
        this.staff_lname = staff_lname;
        this.staff_avatar = staff_avatar;
        this.comment_created = comment_created;
        this.comment_content = comment_content;
        this.comment_modified = comment_modified;
        this.isDisplay = isDisplay;
    }

    public String getComment_created() {
        return comment_created;
    }

    public String getComment_modified() {
        return comment_modified;
    }

    public void setComment_modified(String comment_modified) {
        this.comment_modified = comment_modified;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
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

    public String getcomment_created() {
        return comment_created;
    }

    public void setComment_created(String comment_created) {
        this.comment_created = comment_created;
    }

    public String getComment_content() {
        return comment_content;
    }

    public void setComment_content(String comment_content) {
        this.comment_content = comment_content;
    }

    public int getIsDisplay() {
        return isDisplay;
    }

    public void setIsDisplay(int isDisplay) {
        this.isDisplay = isDisplay;
    }
}