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
import com.sun.net.httpserver.HttpExchange;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpCookie;
import java.nio.file.Files;
import java.util.*;

public class HttpExchangeUtils {

    protected final HttpExchange httpExchange;
    protected final Headers headers;
    protected final StringBuilder response;
    protected KWSB kwsb;
    protected File htmlFile;

    /**
     * Initialise the object
     *
     * @param httpExchange
     *        the HttpExchange object of the requets
     *
     * @param file
     *        the file which should be sent
     */
    public HttpExchangeUtils(HttpExchange httpExchange, File file) {
        this.httpExchange = httpExchange;
        this.headers = httpExchange.getResponseHeaders();
        this.htmlFile = file;
        this.response = new StringBuilder();
    }

    /**
     * Parse a file
     *
     * @throws FileNotFoundException
     *         will be thrown when the given file is not found
     */
    public void parseFile(HashMap<String, String> constants) throws FileNotFoundException {
        if (!this.htmlFile.exists()) {
            System.out.println("Error by parsing a file: " + this.htmlFile.getPath());
            return;
        }
        Scanner scanner = new Scanner(this.htmlFile);
        while (scanner.hasNextLine()) {
            String appendString = scanner.nextLine();
            if (appendString.replaceAll(" ", "").startsWith("<<%-")) {
                String path = appendString.split("<<%-")[1].replace(">>", "");
                File importFile = new File("./src/main/web/" + (path.replaceAll(" ", "").endsWith(".html") ? path.replaceAll(" ", "") : path.replaceAll(" ", "") + ".html"));
                if (!importFile.exists()) {
                    System.out.println("Error when parsing a line: " + appendString);
                    break;
                }
                Scanner importFileScanner = new Scanner(importFile);
                while (importFileScanner.hasNextLine()) response.append(importFileScanner.nextLine()).append("\n");
                continue;
            }
            if (appendString.split("<<%=").length != 1) {
                if (constants == null) continue;
                String[] open_tags = appendString.split("<<%=");
                String[] close_tags = appendString.split(">>");

                int index = 0;
                String last_key = "";
                while (index < open_tags.length - 1) {
                    String between_tags = open_tags[index + 1].split(">>")[0].replaceAll(" ", "");
                    this.response.append(open_tags[index].replace(last_key + " >>", "")).append(constants.get(between_tags));
                    index++;
                    last_key = between_tags;
                }
                this.response.append(close_tags[close_tags.length - 1]).append("\n");
                continue;
            }
            this.response.append(appendString).append("\n");
        }
    }

    /**
     * Set the default headers
     */
    public void setDefaultHeaders() {
        this.headers.add("Access-Control-Allow-Origin", "*");
        this.headers.add("Access-Control-Allow-Methods", "*");
        this.headers.add("Access-Control-Allow-Headers", "*");
        this.headers.add("Content-Type", "text/html");
    }

    /**
     * Set a cookie
     *
     * @param name
     *        the name of the cookie
     *
     * @param value
     *        the value of the cookie
     */
    public void setCookie(String name, String value) {
        HttpCookie cookie = new HttpCookie(name, value);
        if(this.kwsb != null) {
            cookie.setMaxAge(kwsb.cookie_expiry_max_age);
        }
        httpExchange.getResponseHeaders().add("Set-Cookie", cookie.toString());
    }

    /**
     * Get a cookie
     *
     * @param key
     *        the name of the cookie
     *
     * @return returns the cookie
     */
    public HttpCookie getCookie(String key) {
        try {
            List<String> cookie_values = this.httpExchange.getRequestHeaders().get("Cookie");
            String cookie = "";
            int index = 0;
            for (String s : cookie_values) {
                if (cookie_values.get(index).split("=")[0].replace("[", "").equalsIgnoreCase(key)) {
                    cookie = cookie_values.get(index);
                    break;
                }
                index++;
            }
            if (cookie.equalsIgnoreCase("")) return null;
            return HttpCookie.parse(cookie).get(0);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Send the file and everything else what we set up here
     *
     * @throws IOException
     *         will be thrown when there are some issues due sending the data
     */
    public void sendData() throws IOException {
        if ("text".equals(this.headers.get("Content-Type").get(0).split("/")[0])) {
            this.httpExchange.sendResponseHeaders(200, this.response.toString().getBytes().length);
            OutputStream os = httpExchange.getResponseBody();
            os.write(this.response.toString().getBytes());
            os.close();
        } else {
            this.httpExchange.sendResponseHeaders(200, this.htmlFile.length());
            OutputStream outputStream = this.httpExchange.getResponseBody();
            Files.copy(this.htmlFile.toPath(), outputStream);
            outputStream.close();
        }
    }

    /**
     * Redirect to a url
     *
     * @param url
     *        the url you want to redirect
     *
     * @throws IOException
     *         will be thrown when there are some issues due sending the data
     */
    public void redirect(String url) throws IOException {
        this.headers.add("Location", url);
        this.httpExchange.sendResponseHeaders(302, this.htmlFile.length());
        OutputStream outputStream = this.httpExchange.getResponseBody();
        Files.copy(this.htmlFile.toPath(), outputStream);
        outputStream.close();
    }

    /**
     * Get the url parameter
     *
     * @param query
     *        the query of the HttpExchange object
     *
     * @return returns the url parameters as Map
     */
    public Map<String, String> getUrlParameter(String query) {
        if (query == null) {
            return null;
        }
        Map<String, String> result = new HashMap<>();
        for (String param : query.split("&")) {
            String[] entry = param.split("=");
            if (entry.length > 1) {
                result.put(entry[0], entry[1]);
            } else {
                result.put(entry[0], "");
            }
        }
        return result;
    }

    /**
     * Add a header
     *
     * @param name
     *        the name of the header
     *
     * @param content
     *        the value of the header
     */
    public void addHeader(String name, String content) {
        this.headers.add(name, content);
    }

    /**
     * Get all headers
     *
     * @return returns the headers
     */
    public Headers getHeaders() {
        return headers;
    }

    /**
     * Get the HttpExchange object
     *
     * @return returns the HttpExchange object
     */
    public HttpExchange getHttpExchange() {
        return httpExchange;
    }

    /**
     * Get the file which should be sent
     *
     * @return returns the file
     */
    public File getHtmlFile() {
        return htmlFile;
    }

    /**
     * Get the response of the parsed file
     *
     * @return returns the parsed file
     */
    public StringBuilder getResponse() {
        return response;
    }

    public KWSB getKWSB() {
        return kwsb;
    }

    /**
     * Set the file which should be sent
     *
     * @param htmlFile
     *        the file
     */
    public void setHtmlFile(File htmlFile) {
        this.htmlFile = htmlFile;
    }

    public void setKWSB(KWSB kwsb) {
        this.kwsb = kwsb;
    }
}