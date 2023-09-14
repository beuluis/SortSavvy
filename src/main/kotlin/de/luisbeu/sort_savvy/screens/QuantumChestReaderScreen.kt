package de.luisbeu.sort_savvy.screens

import com.mojang.blaze3d.systems.RenderSystem
import de.luisbeu.sort_savvy.network.QuantumInventoryReaderScreenHandler
import de.luisbeu.sort_savvy.util.SortSavvyConstants
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

class QuantumInventoryReaderScreen(
    handler: ScreenHandler, playerInventory: PlayerInventory, title: Text
) : HandledScreen<ScreenHandler>(handler, playerInventory, title) {

    companion object {
        val texture = Identifier(SortSavvyConstants.MOD_ID, "textures/gui/container/quantum_inventory_reader.png")
    }

    // Add all ui attributes to make them available
    private lateinit var textField: TextFieldWidget
    private lateinit var safeButton: ButtonWidget
    private val margin = 7 // All stuff where we need space to a border
    private val padding = 5 // All stuff where we need space to content
    private var isDirty = false

    // It`s important to do it in init and not during the constructor. Here some attributes are initialized that we need to access
    override fun init() {
        // Crash when the screen gets called by the wrong handler
        if (handler !is QuantumInventoryReaderScreenHandler) {
            throw IllegalStateException("Invalid screen handler type")
        }

        backgroundWidth = 174
        backgroundHeight = 93

        val headerHeight = 13
        val footerHeight = 30

        titleX = margin
        titleY = (headerHeight - textRenderer.fontHeight + 2) / 2 // + 2 just looks nicer when the font goes more to the bottom

        val textFieldWidth = backgroundWidth - 2 * margin
        val textFieldHeight = textRenderer.fontHeight + 2 * padding

        val buttonText = Text.translatable("gui.sort_savvy.save")
        val buttonWidth = textRenderer.getWidth(buttonText) + 2 * padding
        val buttonHeight = textRenderer.fontHeight + 2 * padding

        // Initialize the text field
        textField = TextFieldWidget(
            textRenderer,
            // Position it in the middle of the inner texture body
            (width - textFieldWidth) / 2,
            (height - headerHeight - textFieldHeight) / 2,
            textFieldWidth,
            textFieldHeight,
            // Call the handler to get the buffered id
            Text.of("")
        )
        textField.text = (handler as QuantumInventoryReaderScreenHandler).getQuantumInventoryReaderId()
        textField.setTextFieldFocused(true)
        // Register new ui element
        addDrawableChild(textField)

        // Initialize the button
        safeButton = ButtonWidget(
            (width - backgroundWidth) / 2 + backgroundWidth - buttonWidth - margin,
            (height - backgroundHeight) / 2 + backgroundHeight - (footerHeight + buttonHeight) / 2,
            buttonWidth,
            buttonHeight,
            buttonText
        ) {
            // Update the handler with the new value
            (handler as QuantumInventoryReaderScreenHandler).setQuantumInventoryReaderId(textField.text)
            // Close the screen
            close()
        }
        // Register new ui element
        addDrawableChild(safeButton)

        // Call super last to make sure all our stuff is used
        super.init()
    }

    // Draw the background
    override fun drawBackground(matrices: MatrixStack?, delta: Float, mouseX: Int, mouseY: Int) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader)
        // Set fon colors
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f)
        // Set our texture of 256x256
        RenderSystem.setShaderTexture(0, texture)

        // Calculate the 0, 0 position of out screen
        val x = (width - backgroundWidth) / 2
        val y = (height - backgroundHeight) / 2

        // Draw the section from 0 to backgroundWidth and 0 to backgroundHeight on out texture at x and y
        drawTexture(
            matrices, x, y, 0, 0, backgroundWidth, backgroundHeight
        )
    }


    // Call all render functions of the registered ui elements
    override fun render(matrices: MatrixStack, mouseX: Int, mouseY: Int, delta: Float) {
        renderBackground(matrices)
        textField.render(matrices, mouseX, mouseY, delta)
        super.render(matrices, mouseX, mouseY, delta)
        drawMouseoverTooltip(matrices, mouseX, mouseY)
    }

    override fun keyPressed(keyCode: Int, scanCode: Int, modifiers: Int): Boolean {
        isDirty = true
        // handle all key presses
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
        // handle all typed chars
        if (textField.charTyped(chr, modifiers)) {
            return true
        }
        return super.charTyped(chr, modifiers)
    }

    override fun drawForeground(matrices: MatrixStack?, mouseX: Int, mouseY: Int) {
        val overflowAppend = Text.of("...")
        val maxWidth = backgroundWidth - textRenderer.getWidth(overflowAppend) - 2 * padding

        var truncatedTitle = title.string

        // Cut the title if it should overflow
        if (textRenderer.getWidth(title.string) > maxWidth) {
            truncatedTitle = textRenderer.trimToWidth(title.string, maxWidth) + "..."
        }

        // Implement out own foreground objects. Don`t call super because we don`t want them here
        textRenderer.draw(matrices, truncatedTitle, titleX.toFloat(), titleY.toFloat(), Color.WHITE.rgb)
    }
}