package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.spec.IsolationMode
import io.kotest.extensions.spring.SpringExtension

class ProjectConfig : AbstractProjectConfig() {
    override val isolationMode = IsolationMode.InstancePerTest
    override val extensions = listOf(SpringExtension())
}
