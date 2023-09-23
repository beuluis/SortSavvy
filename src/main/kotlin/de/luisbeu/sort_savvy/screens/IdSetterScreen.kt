package de.luisbeu.sort_savvy.screens

import com.mojang.blaze3d.systems.RenderSystem
import de.luisbeu.sort_savvy.SortSavvy
import de.luisbeu.sort_savvy.network.IdSetterScreenHandler
import net.minecraft.client.gui.screen.ingame.HandledScreen
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.gui.widget.TextFieldWidget
import net.minecraft.client.render.GameRenderer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.player.PlayerInventory
import net.minecraft.screen.ScreenHandler
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.lwjgl.glfw.GLFW
import java.awt.Color
import kotlin.properties.Delegates

class IdSetterScreen(
    handler: ScreenHandler, playerInventory: PlayerInventory, title: Text
) : HandledScreen<ScreenHandler>(handler, playerInventory, title) {

    companion object {
        // The texture used for the background of the screen
        val texture = Identifier(SortSavvy.Constants.modId, "textures/gui/container/default_backdrop.png")
        // The margin between the edge of the screen and the content
        const val MARGIN = 7
        // The padding between elements
        const val PADDING = 5
        // The height of the header section
        const val HEADER_HEIGHT = 13
        // The height of the footer section
        const val FOOTER_HEIGHT = 30
    }

    // The text field used to enter the ID
    private lateinit var textField: TextFieldWidget
    // The button used to save the ID
    private lateinit var safeButton: ButtonWidget

    // The text displayed below the text field
    private var labelText: Text? = null
    // The x position of the label
    private var labelX = MARGIN.toFloat()
    // The y position of the label
    private var labelY by Delegates.notNull<Float>()

    override fun init() {
        // Check if the handler is of the correct type
        if (handler !is IdSetterScreenHandler) {
            val msg = "Invalid screen handler type"
            SortSavvy.logger.error(msg)
            throw IllegalStateException(msg)
        }

        // Set the size of the screen
        backgroundWidth = 174
        backgroundHeight = 93

        // Set the position of the title
        titleX = MARGIN
        titleY = (HEADER_HEIGHT - textRenderer.fontHeight + 2) / 2

        // Calculate the size of the text field
        val textFieldWidth = backgroundWidth - 2 * MARGIN
        val textFieldHeight = textRenderer.fontHeight + 2 * PADDING

        // Calculate the size of the save button
        val buttonText = Text.translatable("gui.sort_savvy.save")
        val buttonWidth = textRenderer.getWidth(buttonText) + 2 * PADDING
        val buttonHeight = textRenderer.fontHeight + 2 * PADDING

        // Calculate the position of the label
        labelY = (backgroundHeight - (FOOTER_HEIGHT / 2) - (textRenderer.fontHeight / 2)).toFloat()

        // Initialize the text field
        textField = TextFieldWidget(
            textRenderer,
            // Position it in the middle of the inner texture body
            (width - textFieldWidth) / 2,
            (height - HEADER_HEIGHT - textFieldHeight) / 2,
            textFieldWidth,
            textFieldHeight,
            // Call the handler to get the buffered id
            Text.of("")
        ).apply {
            text = (handler as IdSetterScreenHandler).id
            setTextFieldFocused(true)
        }

        // Register the text field as a UI element
        addDrawableChild(textField)

        // Initialize the save button
        safeButton = ButtonWidget(
            (width - backgroundWidth) / 2 + backgroundWidth - buttonWidth - MARGIN,
            (height - backgroundHeight) / 2 + backgroundHeight - (FOOTER_HEIGHT + buttonHeight) / 2,
            buttonWidth,
            buttonHeight,
            buttonText
        ) {
            // Set the ID and close the screen
            (handler as IdSetterScreenHandler).setId(textField.text)
            close()
        }

        // Register the save button as a UI element
        addDrawableChild(safeButton)

        // Set the label text if there is a direction to scan
        val toScanDirection = (handler as IdSetterScreenHandler).directionToScan
        if (toScanDirection != "") {
            labelText = Text.translatable("gui.sort_savvy.toScanDirectoryHelper", toScanDirection)
        }

        // Call super last to make sure all our stuff is used
        super.init()
    }

    override fun drawBackground(matrices: MatrixStack?, delta: Float, mouseX: Int, mouseY: Int) {
        // Set the shader to draw textures
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        // Set the color to white
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        // Set the texture to the background texture
        RenderSystem.setShaderTexture(0, texture)

        // Calculate the position of the screen
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2

        // Draw the background texture
        drawTexture(
            matrices, x, y, 0, 0, backgroundWidth, backgroundHeight
        )
    }


    // Call all render functions of the registered ui elements
    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        textField.render(matrices, mouseX, mouseY, delta)

        super.render(matrices, mouseX, mouseY, delta)
    }

    // Check if the mouse is over a label
    private fun isMouseOverLabel(mouseX: Int, mouseY: Int, x: Float, y: Float, label: Text): Boolean {
        val labelWidth = textRenderer.getWidth(label)
        val labelHeight = textRenderer.fontHeight

        // Convert mouseX and mouseY to be relative to the IdSetterScreen element
        val relativeMouseX = mouseX - (width - backgroundWidth) / 2
        val relativeMouseY = mouseY - (height - backgroundHeight) / 2

        return relativeMouseX >= x && relativeMouseX < x + labelWidth && relativeMouseY >= y && relativeMouseY < y + labelHeight
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        // Handle all key presses
        if (textField.keyPressed(keyCode, scanCode, modifiers)) {
            return true
        }
        if (keyCode == GLFW.GLFW_KEY_E) {
            // Override the close event when e is pressed
            return false
        }
        return super.keyPressed(keyCode, scanCode, modifiers)
    }

    override fun charTyped(chr: Char, modifiers: Int): Boolean {
        // Handle all typed chars
        if (textField.charTyped(chr, modifiers)) {
            return true
        }
        return super.charTyped(chr, modifiers)
    }

    private fun renderRelativeTooltip(matrices: MatrixStack?, tooltipText: Text, mouseX: Int, mouseY: Int) {
        // Convert mouseX and mouseY to be relative to the IdSetterScreen element
        val relativeMouseX = mouseX - (width - backgroundWidth) / 2
        val relativeMouseY = mouseY - (height - backgroundHeight) / 2

        renderTooltip(matrices, tooltipText, relativeMouseX, relativeMouseY)
    }

    override fun drawForeground(matrices: MatrixStack?, mouseX: Int, mouseY: Int) {
        // The text to append to the title if it overflows
        val overflowAppend = Text.of("...")
        // The maximum width of the title before it overflows
        val maxWidth = backgroundWidth - textRenderer.getWidth(overflowAppend) - 2 * PADDING

        var truncatedTitle = title.string

        // Cut the title if it should overflow
        if (textRenderer.getWidth(title.string) > maxWidth) {
            truncatedTitle = textRenderer.trimToWidth(title.string, maxWidth) + "..."
        }

        // Draw the title
        textRenderer.draw(matrices, truncatedTitle, titleX.toFloat(), titleY.toFloat(), Color.WHITE.rgb)

        // Draw the label if there is one
        labelText?.let {
            textRenderer.draw(
                matrices,
                it,
                labelX,
                labelY,
                Color.BLACK.rgb
            )

            // Check if the mouse is over the label
            if (isMouseOverLabel(mouseX, mouseY, labelX, labelY, it)) {
                // If it is, show a tooltip
                val tooltipText = Text.translatable("gui.sort_savvy.toScanDirectoryHelper.tooltip")

                renderRelativeTooltip(matrices, tooltipText, mouseX, mouseY)
            }
        }
    }
}