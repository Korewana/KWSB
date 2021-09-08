/*
 * Copyright 2021 by MauricePascal
 * Licensed under the GNU General Public License v3.0(the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0.txt
 */

package de.mp.kwsb.internal.entities;

import java.net.HttpCookie;

public class Cookie {

    protected final HttpCookie httpCookie;

    public Cookie(HttpCookie httpCookie) {
        this.httpCookie = httpCookie;
    }

    public String getKey() {
        return this.httpCookie.getName();
    }

    public String getValue() {
        return (this.httpCookie.getValue().equalsIgnoreCase("null") ? null : this.httpCookie.getValue());
    }

    public HttpCookie getHttpCookie() {
        return httpCookie;
    }
}
