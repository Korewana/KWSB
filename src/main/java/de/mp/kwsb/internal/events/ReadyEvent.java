/*
 * Copyright 2021 by MauricePascal
 * Licensed under the GNU General Public License v3.0(the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0.txt
 */

package de.mp.kwsb.internal.events;

import de.mp.kwsb.internal.KWSB;

public class ReadyEvent {

    protected final KWSB kwsb;

    public ReadyEvent(KWSB kwsb) {
        this.kwsb = kwsb;
    }

    public int getPort() {
        return this.kwsb.getPort();
    }

}
