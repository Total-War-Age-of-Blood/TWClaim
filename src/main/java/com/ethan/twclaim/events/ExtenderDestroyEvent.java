package com.ethan.twclaim.events;

import com.ethan.twclaim.data.Extender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ExtenderDestroyEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList(){
        return handlers;
    }
    Extender extender;

    public ExtenderDestroyEvent(Extender extender){
        this.extender = extender;
    }

    public Extender getExtender() {
        return extender;
    }

    public void setExtender(Extender extender) {
        this.extender = extender;
    }
}
