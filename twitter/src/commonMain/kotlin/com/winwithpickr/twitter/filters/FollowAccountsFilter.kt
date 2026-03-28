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

    override suspend fun apply(candidates: MutableMap<String, XUser>, context: PipelineContext) {
        val accountIds = accounts.mapNotNull { dataSource.resolveHandle(it) }
        if (accountIds.isEmpty()) return

        val toRemove = mutableSetOf<String>()
        for ((userId, _) in candidates) {
            val following = dataSource.fetchFollowing(userId)
            if (!following.containsAll(accountIds)) toRemove.add(userId)
        }
        candidates.keys.removeAll(toRemove)
    }
}
