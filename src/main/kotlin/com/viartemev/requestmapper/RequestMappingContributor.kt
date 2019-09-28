package com.viartemev.requestmapper

import com.intellij.navigation.ChooseByNameContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.viartemev.requestmapper.annotations.MappingAnnotation.Companion.mappingAnnotation
import com.viartemev.requestmapper.annotations.MappingAnnotation.Companion.supportedAnnotations
import com.viartemev.requestmapper.utils.isMethodAnnotation

class RequestMappingContributor(private val annotationSearcher: (string: String, project: Project) -> Sequence<PsiAnnotation>, private var navigationItems: List<RequestMappingItem> = emptyList()) : ChooseByNameContributor {

    override fun getNames(project: Project, includeNonProjectItems: Boolean): Array<String> {
        navigationItems = supportedAnnotations
            .flatMap { annotation -> findRequestMappingItems(project, annotation) }

        return navigationItems
            .map { it.name }
            .distinct()
            .toTypedArray()
    }

    override fun getItemsByName(name: String, pattern: String, project: Project, includeNonProjectItems: Boolean): Array<NavigationItem> {
        val normalizedName = name.replace("\\s+".toRegex(), " ").trim()
        return navigationItems
            .filter { it.name == normalizedName }
            .toTypedArray()
    }

    private fun findRequestMappingItems(project: Project, annotationName: String): List<RequestMappingItem> {
        return annotationSearcher(annotationName, project)
            .asSequence()
            .filterNotNull()
            .filter { it.isMethodAnnotation() }
            .map { annotation -> mappingAnnotation(annotationName, annotation) }
            .flatMap { mappingAnnotation -> mappingAnnotation.values().asSequence() }
            .toList()
    }
}
