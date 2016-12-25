package com.example.shopmeet.model;

import java.io.Serializable;

public class NoteData implements Serializable {
    String note_id, staff_id, staff_fname, staff_lname, staff_avatar, note_created, note_modified, note_content;
    int isDisplay = 1;

    public NoteData() {
    }

    public NoteData(String note_id, String staff_id, String staff_fname, String staff_lname,
                    String staff_avatar, String note_created, String note_modified, String note_content, int isDisplay) {
        this.note_id = note_id;
        this.staff_id = staff_id;
        this.staff_fname = staff_fname;
        this.staff_lname = staff_lname;
        this.staff_avatar = staff_avatar;
        this.note_created = note_created;
        this.note_modified = note_modified;
        this.note_content = note_content;
        this.isDisplay = isDisplay;
    }

    public String getNote_modified() {
        return note_modified;
    }

    public void setNote_modified(String note_modified) {
        this.note_modified = note_modified;
    }

    public String getNote_id() {
        return note_id;
    }

    public void setNote_id(String note_id) {
        this.note_id = note_id;
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

    public String getNote_created() {
        return note_created;
    }

    public void setNote_created(String note_created) {
        this.note_created = note_created;
    }

    public String getNote_content() {
        return note_content;
    }

    public void setNote_content(String note_content) {
        this.note_content = note_content;
    }

    public int getIsDisplay() {
        return isDisplay;
    }

    public void setIsDisplay(int isDisplay) {
        this.isDisplay = isDisplay;
    }
}