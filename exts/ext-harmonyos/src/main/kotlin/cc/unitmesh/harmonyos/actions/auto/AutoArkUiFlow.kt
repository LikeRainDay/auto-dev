package cc.unitmesh.harmonyos.actions.auto

import cc.unitmesh.devti.AutoDevBundle
import cc.unitmesh.devti.flow.TaskFlow
import cc.unitmesh.devti.gui.chat.ChatCodingPanel
import cc.unitmesh.devti.llms.LLMProvider
import cc.unitmesh.devti.template.GENIUS_HARMONYOS
import cc.unitmesh.devti.template.TemplateRender
import kotlinx.coroutines.runBlocking

class AutoArkUiFlow(val panel: ChatCodingPanel, val llm: LLMProvider, val context: AutoArkUiContext) :
    TaskFlow<String> {
    override fun clarify(): String {
        val stepOnePrompt = generateStepOnePrompt(context)

        panel.addMessage(stepOnePrompt, true, stepOnePrompt)
        panel.addMessage(AutoDevBundle.message("autodev.loading"))

        return runBlocking {
            val prompt = llm.stream(stepOnePrompt, "")
            return@runBlocking panel.updateMessage(prompt)
        }
    }

    private fun generateStepOnePrompt(context: AutoArkUiContext): String {
        val templateRender = TemplateRender(GENIUS_HARMONYOS)
        val template = templateRender.getTemplate("arkui-clarify.vm")

        templateRender.context = context

        val prompter = templateRender.renderTemplate(template)
        return prompter
    }


    override fun design(context: Any): List<String> {
        val componentList = context as List<String>
        val stepTwoPrompt = generateStepTwoPrompt(componentList)

        panel.addMessage(stepTwoPrompt, true, stepTwoPrompt)
        panel.addMessage(AutoDevBundle.message("autodev.loading"))

        return runBlocking {
            val prompt = llm.stream(stepTwoPrompt, "", false)
            return@runBlocking panel.updateMessage(prompt)
        }.let { listOf(it) }
    }

    private fun generateStepTwoPrompt(selectedComponents: List<String>): String {
        val templateRender = TemplateRender(GENIUS_HARMONYOS)
        val template = templateRender.getTemplate("arkui-design.vm")

        context.elements = selectedComponents.mapNotNull {
            ArkUiLayoutType.tryFormat(it) ?: ArkUiComponentType.tryFormat(it)
        }

        templateRender.context = context

        val prompter = templateRender.renderTemplate(template)
        return prompter
    }
}