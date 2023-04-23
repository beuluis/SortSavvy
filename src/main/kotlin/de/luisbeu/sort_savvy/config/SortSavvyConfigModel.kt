package de.luisbeu.sort_savvy.config

import io.wispforest.owo.config.annotation.Config
import io.wispforest.owo.config.annotation.RangeConstraint
import io.wispforest.owo.config.annotation.RegexConstraint
import io.wispforest.owo.config.annotation.RestartRequired

@Config(name = "sort-savvy", wrapperName = "SortSavvyConfig")
class SortSavvyConfigModel {
    @RestartRequired()
    @RangeConstraint(min = 0.0, max = 1023.0)
    var restServerPort = 8082

    @RestartRequired()
    @RegexConstraint("[a-z]{1,10}")
    var restServerBearerToken = "TEST"
}