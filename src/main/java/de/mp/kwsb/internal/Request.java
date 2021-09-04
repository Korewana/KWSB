/*
 * Copyright 2021 by MauricePascal
 * Licensed under the GNU General Public License v3.0(the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0.txt
 */

package de.mp.kwsb.internal;

import com.sun.net.httpserver.Headers;
import de.mp.kwsb.internal.entities.Cookie;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpCookie;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
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
        if(this.httpExchangeUtils.getCookie(key) == null) return null;
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

    public Headers getHeaders() {
        return this.httpExchangeUtils.getHttpExchange().getRequestHeaders();
    }

    public List<String> getHeaders(String key) {
        return this.httpExchangeUtils.getHttpExchange().getRequestHeaders().getOrDefault(key, null);
    }

    public String getHeader(String key) {
        List<String> headers = getHeaders(key);
        if(headers == null || headers.size() == 0) return null; else return headers.get(0);
    }

    public JSONObject getBodyAsJSON() {
        InputStream inputStream = this.getHttpExchangeUtils().getHttpExchange().getRequestBody();
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new JSONObject(textBuilder.toString());
    }

    public String getBody() {
        InputStream inputStream = this.getHttpExchangeUtils().getHttpExchange().getRequestBody();
        StringBuilder textBuilder = new StringBuilder();
        try (Reader reader = new BufferedReader(new InputStreamReader
                (inputStream, Charset.forName(StandardCharsets.UTF_8.name())))) {
            int c = 0;
            while ((c = reader.read()) != -1) {
                textBuilder.append((char) c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return textBuilder.toString();
    }

}
