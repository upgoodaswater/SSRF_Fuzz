package ui.dashboard;

import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.message.responses.HttpResponse;
import lombok.*;
import ui.common.CommonTableData;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DashboardTableData extends CommonTableData {
    private Integer id;
    private String status;
    private String host;
    private String method;
    private String url;
    private Integer statusCode;
    private Integer length;
    private Integer edit_length;
    private String mime;
    private HttpRequestResponse requestResponse;
    private HttpRequestResponse edit_requestResponse;
    private String time;

    private DashboardTableData(
            Integer id,
            String host,
            String method,
            String url,
            HttpRequestResponse requestResponse,
            HttpRequestResponse edit_requestResponse,
            Integer length,
            Integer edit_length,
            String time
    ) {
        this.id = id;
        this.host = host;
        this.method = method;
        this.url = url;
        this.requestResponse = requestResponse;
        this.edit_requestResponse=edit_requestResponse;
        this.length=length;
        this.edit_length=edit_length;
        this.time=time;
    }


    public static DashboardTableData buildDashboardTableData(
            Integer id,
            HttpRequestResponse httpRequestResponse,
            HttpRequestResponse edit_httpRequestResponse
    ) {
        HttpRequest request = httpRequestResponse.request();
        HttpResponse response = httpRequestResponse.response();

        HttpResponse editrespose=edit_httpRequestResponse.response();
        String time = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MM-dd|HH:mm:ss"));
        return new DashboardTableData(
                id,
//                StatusEnum.WAITING.getText(),
                request.url(),
                request.method(),
                request.path(),
//               (int) response.statusCode(),
//                response.statedMimeType().description(),
                httpRequestResponse,
                edit_httpRequestResponse,
                response.body().length(),
                editrespose.body().length(),
                time
        );
    }
}


