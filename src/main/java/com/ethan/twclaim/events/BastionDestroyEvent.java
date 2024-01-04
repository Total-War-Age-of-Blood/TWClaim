package com.ethan.twclaim.events;

import com.ethan.twclaim.data.Bastion;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class BastionDestroyEvent extends Event {
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

    public BastionDestroyEvent(Bastion bastion){
        this.bastion = bastion;
    }

    public Bastion getBastion() {
        return bastion;
    }

    public void setBastion(Bastion bastion) {
        this.bastion = bastion;
    }
}
