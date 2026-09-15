package io.github.mortuusars.exposure.client.gui.screen.album;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.render.photograph.PhotographStyle;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.util.color.Color;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.item.PhotographItem;
import io.github.mortuusars.exposure.world.item.util.ItemAndStack;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class PhotographSlotWidget extends AbstractWidget {
    public static final WidgetSprites SPRITES = new WidgetSprites(
            Exposure.resource("album/photograph_slot"), Exposure.resource("album/photograph_slot_highlighted"));
    public static final WidgetSprites EMPTY_SPRITES = new WidgetSprites(
            Exposure.resource("album/photograph_slot_empty"), Exposure.resource("album/photograph_slot_empty_highlighted"));

    private final Screen parent;
    protected final Supplier<ItemStack> photographSupplier;

    protected boolean editable;
    protected Consumer<PhotographSlotWidget> primaryAction = slot -> {};
    protected Consumer<PhotographSlotWidget> secondaryAction = slot -> {};

    protected boolean hasPhotograph;

    public PhotographSlotWidget(Screen parent, int x, int y, int width, int height, Supplier<ItemStack> photographSupplier) {
        super(x, y, width, height, Component.empty());
        this.parent = parent;
        this.photographSupplier = photographSupplier;
    }

    // --

    public PhotographSlotWidget editable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public PhotographSlotWidget primaryAction(Consumer<PhotographSlotWidget> primaryAction) {
        this.primaryAction = primaryAction;
        return this;
    }

    public PhotographSlotWidget secondaryAction(Consumer<PhotographSlotWidget> secondaryAction) {
        this.secondaryAction = secondaryAction;
        return this;
    }

    // --

    public boolean isEditable() {
        return editable;
    }

    public ItemStack getPhotograph() {
        return photographSupplier.get();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        ItemStack photograph = getPhotograph();

        if (photograph.getItem() instanceof PhotographItem) {
            hasPhotograph = true;

            PhotographStyle photographStyle = PhotographStyle.of(photograph);

            // All three layers are submitted into the current stratum in draw order. 1.21.11's
            // GuiRenderState.findAppropriateNode auto-promotes nested/intersecting elements: the
            // paper (108x108) encompasses the photo (inset 6px) -> the photo is lifted above it,
            // and the overlay submitted afterwards intersects -> lifted above the photo. We must
            // NOT call nextStratum() here: that would open a new top-level stratum that pins the
            // photograph above the player-inventory slots drawn later by AbstractContainerScreen,
            // which is what made the left-page photo cover the add-photo selection UI.
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, photographStyle.albumPaperTexture(),
                    getX(), getY(), 0, 0, width, height, width, height);

            ExposureClient.photographRenderer().renderImageInGui(guiGraphics, photograph,
                    getX() + 6, getY() + 6, width - 12, height - 12, 255, 255, 255, 255);

            // Paper overlay (frame/border) above the photo; auto-promoted because it intersects.
            if (photographStyle.hasAlbumOverlayTexture()) {
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, photographStyle.albumOverlayTexture(),
                        getX(), getY(), 0, 0, width, height, width, height);
            }
        }
        else {
            hasPhotograph = false;
        }

        WidgetSprites sprites = hasPhotograph ? SPRITES : EMPTY_SPRITES;
        Identifier resourceLocation = sprites.get(isActive(), isHoveredOrFocused());
        if (!editable && !hasPhotograph) {
            resourceLocation = sprites.get(isActive(), false);
        }
        guiGraphics.blitSprite(RenderPipelines.GUI_TEXTURED, resourceLocation, getX(), getY(), width, height);
    }

    public void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (editable && !hasPhotograph) {
            guiGraphics.renderTooltip(Minecrft.get().font, List.of(ClientTooltipComponent.create(
                    Component.translatable("gui.exposure.album.add_photograph").getVisualOrderText())), mouseX, mouseY,
                    DefaultTooltipPositioner.INSTANCE, null);
            return;
        }

        ItemStack photograph = getPhotograph();
        if (photograph.isEmpty()) return;

        List<Component> itemTooltip = Screen.getTooltipFromItem(Minecrft.get(), photograph);
        itemTooltip.add(Component.translatable("gui.exposure.album.left_click_or_scroll_up_to_view"));
        if (editable) {
            itemTooltip.add(Component.translatable("gui.exposure.album.right_click_to_remove"));
        }

        // Photograph image in tooltip is not rendered

        List<ClientTooltipComponent> tooltipComponents = itemTooltip.stream()
                .map(component -> ClientTooltipComponent.create(component.getVisualOrderText()))
                .collect(Collectors.toList());
        guiGraphics.renderTooltip(Minecrft.get().font, tooltipComponents, mouseX, mouseY,
                DefaultTooltipPositioner.INSTANCE, null);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        int button = mouseButtonEvent.button();
        if (!this.active || !this.visible || !isMouseOver(mouseX, mouseY)) return false;

        if (button == InputConstants.MOUSE_BUTTON_LEFT) {
            primaryAction.accept(this);
        } else if (button == InputConstants.MOUSE_BUTTON_RIGHT) {
            secondaryAction.accept(this);
        } else return false;

        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        int keyCode = keyEvent.input();
        if (this.active && this.visible && (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE || keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_KP_ENTER)) {
            if (Minecrft.hasShiftDown()) {
                secondaryAction.accept(this);
            } else {
                primaryAction.accept(this);
            }
            return true;
        }

        return super.keyPressed(keyEvent);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        ItemStack photograph = getPhotograph();
        if (!photograph.isEmpty()) {
            narrationElementOutput.add(NarratedElementType.TITLE, photograph.getHoverName());
        }
    }

    public boolean inspectPhotograph() {
        ItemStack photograph = getPhotograph();
        if (!(photograph.getItem() instanceof PhotographItem)) {
            return false;
        }

        Minecrft.get().setScreen(new ChildPhotographScreen(parent, List.of(new ItemAndStack<>(photograph))));
        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.PHOTOGRAPH_RUSTLE.get(),
                        Minecrft.level().getRandom().nextFloat() * 0.2f + 1.3f, 0.75f));
        return true;
    }
}
