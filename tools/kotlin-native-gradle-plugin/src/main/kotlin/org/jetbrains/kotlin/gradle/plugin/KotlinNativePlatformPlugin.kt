package org.jetbrains.kotlin.gradle.plugin

import org.gradle.api.GradleException
import org.gradle.api.Named
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.jetbrains.kotlin.gradle.plugin.tasks.KonanCompileTask
import javax.inject.Inject

open class KotlinNativePlatformPlugin: KotlinPlatformImplementationPluginBase("native") {

    private val Project.konanMultiplatformTasks: Collection<KonanCompileTask>
        get() = tasks.withType(KonanCompileTask::class.java).filter { it.enableMultiplatform }

    override fun configurationsForCommonModuleDependency(project: Project) = emptyList<Configuration>()

    open class RequestedCommonSourceSet @Inject constructor(private val name: String): Named {
        override fun getName() = name
    }

    override fun addCommonSourceSetToPlatformSourceSet(commonSourceSet: Named, platformProject: Project) {
        val commonSourceSetName = commonSourceSet.name

        platformProject.konanMultiplatformTasks
            .filter { it.commonSourceSets.contains(commonSourceSetName) }
            .forEach { task: KonanCompileTask ->
                getKotlinSourceDirectorySetSafe(commonSourceSet)!!.srcDirs.forEach {
                    task.commonSrcDir(it)
                }
            }
    }

    override fun namedSourceSetsContainer(project: Project): NamedDomainObjectContainer<*> =
        project.container(RequestedCommonSourceSet::class.java).apply {
            project.konanMultiplatformTasks.forEach { task ->
                task.commonSourceSets.forEach { maybeCreate(it) }
            }
        }
}
