package com.winwithpickr.twitter.filters

import com.winwithpickr.core.pipeline.PipelineContext
import com.winwithpickr.core.pipeline.PoolFilter
import com.winwithpickr.twitter.models.XUser

class HashtagFilter(private val hashtag: String) : PoolFilter<XUser> {
    override val name = "hashtag_filter"

    private val tagRegex = Regex("""#${Regex.escape(hashtag)}""", RegexOption.IGNORE_CASE)

    override suspend fun apply(candidates: MutableMap<String, XUser>, context: PipelineContext) {
        candidates.entries.removeAll { (_, user) ->
            user.replyText?.let { tagRegex.containsMatchIn(it) } != true
        }
    }
}
