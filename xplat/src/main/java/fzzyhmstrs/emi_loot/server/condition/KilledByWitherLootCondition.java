package fzzyhmstrs.emi_loot.server.condition;

import com.mojang.serialization.MapCodec;
import fzzyhmstrs.emi_loot.EMILoot;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionType;
import net.minecraft.loot.context.LootContext;

public class KilledByWitherLootCondition implements LootCondition {

    public static final KilledByWitherLootCondition INSTANCE = new KilledByWitherLootCondition();
    public static final MapCodec<KilledByWitherLootCondition> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public LootConditionType getType() {
        return EMILoot.WITHER_KILL.get();
    }

    @Override
    public boolean test(LootContext lootContext) {
        return false;
    }

}