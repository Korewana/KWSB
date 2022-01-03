/*
 * Copyright 2021 by MauricePascal
 * Licensed under the GNU General Public License v3.0(the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0.txt
 */

import de.mp.kwsb.internal.KWSB;
import de.mp.kwsb.internal.handlers.GetRequestHandler;
import de.mp.kwsb.internal.handlers.Router;
import de.mp.kwsb.internal.handlers.RouterInterface;
import de.mp.kwsb.internal.handlers.errors.HttpException;
import de.mp.kwsb.internal.KWSBListenerAdapter;
import de.mp.kwsb.internal.Response;
import de.mp.kwsb.internal.Request;
import de.mp.kwsb.internal.events.ReadyEvent;

public class Test extends KWSBListenerAdapter {

    public static void main(String[] args) throws Exception {
        KWSB kwsb = new KWSB();

        kwsb.addRequestHandler("/", new GetRequestHandler() {
            @Override
            public void onRequest(Request req, Response res) throws Exception {
                res.send("<h1>Hello World!</h1>");
            }
        });
        kwsb.addRouter("/api", new TestRouter().start());

        kwsb.registerEvents(new Test());
        kwsb.listen(5555).whenComplete((readyEvent, err) -> {
            System.out.println("[Consumer of KWSB#listen()]: Server started with port "+readyEvent.getPort());
        });
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("[Event of the KWSBListenerAdapter]: Server started with port "+event.getPort());
    }

    @Override
    public void onHttpNotFound(Request req, Response res) throws HttpException {
        res.send("<h1>404 - Page not found</h1>");
    }
}
