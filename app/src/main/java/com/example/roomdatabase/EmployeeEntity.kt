package com.example.roomdatabase

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("tabel-karyawan")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name:String = "",
    @ColumnInfo(name = "email-id")
    val email: String = ""
)