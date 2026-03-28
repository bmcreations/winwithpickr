package com.winwithpickr.twitter.sources

import com.winwithpickr.core.pipeline.PipelineContext
import com.winwithpickr.core.pipeline.PoolSource
import com.winwithpickr.twitter.XDataSource
import com.winwithpickr.twitter.models.EntryConditions
import com.winwithpickr.twitter.models.XUser

class QuoteTweetSource(
    private val dataSource: XDataSource,
    private val conditions: EntryConditions,
) : PoolSource<XUser> {
    override val name = "quotes"
    override suspend fun fetch(context: PipelineContext): List<XUser> =
        dataSource.fetchQuoteTweets(context.targetId, context.maxEntries)
    override fun intersects(context: PipelineContext) = conditions.reply || conditions.retweet
}
