package com.najah.qurantest.model
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quran")

data class QuranVerse(
    @PrimaryKey val id: Int?,
    @ColumnInfo(name = "jozz") val jozz: Int?,
    @ColumnInfo(name = "sura_no") val suraNo: Int?,
    @ColumnInfo(name = "sura_name_en") val suraNameEn: String?,
    @ColumnInfo(name = "sura_name_ar") val suraNameAr: String?,
    @ColumnInfo(name = "page") val page: Int?,
    @ColumnInfo(name = "line_start") val lineStart: Int?,
    @ColumnInfo(name = "line_end") val lineEnd: Int?,
    @ColumnInfo(name = "aya_no") val ayaNo: Int?,
    @ColumnInfo(name = "aya_text") val ayaText: String?,
    @ColumnInfo(name = "aya_text_emlaey") val ayaTextEmlaey: String?
)
