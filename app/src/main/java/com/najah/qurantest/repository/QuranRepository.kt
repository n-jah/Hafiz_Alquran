package com.najah.qurantest.repository

import com.najah.qurantest.database.QuranDao
import com.najah.qurantest.model.QuranVerse

class QuranRepository(private val quranDao: QuranDao) {

    suspend fun getVersesByJuzz(juzz: Int): List<QuranVerse> {
        return quranDao.getVersesByJuzz(juzz)
    }


    // Fetch all Ayahs within a specific Juz
    suspend fun getVersesInJuz(jozz: Int): List<QuranVerse> {
        return quranDao.getVersesByJuz(jozz)
    }

    // Fetch all Surahs in a specific Juz
    suspend fun getSurahsInJuz(jozz: Int): List<Int> {
        return quranDao.getSurahNumbersByJuz(jozz)
    }

    // Fetch Ayahs from a specific Surah
    suspend fun getVersesInSurah(surahNo: Int): List<QuranVerse> {
        return quranDao.getVersesBySurah(surahNo)
    }

    // Fetch a sequential block of Ayahs starting from a given Ayah
    suspend fun getSequentialVerses(
        surahNo: Int,
        startAya: Int,
        numOfLines: Int
    ): List<QuranVerse> {
        return quranDao.getSequentialVerses(surahNo, startAya, numOfLines)
    }

    // Get the next Surah within the same Juz
    suspend fun getNextSurahInJuz(jozz: Int, currentSurah: Int): Int? {
        val surahs = quranDao.getSurahNumbersByJuz(jozz)
        val currentIndex = surahs.indexOf(currentSurah)
        return if (currentIndex != -1 && currentIndex + 1 < surahs.size) {
            surahs[currentIndex + 1]
        } else null
    }


}
