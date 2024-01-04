package com.ethan.twclaim.events;

import com.ethan.twclaim.data.Bastion;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BastionChangeFuelStateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public static HandlerList getHandlerList(){
        return handlers;
    }

    Bastion bastion;
    boolean hasFuel;
    public BastionChangeFuelStateEvent(Bastion bastion, boolean hasFuel){
        this.bastion = bastion;
        this.hasFuel = hasFuel;
    }

    public Bastion getBastion() {
        return bastion;
    }

    public void setBastion(Bastion bastion) {
        this.bastion = bastion;
    }

    public boolean isHasFuel() {
        return hasFuel;
    }

    public void setHasFuel(boolean hasFuel) {
        this.hasFuel = hasFuel;
    }
}
