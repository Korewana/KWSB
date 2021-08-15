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

import java.io.IOException;
import java.io.OutputStream;

public class Response {

    private final Request request;

    public Response(Request request) {
        this.request = request;
    }

    public void send(String s) throws HttpException {
        try {
            this.request.getHttpExchangeUtils().setDefaultHeaders();
            this.request.getHttpExchangeUtils().getHttpExchange().sendResponseHeaders(200, s.getBytes().length);
            OutputStream os = this.request.getHttpExchangeUtils().getHttpExchange().getResponseBody();
            os.write(s.getBytes());
            os.close();
        } catch(IOException e) {
            if (e.getMessage().equalsIgnoreCase("headers already sent")) {
                throw new HttpException("Can't send headers twice.");
            }
        }
    }

    public Request getRequest() {
        return request;
    }
}
