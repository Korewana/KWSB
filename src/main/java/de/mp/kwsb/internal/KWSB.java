/*
 * Copyright 2021 by MauricePascal
 * Licensed under the GNU General Public License v3.0(the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0.txt
 */

package de.mp.kwsb.internal;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import de.mp.kwsb.internal.errors.HttpException;
import de.mp.kwsb.internal.events.ReadyEvent;
import de.mp.kwsb.internal.handlers.RequestHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public class KWSB {

    protected HttpServer server;
    protected int port;
    protected long cookie_expiry_max_age = 2592000L;
    protected final HashMap<String, RequestHandler> requestHandlers = new HashMap<>();
    protected final List<Object> listeners = new LinkedList<>();

    private void callListener(String s, HttpExchange httpExchange) throws HttpException {
        if(this.listeners.size() == 0) return;
        Object obj = this.listeners.get(0);
        if(obj instanceof KWSBListenerAdapter) {
            switch (s) {
                case "404":
                    Request request = new Request(new HttpExchangeUtils(httpExchange, null), null);
                    ((KWSBListenerAdapter) obj).onHttpNotFound(request, new Response(request));
                    break;
                case "ready":
                    ((KWSBListenerAdapter) obj).onReady(new ReadyEvent(this));
                default:
                    return;
            }
        }
    }

    public void registerEvents(Object... listeners) {
        Collections.addAll(this.listeners, listeners);
    }

    public void addRequestHandler(String route, RequestHandler handler) {
        requestHandlers.put(route, handler);
    }

    public void setCookieExpiry(int cookie_expiry) {
        this.cookie_expiry_max_age = cookie_expiry;
    }

    public KWSB listen(int port) throws Exception {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/", new RequestManager(this));
        this.server.setExecutor(null);
        this.server.start();
        this.callListener("ready", null);
        return this;
    }

    public HashMap<String, RequestHandler> getRequestHandlers() {
        return requestHandlers;
    }

    public int getPort() {
        return port;
    }

    private static class RequestManager implements HttpHandler {

        private final KWSB kwsb;

        public RequestManager(KWSB kwsb) {
            this.kwsb = kwsb;
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            try {
                final HashMap<String, String> params = new HashMap<>();
                String[] url = httpExchange.getRequestURI().getPath().split("/");
                String path = "/";
                StringBuilder stringBuilder = new StringBuilder();
                for(int i = 1; i<url.length;i++) {
                    stringBuilder.append(url[i]);
                    if(i!=(url.length-1)) stringBuilder.append("/");
                }
                path+=stringBuilder.toString();
                AtomicReference<RequestHandler> handler = new AtomicReference<>();
                kwsb.requestHandlers.forEach((route, requestHandler) -> {
                    if(handler.get() != null) return;
                    String[] route_url = route.split("/");
                    if(url.length != route_url.length) return;
                    int index = 0;
                    for (String url_obj : route_url) {
                        if(!url_obj.startsWith(":")) {
                            index++;
                            continue;
                        }
                        String var_name = url_obj.split(":")[1];
                        String value = url[index];
                        params.put(var_name, value);
                        index++;
                    }
                    handler.set(kwsb.requestHandlers.get(route));
                });
                if(handler.get() == null) {
                    kwsb.callListener("404", httpExchange);
                    return;
                }
                HttpExchangeUtils httpExchangeUtils = new HttpExchangeUtils(httpExchange, null);
                Request request = new Request(httpExchangeUtils, params);
                handler.get().onRequest(request, new Response(request));
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

}
