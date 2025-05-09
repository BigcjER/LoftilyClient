package net.minecraft.util;

import net.minecraft.client.Minecraft;

public class Timer
{
    /**
     * How many full ticks have turned over since the last call to updateTimer(), capped at 10.
     */
    public int elapsedTicks;
    public float renderPartialTicks;
    public float elapsedPartialTicks;

    /**
     * The time reported by the system clock at the last sync, in milliseconds
     */
    private long lastSyncSysClock;
    private float ticksPerSecond;

    public Timer(float tps)
    {
        this.ticksPerSecond = 1000.0F / tps;
        this.lastSyncSysClock = Minecraft.getSystemTime();
    }

    /**
     * Updates all fields of the Timer using the current time
     */
    public void updateTimer()
    {
        long i = Minecraft.getSystemTime();
        this.elapsedPartialTicks = (float)(i - this.lastSyncSysClock) / this.ticksPerSecond;
        this.lastSyncSysClock = i;
        this.renderPartialTicks += this.elapsedPartialTicks;
        this.elapsedTicks = (int)this.renderPartialTicks;
        this.renderPartialTicks -= (float)this.elapsedTicks;
    }
}
