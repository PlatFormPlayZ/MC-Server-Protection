/*
 * This software is licensed under the MIT License
 * https://github.com/GStefanowich/MC-Server-Protection
 *
 * Copyright (c) 2019 Gregory Stefanowich
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package net.TheElm.project.mixins.Player;

import net.TheElm.project.config.SewingMachineConfig;
import net.TheElm.project.interfaces.Nicknamable;
import net.TheElm.project.utilities.DeathChestUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class DeathChest extends LivingEntity implements Nicknamable {
    
    private Text playerNickname = null;
    @Shadow public PlayerInventory inventory;
    @Shadow protected native void vanishCursedItems();
    
    protected DeathChest(EntityType<? extends LivingEntity> entityType_1, World world_1) {
        super(entityType_1, world_1);
    }
    
    // If player drops inventory (At death, stop that!)
    @Inject(at = @At("HEAD"), method = "dropInventory", cancellable = true)
    public void onInventoryDrop(CallbackInfo callback) {
        if (!SewingMachineConfig.INSTANCE.DO_DEATH_CHESTS.get())
            return;
        
        // Only do if we're not keeping the inventory, and the player is actually dead! (Death Chest!)
        if ((!this.world.getGameRules().getBoolean(GameRules.KEEP_INVENTORY)) && (!this.isAlive())) {
            BlockPos chestPos;
            // If the inventory is NOT empty, and we found a valid position for the death chest
            if ((!this.inventory.isInvEmpty()) && ((chestPos = DeathChestUtils.getChestPosition( this.getEntityWorld(), this.getBlockPos() )) != null)) {
                // Vanish cursed items
                this.vanishCursedItems();
                
                // If a death chest was successfully spawned
                if (DeathChestUtils.createDeathChestFor( (PlayerEntity)(LivingEntity)this, chestPos, this.inventory )) {
                    callback.cancel();
                }
            }
        }
    }
    
    @Override
    public void setPlayerNickname(@Nullable Text nickname) {
        this.playerNickname = nickname;
    }
    @Nullable @Override
    public Text getPlayerNickname() {
        return this.playerNickname;
    }
    
    // Override the players display name
    @Inject(at = @At("RETURN"), method = "getDisplayName", cancellable = true)
    public void getPlayerNickname(CallbackInfoReturnable<Text> callback) {
        if (this.playerNickname != null)
            callback.setReturnValue( this.playerNickname );
    }
    
    @Inject(at = @At("TAIL"), method = "writeCustomDataToTag")
    public void onSavingData(CompoundTag compoundTag, CallbackInfo callback) {
        if ( this.playerNickname != null ) {
            compoundTag.putString( "PlayerNickname", Text.Serializer.toJson( this.playerNickname ));
        }
    }
    
    @Inject(at = @At("TAIL"), method = "readCustomDataFromTag")
    public void onReadingData(CompoundTag compoundTag, CallbackInfo callback) {
        if (compoundTag.containsKey("PlayerNickname", 8)) {
            this.playerNickname = Text.Serializer.fromJson( compoundTag.getString("PlayerNickname") );
        }
    }
    
}
