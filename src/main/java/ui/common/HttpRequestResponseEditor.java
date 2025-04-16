package ui.common;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.ui.editor.HttpRequestEditor;
import burp.api.montoya.ui.editor.HttpResponseEditor;
import common.provider.UIProvider;
import lombok.Data;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

@Data
public class HttpRequestResponseEditor {
    private final HttpRequestEditor httpRequestEditor;
    private final HttpResponseEditor httpResponseEditor;
    private final HttpRequestEditor edit_httpRequestEditor;
    private final HttpResponseEditor edit_httpResponseEditor;
    private final UIProvider uiProvider = UIProvider.INSTANCE;

    public HttpRequestResponseEditor() {
        this.httpRequestEditor = uiProvider.createHttpRequestEditor();
        this.httpResponseEditor = uiProvider.createHttpResponseEditor();
        this.edit_httpRequestEditor = uiProvider.createHttpRequestEditor();
        this.edit_httpResponseEditor = uiProvider.createHttpResponseEditor();

    }

    //从setdata函数传入对象
    public void setData(HttpRequestResponse httpRequestResponse,HttpRequestResponse edit_httpRequestResponse) {
        httpRequestEditor.setRequest(httpRequestResponse.request());
        httpResponseEditor.setResponse(httpRequestResponse.response());
        edit_httpRequestEditor.setRequest(edit_httpRequestResponse.request());
        edit_httpResponseEditor.setResponse(edit_httpRequestResponse.response());
    }

    public Component uiComponent() {
        JSplitPane requestResponsePane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        requestResponsePane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                requestResponsePane.setDividerLocation(0.4);
            }
        });
        JTabbedPane requestPane = new JTabbedPane();
        JTabbedPane edit_resposerequestPane = new JTabbedPane();


        requestPane.addTab("Request", httpRequestEditor.uiComponent());
        edit_resposerequestPane.addTab("edit_Request", edit_httpRequestEditor.uiComponent());
        edit_resposerequestPane.addTab("edit_Response", edit_httpResponseEditor.uiComponent());

        requestResponsePane.add(requestPane, "left");
        requestResponsePane.add(edit_resposerequestPane, "right");
        return requestResponsePane;
    }
}
