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


    // Fetch all verses within a specific Juz
    @Query("SELECT * FROM quran WHERE jozz = :jozz ORDER BY sura_no, aya_no")
    suspend fun getVersesByJuz(jozz: Int): List<QuranVerse>

    // Fetch all Surah numbers in a specific Juz
    @Query("SELECT DISTINCT sura_no FROM quran WHERE jozz = :jozz ORDER BY sura_no")
    suspend fun getSurahNumbersByJuz(jozz: Int): List<Int>

    // Fetch all verses in a specific Surah
    @Query("SELECT * FROM quran WHERE sura_no = :suraNo ORDER BY aya_no")
    suspend fun getVersesBySurah(suraNo: Int): List<QuranVerse>

    // Fetch sequential verses starting from a specific Ayah
    @Query("""
        SELECT * FROM quran 
        WHERE sura_no = :suraNo AND aya_no >= :startAya 
        ORDER BY aya_no LIMIT :numOfLines
    """)
    suspend fun getSequentialVerses(
        suraNo: Int,
        startAya: Int,
        numOfLines: Int
    ): List<QuranVerse>

    // Fetch a specific Ayah by Surah and Ayah number
    @Query("SELECT * FROM quran WHERE sura_no = :suraNo AND aya_no = :ayaNo LIMIT 1")
    suspend fun getSpecificAyah(suraNo: Int, ayaNo: Int): QuranVerse?

    // Count the total number of Ayahs in a Surah
    @Query("SELECT COUNT(*) FROM quran WHERE sura_no = :suraNo")
    suspend fun countAyahsInSurah(suraNo: Int): Int

    // Count the total number of Ayahs in a specific Juz
    @Query("SELECT COUNT(*) FROM quran WHERE jozz = :jozz")
    suspend fun countAyahsInJuz(jozz: Int): Int

    // Fetch the next Surah in the same Juz
    @Query("""
        SELECT DISTINCT sura_no FROM quran 
        WHERE jozz = :jozz AND sura_no > :currentSurah 
        ORDER BY sura_no LIMIT 1
    """)
    suspend fun getNextSurahInJuz(jozz: Int, currentSurah: Int): Int?



}
