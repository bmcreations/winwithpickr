package com.winwithpickr.twitter.filters

import com.winwithpickr.core.pipeline.PipelineContext
import com.winwithpickr.core.pipeline.PoolFilter
import com.winwithpickr.twitter.XDataSource
import com.winwithpickr.twitter.models.XUser

class FollowHostFilter(private val dataSource: XDataSource) : PoolFilter<XUser> {
    override val name = "follower_filter"

    /** Set after apply() — true if the follower set was rate-limited. */
    var isPartial: Boolean = false
        private set

    override suspend fun apply(candidates: MutableMap<String, XUser>, context: PipelineContext) {
        val (followingCandidates, partial) = dataSource.checkFollowers(context.hostId, candidates.keys.toSet())
        isPartial = partial
        candidates.keys.retainAll(followingCandidates)
    }
}
