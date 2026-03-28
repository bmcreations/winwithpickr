package com.winwithpickr.twitter.sources

import com.winwithpickr.core.pipeline.PipelineContext
import com.winwithpickr.core.pipeline.PoolSource
import com.winwithpickr.twitter.XDataSource
import com.winwithpickr.twitter.models.EntryConditions
import com.winwithpickr.twitter.models.XUser

class RetweetSource(
    private val dataSource: XDataSource,
    private val conditions: EntryConditions,
) : PoolSource<XUser> {
    override val name = "retweets"
    override suspend fun fetch(context: PipelineContext): List<XUser> =
        dataSource.fetchRetweeters(context.targetId, context.maxEntries)
    override fun intersects(context: PipelineContext) = conditions.reply
}
