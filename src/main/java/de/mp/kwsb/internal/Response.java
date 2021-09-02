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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;

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

    public void sendFile(File f) throws HttpException {
        try {
            this.request.getHttpExchangeUtils().getHttpExchange().sendResponseHeaders(200, f.length());
            OutputStream outputStream = this.request.getHttpExchangeUtils().getHttpExchange().getResponseBody();
            Files.copy(f.toPath(), outputStream);
            outputStream.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void render(File f, HashMap<String, String> constants) throws IOException {
        this.request.getHttpExchangeUtils().setDefaultHeaders();
        this.request.getHttpExchangeUtils().setHtmlFile(new File("./baum.html"));
        this.request.getHttpExchangeUtils().parseFile(constants);
        this.request.getHttpExchangeUtils().sendData();
    }

    public void setCookie(String key, String value) {
        this.request.getHttpExchangeUtils().setCookie(key, value);
    }

    public Request getRequest() {
        return request;
    }
}
