/*
 * Copyright 2021 by MauricePascal
 * Licensed under the GNU General Public License v3.0(the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0.txt
 */

import de.mp.kwsb.internal.KWSB;
import de.mp.kwsb.internal.errors.HttpException;
import de.mp.kwsb.internal.KWSBListenerAdapter;
import de.mp.kwsb.internal.Response;
import de.mp.kwsb.internal.Request;
import de.mp.kwsb.internal.events.ReadyEvent;
import de.mp.kwsb.internal.handlers.GetRequestHandler;

import java.io.File;
import java.util.UUID;

public class Test extends KWSBListenerAdapter {

    public static void main(String[] args) throws Exception {
        KWSB kwsb = new KWSB();

        kwsb.addRequestHandler("/", new GetRequestHandler() {
            @Override
            public void onRequest(Request req, Response res) throws Exception {
                res.send("<h1>Hello World</h1>");
            }
        });

        kwsb.addRequestHandler("/license", new GetRequestHandler() {
            @Override
            public void onRequest(Request req, Response res) throws Exception {
                res.setCookie("token", UUID.randomUUID().toString());
                res.sendFile(new File("./LICENSE"));
            }
        });

        kwsb.addRequestHandler("/bot/:id", new GetRequestHandler() {
            @Override
            public void onRequest(Request req, Response res) throws Exception {
                res.send(req.getParam("id"));
            }
        });

        kwsb.addRequestHandler("/bot/:id/:opt", new GetRequestHandler() {
            @Override
            public void onRequest(Request req, Response res) throws Exception {
                res.send("Today we will "+req.getParam("opt")+" "+req.getParam("id"));
            }
        });

        kwsb.registerEvents(new Test());
        kwsb.listen(5555);
    }

    @Override
    public void onReady(ReadyEvent event) {
        System.out.println("Server started with port "+event.getPort());
    }

    @Override
    public void onHttpNotFound(Request req, Response res) throws HttpException {
        res.send("<h1>404 - Page not found</h1>");
    }
}
