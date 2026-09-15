package io.github.mortuusars.exposure.client.gui.screen;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.toast.BetterTutorialToast;
import io.github.mortuusars.exposure.client.gui.toast.ToastIcon;
import io.github.mortuusars.exposure.client.input.Key;
import io.github.mortuusars.exposure.client.input.KeyBindings;
import io.github.mortuusars.exposure.client.util.GuiUtil;
import io.github.mortuusars.exposure.client.util.Minecrft;
import io.github.mortuusars.exposure.world.block.entity.Lightroom;
import io.github.mortuusars.exposure.world.block.entity.LightroomBlockEntity;
import io.github.mortuusars.exposure.client.gui.component.CycleButton;
import io.github.mortuusars.exposure.client.image.modifier.ImageEffect;
import io.github.mortuusars.exposure.client.image.renderable.RenderableImage;
import io.github.mortuusars.exposure.client.render.image.RenderCoordinates;
import io.github.mortuusars.exposure.world.camera.FilmColor;
import io.github.mortuusars.exposure.world.camera.ExposureType;
import io.github.mortuusars.exposure.world.item.FilmRollItem;
import io.github.mortuusars.exposure.world.lightroom.PrintingMode;
import io.github.mortuusars.exposure.world.item.DevelopedFilmItem;
import io.github.mortuusars.exposure.world.camera.frame.Frame;
import io.github.mortuusars.exposure.world.inventory.LightroomMenu;
import io.github.mortuusars.exposure.util.PagingDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.components.toasts.TutorialToast;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LightroomScreen extends AbstractContainerScreen<LightroomMenu> {
    public static final Identifier MAIN_TEXTURE = Exposure.resource("textures/gui/lightroom.png");
    public static final Identifier FILM_OVERLAYS_TEXTURE = Exposure.resource("textures/gui/lightroom_film_overlays.png");

    public static final WidgetSprites PRINT_BUTTON_SPRITES = new WidgetSprites(
            Exposure.resource("lightroom/print_button"),
            Exposure.resource("lightroom/print_button_disabled"),
            Exposure.resource("lightroom/print_button_highlighted"));

    public static final WidgetSprites PRINTING_MODE_TOGGLE_REGULAR_SPRITES = new WidgetSprites(
            Exposure.resource("lightroom/printing_mode_regular"),
            Exposure.resource("lightroom/printing_mode_regular_highlighted"));

    public static final WidgetSprites PRINTING_MODE_TOGGLE_CHROMATIC_SPRITES = new WidgetSprites(
            Exposure.resource("lightroom/printing_mode_chromatic"),
            Exposure.resource("lightroom/printing_mode_chromatic_highlighted"));

    public static final int FRAME_SIZE = 54;

    protected final KeyBindings keyBindings = KeyBindings.of(
            Key.press(InputConstants.KEY_ADD).or(Key.press(InputConstants.KEY_EQUALS)).executes(this::enterFrameInspectMode),
            Key.press(InputConstants.KEY_LEFT).or(Key.press(InputConstants.KEY_A)).executes(() -> changeFrame(PagingDirection.PREVIOUS)),
            Key.press(InputConstants.KEY_RIGHT).or(Key.press(InputConstants.KEY_D)).executes(() -> changeFrame(PagingDirection.NEXT))
    );

    protected Player player;
    protected Button printButton;
    protected PrintingMode mode;
    protected CycleButton<PrintingMode> printingModeToggleButton;

    protected Map<Integer, Rect2i> slotPlaceholders = Collections.emptyMap();

    protected boolean hasShownDevelopingToast;

    public LightroomScreen(LightroomMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.player = playerInventory.player;

        this.mode = getMenu().getBlockEntity().getActualPrintingMode();
    }

    @Override
    protected void init() {
        imageWidth = 176;
        imageHeight = 209;
        super.init();
        inventoryLabelY = 116;

        slotPlaceholders = Map.of(
                Lightroom.FILM_SLOT, new Rect2i(238, 0, 18, 18),
                Lightroom.PAPER_SLOT, new Rect2i(238, 18, 18, 18),
                Lightroom.CYAN_SLOT, new Rect2i(238, 36, 18, 18),
                Lightroom.MAGENTA_SLOT, new Rect2i(238, 54, 18, 18),
                Lightroom.YELLOW_SLOT, new Rect2i(238, 72, 18, 18),
                Lightroom.BLACK_SLOT, new Rect2i(238, 90, 18, 18)
        );

        printButton = new ImageButton(leftPos + 117, topPos + 89, 22, 22, PRINT_BUTTON_SPRITES,
                button -> {
                    int buttonId = Minecrft.hasShiftDown() && player.isCreative() ? LightroomMenu.PRINT_CREATIVE_BUTTON_ID : LightroomMenu.PRINT_BUTTON_ID;
                    clickButton(buttonId);
                }, Component.translatable("gui.exposure.lightroom.print"));
        updatePrintButtonTooltip();

        addRenderableWidget(printButton);

        printingModeToggleButton = createPrintingModeToggleButton();
        addRenderableWidget(printingModeToggleButton);

        updateButtons();
    }

    protected void updatePrintButtonTooltip() {
        MutableComponent tooltip = Component.translatable("gui.exposure.lightroom.print");
        if (!getMenu().getBlockEntity().hasSufficientLightLevel()) {
            tooltip.append("\n")
                    .append(Component.translatable("gui.exposure.lightroom.print.not_enough_light_tooltip").withStyle(ChatFormatting.RED));
        }

        if (player.isCreative()) {
            tooltip.append("\n")
                    .append(Component.translatable("gui.exposure.lightroom.print.creative_tooltip"));
        }

        printButton.setTooltip(Tooltip.create(tooltip));
    }

    protected CycleButton<PrintingMode> createPrintingModeToggleButton() {
        Map<PrintingMode, WidgetSprites> spritesMap = Map.of(PrintingMode.REGULAR, PRINTING_MODE_TOGGLE_REGULAR_SPRITES,
                PrintingMode.CHROMATIC, PRINTING_MODE_TOGGLE_CHROMATIC_SPRITES);
        Map<PrintingMode, Tooltip> tooltipMap = Map.of(
                PrintingMode.REGULAR, Tooltip.create(Component.translatable("gui.exposure.lightroom.printing_mode.regular")
                        .append(CommonComponents.NEW_LINE)
                        .append(Component.translatable("gui.exposure.lightroom.printing_mode.regular.info").withStyle(ChatFormatting.GRAY))),
                PrintingMode.CHROMATIC, Tooltip.create(Component.translatable("gui.exposure.lightroom.printing_mode.chromatic")
                        .append(CommonComponents.NEW_LINE)
                        .append(Component.translatable("gui.exposure.lightroom.printing_mode.chromatic.info").withStyle(ChatFormatting.GRAY))));
        return new CycleButton<>(leftPos - 17, topPos + 91, 18, 18,
                Arrays.asList(PrintingMode.values()), getMenu().getBlockEntity().getActualPrintingMode(),
                spritesMap, (button, newMode) -> clickButton(LightroomMenu.TOGGLE_PROCESS_BUTTON_ID))
                .setClickSound(SoundEvents.UI_BUTTON_CLICK.value())
                .setTooltips(tooltipMap);
    }

    protected void clickButton(int buttonId) {
        getMenu().clickMenuButton(player, buttonId);
        Minecrft.gameMode().handleInventoryButtonClick(getMenu().containerId, buttonId);
    }

    @Override
    protected void containerTick() {
        PrintingMode currentMode = getMenu().getBlockEntity().getActualPrintingMode();
        if (currentMode != printingModeToggleButton.getCurrentValue()) {
            printingModeToggleButton.setCurrentValue(currentMode);
        }
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateButtons();
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    protected void updateButtons() {
        printButton.active = getMenu().getBlockEntity().canPrint() || (player.isCreative() && Minecrft.hasShiftDown() && getMenu().getBlockEntity().canPrintInCreativeMode());
        printButton.visible = !getMenu().isPrinting();
        updatePrintButtonTooltip();

        printingModeToggleButton.active = true;
        printingModeToggleButton.visible = getMenu().canChangeProcess();
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MAIN_TEXTURE, leftPos, topPos, 0f, 0f, imageWidth, imageHeight, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MAIN_TEXTURE, leftPos - 27, topPos + 35, 0f, 209f, 28, 31, 256, 256);

        renderSlotPlaceholders(guiGraphics, mouseX, mouseY, partialTick);

        if (getMenu().isPrinting()) {
            int progress = getMenu().getData().get(LightroomBlockEntity.CONTAINER_DATA_PROGRESS_ID);
            int time = getMenu().getData().get(LightroomBlockEntity.CONTAINER_DATA_PRINT_TIME_ID);
            int width = progress != 0 && time != 0 ? progress * 24 / time : 0;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MAIN_TEXTURE, leftPos + 116, topPos + 91, 176f, 0f, width, 17, 256, 256);
        }

        List<Frame> frames = getMenu().getExposedFrames();
        if (frames.isEmpty()) {
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FILM_OVERLAYS_TEXTURE, leftPos + 4, topPos + 15, 0f, 136f, 168, 68, 256, 256);
            return;
        }

        ItemStack filmStack = getMenu().getSlot(Lightroom.FILM_SLOT).getItem();
        if (!(filmStack.getItem() instanceof DevelopedFilmItem film))
            return;

        ExposureType exposureType = film.getType();
        FilmColor filmColor = exposureType.getFilmColor();

        // 1.21.11 equivalent of the old RenderSystem.setShaderColor(filmColor): tint the film overlay
        // vertices through the coloured blit overload. Channels are 0..1 floats (red may exceed 1, e.g. 1.2),
        // so floor to 8-bit and clamp to 0..255 before packing as ARGB (AARRGGBB).
        int filmOverlayColor = ARGB.color(
                Mth.clamp(Mth.floor(filmColor.a() * 255.0F), 0, 255),
                Mth.clamp(Mth.floor(filmColor.r() * 255.0F), 0, 255),
                Mth.clamp(Mth.floor(filmColor.g() * 255.0F), 0, 255),
                Mth.clamp(Mth.floor(filmColor.b() * 255.0F), 0, 255));

        int selectedFrame = getMenu().getSelectedFrame();
        @Nullable Frame leftFrame = getMenu().getFrameByIndex(selectedFrame - 1);
        @Nullable Frame centerFrame = getMenu().getFrameByIndex(selectedFrame);
        @Nullable Frame rightFrame = getMenu().getFrameByIndex(selectedFrame + 1);


        // Left film part
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FILM_OVERLAYS_TEXTURE, leftPos + 1, topPos + 15, 0f, leftFrame != null ? 68f : 0f, 54, 68, 256, 256, filmOverlayColor);
        // Center film part
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FILM_OVERLAYS_TEXTURE, leftPos + 55, topPos + 15, 55f, rightFrame != null ? 0f : 68f, 64, 68, 256, 256, filmOverlayColor);
        // Right film part
        if (rightFrame != null) {
            boolean hasMoreFrames = selectedFrame + 2 < frames.size();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, FILM_OVERLAYS_TEXTURE, leftPos + 119, topPos + 15, 120f, hasMoreFrames ? 68f : 0f, 56, 68, 256, 256, filmOverlayColor);
        }

        // Photographs go in a higher stratum than the opaque film overlay so they are never covered.
        guiGraphics.nextStratum();
        if (leftFrame != null)
            renderFrame(leftFrame, guiGraphics, leftPos + 6, topPos + 22, FRAME_SIZE, isOverLeftFrame(mouseX, mouseY) ? 0.8f : 0.25f, exposureType);
        if (centerFrame != null)
            renderFrame(centerFrame, guiGraphics, leftPos + 61, topPos + 22, FRAME_SIZE, 0.9f, exposureType);
        if (rightFrame != null)
            renderFrame(rightFrame, guiGraphics, leftPos + 116, topPos + 22, FRAME_SIZE, isOverRightFrame(mouseX, mouseY) ? 0.8f : 0.25f, exposureType);


        if (getMenu().getBlockEntity().isAdvancingFrameOnPrint()) {
            if (selectedFrame < getMenu().getTotalFramesCount() - 1) {
                // Advance Arrow
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MAIN_TEXTURE, leftPos + 111, topPos + 44, 200f, 0f, 10, 10, 256, 256, filmOverlayColor);
            } else {
                // Eject Arrow
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MAIN_TEXTURE, leftPos + 111, topPos + 44, 210f, 0f, 10, 10, 256, 256, filmOverlayColor);
            }

        }

    }

    private void renderSlotPlaceholders(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        for (int slotIndex : slotPlaceholders.keySet()) {
            Slot slot = getMenu().getSlot(slotIndex);
            if (!slot.hasItem()) {
                Rect2i placeholder = slotPlaceholders.get(slotIndex);
                guiGraphics.blit(RenderPipelines.GUI_TEXTURED, MAIN_TEXTURE, leftPos + slot.x - 1, topPos + slot.y - 1,
                        (float) placeholder.getX(), (float) placeholder.getY(), placeholder.getWidth(), placeholder.getHeight(), 256, 256);
            }
        }
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);

        boolean advancedTooltips = Minecraft.getInstance().options.advancedItemTooltips;
        int selectedFrame = getMenu().getSelectedFrame();
        List<Component> tooltipLines = new ArrayList<>();

        int hoveredFrameIndex = -1;

        if (isOverLeftFrame(mouseX, mouseY)) {
            hoveredFrameIndex = selectedFrame - 1;
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.previous_frame"));
        } else if (isOverCenterFrame(mouseX, mouseY)) {
            hoveredFrameIndex = selectedFrame;
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.current_frame", Integer.toString(getMenu().getSelectedFrame() + 1)));
        } else if (isOverRightFrame(mouseX, mouseY)) {
            hoveredFrameIndex = selectedFrame + 1;
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.next_frame"));
        }

        if (hoveredFrameIndex != -1) {
            addFrameInfoTooltipLines(tooltipLines, hoveredFrameIndex, advancedTooltips);
        }

        if (isOverCenterFrame(mouseX, mouseY)) {
            tooltipLines.add(Component.translatable("gui.exposure.lightroom.zoom_in.tooltip"));
        }

        // 1.21.11: tooltips are deferred and drawn in a single unified pass. Use the
        // Component-list entry point (equivalent to vanilla 1.21.1 GuiGraphics#renderTooltip).
        // It no-ops on an empty list (so no empty/black tooltip box is drawn when the cursor
        // is over no frame) and positions with DefaultTooltipPositioner in the correct render stratum.
        guiGraphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, tooltipLines, mouseX, mouseY);
    }

    private void addFrameInfoTooltipLines(List<Component> tooltipLines, int frameIndex, boolean isAdvancedTooltips) {
        @Nullable Frame frame = getMenu().getFrameByIndex(frameIndex);
        if (frame != null) {
            if (getMenu().getBlockEntity().isRefracted()) {
                frame.getColorChannel().ifPresent(c ->
                        tooltipLines.add(Component.translatable("gui.exposure.channel." + c.getSerializedName())
                            .withStyle(Style.EMPTY.withColor(c.getRepresentationColor()))));
            }

            if (isAdvancedTooltips) {
                Component component = frame.identifier().map(
                                id -> !id.isEmpty() ? Component.translatable("gui.exposure.frame.id",
                                        Component.literal(id).withStyle(ChatFormatting.GRAY)) : Component.empty(),
                                texture -> Component.translatable("gui.exposure.frame.texture",
                                        Component.literal(texture.toString()).withStyle(ChatFormatting.GRAY)))
                        .withStyle(ChatFormatting.DARK_GRAY);
                tooltipLines.add(component);
            }
        }
    }

    private boolean isOverLeftFrame(int mouseX, int mouseY) {
        List<Frame> frames = getMenu().getExposedFrames();
        int selectedFrame = getMenu().getSelectedFrame();
        return selectedFrame - 1 >= 0 && selectedFrame - 1 < frames.size() && isHovering(6, 22, FRAME_SIZE, FRAME_SIZE, mouseX, mouseY);
    }

    private boolean isOverCenterFrame(int mouseX, int mouseY) {
        List<Frame> frames = getMenu().getExposedFrames();
        int selectedFrame = getMenu().getSelectedFrame();
        return selectedFrame >= 0 && selectedFrame < frames.size() && isHovering(61, 22, FRAME_SIZE, FRAME_SIZE, mouseX, mouseY);
    }

    private boolean isOverRightFrame(int mouseX, int mouseY) {
        List<Frame> frames = getMenu().getExposedFrames();
        int selectedFrame = getMenu().getSelectedFrame();
        return selectedFrame + 1 >= 0 && selectedFrame + 1 < frames.size() && isHovering(116, 22, FRAME_SIZE, FRAME_SIZE, mouseX, mouseY);
    }

    public void renderFrame(@NotNull Frame frame, GuiGraphics guiGraphics, float x, float y, float size, float alpha, ExposureType exposureType) {
        RenderableImage image = ExposureClient.renderedExposures().getOrCreate(frame).modifyWith(ImageEffect.NEGATIVE_FILM);

        io.github.mortuusars.exposure.util.color.Color tint =
                exposureType.getImageColor().withAlpha((int) (alpha * 255));
        // Unified GUI photograph path (GuiGraphics + GUI_TEXTURED + DynamicTexture).
        ExposureClient.photographRenderer().renderImageInGui(guiGraphics, image,
                (int) x, (int) y, (int) size, (int) size,
                tint.getR(), tint.getG(), tint.getB(), tint.getA());
    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        return keyBindings.keyPressed(keyEvent) || super.keyPressed(keyEvent);
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {
        return keyBindings.keyReleased(keyEvent) || super.keyReleased(keyEvent);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent mouseButtonEvent, boolean bl) {
        double mouseX = mouseButtonEvent.x();
        double mouseY = mouseButtonEvent.y();
        if (mouseButtonEvent.button() == 0) {
            Slot filmSlot = getMenu().getSlot(Lightroom.FILM_SLOT);
            if (!hasShownDevelopingToast && !filmSlot.hasItem()
                    && isHovering(filmSlot.x, filmSlot.y, 16, 16, mouseX, mouseY)
                    && getMenu().getCarried().getItem() instanceof FilmRollItem) {
                Minecrft.get().getToastManager().addToast(new BetterTutorialToast(ToastIcon.HEADS_UP,
                        Component.translatable("gui.exposure.lightroom.toast.develop_film.title"),
                        null, BetterTutorialToast.DEFAULT_SHOW_DURATION_MS));
                hasShownDevelopingToast = true;
            }

            if (isOverCenterFrame((int) mouseX, (int) mouseY)) {
                enterFrameInspectMode();
                return true;
            }

            if (isOverLeftFrame((int) mouseX, (int) mouseY)) {
                changeFrame(PagingDirection.PREVIOUS);
                return true;
            }

            if (isOverRightFrame((int) mouseX, (int) mouseY)) {
                changeFrame(PagingDirection.NEXT);
                return true;
            }
        }

        return super.mouseClicked(mouseButtonEvent, bl);
    }

    public void changeFrame(PagingDirection navigation) {
        if ((navigation == PagingDirection.PREVIOUS && getMenu().getSelectedFrame() == 0)
                || (navigation == PagingDirection.NEXT && getMenu().getSelectedFrame() == getMenu().getTotalFramesCount() - 1)) {
            return;
        }

        Preconditions.checkState(minecraft != null);
        Preconditions.checkState(minecraft.player != null);
        Preconditions.checkState(minecraft.gameMode != null);
        int buttonId = navigation == PagingDirection.NEXT ? LightroomMenu.NEXT_FRAME_BUTTON_ID : LightroomMenu.PREVIOUS_FRAME_BUTTON_ID;
        clickButton(buttonId);
        Minecrft.get().getSoundManager().play(SimpleSoundInstance.forUI(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(),
                1f, ThreadLocalRandom.current().nextFloat() * 0.4f + 0.8f));

        // Update block entity clientside to faster update advance frame arrows:
        getMenu().getBlockEntity().setSelectedFrameIndex(getMenu().getBlockEntity().getSelectedFrameIndex() + (navigation == PagingDirection.NEXT ? 1 : -1));
    }

    private void enterFrameInspectMode() {
        Minecraft.getInstance().setScreen(new LightroomFrameInspectScreen(this));
        player.playSound(Exposure.SoundEvents.CAMERA_LENS_RING_CLICK.get(), 1f, 1.3f);
    }

    @Override
    protected boolean hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop) {
        return super.hasClickedOutside(mouseX, mouseY, guiLeft, guiTop)
                && hoveredSlot == null;
    }
}
