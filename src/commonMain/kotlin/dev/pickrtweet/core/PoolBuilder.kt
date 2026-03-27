package dev.pickrtweet.core

import dev.pickrtweet.core.models.EntryConditions
import dev.pickrtweet.core.models.PoolResult
import dev.pickrtweet.core.models.TierConfig
import dev.pickrtweet.core.models.XUser
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days

interface PoolDataSource {
    suspend fun fetchReplies(tweetId: String, maxResults: Int): List<XUser>
    suspend fun fetchRetweeters(tweetId: String, maxResults: Int): List<XUser>
    suspend fun buildFollowerSet(hostId: String): Pair<Set<String>, Boolean>
}

interface PoolAuditLog {
    suspend fun logStep(giveawayId: String, step: String, entryCount: Int? = null, detail: Any? = null)
}

class PoolBuilder(
    private val dataSource: PoolDataSource,
    private val auditLog: PoolAuditLog? = null,
) {

    suspend fun build(
        parentTweetId: String,
        hostXId: String,
        conditions: EntryConditions,
        tierConfig: TierConfig,
        giveawayId: String,
        excludeHandles: Set<String> = emptySet(),
    ): PoolResult {
        val maxEntries = tierConfig.maxEntries
        val candidates = mutableMapOf<String, XUser>()
        var followHostPartial = false

        // 1. Fetch reply authors (always on — reply is baseline condition)
        if (conditions.reply) {
            val replyUsers = dataSource.fetchReplies(parentTweetId, maxEntries)
            for (u in replyUsers) {
                if (u.id != hostXId) candidates[u.id] = u
            }
            auditLog?.logStep(giveawayId, "fetch_replies", candidates.size)
        }

        // 2. Intersect with retweeters if required
        if (conditions.retweet) {
            val retweeters = dataSource.fetchRetweeters(parentTweetId, maxEntries)
            val rtIds = retweeters.associateBy { it.id }
            auditLog?.logStep(giveawayId, "fetch_retweets", retweeters.size)

            if (conditions.reply) {
                // Intersect: keep only users who replied AND retweeted
                candidates.keys.retainAll(rtIds.keys)
            } else {
                // Retweet-only pool
                for (u in retweeters) {
                    if (u.id != hostXId) candidates[u.id] = u
                }
            }
        }

        // 3. Follower check (Pro+ only, enforced upstream but double-check)
        if (conditions.followHost && tierConfig.followerCheck) {
            val (followerSet, isPartial) = dataSource.buildFollowerSet(hostXId)
            followHostPartial = isPartial
            candidates.keys.retainAll(followerSet)
            auditLog?.logStep(
                giveawayId, "follower_filter",
                candidates.size,
                "partial=$isPartial, followerSetSize=${followerSet.size}"
            )
        }

        // 4. Required hashtag filter (all tiers)
        if (conditions.requiredHashtag != null) {
            val tag = "#${conditions.requiredHashtag!!.lowercase()}"
            candidates.entries.removeAll { (_, user) ->
                user.replyText?.lowercase()?.contains(tag) != true
            }
            auditLog?.logStep(giveawayId, "hashtag_filter", candidates.size, "required=$tag")
        }

        // 5. Min tags filter (all tiers)
        if (conditions.minTags > 0) {
            val mentionRegex = Regex("""@(\w+)""")
            val excluded = excludeHandles.map { it.lowercase() }.toSet()
            candidates.entries.removeAll { (_, user) ->
                val text = user.replyText ?: return@removeAll true
                val tagCount = mentionRegex.findAll(text)
                    .map { it.groupValues[1].lowercase() }
                    .filter { it !in excluded }
                    .distinct()
                    .count()
                tagCount < conditions.minTags
            }
            auditLog?.logStep(giveawayId, "min_tags_filter", candidates.size, "minTags=${conditions.minTags}")
        }

        // 6. Fraud filter (Business only)
        if (tierConfig.fraudFilter) {
            val minAge = conditions.minAccountAgeDays.takeIf { it > 0 } ?: tierConfig.minAccountAgeDays
            val minFol = conditions.minFollowers.takeIf { it > 0 } ?: tierConfig.minFollowers
            if (minAge > 0 || minFol > 0) {
                val cutoff = Clock.System.now().minus(minAge.days)
                candidates.entries.removeAll { (_, user) ->
                    val tooYoung = minAge > 0 && user.createdAt != null &&
                        Instant.parse(user.createdAt!!) > cutoff
                    val tooFewFollowers = minFol > 0 &&
                        (user.publicMetrics?.followersCount ?: 0) < minFol
                    tooYoung || tooFewFollowers
                }
                auditLog?.logStep(
                    giveawayId, "fraud_filter", candidates.size,
                    "minAge=${minAge}d, minFollowers=$minFol"
                )
            }
        }

        // 7. Cap at tier maxEntries
        val pool = if (candidates.size > maxEntries) {
            candidates.values.take(maxEntries)
        } else {
            candidates.values.toList()
        }

        auditLog?.logStep(giveawayId, "pool_final", pool.size)
        return PoolResult(users = pool, followHostPartial = followHostPartial)
    }
}
