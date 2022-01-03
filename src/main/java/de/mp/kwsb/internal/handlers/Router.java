package de.mp.kwsb.internal.handlers;

import de.mp.kwsb.internal.entities.KWSBList;

public class Router {

    private final KWSBList<String, RequestHandler> requestHandlers;

    public Router() {
        this.requestHandlers = new KWSBList<>();
    }

    public KWSBList<String, RequestHandler>  getRequestHandlers() {
        return requestHandlers;
    }

    public void addRequestHandler(String invoke, RequestHandler handler) {
        requestHandlers.add(invoke, handler);
    }

}
