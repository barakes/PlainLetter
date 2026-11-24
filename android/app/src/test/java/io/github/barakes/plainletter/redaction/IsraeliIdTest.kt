package io.github.barakes.plainletter.redaction

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class IsraeliIdTest {

    @Test
    fun aCorrectCheckDigitIsValid() {
        assertTrue(IsraeliId.isValid("000000018"))
        assertTrue(IsraeliId.isValid("123456782"))
    }

    @Test
    fun aWrongCheckDigitIsNotValid() {
        assertFalse(IsraeliId.isValid("123456789"))
        assertFalse(IsraeliId.isValid("000000019"))
    }

    @Test
    fun anIdWrittenWithoutItsLeadingZerosIsValid() {
        assertTrue(IsraeliId.isValid("0000018"))
    }

    @Test
    fun onlyDigitsOfTheRightLengthCanBeAnId() {
        assertFalse(IsraeliId.isValid("12345678a"))
        assertFalse(IsraeliId.isValid("1234567890"))
    }
}
