package com.winwithpickr.twitter.filters

import com.winwithpickr.core.pipeline.PipelineContext
import com.winwithpickr.core.pipeline.PoolFilter
import com.winwithpickr.twitter.XDataSource
import com.winwithpickr.twitter.models.XUser

class FollowAccountsFilter(
    private val dataSource: XDataSource,
    private val accounts: List<String>,
) : PoolFilter<XUser> {
    override val name = "follow_accounts_filter"

    /** True if any account's follower set was rate-limited (partial). */
    var isPartial: Boolean = false
        private set

    override suspend fun apply(candidates: MutableMap<String, XUser>, context: PipelineContext) {
        val accountIds = accounts.mapNotNull { dataSource.resolveHandle(it) }
        if (accountIds.isEmpty()) return

        // Check each required account's followers against candidates with early exit.
        // Stops paginating once all candidates are found — avoids fetching entire follower list.
        for (accountId in accountIds) {
            val (followingCandidates, partial) = dataSource.checkFollowers(accountId, candidates.keys.toSet())
            if (partial) isPartial = true
            candidates.keys.retainAll(followingCandidates)
        }
    }
}
