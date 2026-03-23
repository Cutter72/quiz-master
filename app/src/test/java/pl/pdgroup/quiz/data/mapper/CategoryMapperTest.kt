package pl.pdgroup.quiz.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test

class CategoryMapperTest {

    @Test
    fun `getCategoryId returns correct id for valid categories`() {
        assertEquals(9, CategoryMapper.getCategoryId("General Knowledge"))
        assertEquals(17, CategoryMapper.getCategoryId("Science & Nature"))
        assertEquals(21, CategoryMapper.getCategoryId("Sports"))
        assertEquals(22, CategoryMapper.getCategoryId("Geography"))
        assertEquals(23, CategoryMapper.getCategoryId("History"))
        assertEquals(27, CategoryMapper.getCategoryId("Animals"))
    }

    @Test
    fun `getCategoryId returns default id for unknown category`() {
        assertEquals(9, CategoryMapper.getCategoryId("Unknown Category"))
        assertEquals(9, CategoryMapper.getCategoryId(""))
    }
}
