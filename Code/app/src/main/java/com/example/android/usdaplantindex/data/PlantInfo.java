package com.example.android.usdaplantindex.data;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.io.Serializable;

@Entity(tableName = "plants")
public class PlantInfo implements Serializable {
    @NonNull
    @PrimaryKey
    public Integer id;

    public String Scientific_Name_x;
    public String Common_Name;
    public String Symbol;
    public String Group;
    public String Family;
    public String Duration;
    public String Growth_Habit;
    public String Native_Status;
    public String Category;
    public String xOrder;
    public String SubClass;
    public String Class;
    public String Kingdom;
    public String Species;
    public String Subspecies;
    public String State_and_Province;
}