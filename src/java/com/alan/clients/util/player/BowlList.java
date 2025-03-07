package com.alan.clients.util.player;

import lombok.AllArgsConstructor;
    @AllArgsConstructor
    public enum BowlList {
        Drop("Drop"),
        Move("Move"),
        Stay("Stay");

        String name;

        @Override
        public String toString() {
            return name;
        }
}
