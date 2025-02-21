/*
 * Licensed under the EUPL, Version 1.2.
 * You may obtain a copy of the Licence at:
 * https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 */

package net.dries007.tfc.common.entities.predator;

import java.util.function.Supplier;

import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;

import com.mojang.serialization.Dynamic;
import net.dries007.tfc.client.TFCSounds;
import net.dries007.tfc.client.particle.TFCParticles;
import net.dries007.tfc.common.TFCEffects;
import net.dries007.tfc.common.entities.WildAnimal;
import net.dries007.tfc.common.entities.ai.predator.PredatorAi;


import org.jetbrains.annotations.Nullable;

public class Predator extends WildAnimal
{
    public static AttributeSupplier.Builder createAttributes()
    {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.MOVEMENT_SPEED, 0.3F).add(Attributes.ATTACK_KNOCKBACK, 1).add(Attributes.ATTACK_DAMAGE, 6);
    }

    public static final EntityDataAccessor<Boolean> DATA_SLEEPING = SynchedEntityData.defineId(Predator.class, EntityDataSerializers.BOOLEAN);

    private final int attackAnimationLength;

    private final Supplier<SoundEvent> attack;
    private final Supplier<SoundEvent> sleeping;

    public final boolean diurnal;
    private int attackAnimationRemainingTicks = 0;

    public static Predator createBear(EntityType<? extends Predator> type, Level level)
    {
        return new Predator(type, level, true, 20, 20, TFCSounds.BEAR);
    }

    public Predator(EntityType<? extends Predator> type, Level level, boolean diurnal, int attackLength, int walkLength, TFCSounds.EntitySound sounds)
    {
        super(type, level, sounds, walkLength);
        attackAnimationLength = attackLength;
        this.diurnal = diurnal;
        this.attack = sounds.attack().orElseThrow();
        this.sleeping = sounds.sleep().orElseThrow();
        this.setPathfindingMalus(BlockPathTypes.POWDER_SNOW, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.DANGER_POWDER_SNOW, -1.0F);
    }

    @Override
    protected Brain.Provider<Predator> brainProvider()
    {
        return Brain.provider(PredatorAi.MEMORY_TYPES, PredatorAi.SENSOR_TYPES);
    }

    @Override
    protected Brain<?> makeBrain(Dynamic<?> dynamic)
    {
        return PredatorAi.makeBrain(brainProvider().makeBrain(dynamic), this);
    }

    @Override
    protected void customServerAiStep()
    {
        getBrain().tick((ServerLevel) level, this);
        PredatorAi.updateActivity(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Brain<Predator> getBrain()
    {
        return (Brain<Predator>) super.getBrain();
    }

    @Override
    public void tick()
    {
        super.tick();
        if (level.isClientSide && isSleeping() && getRandom().nextInt(10) == 0)
        {
            level.addParticle(TFCParticles.SLEEP.get(), getX(), getY() + getEyeHeight(), getZ(), 0.01, 0.05, 0.01);
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount)
    {
        boolean hurt = super.hurt(source, amount);
        if (!level.isClientSide && source instanceof EntityDamageSource entitySource && entitySource.getEntity() instanceof LivingEntity livingEntity && getHealth() > 0)
        {
            brain.setMemory(MemoryModuleType.ATTACK_TARGET, livingEntity);
            brain.setActiveActivityIfPossible(Activity.FIGHT);
            setSleeping(false);
        }
        return hurt;
    }

    @Override
    public boolean doHurtTarget(Entity target)
    {
        boolean hurt = super.doHurtTarget(target);
        attackAnimationRemainingTicks = attackAnimationLength;
        level.broadcastEntityEvent(this, (byte) 4);
        playSound(getAttackSound(), 1.0f, getVoicePitch());

        if (hurt && target instanceof Player player && random.nextInt(5) == 0 && player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) <= 0)
        {
            pinPlayer(player);
        }

        return hurt;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("sleeping", isSleeping());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        setSleeping(tag.getBoolean("sleeping"));
    }

    @Override
    public void aiStep()
    {
        if (attackAnimationRemainingTicks > 0)
        {
            --attackAnimationRemainingTicks;
        }
        super.aiStep(); // this method is called on both sides of LivingEntity#tick. So the name is rather improper
    }

    @Override
    public void handleEntityEvent(byte id)
    {
        if (id == 4)
        {
            attackAnimationRemainingTicks = 20;
            playSound(getAttackSound(), 1.0F, getVoicePitch());
        }
        super.handleEntityEvent(id);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type, @Nullable SpawnGroupData data, @Nullable CompoundTag tag)
    {
        SpawnGroupData spawnData = super.finalizeSpawn(level, difficulty, type, data, tag);
        getBrain().setMemory(MemoryModuleType.HOME, GlobalPos.of(level.getLevel().dimension(), blockPosition()));
        return spawnData;
    }

    @Override
    public void defineSynchedData()
    {
        super.defineSynchedData();
        entityData.define(DATA_SLEEPING, false);
    }

    @Override
    public boolean isSleeping()
    {
        return entityData.get(DATA_SLEEPING);
    }

    public void setSleeping(boolean asleep)
    {
        entityData.set(DATA_SLEEPING, asleep);
    }

    public int getAttackTicks()
    {
        return attackAnimationRemainingTicks <= 0 ? 0 : attackAnimationLength - attackAnimationRemainingTicks;
    }

    public SoundEvent getAttackSound()
    {
        return attack.get();
    }

    @Override
    protected SoundEvent getAmbientSound()
    {
        return isSleeping() ? sleeping.get() : super.getAmbientSound();
    }

    private void pinPlayer(Player player)
    {
        if (distanceToSqr(player) < 6D)
        {
            if (!player.level.isClientSide)
            {
                player.addEffect(new MobEffectInstance(TFCEffects.PINNED.get(), 35, 0, false, false));
            }
        }
    }
}
