package com.najah.qurantest.database

import androidx.room.Dao
import androidx.room.Query
import com.najah.qurantest.model.QuranVerse
import com.najah.qurantest.model.RandomSurahInfo

@Dao
interface QuranDao {

    @Query("""
    SELECT * FROM quran 
    WHERE jozz = :juzz
    ORDER BY sura_no, aya_no
    """)
    suspend fun getVersesByJuzz(juzz: Int): List<QuranVerse>

    @Query("SELECT * FROM quran WHERE sura_no = :suraNo AND aya_no BETWEEN :startAya AND :endAya ORDER BY aya_no")
    suspend fun getVersesInRange(suraNo: Int, startAya: Int, endAya: Int): List<QuranVerse>



}
