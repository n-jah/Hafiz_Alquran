package com.najah.qurantest.repository

import com.najah.qurantest.database.QuranDao
import com.najah.qurantest.model.QuranVerse

class QuranRepository(private val quranDao: QuranDao) {

    suspend fun getVersesByJuzz(juzz: Int): List<QuranVerse> {
        return quranDao.getVersesByJuzz(juzz)
    }

}
