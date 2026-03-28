package com.winwithpickr.twitter

import com.winwithpickr.twitter.models.XUser

/**
 * Platform-specific data source for Twitter/X API operations.
 * Implemented by XClient on the server side.
 */
interface XDataSource {
    suspend fun fetchReplies(tweetId: String, maxResults: Int): List<XUser>
    suspend fun fetchRetweeters(tweetId: String, maxResults: Int): List<XUser>
    suspend fun fetchQuoteTweets(tweetId: String, maxResults: Int): List<XUser>
    suspend fun buildFollowerSet(hostId: String): Pair<Set<String>, Boolean>

    /**
     * Check which candidates follow a given account. Pages through followers
     * and stops early once all candidates are found — avoids fetching the
     * entire follower list when the candidate pool is small relative to the
     * follower count. Returns (followers ∩ candidates, isPartial).
     */
    suspend fun checkFollowers(accountId: String, candidateIds: Set<String>): Pair<Set<String>, Boolean>
    suspend fun resolveHandle(handle: String): String?
}
