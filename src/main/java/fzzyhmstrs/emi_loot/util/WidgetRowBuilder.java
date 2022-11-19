package fzzyhmstrs.emi_loot.util;

import dev.emi.emi.api.stack.EmiIngredient;
import fzzyhmstrs.emi_loot.client.ClientMobLootTable;
import it.unimi.dsi.fastutil.floats.Float2ObjectArrayMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectMap;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class WidgetRowBuilder {

    public WidgetRowBuilder(int maxWidth){
        this.maxWidth = maxWidth;
    }

    private final int maxWidth;
    private final List<ClientMobLootTable.ClientMobBuiltPool> poolList = new LinkedList<>();
    private int width = 0;

    public List<ClientMobLootTable.ClientMobBuiltPool> getPoolList(){
        return poolList;
    }

    private boolean add(ClientMobLootTable.ClientMobBuiltPool newPool){
        int newWidth;
        if (poolList.isEmpty()){
            newWidth = 14 + (11 * (((newPool.list().size() - 1)/2) - 1)) + 20 * newPool.stackMap().size();
        } else {
            newWidth = 20 + (11 * (((newPool.list().size() - 1)/2) - 1)) + 20 * newPool.stackMap().size();
        }
        if (width + newWidth > maxWidth) return false;
        width += newWidth;
        poolList.add(newPool);
        return true;
    }

    public Optional<ClientMobLootTable.ClientMobBuiltPool> addAndTrim(ClientMobLootTable.ClientMobBuiltPool newPool){
        if (add(newPool)) return Optional.empty();
        if (width == 0) {
            Float2ObjectMap<EmiIngredient> madeItIn = new Float2ObjectArrayMap<>();
            Float2ObjectMap<EmiIngredient> leftOvers = new Float2ObjectArrayMap<>();
            AtomicInteger newWidth = new AtomicInteger(14 + (11 * (((newPool.list().size() - 1) / 2) - 1)));
            newPool.stackMap().forEach((weight, stacks) -> {
                if (newWidth.addAndGet(20) <= maxWidth) {
                    madeItIn.put((float) weight, stacks);
                } else {
                    leftOvers.put((float) weight, stacks);
                }
            });
            add(new ClientMobLootTable.ClientMobBuiltPool(newPool.list(), madeItIn));
            return Optional.of(new ClientMobLootTable.ClientMobBuiltPool(newPool.list(), leftOvers));
        } else {
            return Optional.of(newPool);
        }
    }

}
