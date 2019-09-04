package com.block90.wallet;

import org.kohsuke.args4j.Option;

public class Args {
    @Option(required = true, name = "-u", aliases = "-url", usage = "the request url is required")
    private String url;

    @Option(name = "-un", aliases = "-username", usage = "the hmac-auth username")
    private String username;

    @Option(name = "-s", aliases = "-secret", usage = "the hmac-auth secret")
    private String secret;

    @Option(name = "-b", aliases = "-body", usage = "the body of request")
    private String body;

    @Option(name = "-m", aliases = "-method", usage = "http method, GET or POST, default is POST")
    private String method;

    @Option(name = "-l", aliases = "-level", usage = "change the log level, default is INFO")
    private String level;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
