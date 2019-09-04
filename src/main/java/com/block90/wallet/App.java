package com.block90.wallet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.ExampleMode;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Main Class
 */
public class App {
    private static final Log log = LogFactory.getLog(App.class.getName());
    private static final String HMAC_USERNAME = "block90cred";
    private static final String HMAC_SECRET = "zkYEGk5KIYfuWgZqCeU6146uctQQVtnc";
    private static final String ENV_SYSTEM = "test";
    private static final String HOST = "http://localhost:8000/";
    private static final String GET_WALLET_INFO_BODY = "{\"method\": \"getwalletinfo\", \"params\": [] }";

    public static void main(String[] args) {
        Args myArgs = new Args();
        CmdLineParser parser = new CmdLineParser(myArgs);
        try {
            parser.parseArgument(args);

            if (StringUtils.isNotBlank(myArgs.getLevel())) {
                LogManager.getRootLogger().setLevel(Level.toLevel(myArgs.getLevel()));
            }

            KongService service = new KongService(HOST);
//            service.callRpc(HMAC_USERNAME, HMAC_SECRET, GET_WALLET_INFO_BODY);
//            service.callPlDifficulty();
            if (KongService.HttpMethod.GET.getName().equalsIgnoreCase(myArgs.getMethod())) {
                service.getService(myArgs.getUrl(), StringUtils.defaultString(myArgs.getBody()), myArgs.getUsername(),
                        myArgs.getSecret());
            } else {
                service.postService(myArgs.getUrl(), StringUtils.defaultString(myArgs.getBody()), myArgs.getUsername(),
                        myArgs.getSecret());
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error(e.getMessage(), e);
        } catch (CmdLineException e) {
            System.err.println(e.getMessage());
            System.err.println("java -jar kongService.jar [options...] arguments...");
            parser.printUsage(System.err);
            System.err.println();
            System.err.println("  Example:\njava -jar kongService.jar -url http://localhost:8000/rpc" +
                    " -username block90cred -s zkYEGk5KIYfuWgZqCeU6146uctQQVtnc" +
                    " -b \"{\\\"method\\\": \\\"getwalletinfo\\\", \\\"params\\\": [] }\"" +
                    " -m post -l debug");
        }
    }

}
