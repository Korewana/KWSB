/*
 * Copyright 2021 by MauricePascal
 * Licensed under the GNU General Public License v3.0(the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0.txt
 */

package de.mp.kwsb.internal;

import de.mp.kwsb.internal.errors.HttpException;
import de.mp.kwsb.internal.events.ReadyEvent;

public abstract class KWSBListenerAdapter {

    public void onHttpNotFound(Request req, Response res) throws HttpException {

    }

    public void onReady(ReadyEvent event) {

    }

}
