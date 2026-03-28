package com.winwithpickr.twitter.sources

import com.winwithpickr.core.pipeline.PipelineContext
import com.winwithpickr.core.pipeline.PoolSource
import com.winwithpickr.twitter.XDataSource
import com.winwithpickr.twitter.models.XUser

class ReplySource(private val dataSource: XDataSource) : PoolSource<XUser> {
    override val name = "replies"
    override suspend fun fetch(context: PipelineContext): List<XUser> =
        dataSource.fetchReplies(context.targetId, context.maxEntries)
    override fun intersects(context: PipelineContext) = false // replies seed the pool
}
