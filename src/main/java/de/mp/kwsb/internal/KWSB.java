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
import de.mp.kwsb.internal.entities.KWSBList;
import de.mp.kwsb.internal.handlers.*;
import de.mp.kwsb.internal.handlers.errors.HttpException;
import de.mp.kwsb.internal.events.HttpNotFoundEvent;
import de.mp.kwsb.internal.events.ReadyEvent;
import javafx.beans.binding.BooleanExpression;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

public class KWSB {

    protected HttpServer server;
    protected int port;
    protected long cookie_expiry_max_age = 2592000L;
    protected final KWSBList<String, RequestHandler> requestHandlers = new KWSBList<>();
    protected final KWSBList<String, RouterInterface> routers = new KWSBList<>();
    protected final List<Object> listeners = new LinkedList<>();

    private <T> void callListener(Class<T> clazz, HttpExchange httpExchange) {
        if(this.listeners.size() == 0) return;
        this.listeners.forEach(obj -> {
            if(obj instanceof KWSBListenerAdapter) {
                try {
                    if(clazz == Class.forName("de.mp.kwsb.internal.events.ReadyEvent")) {
                        ((KWSBListenerAdapter) obj).onReady(new ReadyEvent(this));
                    } else if(clazz == Class.forName("de.mp.kwsb.internal.events.HttpNotFoundEvent")) {
                        Request request = new Request(new HttpExchangeUtils(httpExchange, null), null);
                        ((KWSBListenerAdapter) obj).onHttpNotFound(request, new Response(request));
                    }
                } catch (ClassNotFoundException | HttpException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void registerEvents(Object... listeners) {
        Collections.addAll(this.listeners, listeners);
    }

    public void addRequestHandler(String route, RequestHandler handler) {
        requestHandlers.add(route, handler);
    }

    public void addRouter(String route, RouterInterface router) {
        routers.add(route, router);
    }

    public void setCookieExpiry(int cookie_expiry) {
        this.cookie_expiry_max_age = cookie_expiry;
    }

    public CompletionStage<ReadyEvent> listen(int port) throws Exception {
        this.port = port;
        this.server = HttpServer.create(new InetSocketAddress(port), 0);
        this.server.createContext("/", new RequestManager(this));
        this.server.setExecutor(null);
        this.server.start();
        this.callListener(ReadyEvent.class, null);
        CompletableFuture<ReadyEvent> future = new CompletableFuture<>();
        future.complete(new ReadyEvent(this));
        return future;
    }

    public KWSBList<String, RequestHandler> getRequestHandlers() {
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
            AtomicReference<Boolean> executed = new AtomicReference<>();
            executed.set(false);

            AtomicReference<Thread> router_thread_reference = new AtomicReference<>();
            Thread router_thread = new Thread(() -> {
                try {
                    final HashMap<String, String> params = new HashMap<>();
                    String[] url = httpExchange.getRequestURI().getPath().split("/");
                    String path = "/";
                    StringBuilder stringBuilder = new StringBuilder();
                    for(int i = 1; i<url.length;i++) {
                        stringBuilder.append(url[i]);
                        if(i != (url.length-1)) stringBuilder.append("/");
                    }
                    path += stringBuilder.toString();
                    AtomicReference<RouterInterface> handler = new AtomicReference<>();
                    AtomicReference<String> atomic_route = new AtomicReference<>();
                    kwsb.routers.forEach((route, router) -> {
                        if(handler.get() != null) return;
                        String[] route_url = route.split("/");
                        if(Arrays.toString(route_url).contains(":")) {
                            if (url.length != route_url.length) return;
                        }
/*                        else {
                            if (!Arrays.toString(route_url).equalsIgnoreCase(Arrays.toString(url))) return;
                        }*/
                        int index = 0;
                        for (String url_obj : route_url) {
                            if(!url_obj.startsWith(":")) {
                                index++;
                                if(!url_obj.equalsIgnoreCase(url[index-1])) return;
                                continue;
                            }
                            String var_name = url_obj.split(":")[1];
                            String value = url[index];
                            params.put(var_name, value);
                            index++;
                        }
                        handler.set(router);
                        atomic_route.set(route);
                        executed.set(true);
                    });
                    if(handler.get() == null) return;
                    AtomicReference<RequestHandler> routerhandler = new AtomicReference<>();
                    handler.get().getRouter().getRequestHandlers().forEach((invoke, routerhandler_forEach) -> {
                        if(routerhandler.get() != null) return;
                        String[] route_url = invoke.split("/");
                        if(Arrays.toString(route_url).contains(":")) {
                            if (url.length != route_url.length) return;
                        } else {
                            if (!(Arrays.toString(atomic_route.get().split("/")).replace("]", ", ")+Arrays.toString(route_url).replace("[, ", "")).equalsIgnoreCase(Arrays.toString(url))) return;
                        }
                        ArrayList<String> list = new ArrayList<>();
                        Collections.addAll(list, atomic_route.get().split("/"));
                        Collections.addAll(list, Arrays.copyOfRange(route_url, 1, route_url.length));
                        System.out.println(Arrays.toString(list.toArray(new String[atomic_route.get().length()])));
                        int index = 0;
                        for (String url_obj : list.toArray(new String[atomic_route.get().length()])) {
                            if(url_obj == null) break;
                            if(!url_obj.startsWith(":")) {
                                index++;
                                System.out.println("URLOBJ: "+url_obj+" "+index);
                                System.out.println("URL: "+url[index-1]+ " "+ index);
                                if(!url_obj.equalsIgnoreCase(url[index-1])) return;
                                continue;
                            }
                            String var_name = url_obj.split(":")[1];
                            String value = url[index];
                            params.put(var_name, value);
                            index++;
                        }
                        routerhandler.set(routerhandler_forEach);
                        executed.set(true);
                    });
                    if(routerhandler.get() == null) {
                        kwsb.callListener(HttpNotFoundEvent.class, httpExchange);
                        return;
                    }
                    HttpExchangeUtils httpExchangeUtils = new HttpExchangeUtils(httpExchange, null);
                    Request request = new Request(httpExchangeUtils, params);
                    routerhandler.get().onRequest(request, new Response(request));
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
            });
            router_thread_reference.set(router_thread);

            Thread route_thread = new Thread(() -> {
                try {
                    final HashMap<String, String> params = new HashMap<>();
                    final String method = httpExchange.getRequestMethod().toLowerCase();
                    String[] url = httpExchange.getRequestURI().getPath().split("/");
                    String path = "/";
                    StringBuilder stringBuilder = new StringBuilder();
                    for (int i = 1; i < url.length; i++) {
                        stringBuilder.append(url[i]);
                        if (i != (url.length - 1)) stringBuilder.append("/");
                    }
                    path += stringBuilder.toString();
                    AtomicReference<RequestHandler> handler = new AtomicReference<>();
                    kwsb.requestHandlers.forEach((route, httphandler) -> {
                        if (handler.get() != null) return;
                        if (method.equals("get") && !(httphandler instanceof GetRequestHandler)) return;
                        if (method.equals("post") && !(httphandler instanceof PostRequestHandler)) return;
                        String[] route_url = route.split("/");
                        if (Arrays.toString(route_url).contains(":")) {
                            if (url.length != route_url.length) return;
                        } else {
                            if (!Arrays.toString(route_url).equalsIgnoreCase(Arrays.toString(url))) return;
                        }
                        int index = 0;
                        for (String url_obj : route_url) {
                            if (!url_obj.startsWith(":")) {
                                index++;
                                if (!url_obj.equalsIgnoreCase(url[index - 1])) return;
                                continue;
                            }
                            String var_name = url_obj.split(":")[1];
                            String value = url[index];
                            params.put(var_name, value);
                            index++;
                        }
                        handler.set(httphandler);
                        executed.set(true);
                    });
                    if (handler.get() == null) {
                        router_thread_reference.get().start();
                        return;
                    }
                    HttpExchangeUtils httpExchangeUtils = new HttpExchangeUtils(httpExchange, null);
                    Request request = new Request(httpExchangeUtils, params);
                    handler.get().onRequest(request, new Response(request));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            route_thread.start();
        }
    }

}
