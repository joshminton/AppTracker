package com.example.drawtest;

import androidx.room.Database;
import androidx.room.RoomDatabase;

//@Database(entities = {TrackedApp.class}, version = 1)
public abstract class TrackedAppDatabase extends RoomDatabase {
    public abstract TrackedAppDao trackedAppDaoDao();
}