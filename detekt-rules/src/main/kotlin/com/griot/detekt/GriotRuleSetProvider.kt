package com.griot.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

/** Griot's own rules, enabled under `griot:` in `config/detekt/detekt.yml`. */
class GriotRuleSetProvider : RuleSetProvider {
    override val ruleSetId: String = "griot"

    override fun instance(config: Config): RuleSet = RuleSet(ruleSetId, listOf(HardcodedUiText(config)))
}
