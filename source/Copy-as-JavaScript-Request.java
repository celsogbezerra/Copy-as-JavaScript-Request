package burp;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

public class BurpExtender implements IBurpExtender, IContextMenuFactory, ClipboardOwner {
    private IExtensionHelpers helpers;

    private final static String NAME = "Copy as JavaScript Request";
    private final static String[] ESCAPE = new String[256];

    static {
        for (int i = 0x00; i <= 0xFF; i++)
            ESCAPE[i] = String.format("\\x%02x", i);
        for (int i = 0x20; i < 0x80; i++)
            ESCAPE[i] = String.valueOf((char) i);
        ESCAPE['\''] = "\\\'";
        ESCAPE['\\'] = "\\\\";
    }

    @Override
    public void registerExtenderCallbacks(IBurpExtenderCallbacks callbacks) {
        helpers = callbacks.getHelpers();
        callbacks.setExtensionName(NAME);
        callbacks.registerContextMenuFactory(this);
    }

    @Override
    public List<JMenuItem> createMenuItems(IContextMenuInvocation invocation) {
        final IHttpRequestResponse[] messages = invocation.getSelectedMessages();
        if (messages == null || messages.length == 0) {
            return null;
        }
        JMenuItem i = new JMenuItem(NAME);
        i.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyMessages(messages);
            }
        });
        return Collections.singletonList(i);
    }

    private void copyMessages(IHttpRequestResponse[] messages) {
        StringBuilder node = new StringBuilder();
        for (IHttpRequestResponse message : messages) {
            IRequestInfo info = helpers.analyzeRequest(message);
            byte[] req = message.getRequest();
            List<String> headers = info.getHeaders();
            String method = info.getMethod();

            String cookiesExist = processCookies(node, headers);
            Boolean bodyExists = processBody(node, req, info);
            node.append("fetch('").append(info.getUrl().toString()).append("',{\n    ");
            node.append("method: '").append(method).append("',\n    ");
            node.append("headers: {\n        ");
            processHeaders(node, headers);
            if (cookiesExist != "") {
                node.append("\n    },\n    credentials: 'include'");
            } else {
                node.append("    \n}");
            }
            if (bodyExists) {
                node.append(",\n    body: '");
                int body = info.getBodyOffset();
                escapeBytes(req, node, body, req.length);
                node.append("'");
            } else if (method.equals("POST") || method.equals("PUT")) {
                node.append(",\n    body: ''");
            }
            node.append("\n});");
            if (cookiesExist != "") {
                node.append("console.log('Cookie: ").append(cookiesExist).append("');");
            }
        }

        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(node.toString()), this);
    }

    private static String processCookies(StringBuilder node, List<String> headers) {
        ListIterator<String> iterator = headers.listIterator();
        String cookie = "";
        while (iterator.hasNext()) {
            String header = iterator.next();
            if (!header.toLowerCase().startsWith("cookie:")) {
                continue;
            }
            iterator.remove();
            cookie = header.substring(8);
        }
        return cookie;
    }

    private static void processHeaders(StringBuilder node, List<String> headers) {
        Boolean firstHeader = true;
        for (String header : headers) {
            if (header.toLowerCase().startsWith("host:")) {
                continue;
            }
            int colonPos = header.indexOf(':');
            if (colonPos == -1) {
                continue;
            }
            if (firstHeader) {
                firstHeader = false;
                node.append("'");
            } else {
                node.append(", \n        '");
            }
            node.append(header, 0, colonPos);
            node.append("': '");
            node.append(header, colonPos + 2, header.length());
            node.append("'");
        }
    }

    private Boolean processBody(StringBuilder node, byte[] req, IRequestInfo info) {
        int body = info.getBodyOffset();
        if (body >= req.length - 2) {
            return false;
        }
        return true;
    }

    private static void escapeBytes(byte[] input, StringBuilder output, int start, int end) {
        for (int i = start; i < end; i++) {
            output.append(ESCAPE[input[i] & 0xFF]);
        }
    }

    @Override
    public void lostOwnership(Clipboard aClipboard, Transferable aContents) {
    }
}