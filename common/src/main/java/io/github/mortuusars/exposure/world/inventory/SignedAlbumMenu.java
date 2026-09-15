package io.github.mortuusars.exposure.world.inventory;

import io.github.mortuusars.exposure.Exposure;
import io.github.mortuusars.exposure.util.PagingDirection;
import io.github.mortuusars.exposure.util.Side;
import io.github.mortuusars.exposure.world.item.AlbumItem;
import io.github.mortuusars.exposure.world.item.SignedAlbumItem;
import io.github.mortuusars.exposure.world.item.component.album.SignedAlbumPage;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class SignedAlbumMenu extends AbstractContainerMenu {
    public static final int PREVIOUS_PAGE_BUTTON = PagingDirection.PREVIOUS.ordinal();
    public static final int NEXT_PAGE_BUTTON = PagingDirection.NEXT.ordinal();

    protected final int albumSlot;
    protected final ItemStack albumStack;
    protected final SignedAlbumItem albumItem;

    protected DataSlot currentSpreadIndex = DataSlot.standalone();

    protected final Map<Integer, Consumer<Player>> buttonActions = new HashMap<>() {{
        put(PREVIOUS_PAGE_BUTTON, p -> {
            setCurrentSpreadIndex(Math.max(0, getCurrentSpreadIndex() - 1));
        });
        put(NEXT_PAGE_BUTTON, p -> {
            setCurrentSpreadIndex(Math.min((getPages().size() - 1) / 2, getCurrentSpreadIndex() + 1));
        });
    }};

    public SignedAlbumMenu(int containerId, Inventory playerInventory, int albumSlot) {
        this(Exposure.MenuTypes.SIGNED_ALBUM.get(), containerId, playerInventory, albumSlot);
    }

    protected SignedAlbumMenu(MenuType<? extends AbstractContainerMenu> type, int containerId, Inventory playerInventory, int albumSlot) {
        super(type, containerId);
        this.albumSlot = albumSlot;

        this.albumStack = playerInventory.getItem(albumSlot);
        if (!(albumStack.getItem() instanceof SignedAlbumItem item)) {
            throw new IllegalStateException("Expected SignedAlbumItem in slot '" + albumSlot + "'. Got: " + albumStack);
        }

        this.albumItem = item;

        addDataSlot(currentSpreadIndex);
    }

    public int getAlbumSlot() {
        return albumSlot;
    }

    public List<SignedAlbumPage> getPages() {
        return albumItem.getContent(albumStack).pages();
    }

    public Optional<SignedAlbumPage> getPage(int pageIndex) {
        if (pageIndex <= getPages().size() - 1)
            return Optional.ofNullable(getPages().get(pageIndex));

        return Optional.empty();
    }

    public Optional<SignedAlbumPage> getPage(Side side) {
        return getPage(getCurrentSpreadIndex() * 2 + side.getIndex());
    }

    public ItemStack getPhotograph(Side side) {
        return getPage(side).orElse(SignedAlbumPage.EMPTY).photograph();
    }

    public int getCurrentSpreadIndex() {
        return this.currentSpreadIndex.get();
    }

    public void setCurrentSpreadIndex(int spreadIndex) {
        this.currentSpreadIndex.set(spreadIndex);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        @Nullable Consumer<Player> buttonAction = buttonActions.get(id);
        if (buttonAction != null) {
            buttonAction.accept(player);
            return true;
        }

        return false;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return player.getInventory().getItem(albumSlot).getItem() instanceof AlbumItem;
    }

    public static SignedAlbumMenu fromBuffer(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buffer) {
        return new SignedAlbumMenu(containerId, playerInventory, buffer.readVarInt());
    }
}
