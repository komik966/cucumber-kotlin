package net.lagerwey.plugins.cucumber.kotlin

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.search.UsageSearchContext
import com.intellij.psi.util.CreateClassUtil
import net.lagerwey.plugins.cucumber.kotlin.steps.KotlinStepDefinition
import org.jetbrains.kotlin.idea.KotlinFileType
import org.jetbrains.kotlin.idea.references.KtInvokeFunctionReference
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.plugins.cucumber.AbstractStepDefinitionCreator
import org.jetbrains.plugins.cucumber.BDDFrameworkType
import org.jetbrains.plugins.cucumber.StepDefinitionCreator
import org.jetbrains.plugins.cucumber.psi.GherkinFile
import org.jetbrains.plugins.cucumber.psi.GherkinStep
import org.jetbrains.plugins.cucumber.steps.AbstractCucumberExtension
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition

class KotlinCucumberExtension : AbstractCucumberExtension() {

    override fun getStepDefinitionCreator(): StepDefinitionCreator = throw NotImplementedError()

    override fun isStepLikeFile(child: PsiElement, parent: PsiElement) = child is KtFile

    override fun isWritableStepLikeFile(child: PsiElement, parent: PsiElement) = isStepLikeFile(child, parent)

    override fun getStepFileType() = BDDFrameworkType(KotlinFileType.INSTANCE)

    override fun getGlues(file: GherkinFile, jGluesFromOtherFiles: MutableSet<String>?) = emptyList<String>()

    override fun getStepDefinitionContainers(featureFile: GherkinFile): MutableCollection<out PsiFile> = mutableListOf()

    override fun loadStepsFor(featureFile: PsiFile?, module: Module): MutableList<AbstractStepDefinition> {
        val result = mutableListOf<AbstractStepDefinition>()
        val dependenciesScope = module.moduleContentWithDependenciesScope
        val kotlinFiles = GlobalSearchScope.getScopeRestrictedByFileTypes(dependenciesScope, KotlinFileType.INSTANCE)
        for (method in arrayOf("Given", "And", "Then", "But", "When", "Ale", "Gdy", "I", "Jeśli", "Jeżeli", "Kiedy", "Mając", "Oraz", "Wtedy", "Zakładając", "Zakładającże")) {
            val occurrencesProcessor: (PsiElement, Int) -> Boolean = { element, _ ->
                val parent = element.parent
                if (parent != null) {
                    val references = parent.references
                    for (ref in references) {
                        if (ref is KtInvokeFunctionReference) {
                            result.add(KotlinStepDefinition(parent))
                            break
                        }
                    }
                }
                true
            }
            PsiSearchHelper.SERVICE.getInstance(module.project).processElementsWithWord(occurrencesProcessor, kotlinFiles, method, UsageSearchContext.IN_CODE, true)
        }
        return result
    }

}
