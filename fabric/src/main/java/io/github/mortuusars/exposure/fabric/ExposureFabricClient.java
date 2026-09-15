package io.github.mortuusars.exposure.fabric;

import fuzs.forgeconfigapiport.fabric.api.v5.client.ConfigScreenFactoryRegistry;
import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.ExposureClient;
import io.github.mortuusars.exposure.client.gui.tooltip.CameraStandTooltip;
import io.github.mortuusars.exposure.client.input.KeyboardHandler;
import io.github.mortuusars.exposure.client.render.CameraStandEntityRenderer;
import io.github.mortuusars.exposure.client.render.GlassPhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.fabric.resources.ExposureFabricClientReloadListener;
import io.github.mortuusars.exposure.client.gui.tooltip.PhotographClientTooltip;
import io.github.mortuusars.exposure.client.gui.screen.ItemRenameScreen;
import io.github.mortuusars.exposure.client.gui.screen.LightroomScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.AlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.album.LecternAlbumScreen;
import io.github.mortuusars.exposure.client.gui.screen.camera.CameraAttachmentsScreen;
import io.github.mortuusars.exposure.integration.ModCompatibilityClient;
import io.github.mortuusars.exposure.world.inventory.tooltip.PhotographTooltip;
import io.github.mortuusars.exposure.network.fabric.FabricS2CPacketHandler;
import io.github.mortuusars.exposure.client.render.PhotographFrameEntityRenderer;
import io.github.mortuusars.exposure.client.render.model.CameraItemModel;
import io.github.mortuusars.exposure.client.render.model.AlbumItemModel;
import io.github.mortuusars.exposure.world.item.camera.CameraItem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;

public class ExposureFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ExposureClient.init();

        ConfigScreenFactoryRegistry.INSTANCE.register(Exposure.ID, ConfigurationScreen::new);

        KeyboardHandler.registerKeymappings(KeyBindingHelper::registerKeyBinding);

        MenuScreens.register(Exposure.MenuTypes.CAMERA_IN_HAND.get(), CameraAttachmentsScreen::new);
        MenuScreens.register(Exposure.MenuTypes.CAMERA_ON_STAND.get(), CameraAttachmentsScreen::new);
        MenuScreens.register(Exposure.MenuTypes.ALBUM.get(), AlbumScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LECTERN_ALBUM.get(), LecternAlbumScreen::new);
        MenuScreens.register(Exposure.MenuTypes.LIGHTROOM.get(), LightroomScreen::new);
        MenuScreens.register(Exposure.MenuTypes.ITEM_RENAME.get(), ItemRenameScreen::new);

        ModelLoadingPlugin.register(pluginContext -> {
            PlatformHelperClientImpl.registerExtraBlockModels(pluginContext);
            // 1.21.11: assets/exposure/items/camera.json declares a minecraft:composite whose children,
            // in a fixed order, are every camera sub-model (base/gui/gold/active/selfie/parts/stick).
            // Before baking we wrap that composite: each child is baked by the vanilla context, then the
            // baked list is handed to CameraItemModel to select from at render time. We cannot resolve
            // those sub-models at runtime via ModelManager#getItemModel (that map is keyed by item id).
            pluginContext.modifyItemModelBeforeBake().register((unbaked, context) -> {
                // Camera: wrap the declared minecraft:composite; children are baked by the vanilla
                // context and handed to CameraItemModel. Unchanged and proven working in-world.
                if (context.itemId().equals(Exposure.resource("camera"))
                        && unbaked instanceof net.minecraft.client.renderer.item.CompositeModel.Unbaked composite) {
                    java.util.List<net.minecraft.client.renderer.item.ItemModel.Unbaked> children = composite.models();
                    return new net.minecraft.client.renderer.item.ItemModel.Unbaked() {
                        @Override
                        public void resolveDependencies(net.minecraft.client.resources.model.ResolvableModel.Resolver resolver) {
                            composite.resolveDependencies(resolver);
                        }

                        @Override
                        public net.minecraft.client.renderer.item.ItemModel bake(
                                net.minecraft.client.renderer.item.ItemModel.BakingContext bakingContext) {
                            java.util.List<net.minecraft.client.renderer.item.ItemModel> baked =
                                    new java.util.ArrayList<>(children.size());
                            for (net.minecraft.client.renderer.item.ItemModel.Unbaked child : children) {
                                baked.add(child.bake(bakingContext));
                            }
                            return new CameraItemModel(baked);
                        }

                        @Override
                        public com.mojang.serialization.MapCodec<? extends net.minecraft.client.renderer.item.ItemModel.Unbaked> type() {
                            return net.minecraft.client.renderer.item.CompositeModel.Unbaked.MAP_CODEC;
                        }
                    };
                }

                // Album: replace wholesale with the explicit 4-cover selector. A custom
                // range_dispatch/select property cannot be registered on 1.21.11 (private id mapper,
                // no Fabric registry), so AlbumItemModel mirrors vanilla RangeSelectItemModel with
                // cover ids declared in code. The on-disk items/album.json is only a fallback.
                if (context.itemId().equals(Exposure.resource("album"))) {
                    Exposure.LOGGER.info("[AlbumDebug] beforeBake album -> AlbumItemModel.UNBAKED | source={}",
                            unbaked.getClass().getName());
                    return AlbumItemModel.UNBAKED;
                }
                return unbaked;
            });

            // [AlbumDebug] READ-ONLY: report the model finally bound to exposure:album (returned
            // unchanged). Tells us whether the baked AlbumItemModel is what actually gets bound.
            pluginContext.modifyItemModelAfterBake().register((baked, afterCtx) -> {
                if (afterCtx.itemId().equals(Exposure.resource("album"))) {
                    Exposure.LOGGER.info("[AlbumDebug] afterBake album | finalBakedClass={} sourceUnbaked={}",
                            baked == null ? "null" : baked.getClass().getName(),
                            afterCtx.sourceModel() == null ? "null" : afterCtx.sourceModel().getClass().getSimpleName());
                }
                return baked;
            });
        });

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ExposureFabricClientReloadListener());

        EntityRendererRegistry.register(Exposure.EntityTypes.PHOTOGRAPH_FRAME.get(), PhotographFrameEntityRenderer::new);
        EntityRendererRegistry.register(Exposure.EntityTypes.CLEAR_PHOTOGRAPH_FRAME.get(), GlassPhotographFrameEntityRenderer::new);
        EntityRendererRegistry.register(Exposure.EntityTypes.CAMERA_STAND.get(), CameraStandEntityRenderer::new);

        TooltipComponentCallback.EVENT.register(data -> data instanceof PhotographTooltip photographTooltip
                ? new PhotographClientTooltip(photographTooltip) : null);

        HudRenderCallback.EVENT.register(CameraStandTooltip::render);

        FabricS2CPacketHandler.register();

        ModCompatibilityClient.handle();
    }
}
