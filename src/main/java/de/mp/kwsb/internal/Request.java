/*
 * Copyright 2021 by MauricePascal
 * Licensed under the GNU General Public License v3.0(the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0.txt
 */

package de.mp.kwsb.internal;

import de.mp.kwsb.internal.entities.Cookie;

import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class Request {

    private final HttpExchangeUtils httpExchangeUtils;
    private final HashMap<String, String> params;
    private final String query;

    public Request(HttpExchangeUtils httpExchangeUtils, HashMap<String, String> params) {
        this.httpExchangeUtils = httpExchangeUtils;
        this.params = params;
        this.query = httpExchangeUtils.getHttpExchange().getRequestURI().getQuery();
    }

    public HttpExchangeUtils getHttpExchangeUtils() {
        return httpExchangeUtils;
    }

    public Collection<Cookie> getCookies() {
        Collection<Cookie> return_value = new ArrayList<>();
        try {
            List<String> cookie_values = this.httpExchangeUtils.getHttpExchange().getRequestHeaders().get("Cookie");
            int index = 0;
            for (String s : cookie_values) {
                return_value.add(new Cookie(HttpCookie.parse(cookie_values.get(index)).get(0)));
                index++;
            }
        } catch (Exception e) {
            return null;
        }
        return return_value;
    }

    public Cookie getCookie(String key) {
        return new Cookie(this.httpExchangeUtils.getCookie(key));
    }

    public String getParam(String name) {
        return this.params.getOrDefault(name, null);
    }

    public HashMap<String, String> getParams() {
        return this.params;
    }

    public String getQuery(String key) {
        return this.httpExchangeUtils.getUrlParameter(this.query).getOrDefault(key, null);
    }
}
