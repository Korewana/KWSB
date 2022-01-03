/*
 * Copyright 2021 by MauricePascal
 * Licensed under the GNU General Public License v3.0(the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.gnu.org/licenses/gpl-3.0.txt
 */

import de.mp.kwsb.internal.Request;
import de.mp.kwsb.internal.Response;
import de.mp.kwsb.internal.handlers.GetRequestHandler;
import de.mp.kwsb.internal.handlers.Router;
import de.mp.kwsb.internal.handlers.RouterInterface;

public class TestRouter implements RouterInterface {

    Router r = new Router();

    @Override
    public Router getRouter() {
        return r;
    }

    @Override
    public RouterInterface start() {
        getRouter().addRequestHandler("/endpoint", new GetRequestHandler() {
            @Override
            public void onRequest(Request req, Response res) throws Exception {
                res.send("<h1>Ich mag Erdbeeren</h1>");
            }
        });
        return this;
    }

}
