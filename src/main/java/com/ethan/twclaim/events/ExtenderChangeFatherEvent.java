package com.ethan.twclaim.events;

import com.ethan.twclaim.data.Bastion;
import com.ethan.twclaim.data.Extender;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class ExtenderChangeFatherEvent extends Event {
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
    Bastion oldBastion;
    Bastion newBastion;

    public ExtenderChangeFatherEvent(Extender extender, Bastion oldBastion, Bastion newBastion){
        this.extender = extender;
        this.oldBastion = oldBastion;
        this.newBastion = newBastion;
    }

    public Extender getExtender() {
        return extender;
    }

    public void setExtender(Extender extender) {
        this.extender = extender;
    }

    public Bastion getOldBastion() {
        return oldBastion;
    }

    public void setOldBastion(Bastion oldBastion) {
        this.oldBastion = oldBastion;
    }

    public Bastion getNewBastion() {
        return newBastion;
    }

    public void setNewBastion(Bastion newBastion) {
        this.newBastion = newBastion;
    }
}
