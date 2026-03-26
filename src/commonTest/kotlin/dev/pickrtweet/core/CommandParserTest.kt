package dev.pickrtweet.core

import dev.pickrtweet.core.models.TriggerMode
import kotlin.test.*

class CommandParserTest {

    @Test
    fun `parse basic pick command`() {
        val cmd = CommandParser.parse("@winwithpickr pick", "winwithpickr")
        assertNotNull(cmd)
        assertEquals(1, cmd.winners)
        assertTrue(cmd.conditions.reply)
        assertEquals(TriggerMode.IMMEDIATE, cmd.triggerMode)
    }

    @Test
    fun `parse pick with winner count`() {
        val cmd = CommandParser.parse("@winwithpickr pick 3", "winwithpickr")
        assertNotNull(cmd)
        assertEquals(3, cmd.winners)
    }

    @Test
    fun `parse pick from retweets`() {
        val cmd = CommandParser.parse("@winwithpickr pick from retweets", "winwithpickr")
        assertNotNull(cmd)
        assertTrue(cmd.conditions.retweet)
        assertFalse(cmd.conditions.reply)
    }

    @Test
    fun `parse watch command`() {
        val cmd = CommandParser.parse("@winwithpickr watch", "winwithpickr")
        assertNotNull(cmd)
        assertEquals(TriggerMode.WATCH, cmd.triggerMode)
    }

    @Test
    fun `isTriggerText detects trigger phrases`() {
        assertTrue(CommandParser.isTriggerText("Time to pick a winner!"))
        assertTrue(CommandParser.isTriggerText("Giveaway over!"))
        assertFalse(CommandParser.isTriggerText("Thanks everyone!"))
    }

    @Test
    fun `parse followHost from natural phrases`() {
        val phrases = listOf(
            "@winwithpickr pick 7 from replies who follow me",
            "@winwithpickr pick from replies must follow",
            "@winwithpickr pick must be following",
            "@winwithpickr pick followers only",
            "@winwithpickr pick from replies following me",
        )
        for (phrase in phrases) {
            val cmd = CommandParser.parse(phrase, "winwithpickr")
            assertNotNull(cmd, "Expected followHost for: $phrase")
            assertTrue(cmd.conditions.followHost, "followHost should be true for: $phrase")
        }
    }

    @Test
    fun `returns null for unrelated text`() {
        assertNull(CommandParser.parse("Hello world", "winwithpickr"))
    }
}
